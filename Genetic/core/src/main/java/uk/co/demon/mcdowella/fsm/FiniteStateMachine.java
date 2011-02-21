package uk.co.demon.mcdowella.fsm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import uk.co.demon.mcdowella.algorithms.MaxInRange;
import uk.co.demon.mcdowella.algorithms.TreeDefault;

/** This class is the Finite State Machine actually built from the
 *  user's representation. Once built, it does not depend on or
 *  refer to the State objects passed in - unless, for instance, 
 *  you set each State's Key to be itself. It does keep a reference
 *  to the Actions and EventListeners passed in so it can fire them,
 *  and to the Events, so it can recognise them when they are accepted.
 */
public class FiniteStateMachine {

    /** represents the current state of the machine */
    private FsmNode currentState;

    /** return the key from the current state, or null if no current
     * state */
    public Object getCurrentKey() {
        if (currentState == null) {
            return null;
        }
        return currentState.getKey();
    }
    /** previous state of the machine, or null if none */
    private FsmNode prevState;

    /** return the key from previous state of the machine,
    or null if none 
     */
    public Object getPrevState() {
        if (prevState == null) {
            return null;
        }
        return prevState.getKey();
    }
    /** Event triggering the current transition or action */
    private Event currentEvent;

    /** return the event triggering the current transition or action.
     *  In the case of EXIT or ENTRY events generated by an event,
     *  this will be EXIT or ENTRY, not the event in question.
     */
    public Event getCurrentEvent() {
        return currentEvent;
    }

    /** Info for depth first search to set up FsmNodes */
    private static class DfsState {

        /** node whose children this iterates over */
        private FsmNode parentNode;

        FsmNode getParent() {
            return parentNode;
        }
        /** iterator over children of parentNode not yet handled */
        private Iterator<State> childIt;

        Iterator<State> getChildIt() {
            return childIt;
        }
        /** Value for entry ancestor pointer of children */
        private FsmNode upperEntry;

        FsmNode getUpperEntry() {
            return upperEntry;
        }
        /** Value for exit ancestor pointer of children */
        private FsmNode upperExit;

        FsmNode getUpperExit() {
            return upperExit;
        }
        /** value for listener ancestor pointer of children */
        private FsmNode upperListener;

        FsmNode getUpperListener() {
            return upperListener;
        }

        /** Create DfsState with root node as its children */
        DfsState(State root) {
            ArrayList<State> forIt = new ArrayList<State>();
            forIt.add(root);
            childIt = forIt.iterator();
        }

        private DfsState() {
        }

        /** get DfsState to iterate over a child, or null if none.
         *  Also grabs info from child to pass on to its children,
         *  and keeps notes to see if the children include (at most one)
         *  initial state.
         */
        DfsState getChildState(FsmNode childAsNode, State childAsState) {
            if (childAsNode.getNodeType() == State.StateEnum.INITIAL) {
                if (initialNode != null) {
                    throw new BadArgumentException(
                            "State has multiple initial substates",
                            parentNode.getKey());
                }
                initialNode = childAsNode;
            }
            Iterator<State> it = childAsState.getSubIter();
            if (!it.hasNext()) {
                return null;
            }
            DfsState result = new DfsState();
            result.childIt = it;
            result.parentNode = childAsNode;
            // Whether this node has listeners interested in events
            // fired by substates of this node
            if (childAsNode.hasSubstateListeners()) {
                result.upperListener = childAsNode;
            } else {
                result.upperListener = upperListener;
            }
            if (childAsState.getAction(Event.ENTRY) != null) {
                result.upperEntry = childAsNode;
            } else {
                result.upperEntry = upperEntry;
            }
            if (childAsState.getAction(Event.EXIT) != null) {
                result.upperExit = childAsNode;
            } else {
                result.upperExit = upperExit;
            }
            return result;
        }
        /** Records initial state for parentNode */
        private FsmNode initialNode;

