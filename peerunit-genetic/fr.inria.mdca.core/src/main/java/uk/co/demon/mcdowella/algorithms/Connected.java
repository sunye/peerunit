package uk.co.demon.mcdowella.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.util.HashMap;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/** This class contains a static method to compute the connected
 *  components of a graph, and a static main to test it
 */
public class Connected
{
  /** The input array represents dependencies. The arrays it contains
   *  each represent a single item, numbered starting from zero.
   *  The numbers in its arrays are the indexes of the items which
   *  it requires. The array returned contains a list of the indexes
   *  of connected components (groups items that all require on each
   *  other) in an order such that the first group does not require
   *  any other group, and each group requires only items in preceding
   *  groups, and not following groups.
   */
  public static int[][] connected(int[][] requires)
  {
    // First produce another view of the input data: a set of arrays
    // holding the inverse, 'supports' operation.
    int[][] supports = new int[requires.length][];
    int[] inUse = new int[requires.length];
    int[] zeroLen = new int[0];
    for (int i = 0; i < requires.length; i++)
    {
      int[] req = requires[i];
      if (req == null)
      {
        req = zeroLen;
      }
      for (int j = 0; j < req.length; j++)
      {
	inUse[req[j]]++;
      }
    }
    for (int i = 0; i < requires.length; i++)
    {
      supports[i] = new int[inUse[i]];
      inUse[i] = 0;
    }
    for (int i = 0; i < requires.length; i++)
    {
      int[] req = requires[i];
      if (req == null)
      {
        req = zeroLen;
      }
      for (int j = 0; j < req.length; j++)
      {
	int r = req[j];
	supports[r][inUse[r]++] = i;
      }
    }
    /*
    System.err.println("Supports:");
    for (int i = 0; i < supports.length; i++)
    {
      System.err.print(i);
      int[] here = supports[i];
      if (here == null)
      {
        here = new int[0];
      }
      for (int j = 0; j < here.length; j++)
      {
        System.err.print(" " + here[j]);
      }
      System.err.println();
    }
    */
    // If we traverse the 'supports' graph with depth first search
    // and number it in post-order, then if A supports B but B does
    // not directly or indirectly support A then A will be
    // given a larger number than B. This is true if we come across A
    // first and follow links to B. It is also true if we come across
    // B first as we will number it before moving on to A. Put such
    // a numbering in the inUse array.
    boolean[] seen = new boolean[supports.length];
    final int[][] finUse = new int[][] {inUse};
    final int[] nextNumber = new int[1];
    Visitor v = new Visitor()
    {
      public void visit(int num)
      {
	// System.err.println("Visit " + num + " becomes number " +
	//   nextNumber[0]);
	finUse[0][num] = nextNumber[0]++;
      }
    };
    int[] stack = new int[requires.length * 2];
    for (int i = 0; i < supports.length; i++)
    {
      search(i, supports, seen, v, stack);
    }
    // Now we want to create an index that allows us to visit the
    // nodes in order, ending with the largest numbered node, which
    // cannot possibly be supported by anything it does not itself
    // support.
    int[] index = new int[inUse.length];
    for (int i = 0; i < inUse.length; i++)
    {
      // System.err.println("Address " + inUse[i] + " of " + index.length);
      index[inUse[i]] = i;
    }
    inUse = null;
    finUse[0] = null;
    supports = null;
    // Go through the index array starting at the end and use the
    // requires array to pull out connected components. Each connected
    // component is everything reachable from the node we are working
    // on - they must all require it, because it is the highest numbered
    // component left in.
    Arrays.fill(seen, 0, seen.length, false);
    final int[] storeUsed = new int[1];
    final int[] available = new int[requires.length];
    Visitor fv = new Visitor()
    {
      public void visit(int num)
      {
	// System.err.print(num + " ");
	available[storeUsed[0]++] = num;
      }
    };
    List storeList = new ArrayList();
    for (int i = index.length - 1; i >= 0; i--)
    {
      search(index[i], requires, seen, fv, stack);
      if (storeUsed[0] > 0)
      {
	int[] newInfo = new int[storeUsed[0]];
	System.arraycopy(available, 0, newInfo, 0, newInfo.length);
	storeList.add(newInfo);
	storeUsed[0] = 0;
	// System.err.println();
      }
    }
    int[][] again = new int[storeList.size()][];
    return (int[][])storeList.toArray(again);
  }
  /**
   * Interface of object passed to depth first search routine to
   * receive calls in postorder
   */
  public interface Visitor
  {
    void visit(int num);
  }
  /** Visit nodes starting from a given point using depth first search,
   *  calling visit in postorder. Requires stack. Worst case consumption
   *  is twice number of nodes.
   */
  public static void search(int startHere, int[][] moveTo, 
    boolean[] seen, Visitor visit, int[] stack)
  {
    // System.err.println("Search from " + startHere);
    if (seen[startHere])
    { // already visited this node
      return;
    }
    seen[startHere] = true;
    int depth = 0;
    int offset = 0;
    for (;;)
    {
      // Here with node to work on, and nodes not completed
      // stored in the stack.
      int[] sup = moveTo[startHere];
      if (sup == null)
      {
        sup = new int[0];
      }
      for (;offset < sup.length; offset++)
      {
	int nextNode = sup[offset];
        if (!seen[nextNode])
	{ // new node to visit
	  seen[nextNode] = true;
	  stack[depth++] = startHere;
	  stack[depth++] = offset + 1;
	  startHere = nextNode;
	  offset = 0;
	  break;
	}
      }
      if (offset < sup.length)
      { // go work on new node
        continue;
      }
      // Here to retrace steps
      visit.visit(startHere);
      if (depth <= 0)
      { // all done
        return;
      }
      offset = stack[--depth];
      startHere = stack[--depth];
    }
  }
  /** Never want to construct these */
  private Connected()
  {
  }
  /** Main program reads one node per line, as a name followed by
   *  a list of the nodes it depends on
   */
  public static void main(String[] s) throws IOException
  {
    Map hm = new HashMap();
    BufferedReader br = null;
    try
    {
      br = new BufferedReader(new InputStreamReader(System.in));
      for (;;)
      {
	String line = br.readLine();
	if (line == null)
	{
	  break;
	}
	StringTokenizer st = new StringTokenizer(line);
	if (!st.hasMoreElements())
	{
	  continue;
	}
	String name = st.nextToken();
	List needs = new ArrayList();
	while (st.hasMoreElements())
	{
	  needs.add(st.nextElement());
	}
	if (hm.put(name, needs) != null)
	{
	  System.err.println("Name " + name + " starts two lines");
	  return;
	}
      }
    }
    finally
    {
      if (br != null)
      {
        br.close();
      }
    }
    Map integerByName = new HashMap();
    int next = 0;
    for (Iterator i = hm.entrySet().iterator(); i.hasNext();)
    {
      Map.Entry me = (Map.Entry)i.next();
      String key = (String)me.getKey();
      if (!integerByName.containsKey(key))
      {
	integerByName.put(key, new Integer(next++));
      }
      for (Iterator j = ((List)me.getValue()).iterator(); j.hasNext();)
      {
	key = (String)j.next();
	if (!integerByName.containsKey(key))
	{
	  integerByName.put(key, new Integer(next++));
	}
      }
    }
    // dependencies
    int[][] asInts = new int[next][];
    String[] nameByInt = new String[next];
    for (Iterator i = integerByName.entrySet().iterator(); i.hasNext();)
    {
      Map.Entry me = (Map.Entry)i.next();
      nameByInt[((Integer)me.getValue()).intValue()] =
        (String)me.getKey();
    }
    for (Iterator i = hm.entrySet().iterator(); i.hasNext();)
    {
      Map.Entry me = (Map.Entry)i.next();
      String key = (String)me.getKey();
      int target = ((Integer)integerByName.get(key)).intValue();
      // System.out.println("Key " + key + " target " + target);
      List requireList = (List)me.getValue();
      int[] requireArray = new int[requireList.size()];
      for (int j = 0; j < requireArray.length; j++)
      {
	requireArray[j] = ((Integer)integerByName.get(
	  requireList.get(j))).intValue();
      }
      asInts[target] = requireArray;
    }
    /*
    for (int i = 0; i < asInts.length; i++)
    {
      int[] here = asInts[i];
      if (here == null)
      {
	System.out.println("(No requirement)");
        continue;
      }
      for (int j = 0; j < here.length; j++)
      {
        System.out.print(here[j]);
	System.out.print(' ');
      }
      System.out.println();
    }
    */
    int[][] components = connected(asInts);
    /*
    for (int i = 0; i < components.length; i++)
    {
      int[] here = components[i];
      String sep = "";
      for (int j = 0; j < here.length; j++)
      {
	System.out.print(sep);
	System.out.print(nameByInt[here[j]]);
	sep = " ";
      }
      System.out.println();
    }
    */
  }
}
