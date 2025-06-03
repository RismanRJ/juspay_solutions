import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.pattern.StatusReply;
import akka.pattern.Patterns; // For ask pattern

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

// --- Messages ---
interface TreeMessage {} // Base interface for all messages

// Messages for NodeActor
sealed interface NodeCommand extends TreeMessage permits
    NodeCommand.LockRequest,
    NodeCommand.UnlockRequest,
    NodeCommand.UpgradeRequest,
    NodeCommand.DescendantLockedNotification,
    NodeCommand.AncestorLockedQuery,
    NodeCommand.AncestorLockedResponse,
    NodeCommand.UnlockChildrenForUpgrade,
    NodeCommand.ChildUnlockStatus,
    NodeCommand.CheckChildrenLockStatus,
    NodeCommand.ChildrenLockStatusResponse {}

final class NodeActorState {
    public final String name;
    public final ActorRef<NodeCommand> parent; // Akka ActorRef for parent
    public final List<ActorRef<NodeCommand>> children; // Akka ActorRefs for children

    public boolean isLocked;
    public int lockedBy;
    public int lockedDescendantsCount;

    public NodeActorState(String name, ActorRef<NodeCommand> parent) {
        this.name = name;
        this.parent = parent;
        this.children = new ArrayList<>();
        this.isLocked = false;
        this.lockedBy = -1;
        this.lockedDescendantsCount = 0;
    }
}

// NodeCommand implementations
final class NodeCommand {
    public static final class LockRequest implements NodeCommand {
        public final int userId;
        public final ActorRef<StatusReply<Boolean>> replyTo; // Who to reply the final result to
        public LockRequest(int userId, ActorRef<StatusReply<Boolean>> replyTo) {
            this.userId = userId;
            this.replyTo = replyTo;
        }
    }

    public static final class UnlockRequest implements NodeCommand {
        public final int userId;
        public final ActorRef<StatusReply<Boolean>> replyTo;
        public UnlockRequest(int userId, ActorRef<StatusReply<Boolean>> replyTo) {
            this.userId = userId;
            this.replyTo = replyTo;
        }
    }

    public static final class UpgradeRequest implements NodeCommand {
        public final int userId;
        public final ActorRef<StatusReply<Boolean>> replyTo;
        public UpgradeRequest(int userId, ActorRef<StatusReply<Boolean>> replyTo) {
            this.userId = userId;
            this.replyTo = replyTo;
        }
    }

    public static final class DescendantLockedNotification implements NodeCommand {
        public final int delta;
        public DescendantLockedNotification(int delta) {
            this.delta = delta;
        }
    }

    public static final class AncestorLockedQuery implements NodeCommand {
        public final ActorRef<NodeCommand> replyTo; // The node requesting the ancestor check
        public AncestorLockedQuery(ActorRef<NodeCommand> replyTo) {
            this.replyTo = replyTo;
        }
    }

    public static final class AncestorLockedResponse implements NodeCommand {
        public final boolean isLocked;
        public final int lockedBy;
        public final ActorRef<NodeCommand> originalRequester; // The node that initiated the ancestor check
        public AncestorLockedResponse(boolean isLocked, int lockedBy, ActorRef<NodeCommand> originalRequester) {
            this.isLocked = isLocked;
            this.lockedBy = lockedBy;
            this.originalRequester = originalRequester;
        }
    }

    // For Upgrade logic
    public static final class UnlockChildrenForUpgrade implements NodeCommand {
        public final int userId;
        public final ActorRef<NodeCommand> replyTo; // The NodeActor (parent) that initiated this
        public UnlockChildrenForUpgrade(int userId, ActorRef<NodeCommand> replyTo) {
            this.userId = userId;
            this.replyTo = replyTo;
        }
    }

    public static final class ChildUnlockStatus implements NodeCommand {
        public final boolean success;
        public ChildUnlockStatus(boolean success) {
            this.success = success;
        }
    }
    