        FsmNode getInitialNode() {
            return initialNode;
        }
    }
    /** Root node of tree corresponding to tree of states */
    private FsmNode rootNode;
    /** TreeDefault we use to work out which action to fire (for
     *  external events)
     */
    private TreeDefault actionTree;
    /** Used to keep track of when every node was last visited,
     *  for history states
     */
    private MaxInRange lastVisited;

    /** Class holding old and new numbering scheme for histories */
    private static class OldNew implements Comparable<OldNew> {

        final long old;
        long theNew;

        public int compareTo(OldNew on) {
            if (old < on.old) {
                return -1;
            }
            if (old > on.old) {
                return 1;
            }
            return 0;
        }

        void setNew(long newVal) {
            theNew = newVal;
        }

        OldNew(long theOld) {
            old = theOld;
        }

        long getNew() {
            return theNew;
        }
    }

    /** Translate long - which had better be in the table */
    private static long doTranslate(OldNew[] table, long val) {
        OldNew key = new OldNew(val);
        int offset = Arrays.binarySearch(table, key);
        return key.getNew();
    }

    /** renumber the history states and adjust startCount and
     *  transition count
     */
    void reNumber() {
        int past = lastVisited.getLength();
        OldNew[] translate = new OldNew[past + 2];
        for (int i = 0; i < past; i++) {
            translate[i] = new OldNew(lastVisited.get(i));
        }
        translate[past] = new OldNew(startCount);
        translate[past + 1] = new OldNew(transitionCount);
        Arrays.sort(translate);
        for (int i = 0; i < translate.length; i++) {
            translate[i].setNew(Long.MIN_VALUE + i + 1);
        }
        for (int i = 0; i < past; i++) {
            lastVisited.set(i, doTranslate(translate, lastVisited.get(i)));
        }
        startCount = doTranslate(translate, past);
        transitionCount = doTranslate(translate, past + 1);
    }
    /**
     * Number just before the one assigned to the first transition since
     * the most recent reset, if any
     */
    private long startCount = Long.MIN_VALUE;
    /** Count of transitions made since last reset, but starting from
     *  startCount;
     */
    private long transitionCount = startCount;
    /** Node by depth first search number, from lastVisited */
    private FsmNode[] nodeByNumber;

