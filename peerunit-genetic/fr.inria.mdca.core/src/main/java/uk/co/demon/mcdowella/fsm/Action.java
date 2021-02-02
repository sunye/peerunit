package uk.co.demon.mcdowella.fsm;

import java.util.ArrayList;
import java.util.List;

/** This class represents an Action. It is intended to be
 *  subclassed by the user, so they can implement whatever is
 *  supposed to happen for the action. Could include deferred events
 *  as a special kind of action, but that is just too painful. I can't
 *  see how to do it efficiently in the general case, and the user
 *  has no easy way of defending the system from having to accumulate
 *  huge numbers of deferred events.
 */
public abstract class Action
{
  /** This is called to request that the user perform their action.
   *  Information such as the current state, previous state, if any,
   *  and triggering event can be retrieved from the
   *  FiniteStateMachine
   */
  public abstract void act(FiniteStateMachine machine);
}
