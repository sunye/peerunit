package uk.co.demon.mcdowella.algorithms;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/** Test harness for TreeDefault */
public class TreeDefaultTest
{
  private static class TestNode extends TreeDefault.DefaultNode
  {
    private TestNode parent;
    public void addTestChild(TestNode n)
    {
      addChild(n);
      n.parent = this;
    }
  }
  /** Slow but sure */
  private static Object slowGetValue(TestNode node,
    TreeDefault.Attribute attribute)
  {
    for (TestNode here = node; here != null; here = here.parent)
    {
      Map m = here.getUnmodifiableAttributeMap();
      if (m.containsKey(attribute))
      {
        return m.get(attribute);
      }
    }
    return null;
  }
  private static TestNode createTree(Random r, double probStopChild,
    int maxDepth, double perAttribute,
    TreeDefault.DefaultAttribute[] attributeChoice)
  {
    TestNode tn = new TestNode();
    for (int i = 0; i < attributeChoice.length; i++)
    {
      if (r.nextDouble() < perAttribute)
      {
        tn.put(attributeChoice[i], new Object());
      }
    }
    int nextDepth = maxDepth - 1;
    if (nextDepth > 0)
    {
      while (r.nextDouble() > probStopChild)
      {
	tn.addTestChild(createTree(r, probStopChild, nextDepth,
	  perAttribute, attributeChoice));
      }
    }
    return tn;
  }
  private static int checkTree(TestNode tn, TreeDefault td,
    TreeDefault.DefaultAttribute[] attributeChoice, TestNode root,
    int pass)
  {
    int nodes = 1;
    for (int i = 0; i < attributeChoice.length; i++)
    {
      TreeDefault.DefaultAttribute at = attributeChoice[i];
      Object fast = td.getValueWithDefaults(tn, at);
      Object slow = slowGetValue(tn, at);
      // System.err.println("fast " + fast + " slow " + slow);
      if (fast != slow)
      {
	System.out.println("trouble at pass " + pass);
	System.out.println("Node " + tn + " attribute " + at +
	  " number " + at.getNumber() + " fast " + fast + " slow " +
	  slow);
	dumpTree(root, 0);
	td.dump();
	for (int j = 0; j < attributeChoice.length; j++)
	{
	  TreeDefault.Attribute hat = attributeChoice[j];
	  System.out.println("Attribute " + hat + " has number " +
	    hat.getNumber());
	}
        throw new IllegalStateException("Mismatch at pass " + pass);
      }
    }
    for (Iterator i = tn.getChildrenIterator(); i.hasNext();)
    {
      nodes += checkTree((TestNode)i.next(), td, attributeChoice, root,
        pass);
    }
    return nodes;
  }
  private static void pass(Random r, double probStopChild, int maxDepth,
    double perAttribute, TreeDefault.DefaultAttribute[] attributeChoice,
    int pass)
  {
    // Ensure attributes are marked as not in use
    for (int i = 0; i < attributeChoice.length; i++)
    {
      attributeChoice[i].setNumber(-1);
    }
    TestNode tn = createTree(r, probStopChild, maxDepth,
      perAttribute, attributeChoice);
    TreeDefault td = new TreeDefault(tn);
    int nodes = checkTree(tn, td, attributeChoice, tn, pass);
    // System.out.println("got " + nodes + " nodes");
  }
  public static void main(String[] s)
  {
    int numAttributes = 12;
    double perAttribute = 0.25;
    double probStopChild = 0.3;
    int maxDepth = 3;
    int goes = 100;
    long seed = 42;
    int s1 = s.length - 1;
    boolean trouble = false;
    int argp = 0;

    try
    {
      for (; argp < s.length; argp++)
      {
	if ((argp < s1) && "-atProb".equals(s[argp]))
	{
	  perAttribute = Double.parseDouble(s[++argp].trim());
	}
        else if ((argp < s1) && "-ats".equals(s[argp]))
	{
	  numAttributes = Integer.parseInt(s[++argp].trim());
	}
        else if ((argp < s1) && "-depth".equals(s[argp]))
	{
	  maxDepth = Integer.parseInt(s[++argp].trim());
	}
        else if ((argp < s1) && "-goes".equals(s[argp]))
	{
	  goes = Integer.parseInt(s[++argp].trim());
	}
        else if ((argp < s1) && "-seed".equals(s[argp]))
	{
	  seed = Long.parseLong(s[++argp].trim());
	}
        else if ((argp < s1) && "-stopProb".equals(s[argp]))
	{
	  probStopChild = Double.parseDouble(s[++argp].trim());
	}
	else
	{
	  System.err.println("Could not handle flag " + s[argp]);
	  trouble = true;
	}
      }
    }
    catch (NumberFormatException nfe)
    {
      System.err.println("cannot read number from " + s[argp]);
      trouble = true;
    }

    if (trouble)
    {
      System.err.println("args are [-atProb #] [-ats #] [-depth #] " +
        "[-goes #] [-seed #] [-stopProb #]");
      return;
    }

    System.out.println("atProb " + perAttribute + " ats " +
      numAttributes + " depth " + maxDepth + " goes " + goes +
      " seed " + seed + " stopProb " + probStopChild);

    TreeDefault.DefaultAttribute[] at =
      new TreeDefault.DefaultAttribute[numAttributes];
    for (int i = 0; i < at.length; i++)
    {
      at[i] = new TreeDefault.DefaultAttribute();
    }
    int last = 0;
    for (int pass = 0; pass < goes; pass++)
    {
      if (pass > (last + (last >> 1)))
      {
	System.err.println("Pass " + pass + " of " + goes);
	last = pass;
      }
      Random r = new Random(seed + pass);
      pass(r, probStopChild, maxDepth, perAttribute, at, pass);
    }
  }
  private static void spaces(int n)
  {
    for (int i = 0; i < n; i++)
    {
      System.out.print(' ');
    }
  }
  private static void dumpTree(TestNode tn, int offset)
  {
    spaces(offset);
    System.out.println("Node " + tn + " parent " + tn.parent +
      " number " + tn.getNumber());
    for (Iterator i = tn.getAttributeValueIterator(); i.hasNext();)
    {
      Map.Entry me = (Map.Entry)i.next();
      spaces(offset);
      TreeDefault.Attribute at = (TreeDefault.Attribute)me.getKey();
      System.out.println("Attribute " + at + " value " +
        me.getValue() + " number " + at.getNumber());
    }
    for (Iterator i = tn.getChildrenIterator(); i.hasNext();)
    {
      dumpTree((TestNode)i.next(), offset + 2);
    }
  }
}