    /** Create from the root state of a representation of the
     *  state machine to be produced. The identities of the Events and
     *  Actions in the representation retain their significance, but
     *  any later changes to the representation made via the publically
     *  accessible methods on this class are ignored. States are only
     *  referred to by their associated keys.
     *  <br>
     *  After creation the current state is null - use reset() to
     *  move to the initial state, firing any initial actions
     */
    public FiniteStateMachine(State root) {
        // First we run a depth first search on the tree, checking that it
        // really is a tree, and creating the TreeDefault info we will
        // use to work out which state holds the TransitionInfo for
        // each event. Use an explicit stack, as we don't control
        // the depth of the tree.
        ArrayList<DfsState> stateStack = new ArrayList<DfsState>();
        Map<State, FsmNode> nodeByState = new HashMap<State, FsmNode>();
        stateStack.add(new DfsState(root));
        // This holds a pointer to the latest FsmNode constructed. We
        // assign it to parentNodes passed as we recurse up the tree,
        // and it gives them a pointer to their last numbered child, in
        // depth first search order. If a node has no children its
        // lastChild pointer will be set to null. It will be briefly wrong
        // after creating a node that has children, but this will be set
        // right at its first child.
        FsmNode lastChild = null;
        for (;;) {
            int at = stateStack.size() - 1;
            if (at < 0) {
                break;
            }
            // At this point the stack contains a path down the tree
            // from the root of nodes we are working on. If non-null,
            // lastChild points to the last created node below all of them
            // and we are about to look at the lowest node in the path,
            // except if we have a node with known children at the bottom,
            // in which case lastChild is about to be assigned to
            DfsState here = stateStack.remove(at);
            Iterator<State> it = here.getChildIt();
            for (;;) {
                if (!it.hasNext()) {
                    FsmNode parentNode = here.getParent();
                    if (parentNode != null) {
                        FsmNode initial = here.getInitialNode();
                        State.StateEnum parentType = parentNode.getNodeType();
                        if ((parentType == State.StateEnum.DEEP_HISTORY)
                                || (parentType == State.StateEnum.SHALLOW_HISTORY)) {
                            throw new BadArgumentException(
                                    "History state has substates", parentNode.getKey());
                        }
                        parentNode.setInitialNode(initial);
                        // LastChild has be set to and is correct because this
                        // node had children and we have seen them
                        parentNode.setLastChild(lastChild);
                    }
                    break;
                }
                State st = it.next();
                // We create a new child of the lowest node in the stack,
                // so it is the last created child of any of them
                lastChild = new FsmNode(st, here, st.getStateType(),
                        st.getKey());
                if (nodeByState.put(st, lastChild) != null) { // state seen twice so not a tree
                    throw new BadArgumentException("States do not form a tree",
                            st);
                }
                DfsState there = here.getChildState(lastChild, st);
                if (there != null) {
                    stateStack.add(here);
                    here = there;
                    it = here.getChildIt();
                    // Here when adding a node that is known to have children,
                    // so we will create at least one child, assigning it to
                    // lastChild, before we remove it from the stack
                }
            }
        }
        // Now build the transition info
        for (Map.Entry<State, FsmNode> mi : nodeByState.entrySet()) {
            State st = mi.getKey();
            FsmNode fn = mi.getValue();
            for (Iterator<Map.Entry<Event, State.TransitionInfo>> i =
                    st.getTransitionIterator(); i.hasNext();) {
                Map.Entry<Event, State.TransitionInfo> me = i.next();
                Event e = me.getKey();
                State.TransitionInfo oldInfo = me.getValue();
                State target = oldInfo.getTarget();
                FsmNode targetAsNode = null;
                if (e == Event.INITIAL) {
                    if ((st.getStateType() != State.StateEnum.DEEP_HISTORY)
                            && (st.getStateType() != State.StateEnum.SHALLOW_HISTORY)) { // Don't accept initial events
                        // except on history states: we don't
                        // handle them and UML does not provide for them: we only
                        // use them
                        // to identify the first state to go to on entry, and on
                        // the first entry to a history state
                        throw new BadArgumentException(
                                "Transition on INITIAL event from non-history state",
                                st);
                    }
                    if (oldInfo.getAction() != null) {
                        throw new BadArgumentException(
                                "Action on initial transition", st);
                    }
                }
                if (target != null) {
                    targetAsNode = nodeByState.get(target);
                    if (targetAsNode == null) {
                        throw new BadArgumentException(
                                "Transition to node not in tree", target);
                    }
                    if ((target.getSubIter().hasNext())
                            && (targetAsNode.getInitialNode() == null)) {
                        throw new BadArgumentException(
                                "Transition to target with substates but no initial state",
                                target);
                    }
                    if ((e == Event.EXIT) || (e == Event.ENTRY)) {
                        throw new BadArgumentException(
                                "Transition on EXIT or exit event", st);
                    }
                }
                fn.put(e, new TransitionInfo(oldInfo, targetAsNode));
            }
        }
        // extract root node
        rootNode = nodeByState.get(root);
        // and build the TreeDefault structure to query for transitions
        actionTree = new TreeDefault(rootNode);
        int nodeCount = nodeByState.size();
        lastVisited = new MaxInRange(nodeCount);
        nodeByNumber = new FsmNode[nodeCount];
        for (FsmNode fsmi : nodeByState.values()) {
            nodeByNumber[fsmi.getNumber()] = fsmi;
        }
    }

    /** Reset the machine, entering the top-level initial state */
    public void reset() {
        startCount = transitionCount++;
        currentState = rootNode.getInitialNode();
        if (currentState == null) {
            currentState = rootNode;
        }
        prevState = null;
        lastVisited.set(currentState.getNumber(), transitionCount);
        // Prepare to fire entry actions
        List<FsmNode> al = new ArrayList<FsmNode>();
        al.add(currentState);
        FsmNode wasCurrent = currentState;
        for (;;) {
            wasCurrent = wasCurrent.getUpperEntry();
            if (wasCurrent == null) {
                break;
            }
            al.add(wasCurrent);
        }
        for (int x = al.size() - 1; x > 0; x--) {
            sendEventHere(al.get(x), Event.ENTRY, false);
        }
        if (!al.isEmpty()) {
            sendEventHere(al.get(0), Event.ENTRY, true);
        }
    }

