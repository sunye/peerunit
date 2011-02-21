package uk.co.demon.mcdowella.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Test code for PatriciaClosest */
public class TestPatriciaClosest
{
  public static void main(String[] s)
  {
    long seed = 42000;
    int goes = 10;
    int size = 30000;
    int inner = 10000;
    int dim = 3;
    int numToFind = 1;
    boolean TEST_RANDOM = true;
    boolean TEST_INSDEL = true;
    boolean TIME_INSDEL = true;
    boolean trouble = false;
    int s1 = s.length - 1;
    String num = null;
    try
    {
      for (int i = 0; i < s.length; i++)
      {
        if ("-dim".equals(s[i]) && (i < s1))
	{
	  num = s[++i].trim();
	  dim = Integer.parseInt(num);
	}
        else if ("-find".equals(s[i]) && (i < s1))
	{
	  num = s[++i].trim();
	  numToFind = Integer.parseInt(num);
	}
        else if ("-goes".equals(s[i]) && (i < s1))
	{
	  num = s[++i].trim();
	  goes = Integer.parseInt(num);
	}
        else if ("-inner".equals(s[i]) && (i < s1))
	{
	  num = s[++i].trim();
	  inner = Integer.parseInt(num);
	}
        else if ("-seed".equals(s[i]) && (i < s1))
	{
	  num = s[++i].trim();
	  seed = Long.parseLong(num);
	}
        else if ("-size".equals(s[i]) && (i < s1))
	{
	  num = s[++i].trim();
	  size = Integer.parseInt(num);
	}
	else if ("-skip".equals(s[i]))
	{
	  TEST_RANDOM = false;
	  TEST_INSDEL = false;
	}
	else
	{
	  System.err.println("Cannot handle flag " + s[i]);
	  trouble = true;
	}
      }
    }
    catch (NumberFormatException nfe)
    {
      System.err.println("cannot read number in " + num);
      trouble = true;
    }
    if (trouble)
    {
      System.err.println("Args are [-dim #] [-find #] [-goes #] " +
        "[-inner #] [-seed #] [-size #] [-skip]");
      return;
    }
    System.out.println("Seed " + seed + " goes " + goes + " size " +
      size + " inner " + inner + " dim " + dim +
      " to find " + numToFind + " skip " + (!TEST_RANDOM));
    Swatch cLoad = new Swatch();
    Swatch pLoad = new Swatch();
    Swatch cSearch = new Swatch();
    Swatch pSearch = new Swatch();
    for (int go = 0; go < goes; go++)
    {
      long cseed = seed + go;
      System.out.println("Seed " + cseed);
      Random r = new Random(cseed);
      PatriciaGeneric.DoubleArrayKey[] par =
        new PatriciaGeneric.DoubleArrayKey[size];
      EuclidNode[] ear =
        new EuclidNode[size];
      double[] pos = new double[dim];
      for (int i = 0; i < par.length; i++)
      {
	for (int j = 0; j < pos.length; j++)
	{
	  pos[j] = r.nextGaussian();
	}
        par[i] = new PatriciaGeneric.DoubleArrayKey(pos);
        ear[i] = new EuclidNode(pos);
      }
      if (TEST_RANDOM)
      {
	PatriciaClosest<Integer> dc =
	  new PatriciaClosest<Integer>();
	Map<PatriciaGeneric.DoubleArrayKey, Integer> cc =
	  new HashMap<PatriciaGeneric.DoubleArrayKey, Integer>();
	for (int i = 0; i < inner; i++)
	{
	  PatriciaGeneric.DoubleArrayKey e = par[r.nextInt(par.length)];
	  // System.err.println("Add " + e);
	  Integer ii = new Integer(i);
	  Integer a = dc.put(e, ii);
	  Integer b = cc.put(e, ii);
	  if (a != b)
	  {
	    throw new IllegalStateException("Difference in add: " +
	      a + " vs " + b);
	  }
	  dc.checkInvariants();
	  e = par[r.nextInt(par.length)];
	  // System.err.println("Remove " + e);
	  a = dc.remove(e);
	  b = cc.remove(e);
	  if (a != b)
	  {
	    throw new IllegalStateException("Difference in remove: " +
	      a + " vs " + b);
	  }
	  dc.checkInvariants();
	}
      }
      if (TEST_INSDEL)
      {
	PatriciaClosest<Integer> dc =
	  new PatriciaClosest<Integer>();
	int todo = r.nextInt(par.length);
        for (int i = 0; i < todo; i++)
	{
	  if ((i % 100) == 0)
	  {
	    System.err.println("Insert " + i);
	  }
	  PatriciaGeneric.DoubleArrayKey e = par[i];
	  if (dc.put(e, new Integer(i)) != null)
	  {
	    throw new IllegalStateException("Add added before");
	  }
	  dc.checkInvariants();
	}
        for (int i = 0; i < todo; i++)
	{
	  if ((i % 100) == 0)
	  {
	    System.err.println("Remove " + i);
	  }
	  PatriciaGeneric.DoubleArrayKey e = par[i];
	  if (!dc.remove(e).equals(new Integer(i)))
	  {
	    throw new IllegalStateException("Remove mismatch");
	  }
	  dc.checkInvariants();
	}
      }
      if (TIME_INSDEL)
      {
	r = new Random(cseed);
	PatriciaGeneric.DoubleArrayKey[] forP =
	  new PatriciaGeneric.DoubleArrayKey[size];
	EuclidNode[] forC = new EuclidNode[size];
	for (int i = 0; i < size; i++)
	{
	  for (int j = 0; j < pos.length; j++)
	  {
	    pos[j] = r.nextGaussian();
	  }
	  forP[i] = new PatriciaGeneric.DoubleArrayKey(pos);
	  double[] back = forP[i].toDouble();
	  for (int j = 0; j < pos.length; j++)
	  {
	    if (back[j] != pos[j])
	    {
	      throw new IllegalStateException(
	        "Translation back failed: " + back[j] + " vs " +
		pos[j]);
	    }
	  }
	  forC[i] = new EuclidNode(pos);
	}
	int[] remove = new int[forC.length / 4];
	for (int i = 0; i < remove.length; i++)
	{
	  remove[i] = r.nextInt(forC.length);
	}
	boolean[] removed = new boolean[remove.length];
	cLoad.start();
	// Closest<EuclidNode> close = new Closest<EuclidNode>();
	DynamicClosest<EuclidNode> close =
	  new DynamicClosest<EuclidNode>();
	for (int i = 0; i < size; i++)
	{
	  close.add(forC[i]);
	}
	for (int i = 0; i < remove.length; i++)
	{
	  removed[i] = close.remove(forC[remove[i]]);
	}
	cLoad.stop();
	pLoad.start();
	PatriciaClosest<Object> pat =
	  new PatriciaClosest<Object>();
	Object marker = new Object();
	for (int i = 0; i < size; i++)
	{
	  pat.put(forP[i], marker);
	}
	for (int i = 0; i < remove.length; i++)
	{
	  boolean wasRemoved = (pat.remove(forP[remove[i]]) == marker);
	  if (wasRemoved != removed[i])
	  {
	    throw new IllegalStateException("removed mismatch closest " +
	      removed[i] + " pat " + wasRemoved);
	  }
	}
	pLoad.stop();
	for (int i = 0; i < inner; i++)
	{
	  int qpos = r.nextInt(size);
	  EuclidNode query = forC[qpos];
	  PatriciaGeneric.DoubleArrayKey queryP = forP[qpos];
	  cSearch.start();
	  List<EuclidNode> cList = close.findClosest(query, numToFind,
	    Double.MAX_VALUE);
	  cSearch.stop();
	  pSearch.start();
	  List<PatriciaClosest.WithDistance<Object>> pList =
	    new ArrayList<PatriciaClosest.WithDistance<Object>>();
	  pat.appendClosest(queryP, Double.MAX_VALUE, numToFind,
	    pList);
	  pSearch.stop();
	  int cSize = cList.size();
	  if (cSize != pList.size())
	  {
	    throw new IllegalStateException("List size mismatch: " +
	      cSize + ", " + pList.size());
	  }
	  for (int j = 0; j < cSize; j++)
	  {
	    EuclidNode cn = cList.get(j);
	    PatriciaClosest.WithDistance<Object> dn = pList.get(j);
	    double[] pCoord = dn.getKey().toDouble();
	    double[] qc = queryP.toDouble();
	    /*
	    for (int k = 0; k < dim; k++)
	    {
	      for (int l = 0; l < dim; l++)
	      {
		System.out.println("Coord " + cn.getCoordinate(l) +
		  " pat " + pCoord[l] + " target " + qc[l]);
	      }
	      System.out.println();
	    }
	    */
	    for (int k = 0; k < dim; k++)
	    {
	      if (cn.getCoordinate(k) != pCoord[k])
	      {
		qc = queryP.toDouble();
		for (int l = 0; l < dim; l++)
		{
		  System.out.println("Coord " + cn.getCoordinate(l) +
		    " pat " + pCoord[l] + " target " + qc[l]);
		}
		throw new IllegalStateException("Position mismatch");
	      }
	    }
	  }
	}
      }
    }
    System.out.println("Closest load " + cLoad + " search " + cSearch);
    System.out.println("Pat load " + pLoad + " search " + pSearch);
  }
}