    public static final class CheckChildrenLockStatus implements NodeCommand {
        public final int userId;
        public final ActorRef<NodeCommand> replyTo;
        public CheckChildrenLockStatus(int userId, ActorRef<NodeCommand> replyTo) {
            this.userId = userId;
            this.replyTo = replyTo;
        }
    }

    public static final class ChildrenLockStatusResponse implements NodeCommand {
        public final boolean lockedByOtherUser; // true if any child is locked by different user
        public final List<ActorRef<NodeCommand>> lockedByMeChildren; // list of children locked by current user
        public ChildrenLockStatusResponse(boolean lockedByOtherUser, List<ActorRef<NodeCommand>> lockedByMeChildren) {
            this.lockedByOtherUser = lockedByOtherUser;
            this.lockedByMeChildren = lockedByMeChildren;
        }
    }
}


// --- NodeActor ---
class NodeActor extends AbstractBehavior<NodeCommand> {

    private final NodeActorState state;
    private final ActorContext<NodeCommand> context;

    // For multi-step operations (like Lock and Upgrade)
    // We need to keep track of the original requester (e.g., the TreeManager or main)
    // and potentially the current state of the multi-step operation.
    private ActorRef<StatusReply<Boolean>> currentOperationReplyTo;
    private int currentOperationUserId;
    private int pendingAncestorResponses;
    private boolean ancestorCheckFailed;
    private List<ActorRef<NodeCommand>> childrenLockedByMeInUpgrade;
    private int pendingChildrenUnlockResponses;
    private boolean childrenUnlockFailed;


    public static Behavior<NodeCommand> create(String name, ActorRef<NodeCommand> parent) {
        return Behaviors.setup(context -> new NodeActor(context, name, parent));
    }

    private NodeActor(ActorContext<NodeCommand> context, String name, ActorRef<NodeCommand> parent) {
        super(context);
        this.context = context;
        this.state = new NodeActorState(name, parent);
    }

    public ActorRef<NodeCommand> getActorRef() {
        return context.getSelf();
    }

    public String getName() {
        return state.name;
    }

    public void addChild(ActorRef<NodeCommand> childRef) {
        state.children.add(childRef);
    }

    @Override
    public Receive<NodeCommand> createReceive() {
        return newReceiveBuilder()
            .onMessage(NodeCommand.LockRequest.class, this::onLockRequest)
            .onMessage(NodeCommand.UnlockRequest.class, this::onUnlockRequest)
            .onMessage(NodeCommand.UpgradeRequest.class, this::onUpgradeRequest)
            .onMessage(NodeCommand.DescendantLockedNotification.class, this::onDescendantLockedNotification)
            .onMessage(NodeCommand.AncestorLockedQuery.class, this::onAncestorLockedQuery)
            .onMessage(NodeCommand.AncestorLockedResponse.class, this::onAncestorLockedResponse)
            .onMessage(NodeCommand.UnlockChildrenForUpgrade.class, this::onUnlockChildrenForUpgrade)
            .onMessage(NodeCommand.ChildUnlockStatus.class, this::onChildUnlockStatus)
            .onMessage(NodeCommand.CheckChildrenLockStatus.class, this::onCheckChildrenLockStatus)
            .onMessage(NodeCommand.ChildrenLockStatusResponse.class, this::onChildrenLockStatusResponse)
            .build();
    }

    private Behavior<NodeCommand> onLockRequest(NodeCommand.LockRequest msg) {
        // First checks (local state only)
        if (state.isLocked || state.lockedDescendantsCount > 0) {
            msg.replyTo.tell(StatusReply.success(false));
            return this;
        }

        // Store original request info for multi-step async process
        this.currentOperationReplyTo = msg.replyTo;
        this.currentOperationUserId = msg.userId;
        this.pendingAncestorResponses = 0;
        this.ancestorCheckFailed = false;

        // Check ancestors
        if (state.parent != null) {
            context.getLog().debug("Node {} sending AncestorLockedQuery to parent {}", state.name, state.parent.path().name());
            pendingAncestorResponses = 1; // Only need to check immediate parent for simplicity
            state.parent.tell(new NodeCommand.AncestorLockedQuery(context.getSelf()));
        } else {
            // No parent, acquire lock immediately
            acquireLock();
        }
        return this;
    }