    /** send event and optionally fire related listeners. Fire actions
     *  first, unless the event is the EXIT event, in which case fire
     *  them last
     */
    void sendEventHere(FsmNode node, Event event, boolean doListeners) {
        currentEvent = event;
        TransitionInfo ti = (TransitionInfo) node.get(currentEvent);
        Action act = null;
        if (ti != null) {
            act = ti.getAction();
        }
        if (event != Event.EXIT) {
            if (act != null) {
                act.act(this);
            }
        }
        if (doListeners) {
            if (node.exactListenerList != null) {
                for (EventListener el : node.exactListenerList) {
                    el.eventReceived(this);
                }
            }
            for (;;) {
                if (node.listenerList != null) {
                    for (EventListener el : node.listenerList) {
                        el.eventReceived(this);
                    }
                }
                node = node.upperListener;
                if (node == null) {
                    break;
                }
            }
        }
        if (event == Event.EXIT) {
            if (act != null) {
                act.act(this);
            }
        }
    }

    /** Accept an event, firing actions and so on. Return false if
     *  no handler for event or event not recognised. currentEvent
     *  is only set if the event is recognised. The currentState is
     *  set to any new state before any actions are fired.
     */
    public boolean acceptEvent(Event e) {
        TransitionInfo ti =
                (TransitionInfo) actionTree.getValueWithDefaults(
                currentState, e);
        if (ti == null) {
            return false;
        }
        FsmNode target = ti.getTarget();
        Action action = ti.getAction();
        if (target == null) { // Easy - no transition to make so just fire the action,
            // passing it on to the listeners as well
            sendEventHere(currentState, e, true);
            return true;
        }
        State.StateEnum st = target.getNodeType();
        if (st == State.StateEnum.DEEP_HISTORY) {
            // Sort out history. Note that we never actually visit a history
            // state, or a state with substates, so the result of a deep
            // history search must be a node we actually want to go to
            FsmNode parent = target.getParent();
            FsmNode lastChild = parent.getLastChild();
            // Here we rely on depth first search numbering, looking from
            // the first child of the parent node (which has been numbered
            // just after it) to, inclusive, its last child. Since the
            // parent has children, its lastChild value is non-null
            int targetNumber = lastVisited.getMaxIndex(parent.getNumber() + 1,
                    lastChild.getNumber() + 1);
            if (lastVisited.get(targetNumber) <= startCount) { // never visited since last reset, so look to see where its
                // INITIAL event points. We know it has a transition from here,
                // because we checked for that
                target =
                        ((TransitionInfo) target.get(Event.INITIAL)).getTarget();
            } else {
                target = nodeByNumber[targetNumber];
            }
        } else if (st == State.StateEnum.SHALLOW_HISTORY) {
            FsmNode parent = target.getParent();
            FsmNode lastChild = parent.getLastChild();
            int targetNumber = lastVisited.getMaxIndex(parent.getNumber() + 1,
                    lastChild.getNumber() + 1);
            if (lastVisited.get(targetNumber) <= startCount) { // never visited before
                target =
                        ((TransitionInfo) target.get(Event.INITIAL)).getTarget();
            } else { // need to find which immediate substate contains this target
                // We want the highest numbered substate that has a number
                // <= the target
                int past = parent.getNumChildren();
                int first = 0;
                for (;;) {
                    if (past <= (first + 1)) { // answer is at first
                        break;
                    }
                    int probe = (past + first + 1) >> 1;
                    int num = parent.getChild(probe).getNumber();
                    if (num == targetNumber) {
                        first = probe;
                        break;
                    }
                    if (num < targetNumber) { // reduces range, since probe > first
                        first = probe;
                    } else {
                        past = probe;
                    }
                }
                target = (FsmNode) parent.getChild(first);
                FsmNode initial = target.getInitialNode();
                if (initial != null) {
                    target = initial;
                }
            }
        } else {
            // If we have been targeted on a state with substates, we want
            // its initial state
            FsmNode initial = target.getInitialNode();
            if (initial != null) {
                target = initial;
            }
        }
        transitionCount++;
        if (transitionCount == Long.MIN_VALUE) { // 2^64 transitions since construction, so
            // renumber the history from here. Note that we only need to
            // store the relative order of the transitions, so we can
            // sort them and then renumber as MIN_VALUE+1, MIN_VALUE+2...
            reNumber();
        }
        lastVisited.set(target.getNumber(), transitionCount);
        // We have a transition. UML says we fire the exit actions,
        // then the transition, then the entry actions.
        prevState = currentState;
        currentState = target;
        currentEvent = Event.EXIT;
        // To fire the exit actions, we follow Exit pointers up from
        // the current state until we meet the target state, or one
        // of its ancestors
        FsmNode wasPrev = prevState;
        FsmNode wasCurrent = currentState;
        sendEventHere(prevState, Event.EXIT, true);
        for (;;) {
            wasPrev = wasPrev.getUpperExit();
            if (wasPrev == null) {
                break;
            }
            while ((wasCurrent != null)
                    && (wasCurrent.getDepth() > wasPrev.getDepth())) {
                wasCurrent = wasCurrent.getUpperExit();
            }
            if (wasPrev == wasCurrent) { // we have found a common ancestor at or above the lowest
                // common ancestor - we do not enter or exit this state,
                // or any of its ancestors, so break here
                break;
            }
            sendEventHere(wasPrev, Event.EXIT, false);
        }
        // perform the action itself
        sendEventHere(prevState, e, true);
        // Now gather any entry actions
        wasPrev = prevState;
        wasCurrent = currentState;
        List<FsmNode> al = new ArrayList<FsmNode>();
        al.add(currentState);
        for (;;) {
            wasCurrent = wasCurrent.getUpperEntry();
            if (wasCurrent == null) {
                break;
            }
            while ((wasPrev != null)
                    && (wasPrev.getDepth() > wasCurrent.getDepth())) {
                wasPrev = wasPrev.getUpperEntry();
            }
            if (wasPrev == wasCurrent) { // we have found a common ancestor at or above the lowest
                // common ancestor - we do not enter or exit this state,
                // or any of its ancestors, so break here
                break;
            }
            al.add(wasCurrent);
        }
        for (int x = al.size() - 1; x > 0; x--) {
            sendEventHere(al.get(x), Event.ENTRY, false);
        }
        if (!al.isEmpty()) {
            sendEventHere(al.get(0), Event.ENTRY, true);
        }
        return true;
    }

