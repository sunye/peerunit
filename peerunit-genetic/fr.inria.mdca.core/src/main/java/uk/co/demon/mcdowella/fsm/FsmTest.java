package uk.co.demon.mcdowella.fsm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Test class for FiniteStateMachine */
class FsmTest
{
  public static void main(String[] s)
  {
    State st = new State(State.StateEnum.STANDARD, null);
    FiniteStateMachine fsm = new FiniteStateMachine(st);
    Event e = new Event();
    System.out.println(fsm.acceptEvent(e));

    // This is based on Figure 21-6 of The Unified Modeling User Guide
    System.out.println("Backup example");
    State root = new State(State.StateEnum.STANDARD, "root");
    Action showState = new Action()
    {
      public void act(FiniteStateMachine m)
      {
        System.out.println("Current state is " + m.getCurrentKey());
      }
    };
    State command = new State(State.StateEnum.INITIAL, "command");
    command.addTransition(Event.ENTRY, showState, null);
    root.addSubstate(command);
    root.addTransition(Event.ENTRY, showState, null);
    State backingUp = new State(State.StateEnum.STANDARD, "backingUp");
    backingUp.addTransition(Event.ENTRY, showState, null);
    root.addSubstate(backingUp);
    State history = new State(State.StateEnum.SHALLOW_HISTORY,
      "history");
    history.addTransition(Event.ENTRY, showState, null);
    backingUp.addSubstate(history);
    State collecting = new State(State.StateEnum.STANDARD,
      "collecting");
    backingUp.addSubstate(collecting);
    State copying = new State(State.StateEnum.STANDARD, "copying");
    copying.addTransition(Event.ENTRY, showState, null);
    backingUp.addSubstate(copying);
    State cleaningUp = new State(State.StateEnum.STANDARD,
      "cleaningUp");
    cleaningUp.addTransition(Event.ENTRY, showState, null);
    backingUp.addSubstate(cleaningUp);
    Event query = new Event();
    Event backup = new Event("backup");
    Event tick = new Event("tick");
    command.addTransition(backup, null, history);
    history.addTransition(Event.INITIAL, null, collecting);
    collecting.addTransition(tick, null, copying);
    copying.addTransition(tick, null, cleaningUp);
    cleaningUp.addTransition(tick, null, command);
    backingUp.addTransition(query, null, command);
    root.addEventListener(new AnnounceListener(), true);
    FiniteStateMachine historyExample = new FiniteStateMachine(root);
    historyExample.reset();
    for (int ii = 0; ii < 3; ii++)
    {
      System.err.println(historyExample.acceptEvent(backup));
      for (int i = 0; i < 5; i ++)
      {
	System.err.println(historyExample.acceptEvent(tick));
      }
    }

    // This is based on  Figure 21-5 of the UML User guide
    System.out.println("ATM example");
    State seq = new State(State.StateEnum.STANDARD, "Seq");
    State idle = new State(State.StateEnum.INITIAL, "Idle");
    seq.addSubstate(idle);
    State maintenance = new State(State.StateEnum.STANDARD,
      "maintenance");
    seq.addSubstate(maintenance);
    Event maintain = new Event("maintain");
    idle.addTransition(maintain, new Announce("Maintenance"),
      maintenance);
    maintenance.addTransition(tick, null, idle);
    State active = new State(State.StateEnum.STANDARD,
      "active");
    seq.addSubstate(active);
    Event cardInserted = new Event("cardInserted");
    idle.addTransition(cardInserted, new Announce("card inserted"),
      active);
    Event cancel = new Event("cancel");
    active.addTransition(cancel, new Announce("session cancelled"),
      idle);
    State validating = new State(State.StateEnum.INITIAL,
      "validating");
    active.addSubstate(validating);
    State selecting = new State(State.StateEnum.STANDARD,
      "selecting");
    active.addSubstate(selecting);
    validating.addTransition(tick, null, selecting);
    State processing = new State("processing");
    active.addSubstate(processing);
    selecting.addTransition(tick, null, processing);
    Event continueEvent = new Event("continue");
    processing.addTransition(continueEvent, new Announce("continue"),
      selecting);
    State printing = new State("Printing");
    active.addSubstate(printing);
    Event notContinue = new Event("not continue");
    processing.addTransition(notContinue, new Announce("not continue"),
      printing);
    printing.addTransition(tick, null, idle);
    active.addTransition(Event.ENTRY, new Announce("read card"),
      null);
    active.addTransition(Event.EXIT, new Announce("eject card"),
      null);
    seq.addEventListener(new AnnounceListener(), true);
    FiniteStateMachine atmExample = new FiniteStateMachine(seq);
    atmExample.reset();
    atmExample.acceptEvent(maintain);
    atmExample.acceptEvent(tick);
    atmExample.acceptEvent(cardInserted);
    atmExample.acceptEvent(tick);
    atmExample.acceptEvent(tick);
    atmExample.acceptEvent(continueEvent);
    atmExample.acceptEvent(tick);
    atmExample.acceptEvent(notContinue);
    atmExample.acceptEvent(tick);
    atmExample.acceptEvent(cardInserted);
    atmExample.acceptEvent(tick);
    atmExample.acceptEvent(cancel);
    testInitial();


    System.err.println("Starting random test transitions");
    long seed = 42;
    int maxBase = 20;
    int maxSize = 20000;
    int goes = 20;
    for (int i = 0; i < goes; i++)
    {
      System.err.println("go " + i + " of " + goes);
      Random r = new Random(seed + i);
      int size = 1 + r.nextInt(maxSize - 1);
      int base = 2 + r.nextInt(maxBase - 2);
      testPass(size, base, goes, r);
      testShallow(size, base, goes, r);
    }
  }
  /** Test routine produces a tree of states numbered so that moving
   *  through the tree corresponds to division
   */
  static void testPass(int size, int base, int goes, Random r)
  {
    if (size < 1)
    {
      return;
    }
    State[] byNumbers = new State[size];
    State[] groups = new State[size];
    for (int i = 0; i < size; i++)
    {
      byNumbers[i] = new State(new Integer(i));
      if (!byNumbers[i].getKey().equals(new Integer(i)))
      {
        throw new IllegalStateException("Key botch");
      }
      groups[i] = byNumbers[i];
    }
    List<DivideEvent> del = new ArrayList<DivideEvent>();
    int divideBy = base;
    for (int numGroups = size; numGroups > 1; )
    {
      System.err.println("DivideBy " + divideBy + " base " + base);
      System.err.println("Groups " + numGroups);
      int wp = 0;
      DivideEvent de = new DivideEvent(divideBy);
      del.add(de);
      // At this point, every group consists of the element that
      // yield the same value when divided by base ^ (iterations so
      // far). We merge them, since each set of base groups yields
      // the same result when divided by base ^(iterations so far + 1)
      for (int i = 0; i < numGroups;)
      {
        int j = i + base;
	if (j > numGroups)
	{
	  j = numGroups;
	}
	State parent = new State("Group");
	for (int k = i; k < j; k++)
	{
	  parent.addSubstate(groups[k]);
	}
	int to = ((Integer)groups[i].getKey()).intValue() / divideBy;
	parent.setKey(groups[i].getKey());
	parent.addTransition(de, null, byNumbers[to]);
	groups[wp++] = parent;
	i = j;
      }
      divideBy *= base;
      numGroups = wp;
    }
    Event[] setEvent = new Event[size];
    State numbers = groups[0];
    State withHistory = new State("with history");
    // Will move away from numbered states and then use history
    // state to return, checking that we return to the right one
    State history = new State(State.StateEnum.DEEP_HISTORY,
      "history");
    withHistory.addSubstate(history);
    withHistory.addSubstate(numbers);
    State root = new State("root");
    State other = new State("other");
    root.addSubstate(other);
    root.addSubstate(withHistory);
    Event toOther = new Event("to other");
    withHistory.addTransition(toOther, null, other);
    Event fromOther = new Event("from other");
    other.addTransition(fromOther, null, history);
    // dummy initial transition - never used
    history.addTransition(Event.INITIAL, null, byNumbers[0]);
    for (int i = 0; i < size; i++)
    {
      setEvent[i] = new Event();
      root.addTransition(setEvent[i], null, byNumbers[i]);
    }
    FiniteStateMachine fsm = new FiniteStateMachine(root);
    fsm.reset();
    for (int pass = 0; pass < goes; pass++)
    {
      if (r.nextDouble() < 0.01)
      {
        fsm.reNumber();
      }
      if (r.nextDouble() > 0.5)
      {
        fsm.reset();
	if (fsm.getCurrentKey() != root.getKey())
	{
	  throw new IllegalStateException(
	    "Did not return to root state on reset");
	}
      }
      int target = r.nextInt(size);
      fsm.acceptEvent(setEvent[target]);
      if (fsm.getCurrentKey() != byNumbers[target].getKey())
      {
        throw new IllegalStateException("Did not get to target state");
      }
      fsm.acceptEvent(toOther);
      if (fsm.getCurrentKey() != other.getKey())
      {
        throw new IllegalStateException(
	  "Did not move out to other state");
      }
      fsm.acceptEvent(fromOther);
      if (fsm.getCurrentKey() != byNumbers[target].getKey())
      {
        throw new IllegalStateException(
	  "Did not return to target state");
      }
      DivideEvent divisor = del.get(r.nextInt(del.size()));
      target = ((Integer)fsm.getCurrentKey()).intValue() / 
        divisor.getDivideBy();
      fsm.acceptEvent(divisor);
      if (fsm.getCurrentKey() != byNumbers[target].getKey())
      {
        throw new IllegalStateException(
	  "Divide did not get to target state");
      }
    }
  }
  /** Test routine for shallow history, using 3-level tree */
  static void testShallow(int size, int base, int goes, Random r)
  {
    State root = new State("root");
    Event toOther = new Event("ToOther");
    State other = new State(State.StateEnum.INITIAL, "other");
    root.addSubstate(other);
    root.addTransition(toOther, null, other);
    State withHistory = new State("with history");
    root.addSubstate(withHistory);
    State[] filling = new State[(size + base - 1) / base];
    State[] byFilling = new State[filling.length];
    for (int i = 0; i < filling.length; i++)
    {
      filling[i] = new State("filling");
      withHistory.addSubstate(filling[i]);
      byFilling[i] = new State(State.StateEnum.INITIAL, 
        new Integer(-1 - i));
      filling[i].addSubstate(byFilling[i]);
    }
    State[] floor = new State[size];
    Event[] toFloor = new Event[floor.length];
    for (int i = 0; i < floor.length; i++)
    {
      floor[i] = new State(new Integer(i));
      toFloor[i] = new Event();
      root.addTransition(toFloor[i], null, floor[i]);
      filling[i / base].addSubstate(floor[i]);
    }
    State history = new State(State.StateEnum.SHALLOW_HISTORY,
      "history");
    withHistory.addSubstate(history);
    history.addTransition(Event.INITIAL, null, root);
    Event toHistory = new Event("to history");
    root.addTransition(toHistory, null, history);
    FiniteStateMachine fsm = new FiniteStateMachine(root);
    fsm.reset();
    for (int i = 0; i < goes; i++)
    {
      if (r.nextDouble() < 0.01)
      {
        fsm.reNumber();
      }
      int target = r.nextInt(floor.length);
      fsm.acceptEvent(toFloor[target]);
      if (fsm.getCurrentKey() != floor[target].getKey())
      {
        throw new IllegalStateException("Could not get to floor");
      }
      fsm.acceptEvent(toOther);
      if (fsm.getCurrentKey() != other.getKey())
      {
        throw new IllegalStateException("Could not get to other");
      }
      fsm.acceptEvent(toHistory);
      if (fsm.getCurrentKey() != byFilling[target / base].getKey())
      {
        throw new IllegalStateException("Could not get to filling");
      }
    }
  }
  /** This method is a quick test of the INITIAL state mechanism */
  static void testInitial()
  {
    State root = new State("root");
    State in1 = new State(State.StateEnum.INITIAL, "in1");
    root.addSubstate(in1);
    State in2 = new State(State.StateEnum.INITIAL, "in2");
    in1.addSubstate(in2);
    State in3 = new State(State.StateEnum.INITIAL, "in3");
    in2.addSubstate(in3);
    State inA = new State("inA");
    root.addSubstate(inA);
    Event tick = new Event("tick");
    in3.addTransition(tick, null, inA);
    State inB = new State(State.StateEnum.INITIAL, "inB");
    inA.addSubstate(inB);
    State inC = new State(State.StateEnum.INITIAL, "inC");
    inB.addSubstate(inC);
    State inX = new State("inX");
    root.addSubstate(inX);
    State inXX = new State(State.StateEnum.INITIAL, "inXX");
    inX.addSubstate(inXX);
    inC.addTransition(tick, null, inX);
    State inY = new State("inY");
    inX.addSubstate(inY);
    State inZ = new State(State.StateEnum.INITIAL, "inZ");
    inY.addSubstate(inZ);
    root.addEventListener(new AnnounceListener(), true);
    FiniteStateMachine fsm = new FiniteStateMachine(root);
    fsm.reset();
    if (fsm.getCurrentKey() != in3.getKey())
    {
      throw new IllegalStateException("Mismatch at reset");
    }
    fsm.acceptEvent(tick);
    if (fsm.getCurrentKey() != inC.getKey())
    {
      throw new IllegalStateException("Mismatch at first tick");
    }
    fsm.acceptEvent(tick);
    if (fsm.getCurrentKey() != inXX.getKey())
    {
      throw new IllegalStateException("Mismatch at second tick");
    }
  }
}
class AnnounceListener implements EventListener
{
  public void eventReceived(FiniteStateMachine fsm)
  {
    System.err.println("Current state key " +
      fsm.getCurrentKey() + " event " + fsm.getCurrentEvent());
  }
}
class Announce extends Action
{
  private final String name;
  Announce(String s)
  {
    name = s;
  }
  public void act(FiniteStateMachine m)
  {
    System.out.println("Action " + name);
  }
}
class DivideEvent extends Event
{
  final int divideBy;
  DivideEvent(int by)
  {
    divideBy = by;
  }
  int getDivideBy()
  {
    return divideBy;
  }
}
