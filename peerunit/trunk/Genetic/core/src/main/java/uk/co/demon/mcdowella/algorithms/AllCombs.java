package uk.co.demon.mcdowella.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * This class can take in a specification of the number of possible
 * choices at each point in an array, and a strength. It then produces
 * a selection of integer arrays, such that for each choice of
 * strength points in the first array, some array in the selection 
 * has any possible pair of values at those strength points.
 * In other words, we cover every combinations of size strength or
 * less.
 */
public class AllCombs
{
  /** Copy of input giving number of choices at each stage */
  private int[] numChoices;
  /** used to make loads of decisions at random: this stuff mostly
   *  works by doing its following random guesses so that
   *  we can make loads of attempts and pick the best
   */
  private Random r;
  /** random number seed */
  private long mySeed;
  /** strength - maximum size of combination to consider. Note
   *  that covering every combination of columns of size strength
   *  will automatically include every combination of columns of
   *  size less than that.
   */
  private int strength;
  /** Construct given number of choices at each point and the starting
   *  random number seed. This does some one-time work
   */
  public AllCombs(int[] choices, int forStrength, long seed)
  {
    // Be paranoid about user fiddling with array after calling
    // constructor
    numChoices = choices.clone();
    mySeed = seed;
    strength = forStrength;
  }
  /** print out the design */
  public static void showResult(int[][] result, Translator tran)
  {
    for (int i = 0; i < result.length; i++)
    {
      for (int j = 0; j < result[i].length; j++)
      {
	if (j != 0)
	{
	  System.out.print(' ');
	}
	String toPrint;
	if (tran == null)
	{
	  toPrint = Integer.toString(result[i][j]);
	}
	else
	{
	  toPrint = tran.translate(j, result[i][j]);
	}
	System.out.print(toPrint);
      }
      System.out.println();
    }
    System.out.flush();
  }
  /** Generate and print out stuff for ever, or until max
   *  goes
   */
  public static void indefiniteGenerate(
    int[] choices, long seed, int max,
    Translator tran, int strength, int innerGoes, boolean balance)
  {
    int bestSofar = Integer.MAX_VALUE;
    AllCombs ap = new AllCombs(choices, strength, seed);
    for (int go = 0;;)
    {
      int[][] result = ap.generate(innerGoes, balance);
      if (result.length < bestSofar)
      {
        bestSofar = result.length;
	System.out.println("New best of " + bestSofar + " rows at go " +
	  go);
	showResult(result, tran);
	System.out.println("New best was " +
	  bestSofar + " rows at go " + go);
      }
      go++;
      if ((max > 0) && (go >= max))
      {
        return;
      }
    }
  }
  /** This strategy generates new combinations at random and
   *  picks the best. Each combination we need to include occurs
   *  the same number of times in the original data. So each random
   *  generation includes it with the same probability, and the chance
   *  of it not being included diminishes exponentially as we
   *  add more combinations. If balance is set and a choice does not
   *  have equal numbers of 0s and 1s, we add its inverse.
   */
  public int[][] generate(int innerGoes, boolean balance)
  {
    r = new Random(mySeed++);
    if ((numChoices.length < strength) || (strength < 1))
    {
      return new int[0][];
    }
    List<int[]> result = new ArrayList<int[]>();
    covered = new LongBitSet(16);
    currentChoice = new int[numChoices.length];
    for (;;)
    {
      int[] choice = generateChoice(innerGoes);
      result.add(choice);
      if (finishedAt(choice))
      {
        break;
      }
      if (balance)
      {
	int num1s = 0;
        for (int i = 0; i < choice.length; i++)
	{
	  num1s += choice[i];
	}
	if ((num1s * 2) != choice.length)
	{
	  int[] inverse = new int[choice.length];
	  for (int i = 0; i < choice.length; i++)
	  {
	    inverse[i] = 1 - choice[i];
	  }
	  result.add(inverse);
	  if (finishedAt(inverse))
	  {
	    break;
	  }
	}
      }
    }
    covered = null;
    currentChoice = null;
    return result.toArray(new int[result.size()][]);
  }
  private int[] generateChoice(int innerGoes)
  {
    int bestSofar = -1;
    int[] sofar = new int[numChoices.length];
    for (int i = 0; i < innerGoes; i++)
    {
      for (int j = 0; j < numChoices.length; j++)
      {
	currentChoice[j] = r.nextInt(numChoices[j]);
      }
      bitOffset = 0;
      int score = countCombination(0, 0, 0, 1);
      if (score > bestSofar)
      {
        for (int j = 0; j < numChoices.length; j++)
	{
	  sofar[j] = currentChoice[j];
	}
	bestSofar = score;
      }
    }
    return sofar;
  }
  private long bitOffset;
  private int[] currentChoice;
  private LongBitSet covered;
  /** recursive routine called to count all combinations
   *  of columns with the last column chosen being lastChoice,
   *  the number of columns in the recursive stack so far being
   *  covered, the current value built up being sofar, and the
   *  number of possible values so far in the combination being
   *  possible
   */
  private int countCombination(int nextChoice, int numChosen,
    long sofar, long possible)
  {
    // work out last sensible choice. If we have covered all
    // but one so far, this is numChoices.length - 1
    int lastChoice = numChoices.length - strength + numChosen;
    int newNumChosen = numChosen + 1;
    int newNext;
    int newCovers = 0;
    for (;nextChoice <= lastChoice; nextChoice = newNext)
    {
      newNext = nextChoice + 1;
      int choicesHere = numChoices[nextChoice];
      // build up representation of choices in this combination,
      // putting current choice in as the low order digit in
      // a mixed-radix number
      long newSofar = sofar * choicesHere + currentChoice[nextChoice];
      long newPossible = possible * choicesHere;
      if (newNumChosen == strength)
      {
	// Score one if not covered yet
        if (!covered.get(bitOffset + newSofar))
	{
	  newCovers++;
	}
	bitOffset += newPossible;
      }
      else
      {
        newCovers += countCombination(newNext, newNumChosen, newSofar,
	  newPossible);
      }
    }
    return newCovers;
  }
  /** recursive routine called to consider all combinations
   *  of columns with the last column chosen being lastChoice,
   *  the number of columns in the recursive stack so far being
   *  numChosen, the current value built up being sofar, and the
   *  number of possible values so far in the combination being
   *  possible
   */
  private void checkCombination(int nextChoice, int numChosen,
    long sofar, long possible)
  {
    // work out last sensible choice. If we have covered all
    // but one so far, this is numChoices.length - 1
    int lastChoice = numChoices.length - strength + numChosen;
    int newNumChosen = numChosen + 1;
    int newNext;
    for (;nextChoice <= lastChoice; nextChoice = newNext)
    {
      newNext = nextChoice + 1;
      int choicesHere = numChoices[nextChoice];
      // build up representation of choices in this combination,
      // putting current choice in as the low order digit in
      // a mixed-radix number
      /*
      System.out.println("Current choice " + currentChoice[nextChoice]
	+ " at position " + nextChoice);
      */
      long newSofar = sofar * choicesHere + currentChoice[nextChoice];
      long newPossible = possible * choicesHere;
      if (newNumChosen == strength)
      {
	// Write out this combination within the concatenated
	// bitsets, and advance the write pointer
	/*
	System.out.println("Offset " + bitOffset + " set " +
	  newSofar);
	*/
        covered.set(bitOffset + newSofar);
	bitOffset += newPossible;
      }
      else
      {
        checkCombination(newNext, newNumChosen, newSofar,
	  newPossible);
      }
    }
  }
  private boolean finishedAt(int[] choice)
  {
    bitOffset = 0;
    for (int i = 0; i < numChoices.length; i++)
    {
      currentChoice[i] = choice[i];
    }
    checkCombination(0, 0, 0, 1);
    // System.out.println("Got cardinality " + covered.cardinality());
    return covered.cardinality() == bitOffset;
  }
  public static void main(String[] s) throws IOException
  {
    List<Integer> choiceList = new ArrayList<Integer>();
    String sp = "";
    boolean trouble = false;
    boolean noShuffle = false;
    int maxGoes = 100;
    int strength = 2;
    long seed = 42;
    String choiceFile = null;
    int innerGoes = 100;
    int s1 = s.length - 1;
    boolean balance = false;
    try
    {
      for (int i = 0; i < s.length; i++)
      {
        sp = s[i].trim();
	if (sp.startsWith("-"))
	{
	  if (sp.equals("-balance"))
	  {
	    balance = true;
	  }
	  else if ((sp.equals("-file")) && (i < s1))
	  {
	    choiceFile = s[++i].trim();
	  }
	  else if ((sp.equals("-goes")) && (i < s1))
	  {
	    sp = s[++i].trim();
	    maxGoes = Integer.parseInt(sp);
	  }
	  else if ((sp.equals("-innerGoes")) && (i < s1))
	  {
	    sp = s[++i].trim();
	    innerGoes = Integer.parseInt(sp);
	  }
	  else if ((sp.equals("-seed")) && (i < s1))
	  {
	    sp = s[++i].trim();
	    seed = Long.parseLong(sp);
	  }
	  else if ((sp.equals("-strength")) && (i < s1))
	  {
	    sp = s[++i].trim();
	    strength = Integer.parseInt(sp);
	  }
	  else
	  {
	    System.err.println("Could not handle flag " + sp);
	    trouble = true;
	  }
	}
	else
	{
	  int numChoices = Integer.parseInt(sp);
	  if (numChoices < 1)
	  {
	    System.err.println(
	      "Number of choices must be > 1 at every point");
	    trouble = false;
	  }
	  choiceList.add(numChoices);
	}
      }
    }
    catch (NumberFormatException nf)
    {
      System.err.println("Could not read number in " + sp);
      trouble = true;
    }
    if (innerGoes < 1)
    {
      System.err.println("Must have InnerGoes > 0");
      trouble = true;
    }

    int[] choices = new int[choiceList.size()];
    for (int i = 0; i < choices.length; i++)
    {
      choices[i] = choiceList.get(i);
      if (balance && (choices[i] != 2))
      {
        System.err.println("If balance, all choices must be 2");
	trouble = true;
      }
    }
    Translator tran = null;
    if (choiceFile != null)
    {
      if (!choiceList.isEmpty())
      {
        System.err.println(
	  "Cannot give both -file <name> and numeric choices");
        trouble = true;
      }
      else
      {
        tran = new Translator(choiceFile);
	choices = tran.getNumChoices();
      }
    }
    if (choices.length < strength)
    {
      System.err.println(
        "No point trying to work with less than two points");
      trouble = true;
    }
    if (trouble)
    {
      System.err.println(
        "Arguments should be the number of choices at each point");
      System.err.println(
      "E.g. to find a design for a system with three parameters,");
      System.err.println(
      "with 2 choices for the first, 3 for the second, and 4 for the third,");
      System.err.println("Use arguments 2 3 4");
      System.err.println(
        "Or use -file <name> with one line of strings per option");
      System.err.println("and # a comment");
      System.err.println(
    "Can add flags [-balance] [-innerGoes #] [-goes #] [-seed #] [-strength #]" +
    "[-file <name>]");
      System.err.println("-goes 0 => keep trying until halted");
      return;
    }
    System.out.print("Working on choices:");
    for (int i = 0; i < choices.length; i++)
    {
      System.out.print(' ');
      System.out.print(choices[i]);
    }
    System.out.println();
    System.out.println("MaxGoes " + maxGoes + " seed " + seed +
      " innerGoes " + innerGoes + " strength " + strength);
    AllCombs.indefiniteGenerate(choices, seed, maxGoes,
      tran, strength, innerGoes, balance);
  }
  /** Class to read input file, create choices, and translate */
  private static class Translator
  {
    Translator(String filename) throws IOException
    {
      BufferedReader br = null;
      try
      {
        br = new BufferedReader(new FileReader(filename));
	List<String[]> sofar = new ArrayList<String[]>();
	for (;;)
	{
	  String line = br.readLine();
	  if (line == null)
	  {
	    break;
	  }
	  List<String> sl = new ArrayList<String>();
	  StringTokenizer st = new StringTokenizer(line);
	  while (st.hasMoreTokens())
	  {
	    String token = st.nextToken();
	    int pos = token.indexOf('#');
	    if (pos == 0)
	    {
	      break;
	    }
	    else if (pos > 0)
	    {
	      sl.add(token.substring(0, pos));
	      break;
	    }
	    sl.add(token);
	  }
	  int len = sl.size();
	  if (len == 1)
	  {
	    System.err.println(
	      "Discarding line with only one option: " + sl.get(0));
	  }
	  else if (len > 1)
	  {
	    String[] fromLine = new String[len];
	    sofar.add(sl.toArray(fromLine));
	  }
	}
	int lines = sofar.size();
	choiceTranslator = new String[lines][];
	choiceTranslator = sofar.toArray(choiceTranslator);
	numChoices = new int[lines];
	for (int i = 0; i < numChoices.length; i++)
	{
	  numChoices[i] = choiceTranslator[i].length;
	}
      }
      finally
      {
        if (br != null)
	{
	  br.close();
	}
      }
    }
    /** number of choices */
    private int[] numChoices;
    /** used to convert choices back to numbers */
    private String[][] choiceTranslator;
    int[] getNumChoices()
    {
      return numChoices.clone();
    }
    String translate(int col, int choice)
    {
      return choiceTranslator[col][choice];
    }
  }
  /** A BitSet replacement addressable via longs */
  private static class LongBitSet
  {
    public LongBitSet(int initialSize)
    {
      data = new int[initialSize];
    }
    private int[] data;
    private long numSet = 0;
    public long cardinality()
    {
      return numSet;
    }
    public boolean get(long offset)
    {
      // use signed shift so we die horribly if offset is -ve,
      // which it should never be
      int wordOffset = (int)(offset >> 5);
      if (wordOffset >= data.length)
      {
        return false;
      }
      int found = data[wordOffset] & (1 << (offset & 31));
      return found != 0;
    }
    public void set(long offset)
    {
      if (offset > 0xfffffffffl)
      {
        throw new IllegalArgumentException(
	  "Too big even with bitmap");
      }
      int wordOffset = (int)(offset >> 5);
      if (wordOffset >= data.length)
      {
	int newLength = data.length << 1;
	// System.err.println("New len " + newLength);
	while (newLength <= wordOffset)
	{
	  newLength += newLength;
	}
	int[] newData = new int[newLength];
	for (int i = 0; i < data.length; i++)
	{
	  newData[i] = data[i];
	}
	data = newData;
      }
      int result = data[wordOffset];
      int mask = 1 << (offset & 31);
      if ((result & mask) == 0)
      {
        numSet++;
	data[wordOffset] = result | mask;
      }
    }
  }
}