    /** This class holds the info we need about a state */
    private static class FsmNode extends TreeDefault.DefaultNode {

        /** parent of this node */
        private final FsmNode parent;

        FsmNode getParent() {
            return parent;
        }
        /** type of this node */
        private final State.StateEnum nodeType;

        /** return the node type */
        State.StateEnum getNodeType() {
            return nodeType;
        }
        /** last child visited in dfs order */
        private FsmNode lastChild;

        /** return last child visited in dfs order before exiting this
         *  node for the last time
         */
        FsmNode getLastChild() {
            return lastChild;
        }

        /** set last child in dfs order */
        void setLastChild(FsmNode child) {
            lastChild = child;
        }
        /** depth of this node (root is zero) */
        private final int depth;

        int getDepth() /** return the depth of this node (root is zero) */
        {
            return depth;
        }
        /** points to node corresponding to lowest ancestor with exit
         *  action or non-null event listener list
         */
        private final FsmNode upperExit;

        /** Return the lowest ancestor with an exit action, or non-null
         *  event listener list
         */
        FsmNode getUpperExit() {
            return upperExit;
        }
        /** event listeners for this node and subnodes */
        private final List<EventListener> listenerList;

        /** whether this node has any listeners depending on substates */
        public boolean hasSubstateListeners() {
            if (listenerList == null) {
                return false;
            }
            return !listenerList.isEmpty();
        }
        /** event listeners for this node only */
        private final List<EventListener> exactListenerList;
        /** lowest ancestor with non-null event listener list */
        private final FsmNode upperListener;
        /** points to node corresponding to lowest ancestor with entry
         *  action
         */
        private final FsmNode upperEntry;