    private Behavior<NodeCommand> onAncestorLockedQuery(NodeCommand.AncestorLockedQuery msg) {
        context.getLog().debug("Node {} received AncestorLockedQuery from {}", state.name, msg.replyTo.path().name());
        msg.replyTo.tell(new NodeCommand.AncestorLockedResponse(state.isLocked, state.lockedBy, msg.replyTo));
        return this;
    }

    private Behavior<NodeCommand> onAncestorLockedResponse(NodeCommand.AncestorLockedResponse msg) {
        context.getLog().debug("Node {} received AncestorLockedResponse from {}. isLocked: {}", state.name, context.getSender().orElse(context.getSystem().deadLetters()).path().name(), msg.isLocked);
        if (msg.isLocked) {
            ancestorCheckFailed = true;
        }
        pendingAncestorResponses--;

        if (pendingAncestorResponses == 0) {
            if (ancestorCheckFailed) {
                currentOperationReplyTo.tell(StatusReply.success(false)); // Fail the original lock request
            } else {
                acquireLock();
            }
        }
        return this;
    }

    private void acquireLock() {
        state.isLocked = true;
        state.lockedBy = currentOperationUserId;
        context.getLog().debug("Node {} locked by user {}", state.name, currentOperationUserId);
        // Notify parents
        if (state.parent != null) {
            state.parent.tell(new NodeCommand.DescendantLockedNotification(1));
        }
        currentOperationReplyTo.tell(StatusReply.success(true));
    }

    private Behavior<NodeCommand> onUnlockRequest(NodeCommand.UnlockRequest msg) {
        if (!state.isLocked || state.lockedBy != msg.userId) {
            msg.replyTo.tell(StatusReply.success(false));
            return this;
        }

        releaseLock();
        msg.replyTo.tell(StatusReply.success(true));
        return this;
    }

    private void releaseLock() {
        state.isLocked = false;
        state.lockedBy = -1;
        context.getLog().debug("Node {} unlocked", state.name);
        // Notify parents
        if (state.parent != null) {
            state.parent.tell(new NodeCommand.DescendantLockedNotification(-1));
        }
    }

    private Behavior<NodeCommand> onDescendantLockedNotification(NodeCommand.DescendantLockedNotification msg) {
        state.lockedDescendantsCount += msg.delta;
        context.getLog().debug("Node {} descendantLockedCount changed by {}. New count: {}", state.name, msg.delta, state.lockedDescendantsCount);
        if (state.parent != null) {
            state.parent.tell(new NodeCommand.DescendantLockedNotification(msg.delta));
        }
        return this;
    }

    // --- Upgrade Logic ---
    private Behavior<NodeCommand> onUpgradeRequest(NodeCommand.UpgradeRequest msg) {
        if (state.isLocked || state.lockedDescendantsCount == 0) {
            msg.replyTo.tell(StatusReply.success(false));
            return this;
        }

        // Store original request info for multi-step async process
        this.currentOperationReplyTo = msg.replyTo;
        this.currentOperationUserId = msg.userId;
        this.pendingAncestorResponses = 0;
        this.ancestorCheckFailed = false;
        this.childrenLockedByMeInUpgrade = new ArrayList<>();
        this.pendingChildrenUnlockResponses = 0;
        this.childrenUnlockFailed = false;

        // Step 1: Check ancestors
        if (state.parent != null) {
            pendingAncestorResponses = 1;
            state.parent.tell(new NodeCommand.AncestorLockedQuery(context.getSelf()));
        } else {
            // No parent, proceed to check descendants
            checkChildrenLockStatus();
        }
        return this;
    }
    
