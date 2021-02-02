package uk.co.demon.mcdowella.fsm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/** This class represents a single state in a graph of states and
 *  transitions representing a finite state machine. This graph will
 *  be fed to the constructor of FiniteStateMachine to create the
 *  finite state machine itself.
 *  The states in a graph form a tree, and the FiniteStateMachine
 *  should be passed the root state of this tree. If you just have a
 *  simple non-hierarchical state machine, you will need to make all
 *  of the states you actually care about substates of a root state
 *  created just for this purpose.
 */
public class State
{
  /** Various types of state are distinguished by their enumeration */
  public enum StateEnum
  {
    /** STANDARD is the standard state. It can have substates,
     *  though it need not do so.
     */
    STANDARD, 
    /** The INITIAL state is like a standard state, but with a
     *  special mark. If a state has a direct substate which is an
     *  INITIAL state, then transitions to that state are interpreted
     *  as transitions to that substate, or to one of its substates,
     *  if it also has an INITIAL substate. No state may have two
     *  direct initial states, although a state may have two
     *  substates, both of which have substates which are INITIAl
     *  states.
     */
    INITIAL,
    /**
     * A DEEP_HISTORY state may not have substates. Transitions to
     * it are interpreted as transitions to whichever substate of
     * its parent state (direct or indirect) was last occupied. If
     * no such substate has been occupied since the last reset, the
     * transition will be to a state given by the Event.INITIAL
     * transition from the history substate. There must be such a
     * substate.
     */
    DEEP_HISTORY,
    /**
     * A SHALLOW_HISTORY substate is like a DEEP_HISTORY substate,
     * but where the DEEP_HISTORY substate would go to an indirect
     *  sub-substate
     * of the parent state, the SHALLOW_HISTORY substate goes only
     * to its ancestral direct substate
     */
    SHALLOW_HISTORY
  }
  private final StateEnum stateType;
  /** return the StateEnum showing what sort of state this is */
  public StateEnum getStateType()
  {
    return stateType;
  }
  /** Key returned by FiniteStateMachine to identify this state */
  private Object key;
  /** set the key returned by FiniteStateMachine */
  public void setKey(Object newKey)
  {
    key = newKey;
  }
  /** get the key */
  public Object getKey()
  {
    return key;
  }
  /** Create a state of a given type, with a given key. You
   *  can change they key at any point until you create the
   *  FiniteStateMachine - in fact you can change it after that,
   *  but it won't do you any good.
   */
  public State(StateEnum typeOfState, Object theKey)
  {
    stateType = typeOfState;
    key = theKey;
  }
  /** convenience constructor to produce a standard state */
  public State(Object theKey)
  {
    this(StateEnum.STANDARD, theKey);
  }
  /** set of substates */
  private Set<State> subStates = new HashSet<State>();
  /** Add a substate. Return true on success, or false if
   *  the substate is already present.
   */
  public boolean addSubstate(State newState)
  {
    return subStates.add(newState);
  }
  /** return an iterator over the substates of this state */
  Iterator<State> getSubIter()
  {
    return subStates.iterator();
  }
  /** remove a substate. Return true on success, or false
   *  if no such substate is present.
   */
  public boolean removeSubstate(State oldState)
  {
    return subStates.remove(oldState);
  }
  /** Holds info about transitions on an event */
  static class TransitionInfo
  {
    /** target state - null if internal */
    private final State target;
    /** action associated with the transition */
    private final Action action;
    TransitionInfo(State forTarget, Action forAction)
    {
      target = forTarget;
      action = forAction;
    }
    /** return the target */
    public State getTarget()
    {
      return target;
    }
    /** return the action */
    public Action getAction()
    {
      return action;
    }
  }
  private Map<Event, TransitionInfo> transitionByEvent =
    new HashMap<Event, TransitionInfo>();
  /** Add a transition, firing when the given event is
   *  recognised. Overwrites any existing transition on the same
   *  event. If the target state is null, it is an internal
   *  transition. Otherwise it is an external transition.
   *  It is never valid to add an external transition on an EXIT
   *  or ENTRY
   *  event. Transitions on an INITIAL event are only valid
   *  for history states, where they denote
   *  the action to be taken when a history state is entered for
   *  the first time after a reset. Breaking the INITIAL
   *  rule will cause
   *  the FiniteStateMachine to throw an exception in its constructor.
   *  Breaking the ENTRY and EXIT rules will be detected here.
   */
  public void addTransition(Event e, Action a, State target)
  {
    if ((target != null) && ((e == Event.EXIT) || (e == Event.ENTRY)))
    {
      throw new IllegalArgumentException(
        "Entry or Exit action with non-null target");
    }
    if ((e == Event.INITIAL) && ((target == null) || (a != null)))
    {
      throw new IllegalArgumentException(
        "Initial event must have a target and no action");
    }
    transitionByEvent.put(e, new TransitionInfo(target, a));
  }
  /** Return an iterator of Map.Entry<Event, TransitionInfo> */
  Iterator<Map.Entry<Event, TransitionInfo>> getTransitionIterator()
  {
    return transitionByEvent.entrySet().iterator();
  }
  /** remove any transition on a given event.
   */
  public void removeTransition(Event e)
  {
    transitionByEvent.remove(e);
  }
  /** Return the target of a transition for a given event, or null
   *  if the event is not recognised, or the transition is internal
   */
  public State getTargetState(Event e)
  {
    TransitionInfo ti = transitionByEvent.get(e);
    if (ti == null)
    {
      return null;
    }
    return ti.getTarget();
  }
  /** return the Action of any transition on the given event, or null
   */
  public Action getAction(Event e)
  {
    TransitionInfo ti = transitionByEvent.get(e);
    if (ti == null)
    {
      return null;
    }
    return ti.getAction();
  }
  /** return whether the given event is explicitly recognised in this
   *  state
   */
  public boolean eventRecognised(Event e)
  {
    return transitionByEvent.containsKey(e);
  }
  /** Map from event listener to Boolean saying whether events
   *  fired by substates will be passed on.
   */
  private Map<EventListener, Boolean> includeByListener =
    new HashMap<EventListener, Boolean>();
  /** return an iterator to the map of listeners */
  Iterator<Map.Entry<EventListener, Boolean>> getListeners()
  {
    return includeByListener.entrySet().iterator();
  }
  /** Add a listener for events received by this state, and possibly
   *  by its substates. Overwrites any previous entry for that
   *  listener.
   */
  public void addEventListener(EventListener el,
    boolean includeSubstates)
  {
    includeByListener.put(el, includeSubstates);
  }
  /** Remove a listener for events. */
  public void removeEventListener(EventListener el)
  {
    includeByListener.remove(el);
  }
  /** Return whether the given event listener is registered */
  public boolean isEventListenerRegistered(EventListener el)
  {
    return includeByListener.containsKey(el);
  }
  /** Return true if the given event listener is registered and
   *  asked to receive substate info
   */
  public boolean receivesSubstates(EventListener el)
  {
    Boolean b = includeByListener.get(el);
    if (b == null)
    {
      return false;
    }
    return b.booleanValue();
  }
}