        /** Return the lowest ancestor with an entry action
         */
        FsmNode getUpperEntry() {
            return upperEntry;
        }
        /** Key passed in from State */
        private final Object key;

        /** return the key passed in from the state */
        public Object getKey() {
            return key;
        }

        FsmNode(State asState, DfsState dfsInfo, State.StateEnum typeCode,
                Object forKey) {
            key = forKey;
            if ((typeCode == State.StateEnum.SHALLOW_HISTORY)
                    || (typeCode == State.StateEnum.DEEP_HISTORY)) {
                // Require a target for the INITIAL event for the case when
                // the state has not been entered before
                if (asState.getTargetState(Event.INITIAL) == null) {
                    throw new BadArgumentException(
                            "History state must have a target for the INITIAL event",
                            asState.getKey());
                }
            }
            nodeType = typeCode;
            parent = dfsInfo.getParent();
            if (parent == null) {
                depth = 0;
            } else {
                depth = parent.depth + 1;
            }
            if (parent != null) {
                parent.addChild(this);
            }
            upperExit = dfsInfo.getUpperExit();
            upperEntry = dfsInfo.getUpperEntry();
            upperListener = dfsInfo.getUpperListener();
            List<EventListener> exacts = new ArrayList<EventListener>();
            List<EventListener> anys = new ArrayList<EventListener>();
            for (Iterator<Map.Entry<EventListener, Boolean>> li =
                    asState.getListeners(); li.hasNext();) {
                Map.Entry<EventListener, Boolean> me = li.next();
                if (me.getValue().booleanValue()) {
                    anys.add(me.getKey());
                } else {
                    exacts.add(me.getKey());
                }
            }
            if (anys.isEmpty()) {
                listenerList = null;
            } else {
                listenerList = anys;
            }
            if (exacts.isEmpty()) {
                exactListenerList = null;
            } else {
                exactListenerList = exacts;
            }
        }
        /** initial node, for transitions to states with substates */
        private FsmNode initialNode;

        /** Set the initial field for this state, used to redirect
         *  transitions to a state with substates. This makes use of
         *  the fact that setInitialNode() is called for substates before
         *  it is called on their parent state. If a state has an INITIAL
         *  substate which also has an INITIAL substate, that sub-substate
         *  will be made the INITIAL substate of both states.
         */
        void setInitialNode(FsmNode initial) {
            // Look for lower substate already assigned to the substate
            // we would otherwise go to
            if (initial != null) {
                FsmNode lower = initial.getInitialNode();
                if (lower != null) {
                    initial = lower;
                }
            }
            initialNode = initial;
        }

        FsmNode getInitialNode() {
            return initialNode;
        }
    }

    /** This class is thrown when FiniteStateMachine is given bad
     *  input. The associated Key, if non-null, comes from a state
     *  that seems to be implicated in the problem
     */
    public static class BadArgumentException extends IllegalArgumentException {

        private final Object troubleKey;

        BadArgumentException(String message, Object trouble) {
            super(message);
            troubleKey = trouble;
        }

        /** return the Key of a state somehow associated with
         *  the trouble causing the exception.
         */
        public Object getTroubleKey() {
            return troubleKey;
        }
    }

    /** Our version of TransitionInfo */
    private static class TransitionInfo {

        private final Action action;

        Action getAction() {
            return action;
        }
        private final FsmNode target;

        FsmNode getTarget() {
            return target;
        }

        TransitionInfo(State.TransitionInfo ti, FsmNode forTarget) {
            action = ti.getAction();
            target = forTarget;
        }
    }
}