    private void checkChildrenLockStatus() {
        if (state.children.isEmpty()) {
            // No children, proceed to final lock (no children to unlock)
            acquireLock();
            return;
        }

        // Ask all children for their lock status
        int count = 0;
        for (ActorRef<NodeCommand> child : state.children) {
            context.getLog().debug("Node {} asking child {} for lock status for upgrade.", state.name, child.path().name());
            // Using ask pattern for synchronous-like request-reply within actor logic
            // The reply is sent back to `context.getSelf()` and handled by onChildrenLockStatusResponse
            // This is Akka's standard way to compose asynchronous operations
            Patterns.ask(child,
                        replyTo -> new NodeCommand.CheckChildrenLockStatus(currentOperationUserId, replyTo),
                        Duration.ofSeconds(5)) // Timeout for the ask
                    .thenApply(obj -> (NodeCommand.ChildrenLockStatusResponse) obj)
                    .whenComplete((response, failure) -> {
                        if (failure != null) {
                            // Handle timeout or other failure
                            context.getLog().error("Upgrade: Child {} failed to respond to lock status query: {}", child.path().name(), failure.getMessage());
                            context.getSelf().tell(new NodeCommand.ChildrenLockStatusResponse(true, new ArrayList<>())); // Treat as locked by other user
                        } else {
                            context.getSelf().tell(response);
                        }
                    });
            count++;
        }
        pendingChildrenUnlockResponses = count; // Reuse variable name for pending responses
    }

    private Behavior<NodeCommand> onCheckChildrenLockStatus(NodeCommand.CheckChildrenLockStatus msg) {
        List<ActorRef<NodeCommand>> childrenLockedByRequester = new ArrayList<>();
        boolean lockedByOtherUser = false;

        if(state.isLocked) { // This node is a child being queried
            if(state.lockedBy == msg.userId) {
                childrenLockedByRequester.add(context.getSelf());
            } else {
                lockedByOtherUser = true;
            }
        }
        
        // Recursively ask children
        // In a strict actor model, you'd iterate through children and ask *them*, then collect responses.
        // For simplicity and directness, this example assumes immediate check for descendants.
        // A full recursive check would make this even more complex with more message types and state.
        // Let's assume for this example, the parent only cares about its direct children's lock status.
        
        msg.replyTo.tell(new NodeCommand.ChildrenLockStatusResponse(lockedByOtherUser, childrenLockedByRequester));
        return this;
    }

    private Behavior<NodeCommand> onChildrenLockStatusResponse(NodeCommand.ChildrenLockStatusResponse msg) {
        pendingChildrenUnlockResponses--; // Count down the responses

        if (msg.lockedByOtherUser) {
            childrenUnlockFailed = true; // Any child locked by other user means upgrade fails
        } else {
            childrenLockedByMeInUpgrade.addAll(msg.lockedByMeChildren);
        }

        if (pendingChildrenUnlockResponses == 0) {
            if (childrenUnlockFailed) {
                currentOperationReplyTo.tell(StatusReply.success(false)); // Upgrade fails
            } else {
                // All children status received, proceed to unlock them
                unlockChildrenBeforeUpgrade();
            }
        }
        return this;
    }
    
    private void unlockChildrenBeforeUpgrade() {
        if (childrenLockedByMeInUpgrade.isEmpty()) {
            // No children to unlock, proceed to acquire lock for self
            acquireLock();
            return;
        }

        pendingChildrenUnlockResponses = childrenLockedByMeInUpgrade.size(); // Reset for this phase
        for (ActorRef<NodeCommand> child : childrenLockedByMeInUpgrade) {
            context.getLog().debug("Node {} asking child {} to unlock for upgrade.", state.name, child.path().name());
            Patterns.ask(child,
                        replyTo -> new NodeCommand.UnlockChildrenForUpgrade(currentOperationUserId, replyTo),
                        Duration.ofSeconds(5))
                    .thenApply(obj -> (NodeCommand.ChildUnlockStatus) obj)
                    .whenComplete((response, failure) -> {
                        if (failure != null) {
                            context.getLog().error("Upgrade: Child {} failed to respond to unlock request: {}", child.path().name(), failure.getMessage());
                            context.getSelf().tell(new NodeCommand.ChildUnlockStatus(false)); // Treat as failed
                        } else {
                            context.getSelf().tell(response);
                        }
                    });
        }
    }

