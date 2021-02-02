package uk.co.demon.mcdowella.stats;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

/** This is a wrapper round the Mann-Whitney methods in MannWhitney.
*/
public class MannWhitneyCli
{
  public static void main(String[] s) throws IOException
  {
    boolean trouble = false;
    double confidence = 0.95;
    int s1 = s.length - 1;
    String num = null;
    try
    {
      for (int i = 0; i < s1; i++)
      {
	if ("-conf".equals(s[i]) && (i < s1))
	{
	  num = s[++i];
	  confidence = Double.parseDouble(num.trim());
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
      System.err.println("Cannot read number in " + num);
      trouble = true;
    }
    if ((confidence <= 0.0) || (confidence >= 1.0))
    {
      System.err.println(
    "Confidence must be greater than 0.0 and less than 1.0");
      trouble = true;
    }
    if (trouble)
    {
      System.err.println("Args are [-conf #]");
      return;
    }
    List<Double> aList = new ArrayList<Double>();
    List<Double> bList = new ArrayList<Double>();
    BufferedReader br = new BufferedReader(
      new InputStreamReader(System.in));
    List<Double> nextList = aList;
    for (;;)
    {
      String line = br.readLine();
      if (line == null)
      {
        break;
      }
      StringTokenizer st = new StringTokenizer(line, " \n\t\r", false);
      boolean sawNumbers = false;
      while (st.hasMoreTokens())
      {
        String tok = st.nextToken().trim();
	if (tok.startsWith("#"))
	{
	  break;
	}
	try
	{
	  nextList.add(new Double(tok.trim()));
	  sawNumbers = true;
	}
	catch (NumberFormatException nfe)
	{
	  System.err.println("Could not read number in " + tok +
	    " from line " + line);
	  return;
	}
      }
      if (sawNumbers)
      {
        if (nextList == aList)
	{
	  nextList = bList;
	}
	else
	{
	  nextList = aList;
	}
      }
    }
    System.out.println(aList.size() + " Numbers in a list:");
    System.out.println(aList);
    System.out.println(bList.size() + " Numbers in b list:");
    System.out.println(bList);
    if (aList.isEmpty() || bList.isEmpty())
    {
      System.out.println("Need numbers in both lists");
      return;
    }
    double[] ad = new double[aList.size()];
    for (int i = 0; i < ad.length; i++)
    {
      ad[i] = aList.get(i).doubleValue();
    }
    double[] bd = new double[bList.size()];
    for (int i = 0; i < bd.length; i++)
    {
      bd[i] = bList.get(i).doubleValue();
    }
    double[] rankScore = new double[1];
    SigProb sp = MannWhitney.getSig(ad, bd, rankScore, null);
    System.out.println("Counting from zero, rank sum score from " +
      "b side is " + rankScore[0]);
    System.out.println("Significance is " + sp);
    double[] tails = new double[2];
    double[] intervals = new double[2];
    // random number used only to pick random mid-points. Affects
    // only speed of algorithm, not result
    MannWhitney.locationRange(ad, bd, intervals, tails, 
      new Random(42), confidence, null);
    System.out.println("Targeted " + confidence + " confidence");
    System.out.println("Got tails " + tails[0] + " and " + tails[1]);
    System.out.println("Interval for A - B location difference is ");
    System.out.println("[" + intervals[0] + ", " + intervals[1] + "]");
  }
}
