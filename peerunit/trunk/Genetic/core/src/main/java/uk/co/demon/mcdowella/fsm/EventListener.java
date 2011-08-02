package uk.co.demon.mcdowella.fsm;

/** This interface is used to listen for activity, either due to action
 *  in a state, or action in one of its substates
 */
public interface EventListener
{
  /** An event has been received. More info can be obtained from
   *  the FiniteStateMachine responsible. This includes EXIT and
   *  ENTER events, except that only the very lowest EXIT and
   *  ENTER events are provide here (listeners registered at higher 
   *  nodes
   *  will still pick them up, if they have registered to receive
   *  events fired by substates). Listeners are fired after the actions
   *  have been performed, except for EXIT listeners, which are fired
   *  before the action on EXIT events. INITIAL events are never
   *  provided to Event Listeners - they are never really fired, as
   *  they are not allowed to have actions, and are only used to
   *  mark the initial state for use when transition is to a history
   *  node when no substate associated with that history has been
   *  occupied since the last reset.
   */
  void eventReceived(FiniteStateMachine machine);
}