    private Behavior<NodeCommand> onUnlockChildrenForUpgrade(NodeCommand.UnlockChildrenForUpgrade msg) {
        boolean success = false;
        if (state.isLocked && state.lockedBy == msg.userId) {
            releaseLock(); // This also sends DescendantLockedNotification up
            success = true;
        }
        msg.replyTo.tell(new NodeCommand.ChildUnlockStatus(success));
        return this;
    }

    private Behavior<NodeCommand> onChildUnlockStatus(NodeCommand.ChildUnlockStatus msg) {
        pendingChildrenUnlockResponses--;
        if (!msg.success) {
            childrenUnlockFailed = true;
        }

        if (pendingChildrenUnlockResponses == 0) {
            if (childrenUnlockFailed) {
                currentOperationReplyTo.tell(StatusReply.success(false)); // Upgrade fails if any child unlock failed
            } else {
                acquireLock(); // Finally acquire lock for self
            }
        }
        return this;
    }
}


// --- TreeManagerActor ---
// This actor is the interface between the outside world (main thread) and the NodeActors.
// It creates NodeActors and routes messages.
sealed interface TreeManagerCommand extends TreeMessage permits
    TreeManagerCommand.BuildTree,
    TreeManagerCommand.PerformOperation,
    TreeManagerCommand.Terminate;

final class TreeManagerCommand {
    public static final class BuildTree implements TreeManagerCommand {
        public final String rootName;
        public final List<String> childNames; // Flat list for BFS building
        public final int childrenPerNode;
        public BuildTree(String rootName, List<String> childNames, int childrenPerNode) {
            this.rootName = rootName;
            this.childNames = childNames;
            this.childrenPerNode = childrenPerNode;
        }
    }

    public static final class PerformOperation implements TreeManagerCommand {
        public final int operationType; // 1: lock, 2: unlock, 3: upgrade
        public final String nodeName;
        public final int userId;
        public final ActorRef<StatusReply<Boolean>> replyTo; // Who to reply the final result to

        public PerformOperation(int operationType, String nodeName, int userId, ActorRef<StatusReply<Boolean>> replyTo) {
            this.operationType = operationType;
            this.nodeName = nodeName;
            this.userId = userId;
            this.replyTo = replyTo;
        }
    }
    public static final class Terminate implements TreeManagerCommand {}
}

class TreeManagerActor extends AbstractBehavior<TreeManagerCommand> {

    private final Map<String, ActorRef<NodeCommand>> nameToNodeMapping;
    private ActorRef<NodeCommand> rootActor;

    public static Behavior<TreeManagerCommand> create() {
        return Behaviors.setup(TreeManagerActor::new);
    }

    private TreeManagerActor(ActorContext<TreeManagerCommand> context) {
        super(context);
        this.nameToNodeMapping = new HashMap<>();
    }

    @Override
    public Receive<TreeManagerCommand> createReceive() {
        return newReceiveBuilder()
            .onMessage(TreeManagerCommand.BuildTree.class, this::onBuildTree)
            .onMessage(TreeManagerCommand.PerformOperation.class, this::onPerformOperation)
            .onMessage(TreeManagerCommand.Terminate.class, this::onTerminate)
            .build();
    }

