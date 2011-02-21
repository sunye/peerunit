package uk.co.demon.mcdowella.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.util.Collection;
import java.util.Collections;
import java.util.zip.Deflater;
import java.util.HashMap;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/** This class contains a program to read in a sequence of
 *  paragraphs, produce a minimum spanning tree based on cosine
 *  similarity, and write it out as a cluster. The point of this
 *  is to group similar paragraphs together, and to mimic the
 *  biological group of species. The best order might be based on
 *  that solving the travelling salesman problem, but the distance
 *  along the minimum spanning tree can't be too far off that, because
 *  if the travelling salesman answer was too good, it would also be
 *  the minimum spanning tree.
 */
public class ParaClust
{
  /** Class holding a paragraph */
  private static class Para
  {
    /** lines */
    private final List<String> lines;
    /** Create from a list of lines, which is just copied across */
    public Para(List<String> lineList)
    {
      lines = lineList;
    }
    /** List of word indexes used in sorted order */
    private int[] wordIndexes;
    /** List of word counts parallel to indexes */
    private int[] wordCounts;
    /** euclidean length of vector formed by wordCounts */
    private double len;
    /** compute distance to another paragraph based on the
     *  similarity between them
     */
    public double distance(Para other)
    {
      // compute dot product for cosine similarity
      double sofar = 0.0;
      // pointer into sorted list of words here
      int ourPos = 0;
      // pointer into other sorted list of words
      int theirPos = 0;
      for (;;)
      {
        if (ourPos >= wordIndexes.length)
	{
	  break;
	}
	if (theirPos >= other.wordIndexes.length)
	{
	  break;
	}
	int ours = wordIndexes[ourPos];
	int theirs = other.wordIndexes[theirPos];
	if (ours == theirs)
	{ // word in both paras, so multiply counts
	  sofar += wordCounts[ourPos] * other.wordCounts[theirPos];
	  ourPos++;
	  theirPos++;
	  continue;
	}
	// here for no match
	if (ours < theirs)
	{ // advance ourPos to try and find a match
	  ourPos++;
	  continue;
	}
	// advance theirPos to try for match
	theirPos++;
      }
      double cosine = sofar / (len * other.len);
      // cosine is >= 0 as all counts are >= 0. Just return
      // 1.0 - cosine for distance, which actually does mean that
      // we have 0.0 exactly when the two arrays are the same
      // Not sure about other properties, but minimum spanning tree
      // gets the same answer after any order-preserving transformation
      // of its inputs so we probably don't care anyway.
      double dist = 1.0 - cosine;
      return dist;
    }
    /** Used to sort word-count pairs into order of word */
    private static class WordCount implements Comparable<WordCount>
    {
      private int word;
      private int count;
      public WordCount(int w, int c)
      {
        word = w;
	count = c;
      }
      public int compareTo(WordCount wc)
      {
        if (word < wc.word)
	{
	  return -1;
	}
	if (word > wc.word)
	{
	  return 1;
	}
	return 0;
      }
    }
    /** work out counts and indexes for a chunk of paras */
    public static void doCounts(Collection<Para> pl)
    {
      // dictionary
      Map<String, Integer> dict = new HashMap<String, Integer>();
      for (Para p: pl)
      {
        Map<Integer, Integer> countByWord =
	  new HashMap<Integer, Integer>();
	for (String line: p.lines)
	{
	  StringTokenizer st = new StringTokenizer(line);
	  while (st.hasMoreTokens())
	  {
	    String t = st.nextToken();
	    Integer codeInt = dict.get(t);
	    if (codeInt == null)
	    {
	      codeInt = new Integer(dict.size());
	      dict.put(t, codeInt);
	    }
	    int sofar = 1;
	    Integer count = countByWord.get(codeInt);
	    if (count != null)
	    {
	      sofar += count;
	    }
	    countByWord.put(codeInt, sofar);
	  }
	}
	WordCount[] wc = new WordCount[countByWord.size()];
	int wp = 0;
	for (Map.Entry<Integer, Integer> me: countByWord.entrySet())
	{
	  wc[wp++] = new WordCount(me.getKey(), me.getValue());
	}
	Arrays.sort(wc);
	p.wordIndexes = new int[wc.length];
	p.wordCounts = new int[wc.length];
	double sumSq = 0.0;
	for (int i = 0; i < wc.length; i++)
	{
	  WordCount wcc = wc[i];
	  p.wordIndexes[i] = wcc.word;
	  p.wordCounts[i] = wcc.count;
	  sumSq += wcc.count * wcc.count;
	}
	p.len = Math.sqrt(sumSq);
      }
    }
  }
  /** interface for distance stuff */
  public interface DistanceGuts
  {
    /** Initialise given list */
    public void init(List<Para> pl);
    /** compute distance between two Paras previously in list,
     *  given reference to them and their offsets
     */
    public double distance(Para a, Para b, int numA, int numB);
  }
  /** distance from cosine similarity */
  public static class CosineDistance implements DistanceGuts
  {
    public void init(List<Para> pl)
    {
      Para.doCounts(pl);
    }
    /** return distance */
    public double distance(Para a, Para b, int na, int nb)
    {
      return a.distance(b);
    }
  }
  /** compression distance */
  public static class CompressionDistance implements DistanceGuts
  {
    /** square root of length of compressed text */
    private double rlen[];
    /** work out length of two paras */
    private int compressLength(Para a, Para b)
    {
      Deflater d = new Deflater(Deflater.BEST_COMPRESSION, true);
      byte[] out = new byte[1024];
      int len = 0;
      for (Para pp: new Para[] {a, b})
      {
	for (String l: pp.lines)
	{
	  // System.out.println("line " + l);
	  for (;;)
	  {
	    int here = d.deflate(out);
	    len += here;
	    if (d.needsInput())
	    {
	      break;
	    }
	  }
	  d.setInput(l.getBytes());
	}
      }
      d.finish();
      for (;;)
      {
	int here = d.deflate(out);
	if (here <= 0)
	{
	  break;
	}
	len += here;
      }
      // System.out.println("Len " + len);
      return len;
    }
    public void init(List<Para> pl)
    {
      rlen = new double[pl.size()];
      for (int i = 0; i < rlen.length; i++)
      {
	Para p = pl.get(i);
	rlen[i] = Math.sqrt(compressLength(p, p));
      }
    }
    public double distance(Para pa, Para pb, int a, int b)
    {
      // Compression could depend on order, so make sure that
      // order is always the same
      if (a > b)
      {
        Para tp = pa;
	pa = pb;
	pb = tp;
	int ti = a;
	a = b;
	b = ti;
      }

      // Distance is 1.0 when paras are identical, and hopefully
      // greater than this if they are not, although this is not
      // guaranteed.
      double similarity = compressLength(pa, pb) / (rlen[a] * rlen[b]);
      double distance = similarity - 1.0;
      if (distance < 0.0)
      {
        distance = 0.0;
      }
      return distance;
    }
  }
  /** Build distance object from paras without counts */
  private static class DistanceBuilt implements MinSpan.Distance
  {
    /** DistanceGuts object */
    private final DistanceGuts guts;
    /** list of paras */
    private final List<Para> plist;
    /** create from list of paras which is just copied across */
    public DistanceBuilt(List<Para> pl, DistanceGuts dg)
    {
      guts = dg;
      plist = pl;
      guts.init(plist);
    }
    /** return distance */
    public double distance(int a, int b)
    {
      Para pa = plist.get(a);
      Para pb = plist.get(b);
      double dist = guts.distance(pa, pb, a, b);
      numCalls++;
      totalDistance += dist;
      return dist;
    }
    /** number of calls made to distance */
    private int numCalls;
    /** total distance computed */
    private double totalDistance;
    /** return mean distance computed */
    public double getMeanDistance()
    {
      return totalDistance / numCalls;
    }
    /** return the Para at the given offset */
    public Para getPara(int offset)
    {
      return plist.get(offset);
    }
  }
  /** pair of integers used to store distances in tree */
  private static class IntPair
  {
    private final int a;
    private final int b;
    IntPair(int aa, int bb)
    {
      if (aa < bb)
      {
	a = aa;
	b = bb;
      }
      else
      {
        a = bb;
	b = aa;
      }
    }
    @Override
    public int hashCode()
    {
      return a * 131 + b;
    }
    @Override
    public boolean equals(Object o)
    {
      if (!(o instanceof IntPair))
      {
        return false;
      }
      IntPair other = (IntPair)o;
      return (a == other.a) && (b == other.b);
    }
  }
  public static void main(String[] s) throws Exception
  {
    boolean useCompress = false;
    boolean doCluster = false;
    boolean trouble = false;
    boolean doDiameter = false;
    for (int i = 0; i < s.length; i++)
    {
      if ("-comp".equals(s[i]))
      {
        useCompress = true;
      }
      else if ("-clust".equals(s[i]))
      {
        doCluster = true;
      }
      else if ("-dim".equals(s[i]))
      {
        doDiameter = true;
      }
      else
      {
        System.err.println("Cannot handle flag " + s[i]);
	trouble = true;
      }
    }
    if (doDiameter && doCluster)
    {
      System.err.println("Cannot have -clust and -dim");
      trouble = true;
    }
    if (trouble)
    {
      System.err.println("Args are [-clust] [-comp] [-dim]");
      return;
    }
    System.out.println("Use compression-based distance: " +
      useCompress);
    System.out.println("Turn minimum spanning tree into clustering: " +
      doCluster);
    System.out.println( "Root at start of longest path: " +
      doDiameter);
    // Create a list of paras
    List<Para> pl = new ArrayList<Para>();
    List<String> lines = new ArrayList<String>();
    BufferedReader br = new BufferedReader(
      new InputStreamReader(System.in));
    for (;;)
    {
      String line = br.readLine();
      boolean eof = false;
      if (line != null)
      {
        line = line.trim();
	if (line.length() == 0)
	{
	  line = null;
	}
	else
	{
	  lines.add(line);
	}
      }
      else
      {
        eof = true;
      }
      if ((line == null) && !lines.isEmpty())
      { // turn buffered lines into paragraph
        pl.add(new Para(lines));
	// System.out.println("Para " + pl.size());
	lines = new ArrayList<String>();
      }
      if (eof)
      {
	break;
      }
    }
    int numParas = pl.size();
    System.out.println("Read " + numParas);
    if (numParas <= 1)
    {
      System.out.println("Not enough paragraphs");
      return;
    }
    DistanceBuilt db;
    if (useCompress)
    {
      System.out.println("Using compression distance");
      db =
        new DistanceBuilt(pl, new CompressionDistance());
    }
    else
    {
      System.out.println("Using cosine distance");
      db = new DistanceBuilt(pl, new CosineDistance());
    }
    System.out.println("Built distance object");
    int[] left = new int[numParas - 1];
    int[] right = new int[left.length];
    double[] len = new double[left.length];
    MinSpan.computeMinSpan(numParas, db, left, right, len);
    System.out.println("Mean distance is " + db.getMeanDistance());
    int root;
    ArrayList<ArrayList<Integer>> neighbours =
      new ArrayList<ArrayList<Integer>>();
    if (doCluster)
    {
      int[] leftOut = new int[numParas * 2 - 2];
      int[] rightOut = new int[leftOut.length];
      double[] lenOut = new double[leftOut.length];
      MinSpan.makeClustering(numParas, left, right, len,
        leftOut, rightOut, lenOut);
      left = leftOut;
      right = rightOut;
      len = lenOut;
    }
    // Save distances
    Map<IntPair, Double> distByPair = new HashMap<IntPair, Double>();
    for (int i = 0; i < left.length; i++)
    {
      IntPair ip = new IntPair(left[i], right[i]);
      distByPair.put(ip, new Double(len[i]));
    }
    int numNodes = left.length + 1;
    // Find the longest distance, and create list of neighbours
    for (int i = 0; i < numNodes; i++)
    {
      neighbours.add(new ArrayList<Integer>());
    }
    double longest = len[0];
    int longAt = 0;
    for (int i = 0; i < len.length; i++)
    {
      int l = left[i];
      int r = right[i];
      // System.out.println("Left " + l + " right " + r + " length " +
      //   len[i]);
      neighbours.get(l).add(r);
      neighbours.get(r).add(l);
      if (len[i] > longest)
      {
	longest = len[i];
	longAt = i;
      }
    }
    // Sort neighbours into original order
    for (ArrayList<Integer> al: neighbours)
    {
      Collections.sort(al);
    }
    if (doCluster)
    {
      root = numParas * 2 - 2;
    }
    else if (doDiameter)
    {
      // pick as root one of the nodes on a longest path through
      // the tree and re-order the neighbours so that links on the
      // longest path are visited last. This means that the total
      // length followed as we print out the tree will be minimised
      int[] path = new int[numParas];
      int pathLen = MinSpan.diameter(numParas, left, right,
	len, path);
      // mark links on path.
      int[] pathToByFrom = new int[numNodes];
      Arrays.fill(pathToByFrom, -1);
      for (int i = 0; i < pathLen; i++)
      {
        int from = path[i];
        int to = path[i + 1];
	// Path should never be circular
	if (pathToByFrom[from] != -1)
	{
	  throw new IllegalArgumentException("circular diameter");
	}
	pathToByFrom[from] = to;
      }
      root = path[0];
      for (int i = 0; i < numParas; i++)
      {
        int after = pathToByFrom[i];
	if (after < 0)
	{
	  continue;
	}
	List<Integer> nl = neighbours.get(i);
	int numNeighbours = nl.size();
	int here = -1;
	for (int j = 0; j < numNeighbours; j++)
	{
	  if (nl.get(j) == after)
	  {
	    here = j;
	  }
	}
	if (here < 0)
	{
	  throw new IllegalArgumentException("Path out not found");
	}
	int last = numNeighbours - 1;
	if (here == last)
	{ // path out is already last
	  continue;
	}
	// Move old last entry to where we were pointing to after
	nl.set(here, nl.get(last));
	// make last entry after
	nl.set(last, after);
      }
    }
    else
    {
      // Pick as root whichever of the paras on either side of the
      // longest distance comes first
      root = left[longAt];
      if (root > right[longAt])
      {
	root = right[longAt];
      }
    }
    // Do depth first search with explicit stack to dump out paras,

    // explicit stack for depth first search. Keeps node number,
    // next neighbour to deal with within node, and number
    // for that neighbour, in that order
    ArrayList<Integer> stack = new ArrayList<Integer>();
    stack.add(root);
    stack.add(0);
    stack.add(0);
    boolean[] seen = new boolean[numNodes];
    for (;;)
    {
      // pick current link off stack, returning from child
      // or at start
      int pos = stack.size() - 1;
      if (pos < 0)
      {
	break;
      }
      int neighbourNumber = stack.get(pos);
      stack.remove(pos);
      int offsetNext = stack.get(--pos);
      stack.remove(pos);
      int nodeNum = stack.get(--pos);
      stack.remove(pos);
      int parent = -1;

      if (offsetNext == 0)
      { 
        // At this node for the first time: print it out
        // Can use offsets above as numbering scheme
	seen[nodeNum] = true;
	int past = stack.size();
	if (past == 0)
	{
	  System.out.print("Top node: ");
	}
	else
	{
	  System.out.print("Node ");
	  String sep = "";
	  for (int i = 2; i < past; i += 3)
	  {
	    System.out.print(sep);
	    sep = ".";
	    System.out.print(stack.get(i));
	  }
	  parent = stack.get(stack.size() - 3);
	  Double d1 = distByPair.get(new IntPair(nodeNum, parent));
	  if (d1 == null)
	  {
	    throw new IllegalStateException("Could not find distance");
	  }
	  System.out.print(" dist to parent " +
	    (parent + 1) + " " + d1);
	}
	System.out.println(" Para " + (nodeNum + 1));
	if (nodeNum < numParas)
	{
	  for (String line: db.getPara(nodeNum).lines)
	  {
	    System.out.println(line);
	  }
	}
	System.out.println();
      }

      for (;;)
      {
	if (offsetNext >= neighbours.get(nodeNum).size())
	{ // Here when done all descendants of a node
	  final boolean CHECK_IF_CHILDLESS = true;
	  if (CHECK_IF_CHILDLESS && (parent >= 0) && !doCluster)
	  { // Check: Distance to parent should be minimum
	    if (neighbourNumber == 0)
	    {
	      System.out.println("Para " + (nodeNum + 1) +
		" is childless");
	    }
	    double parentDist = db.distance(nodeNum, parent);
	    for (int i = 0; i < numParas; i++)
	    {
	      if (i == nodeNum)
	      {
	        continue;
	      }
	      double otherDist = db.distance(nodeNum, i);
	      if (otherDist < parentDist)
	      {
	        System.err.println("**** ERROR ERROR ERROR **** Childless node too far: " +
		  otherDist + " to para " + (i + 1));
		return;
	      }
	    }
	  }
	  break;
	}
	int neighbour = neighbours.get(nodeNum).get(offsetNext);
	if (seen[neighbour])
	{
	  offsetNext++;
	  continue;
	}
	// here to branch down to neighbour
	stack.add(nodeNum);
	stack.add(offsetNext + 1);
	stack.add(neighbourNumber + 1);
	stack.add(neighbour);
	stack.add(0);
	stack.add(0);
	break;
      }
    }
  }
}
