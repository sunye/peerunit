package uk.co.demon.mcdowella.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/** Minimum spanning tree algorithm for a dense graph */
public class MinSpan
{
  /** interface to pass in the distance function to compute
   *  the tree
   */
  public interface Distance
  {
    double distance(int a, int b);
  }
  /** no need to create an object, so constructor is private */
  private MinSpan()
  {
  }
  /** Compute the minimum spanning tree with the specified distance
   *  object for a dense graph of n nodes. Put the links into the
   *  arrays, filling in the left and right nodes at the end of
   *  each link in linkLeft and linkRight. Put the distance of the
   *  link in linkLeft. Storage consumption goes up with n. Time
   *  goes up with n*n.
   */
  public static void computeMinSpan(int n, Distance d, int[] linkLeft,
    int[] linkRight, double[] linkLength)
  {
    if (n < 0)
    {
      throw new IllegalArgumentException(" -ve number of nodes");
    }
    if (n == 0)
    {
      return;
    }
    // We will use Prim's algorithm. At each point we have a set of
    // connected nodes, starting off with an arbitrary node. Repeatedly
    // add to that set the node not in that set closest to any of
    // the nodes. This is a minimum spanning tree because if we divide
    // any such tree into two sets one of the links in the tree is
    // a shortest link between the two sets. If not, we could shorten
    // it by adding the shortest link to the tree to form a cycle and
    // then breaking the cycle. See e.g. Algorithms by Sedgewick.

    // Nodes not yet considered
    int[] toDo = new int[n - 1];
    // min length so far to connect
    double[] minLength = new double[n - 1];
    // preferred node to connect to. Starts off connecting
    // to our node 0 which is all we calculate distance to
    int[] connectTo = new int[n - 1];
    double minSeen = Double.MAX_VALUE;
    // position in toDo array of index of closest node
    int minAt = 0;
    for (int i = 0; i < toDo.length; i++)
    {
      int nodeNum = i + 1;
      toDo[i] = nodeNum;
      double dist = d.distance(0, nodeNum);
      // System.out.println("Distance " + dist + " to " + nodeNum);
      minLength[i] = dist;
      if (dist <= minSeen)
      {
        minSeen = dist;
	minAt = i;
      }
    }

    int wp = 0;
    // Now add each other node
    for (int i = 1; i < n; i++)
    {
      System.out.println(i + " of " + n);
      int newNode = toDo[minAt];
      linkLeft[wp] = newNode;
      linkRight[wp] = connectTo[minAt];
      linkLength[wp] = minSeen;
      // can make sure we haven't miscopied anything while shuffling
      // stuff about, but it perturbs the calculation of mean distances
      // in ParaClust, which believe that every distance is evaluated
      // exactly once.
      final boolean DOUBLE_CHECK = false;
      if (DOUBLE_CHECK)
      {
        if (linkLength[wp] != d.distance(linkLeft[wp], linkRight[wp]))
	{
	  throw new IllegalArgumentException("Distance mismatch");
	}
      }
      wp++;
      // number of nodes not yet connected
      // At first iteration have n - 1 nodes to connect to 0
      // have i = 1 and have just done 1 node so n - 2 left
      int numLeft = n - i - 1;
      // copy down to shrink array
      toDo[minAt] = toDo[numLeft];
      minLength[minAt] = minLength[numLeft];
      connectTo[minAt] = connectTo[numLeft];
      // Now see if the newly connected node
      // reduces the distance to the other nodes
      minSeen = minLength[0];
      minAt = 0;
      for (int j = 0; j < numLeft; j++)
      {
	int here = toDo[j];
	double newDist = d.distance(newNode, here);
	double oldDist = minLength[j];
	if (newDist > oldDist)
	{
	  newDist = oldDist;
	}
	else
	{
	  minLength[j] = newDist;
	  connectTo[j] = newNode;
	}
	if (newDist < minSeen)
	{
	  minAt = j;
	  minSeen = newDist;
	}
      }
    }
  }
  /** class for sorting by edge length */
  private static class EdgeLength implements Comparable<EdgeLength>
  {
    private final int num;
    private final double len;
    EdgeLength(int n, double d)
    {
      num = n;
      len = d;
    }
    public int compareTo(EdgeLength el)
    {
      if (len < el.len)
      {
        return -1;
      }
      if (len > el.len)
      {
        return 1;
      }
      if (num < el.num)
      {
        return -1;
      }
      if (num > el.num)
      {
        return 1;
      }
      return 0;
    }
  }
  /** Accept the output from computeMinSpan and turn it into a
   *  clustering by repeatedly joining the two nodes linked by
   *  the smallest link in the minimum spanning tree not seen yet.
   *  If there is a unique minimum spanning tree it will duplicate
   *  what a bottom up clustering will do, because that is also
   *  another way of producing a minimum spanning tree. If not it
   *  is less obvious what will happen, but it is also the answer
   *  you would get if you took the minimum spanning tree you
   *  started with and perturbed the weights just enough to make
   *  it unique. We expect numNodes * 2 - 2 links in
   *  the final answer, with numNodes * 2 - 1 nodes, and the new
   *  nodes numbered in sequence after the old.
   */
  public static void makeClustering(int numNodes, int[] linkLeftIn,
    int[] linkRightIn, double[] linkLengthIn, int[] linkLeftOut,
    int[] linkRightOut, double[] linkLengthOut)
  {
    if (numNodes <= 1)
    {
      return;
    }
    // sort by length
    EdgeLength[] el = new EdgeLength[numNodes - 1];
    for (int i = 0; i < el.length; i++)
    {
      el[i] = new EdgeLength(i, linkLengthIn[i]);
    }
    Arrays.sort(el);
    // Use to merge clusters as we produce them
    DisjointSet ds = new DisjointSet(numNodes);
    // maps DisjointSet id of cluster to node we created for it
    int[] mappedNode = new int[numNodes];
    for (int i = 0; i < numNodes; i++)
    {
      mappedNode[i] = i;
    }
    int wp = 0;
    int newNodeNumber = numNodes;
    for (int i = 0; i < el.length; i++)
    {
      int link = el[i].num;
      int leftNode = linkLeftIn[link];
      int mappedLeft = mappedNode[ds.getSetNumber(leftNode)];
      int rightNode = linkRightIn[link];
      int mappedRight = mappedNode[ds.getSetNumber(rightNode)];
      double distance = linkLengthIn[link];
      // Create links to new node
      linkLeftOut[wp] = mappedLeft;
      linkRightOut[wp] = newNodeNumber;
      linkLengthOut[wp] = distance;
      wp++;
      linkLeftOut[wp] = mappedRight;
      linkRightOut[wp] = newNodeNumber;
      linkLengthOut[wp] = distance;
      wp++;
      // merge clusters
      ds.merge(leftNode, rightNode);
      // and map to new node number
      mappedNode[ds.getSetNumber(leftNode)] = newNodeNumber++;
    }
  }
  /** Utility class: distance function using array */
  public static class ArrayDistance implements Distance
  {
    /** distance array */
    private final double dist[];
    /** dimension of array */
    private final int dim;
    /** Construct from array, which it does not bother to copy.
     *  Only a[i, j] are accessed, where i < j
     */
    public ArrayDistance(int n, double[] distances)
    {
      dim = n;
      dist = distances;
    }
    /** our implementation of distance */
    public double distance(int a, int b)
    {
      if (a == b)
      {
        return 0.0;
      }
      if (a > b)
      {
        int t = a;
	a = b;
	b = t;
      }
      return dist[a * dim + b];
    }
  }
  /** utility routine to work out distances in a tree from some point */
  private static void propagateDist(int numNodes,
    double[] len, ArrayList<ArrayList<Integer>> nodeChildren,
    ArrayList<ArrayList<Double>> childDistances, double[] distance,
    int startNode, int[] reachedFrom)
  {
    boolean[] known = new boolean[numNodes];
    known[startNode] = true;
    distance[startNode] = 0.0;
    reachedFrom[startNode] = -1;
    int numLinks = numNodes - 1;
    // List of pending nodes for which we know the distance but have not
    // propagated the consequences of that
    ArrayList<Integer> pending = new ArrayList<Integer>();
    pending.add(startNode);
    for (;;)
    {
      int atPending = pending.size() - 1;
      if (atPending < 0)
      {
	break;
      }
      int node = pending.get(atPending);
      // System.out.println("At node " + node);
      pending.remove(atPending);
      double toHere = distance[node];
      ArrayList<Integer> fromHere = nodeChildren.get(node);
      // System.out.println("From Here " + fromHere);
      ArrayList<Double> distHere = childDistances.get(node);
      int numFrom = fromHere.size();
      for (int j = 0; j < numFrom; j++)
      {
	int target = fromHere.get(j);
	if (known[target])
	{
	  continue;
	}
	reachedFrom[target] = node;
	known[target] = true;
	distance[target] = toHere + distHere.get(j);
	pending.add(target);
      }
    }
    for (boolean b: known)
    {
      if (!b)
      {
        throw new IllegalArgumentException("Node not reached");
      }
    }
  }
  /** Take a tree and work out a longest path through the tree,
      returning its length. This takes time linear in the number of
      edges in the tree. @return the number of edges in this path.
      @param numEdges the number of nodes in the tree: there is one less
        edge than this
      @param left the left node for each edge
      @param right the right node for each edge
      @param len the length of each edge
      @param path will be set to hold the longest path, so will use one
        more node than is returned as the result
      */
  public static int diameter(int numNodes, int[] left, int[] right,
    double[] len, int[] path)
  {
    // It turns out that we can do this in two passes, relying on the
    // fact that we are working on a tree. The first pass starts
    // from an arbitrary node and finds a farthest point from that node.
    // The second starts from the node found and finds a farthest point
    // from that. Suppose that this fails. Then there is a longer path
    // than the one we have found, and when we draw that path out together
    // with the one we have found, and the link between the two we get a
    // sort of H shape, possibly with the link between the two paths of
    // zero length, or the two paths merged into one at the join. If the
    // longer path is of length l, at least one of its two legs in the H
    // is of length l/2. If two legs of the H join, then we ended up at one
    // of its ends after the first pass, and we would in fact have got the
    // right answer. If the two legs don't join, then the second leg of the
    // path we ended up with is also at least l/2. But this means that we
    // can merge it with the longer leg of the proper path to get another
    // path of length l or more, which we would have found instead of our
    // supposed failure. So, by contradiction, we have proved that this method
    // works: to find a longest path through a tree, hold it up by any node,
    // let the farthest node from here dangle down, hold it up by this node,
    // and then pick the farthest node from that.

    if (numNodes <= 1)
    {
      return 0;
    }
    // distance to each node, if known
    double[] distance = new double[numNodes];
    // whether known distance to this node
    boolean[] known = new boolean[numNodes];
    // For each node, a list of other nodes linked to it
    ArrayList<ArrayList<Integer>> toList = new ArrayList<ArrayList<Integer>>();
    // For each node, the distance along those links
    ArrayList<ArrayList<Double>> lenTo = new ArrayList<ArrayList<Double>>();
    for (int i = 0; i < numNodes; i++)
    {
      toList.add(new ArrayList<Integer>());
      lenTo.add(new ArrayList<Double>());
    }
    int numLinks = numNodes - 1;
    for (int i = 0; i < numLinks; i++)
    {
      int from = left[i];
      int to = right[i];
      double d = len[i];
      // add in this link
      toList.get(from).add(to);
      lenTo.get(from).add(d);
      toList.get(to).add(from);
      lenTo.get(to).add(d);
    }
    // Starting point node
    int startPoint = left[0];
    int[] reachedFrom = new int[numNodes];
    propagateDist(numNodes, len, toList, lenTo,
      distance, startPoint, reachedFrom);
    double maxDist = distance[0];
    int maxStart = 0;
    for (int i = 1; i < numNodes; i++)
    {
      if (distance[i] > maxDist)
      {
        maxDist = distance[i];
	maxStart = i;
      }
    }
    propagateDist(numNodes, len, toList, lenTo,
      distance, maxStart, reachedFrom);
    maxDist = distance[0];
    maxStart = 0;
    for (int i = 1; i < numNodes; i++)
    {
      if (distance[i] > maxDist)
      {
        maxDist = distance[i];
	maxStart = i;
      }
    }
    // Write out longest path, starting from the other end
    int wp = 0;
    for (int x = maxStart;;)
    {
      path[wp++] = x;
      int back = reachedFrom[x];
      if (back < 0)
      {
        break;
      }
      x = back;
    }
    return wp - 1;
  }
  /** test main */
  public static void main(String[] s)
  {
    long seed = 473;
    int goes = 10;
    int maxDim = 8;
    for (int go = 0; go < goes; go++)
    {
      Random r = new Random(seed + go);
      // spill a random number because otherwise nextInt() can
      // be the same for similar seeds
      r.nextLong();
      int dim = r.nextInt(maxDim) + 1;
      double[] dist = new double[dim * dim];
      for (int i = 0; i < dim; i++)
      {
        for (int j = 0; j < i; j++)
	{
	  double d = r.nextInt(1000) / 1000.0;
	  dist[i * dim + j] = d;
	  dist[j * dim + i] = d;
	}
      }
      for (int i = 0; i < dim; i++)
      {
        String sep = "";
	for (int j = 0; j < dim; j++)
	{
	  System.out.print(sep);
	  sep = " ";
	  System.out.print(dist[i * dim + j]);
	}
	System.out.println();
      }
      Distance disat = new ArrayDistance(dim, dist);
      int[] left = new int[dim - 1];
      int[] right = new int[dim - 1];
      double[] len = new double[dim - 1];
      computeMinSpan(dim, disat, left, right, len);
      System.out.println("left: " + Arrays.toString(left));
      System.out.println("right: " + Arrays.toString(right));
      System.out.println("len: " + Arrays.toString(len));
      int clustLeft[] = new int[dim * 2 - 2];
      int clustRight[] = new int[dim * 2 - 2];
      double clustLen[] = new double[dim * 2 - 2];
      makeClustering(dim, left, right, len, clustLeft, clustRight,
        clustLen);
      System.out.println("Clustleft: " + Arrays.toString(clustLeft));
      System.out.println("Clustright: " + Arrays.toString(clustRight));
      System.out.println("Clustlen: " + Arrays.toString(clustLen));
      int[] path = new int[dim];
      int dLen = diameter(dim, left, right, len, path);
      System.out.print("Diameter len " + dLen + " path");
      for (int i = 0; i <= dLen; i++)
      {
        System.out.print(" " + path[i]);
      }
      System.out.println();
    }
  }
}