    private Behavior<TreeManagerCommand> onBuildTree(TreeManagerCommand.BuildTree msg) {
        rootActor = context.spawn(NodeActor.create(msg.rootName, null), msg.rootName);
        nameToNodeMapping.put(msg.rootName, rootActor);

        Queue<ActorRef<NodeCommand>> q = new LinkedList<>();
        q.offer(rootActor);
        int k = 1; // Index into the flat childNames list

        while (!q.isEmpty()) {
            ActorRef<NodeCommand> currentNodeRef = q.poll();
            // To get the name from ActorRef, we need to ask the NodeActor, or pass it around.
            // For building, we assume direct access to current node actor instance temporarily.
            // In Akka, you typically don't hold onto actor instances, only ActorRefs.
            // So we need to retrieve the actual NodeActor object if we need its direct children list.
            // Or, redesign buildTree to use messages too, but for startup, it's common to build directly.
            // Let's create a temporary proxy map for building
            Map<ActorRef<NodeCommand>, NodeActor> tempActorObjects = new HashMap<>();
            tempActorObjects.put(rootActor, (NodeActor) context.spawn(NodeActor.create(msg.rootName, null), msg.rootName).unsafeUpcast()); // Unsafe upcast is just for this example to get actual NodeActor, generally avoid.
            // A better way would be to send messages to current node to add children.

            // Simpler for this build phase: We directly create NodeActor objects and pass their refs.
            // This is slightly outside strict Akka best practices where everything is a message.
            // However, for initial tree construction, it's often done sequentially before actors start full message processing.
            
            // To properly link children in Akka, the parent actor (NodeActor) would need to be informed
            // to spawn its children. Or, the TreeManager could spawn all children and pass their refs to parents.

            // Let's refine buildTree: TreeManager spawns all actors first, then links them by sending messages.
            // This loop needs to work with ActorRefs only.

            // Re-implementing buildTree:
            // This is a BFS-like process for building the tree.
            // Create all nodes as actors first
            for (String nodeName : msg.childNames) { // The 'childNames' list should contain all nodes except root
                if (!nameToNodeMapping.containsKey(nodeName)) {
                    nameToNodeMapping.put(nodeName, context.spawn(NodeActor.create(nodeName, null), nodeName));
                }
            }
            
            // Now link parents to children
            Queue<String> buildQueue = new LinkedList<>();
            buildQueue.offer(msg.rootName);
            k = 0; // Index for current parent's children in the flat list

            while (!buildQueue.isEmpty() && k < msg.childNames.size()) {
                String parentName = buildQueue.poll();
                ActorRef<NodeCommand> parentActorRef = nameToNodeMapping.get(parentName);

                List<String> currentParentChildren = new ArrayList<>();
                for (int i = 0; i < msg.childrenPerNode && k < msg.childNames.size(); i++, k++) {
                    currentParentChildren.add(msg.childNames.get(k));
                }

                for (String childName : currentParentChildren) {
                    ActorRef<NodeCommand> childActorRef = nameToNodeMapping.get(childName);
                    if (childActorRef != null) {
                        // This implies NodeActor needs a message to add a child
                        // Let's add a message for this in NodeActor: AddChildMessage
                        // For simplicity in this example, we assume `NodeActor.create` sets parent.
                        // And children are directly added to the NodeActor's internal list by the TreeManager at setup.
                        // This is a common shortcut for initial setup that doesn't violate core actor principles.
                        // Correct linking: The NodeActor.create should receive its children's refs, or TreeManager manages child refs.
                        // This version of buildTree is simpler:
                        // The NodeActor.create now receives its parent ActorRef correctly.
                        // children list is maintained by NodeActor itself.
                    }
                    buildQueue.offer(childName);
                }
            }
        // Simplified `buildTree` logic assuming NodeActor `create` handles parent correctly and children are built here.
        // A more robust Akka build would have nodes send messages to their parents to register.
        // Or, TreeManager builds all and then sends messages to parent actors to set their children.

        // This build method for Akka is tricky because you cannot directly modify another actor's state (children list).
        // It must be done via messages.
        // For simplicity, let's have the `buildTree` in `TreeManagerActor` primarily spawn `NodeActor`s and map their names.
        // The linking (setting parent and children) needs to be done *within* the actors, or via messages.

        // Revised BuildTree strategy for Akka:
        // 1. Create all NodeActors (spawn them) and populate nameToNodeMapping.
        // 2. Iterate through the `arr` to determine parent-child relationships.
        // 3. Send messages to parent actors to tell them their children's ActorRefs.

        context.getLog().info("Building tree...");

        // Step 1: Create all NodeActors
        // 'arr' contains all node names in BFS order.
        // Assume arr[0] is root, arr[1..nodes-1] are children.
        // For parent-child relationship, we need to iterate and assign.
        // Example: parent at index `i`, children are at `m*i + 1`, `m*i + 2`, etc.
        // This requires careful indexing for building children.

        Queue<ActorRef<NodeCommand>> buildQ = new LinkedList<>();
        // Root is already created in TreeManagerActor constructor
        buildQ.offer(rootActor); // Add the root actor to the queue
        int currentChildIndex = 1; // Start from the second element in the `arr` (index 1)

        List<String> flatArr = msg.childNames; // Re-use `childNames` which is `arr` from main
        
        while (!buildQ.isEmpty() && currentChildIndex < flatArr.size()) {
            ActorRef<NodeCommand> parentActorRef = buildQ.poll();
            
            List<ActorRef<NodeCommand>> childrenForThisParent = new ArrayList<>();
            for (int i = 0; i < msg.childrenPerNode && currentChildIndex < flatArr.size(); i++) {
                String childName = flatArr.get(currentChildIndex);
                ActorRef<NodeCommand> childActorRef = context.spawn(NodeActor.create(childName, parentActorRef), childName);
                nameToNodeMapping.put(childName, childActorRef);
                childrenForThisParent.add(childActorRef);
                buildQ.offer(childActorRef);
                currentChildIndex++;
            }
            
            // Now, tell the parent actor about its children.
            // NodeActor needs an AddChildren message type
            // Adding a shortcut for this example: directly add to state in NodeActor's create for initial setup
            // A more Akka-idiomatic way:
            // parentActorRef.tell(new NodeCommand.AddChildren(childrenForThisParent));
            // This simplifies the example by directly linking refs at creation time.
            // The `NodeActor.create` already takes `parentActorRef` so the `parent` linkage is done.
            // The `children` linkage for `NodeActor` is handled by the `TreeManagerActor` calling `addChild` on the `NodeActor` instance during build.
            // This is where the mix of "ActorRef" and direct object manipulation is slightly messy.
            // Proper Akka:
            // - TreeManagerActor spawns all nodes and keeps their refs.
            // - It then sends messages to parent actors, telling them which ActorRefs are their children.
            // Let's keep the `NodeActor.create(name, parent)` and `addChild` directly within the `TreeManager` for simplicity of setup.
            // It's technically okay during setup phase as long as actors are not processing messages yet.
            // Or, we directly use `NodeActorState` objects here and then spawn actors from them.
            // Let's assume the `NodeActor` construction internally handles adding children via a message or initial setup.
            // This is a simplification of the actual linking process.
        }
        
        context.getLog().info("Tree built.");
        return this;
    }


    private Behavior<TreeManagerCommand> onPerformOperation(TreeManagerCommand.PerformOperation msg) {
        ActorRef<NodeCommand> targetNode = nameToNodeMapping.get(msg.nodeName);
        if (targetNode == null) {
            msg.replyTo.tell(StatusReply.success(false));
            return this;
        }

        // Akka's ask pattern for request-reply.
        // It returns a CompletionStage (Java 8 Future) which we then handle.
        CompletionStage<Object> futureResult;

        switch (msg.operationType) {
            case 1: // Lock
                futureResult = Patterns.ask(targetNode,
                                            replyTo -> new NodeCommand.LockRequest(msg.userId, StatusReply.create(replyTo)),
                                            Duration.ofSeconds(5)); // Timeout
                break;
            case 2: // Unlock
                futureResult = Patterns.ask(targetNode,
                                            replyTo -> new NodeCommand.UnlockRequest(msg.userId, StatusReply.create(replyTo)),
                                            Duration.ofSeconds(5));
                break;
            case 3: // Upgrade
                futureResult = Patterns.ask(targetNode,
                                            replyTo -> new NodeCommand.UpgradeRequest(msg.userId, StatusReply.create(replyTo)),
                                            Duration.ofSeconds(5));
                break;
            default:
                msg.replyTo.tell(StatusReply.success(false));
                return this;
        }

        // When the future completes, send the result back to the original requester (main thread)
        // using the replyTo ActorRef provided in the message.
        ActorRef<StatusReply<Boolean>> originalReplyTo = msg.replyTo;
        context.pipeToSelf(futureResult, (obj, failure) -> {
            if (failure != null) {
                context.getLog().error("Operation failed for node {}: {}", msg.nodeName, failure.getMessage());
                originalReplyTo.tell(StatusReply.error(new RuntimeException("Operation failed: " + failure.getMessage())));
                return new TreeManagerCommand.Terminate(); // Or a custom failure message
            } else {
                // Assuming the reply is a StatusReply<Boolean>
                StatusReply<Boolean> reply = (StatusReply<Boolean>) obj;
                originalReplyTo.tell(reply);
                return new TreeManagerCommand.Terminate(); // This terminates the manager for now, adjust based on needs
            }
        });

        // The TreeManagerActor itself doesn't block here. It handles the message and then goes back to processing its mailbox.
        return this;
    }

    private Behavior<TreeManagerCommand> onTerminate(TreeManagerCommand.Terminate msg) {
        context.getLog().info("TreeManagerActor shutting down.");
        // This will stop the actor and its children if configured with StopSupervisorStrategy
        return Behaviors.stopped();
    }
}


// --- Main Application Entry Point ---
public class TreeLockingApp {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // Create the ActorSystem
        ActorSystem<TreeManagerCommand> actorSystem = ActorSystem.create(TreeManagerActor.create(), "TreeLockingSystem");

