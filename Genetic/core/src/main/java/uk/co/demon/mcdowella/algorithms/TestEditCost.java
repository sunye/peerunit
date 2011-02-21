package uk.co.demon.mcdowella.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** This class tests the edit distance stuff */
public class TestEditCost
{
  /** Run a single test, using the given seed, with random data
   *  of len <= fromPlus and alphabet alen
   */
  public static void singleTest(long seed, int alen, int fromPlus,
    double insertProb, Swatch square, Swatch thresholded,
    double nonCopyProb, boolean zeroCopy, boolean contig)
  {
    Random ran = new Random(seed);
    EditCost ec = new EditCost();
    SparseEditCost sec = new SparseEditCost(contig);
    // final int lots = 10000;
    double copyCost = ran.nextDouble();
    if (zeroCopy)
    {
      copyCost = 0.0;
    }
    ec.setCopyCost(copyCost);
    sec.setCopyCost(copyCost);
    double replaceCost = ran.nextDouble();
    ec.setReplaceCost(replaceCost);
    sec.setReplaceCost(replaceCost);
    double deleteCost = ran.nextDouble();
    ec.setDeleteCost(deleteCost);
    sec.setDeleteCost(deleteCost);
    double insertCost = ran.nextDouble();
    ec.setInsertCost(insertCost);
    sec.setInsertCost(insertCost);
    int[] from = new int[1 + ran.nextInt(fromPlus)];
    ArrayList al = new ArrayList();
    double ourCost = 0.0;
    for (int i = 0; i < from.length; i++)
    {
      if (ran.nextDouble() < insertProb)
      {
        al.add(new Integer(EditCost.INSERT));
	al.add(new Integer(ran.nextInt(alen)));
	ourCost += insertCost;
	i--;
	continue;
      }
      if (ran.nextDouble() < nonCopyProb)
      {
	switch (ran.nextInt(2))
	{
	  case 0:
	    al.add(new Integer(EditCost.REPLACE));
	    al.add(new Integer(ran.nextInt(alen)));
	    ourCost += replaceCost;
	  break;
	  case 1:
	    al.add(new Integer(EditCost.DELETE));
	    ourCost += deleteCost;
	  break;
	  default:
	    throw new IllegalStateException("Bad random case");
	}
      }
      else
      {
	al.add(new Integer(EditCost.COPY));
	ourCost += copyCost;
      }
    }
    int[] commands = new int[al.size()];
    for(int i = 0; i < commands.length; i++)
    {
      commands[i] = ((Integer)al.get(i)).intValue();
    }
    for (int i = 0; i < from.length; i++)
    {
      from[i] = ran.nextInt(alen);
    }
    int[] to = AbstractEditCost.apply(commands, from);
    /*
    System.err.println("Copy " + copyCost);
    System.err.println("Replace " + replaceCost);
    System.err.println("Insert " + insertCost);
    System.err.println("Delete " + deleteCost);
    System.err.print("From: ");
    for (int i = 0; i < from.length; i++)
    {
      System.err.print(from[i]);
    }
    System.err.println();
    System.err.print("To: ");
    for (int i = 0; i < to.length; i++)
    {
      System.err.print(to[i]);
    }
    System.err.println();
    */
    square.start();
    double editCost = ec.computeCost(from, to);
    square.stop();
    // System.err.println("Edit cost " + editCost);
    if (editCost > ourCost)
    {
      throw new IllegalStateException(
        "Found edit cost too high: " + editCost + " > " +
	ourCost);
    }
/*
    System.err.println("Copy " + copyCost);
    System.err.println("Replace " + replaceCost);
    System.err.println("Insert " + insertCost);
    System.err.println("Delete " + deleteCost);
    System.err.print("From: ");
    for (int i = 0; i < from.length; i++)
    {
      System.err.print(from[i]);
    }
    System.err.println();
    System.err.print("To: ");
    for (int i = 0; i < to.length; i++)
    {
      System.err.print(to[i]);
    }
    System.err.println();
*/
    thresholded.start();
    double seditCost = sec.computeCost(from, to);
    thresholded.stop();
    int[] editCommands = ec.getCommands();
    int[] seditCommands = sec.getCommands();
    int[] backTo = AbstractEditCost.apply(editCommands, from);
    int[] sBackTo = AbstractEditCost.apply(seditCommands, from);
    if (backTo.length != to.length)
    {
      throw new IllegalStateException("to length is different");
    }
    for (int i = 0; i < to.length; i++)
    {
      if (to[i] != backTo[i])
      {
        throw new IllegalStateException("Commands do not work");
      }
    }
    if (sBackTo.length != to.length)
    {
      throw new IllegalStateException("sto length is different");
    }
    for (int i = 0; i < to.length; i++)
    {
      if (to[i] != sBackTo[i])
      {
        throw new IllegalStateException("sCommands do not work");
      }
    }

    if (seditCost != sec.computeCost(from, to))
    {
      throw new IllegalStateException("Different scost without trace");
    }
    if (Math.abs(editCost - seditCost) > 1.0e-4)
    {
      throw new IllegalStateException("Edit cost mismatch: edit cost " +
        editCost + " sparse variant cost " + seditCost + " seed " +
	seed);
    }
    ec.setTrace(false);
    sec.setTrace(false);
    if (editCost != ec.computeCost(from, to))
    {
      throw new IllegalStateException("Different cost without trace");
    }

    // System.out.println("constructed " + ourCost + " found " + editCost);
  }
  public static void main(String[] s)
  {
    long seed = 42;
    int alen = 6;
    int fromPlus = 20;
    double insertProb = 0.1;
    double nonCopyProb = 0.6;
    int goes = 100;
    boolean zeroCopy = false;
    boolean contig = false;

    int argp = 0;
    boolean trouble = false;
    int s1 = s.length - 1;
    try
    {
      for (;argp < s.length; argp++)
      {
	if ((argp < s1) && "-alen".equals(s[argp]))
	{
	  alen = Integer.parseInt(s[++argp].trim());
	}
	else if ("-contig".equals(s[argp]))
	{
	  contig = true;
	}
	else if ((argp < s1) && "-goes".equals(s[argp]))
	{
	  goes = Integer.parseInt(s[++argp].trim());
	}
	else if ((argp < s1) && "-insert".equals(s[argp]))
	{
	  insertProb = Double.parseDouble(s[++argp].trim());
	}
	else if ((argp < s1) && "-len".equals(s[argp]))
	{
	  fromPlus = Integer.parseInt(s[++argp].trim());
	}
	else if ((argp < s1) && "-nc".equals(s[argp]))
	{
	  nonCopyProb = Double.parseDouble(s[++argp].trim());
	}
	else if ((argp < s1) && "-seed".equals(s[argp]))
	{
	  seed = Long.parseLong(s[++argp].trim());
	}
	else if ("-z".equals(s[argp]))
	{
	  zeroCopy = true;
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
      System.err.println("Could not read number in " + s[argp]);
      trouble = true;
    }
    if (alen <= 0)
    {
      System.err.println("Must have alphabet length >= 1");
      trouble = true;
    }
    if ((insertProb < 0.0) || (insertProb >= 1.0))
    {
      System.err.println("Must have 0 <= insert prob < 1");
      trouble = true;
    }
    if (fromPlus < 1)
    {
      System.err.println("must have max len >= 1");
      trouble = true;
    }
    if (trouble)
    {
      System.err.println("Args are [-alen #] [-contig] [-goes #] " +
        "[-insert #] " +
        "[-len #] [-nc #] [-seed #] [-z]");
      return;
    }
    System.out.println("Alen " + alen + " contig " + contig + 
      " insert " + insertProb +
      " len " + fromPlus + " goes " + goes + " nonCopy " + nonCopyProb +
      " seed " + seed + " zeroCopy " + zeroCopy);
    Swatch square = new Swatch();
    Swatch thresholded = new Swatch();
    for (int i = 0; i < goes; i++)
    {
      // System.err.println("Go " + i + " of " + goes);
      singleTest(seed + i, alen, fromPlus, insertProb,
        square, thresholded, nonCopyProb, zeroCopy, contig);
    }
    System.out.println("Square cost " + square.millis() +
      " thresholded " + thresholded.millis());
  }
}
