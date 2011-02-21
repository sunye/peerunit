package uk.co.demon.mcdowella.fsm;
import uk.co.demon.mcdowella.algorithms.TreeDefault;

/** This class represents an Event. It is used to build up a
 *  representation of a finite state machine, and also to feed to
 *  the machine built from this representation. The ENTRY and EXIT
 *  events, which are produced by the finite state machine when
 *  entering and exiting states, are provided here so that the user
 *  can use them when setting up internal and external transitions.
 */
public class Event extends TreeDefault.DefaultAttribute
{
  /** Create Event with given name, or null */
  public Event(String forName)
  {
    name = forName;
  }
  /** Create Event with null name */
  public Event()
  {
    this(null);
  }
  /** Hold name to describe state, or null */
  private final String name;
  public String toString()
  {
    if (name != null)
    {
      return name;
    }
    return super.toString();
  }

  /** The Entry event is already provided */
  public static final Event ENTRY = new Event("ENTRY");
  /** The Exit event is already provided */
  public static final Event EXIT = new Event("EXIT");
  /** The Initial event is already provided.
   *  It is used
   *  in history states to determine the state to enter when no
   *  substate of the parent has been entered since the last reset.
   *  It should not be used with an Action, and it is not provided to
   *  EventListeners.
   */
  public static final Event INITIAL = new Event("INITIAL");
}