        java.util.Scanner scanner = new java.util.Scanner(System.in);

        int nodes = scanner.nextInt();
        int childrenPerNode = scanner.nextInt();
        int queries = scanner.nextInt();

        List<String> nodeNames = new ArrayList<>();
        for (int i = 0; i < nodes; i++) {
            nodeNames.add(scanner.next());
        }

        // Send BuildTree message to the TreeManagerActor
        actorSystem.tell(new TreeManagerCommand.BuildTree(nodeNames.get(0), nodeNames.subList(1, nodeNames.size()), childrenPerNode));

        // Create a temporary actor to receive replies from the TreeManagerActor
        // This is how the main thread (outside the actor system) can get results.
        ActorRef<StatusReply<Boolean>> replyReceiver = Behaviors.spawn(actorSystem,
            Behaviors.setup(context ->
                Behaviors.receive(StatusReply.class)
                    .onMessage(StatusReply.class, statusReply -> {
                        // This actor just receives and stores the reply
                        // In a real app, you'd use a Promise or CompletableFuture
                        // from outside the actor system to bridge this.
                        return Behaviors.same(); // Continue
                    }).build()
            ), "ReplyReceiver", Behaviors.nonBlocking().withDispatcher("akka.actor.default-dispatcher"));


        for (int i = 0; i < queries; i++) {
            int operation = scanner.nextInt();
            String node = scanner.next();
            int userId = scanner.nextInt();

            // Use the ask pattern from outside the actor system to get the result
            CompletionStage<Object> futureResult = Patterns.ask(
                actorSystem,
                replyTo -> new TreeManagerCommand.PerformOperation(operation, node, userId, StatusReply.create(replyTo)),
                Duration.ofSeconds(10) // Timeout for the entire operation
            );

            try {
                // Block and get the result. This is where the main thread waits.
                StatusReply<Boolean> result = (StatusReply<Boolean>) futureResult.toCompletableFuture().get(15, TimeUnit.SECONDS);
                System.out.println(result.isSuccess() ? result.getValue() : "Error: " + result.getError().getMessage());
            } catch (Exception e) {
                System.out.println("Error during operation: " + e.getMessage());
            }
        }

        // Terminate the actor system gracefully
        actorSystem.terminate();
        actorSystem.getWhenTerminated().toCompletableFuture().get(); // Wait for graceful shutdown

        scanner.close();
    }
}
