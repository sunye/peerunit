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
 * has set of values at those points which does not repeat a value.
 * This would allow us to construct a set of combinations similar
 * to those produced by AllCombs
 */
public class AllPerms
{
  /** number of choices for each value */
  private int numChoices;
  /** length */
  private int numVariables;
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
  public AllPerms(int numVars, int choices, int forStrength, long seed)
  {
    numVariables = numVars;
    numChoices = choices;
    mySeed = seed;
    strength = forStrength;
  }
  /** print out the design */
  public static void showResult(int[][] result)
  {
    for (int i = 0; i < result.length; i++)
    {
      for (int j = 0; j < result[i].length; j++)
      {
	if (j != 0)
	{
	  System.out.print(' ');
	}
	System.out.print(result[i][j]);
      }
      System.out.println();
    }
    System.out.flush();
  }
  /** Generate and print out stuff for ever, or until max
   *  goes
   */
  public static void indefiniteGenerate(
    int vars, int choices, long seed, int max,
    int strength, int innerGoes)
  {
    int bestSofar = Integer.MAX_VALUE;
    AllPerms ap = new AllPerms(choices, vars, strength, seed);
    for (int go = 0;;)
    {
      int[][] result = ap.generate(innerGoes);
      if (result.length < bestSofar)
      {
        bestSofar = result.length;
	System.out.println("New best of " + bestSofar + " rows at go " +
	  go);
	showResult(result);
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
   *  picks the best. Each combination we need to cater for includes
   *  with the same probability in random data. So each random
   *  generation includes it with the same probability, and the chance
   *  of it not being included diminishes exponentially as we
   *  add more combinations
   */
  public int[][] generate(int innerGoes)
  {
    r = new Random(mySeed++);
    if ((numVariables < strength) || (strength < 1))
    {
      return new int[0][];
    }
    List<int[]> result = new ArrayList<int[]>();
    covered = new BitSet();
    useCount = new int[numChoices];
    currentChoice = new int[numVariables];
    for (;;)
    {
      int[] choice = generateChoice(innerGoes);
      result.add(choice);
      if (finishedAt(choice))
      {
        break;
      }
    }
    covered = null;
    currentChoice = null;
    useCount = null;
    return result.toArray(new int[result.size()][]);
  }
  private int[] generateChoice(int innerGoes)
  {
    int bestSofar = -1;
    int[] sofar = new int[numVariables];
    for (int i = 0; i < innerGoes; i++)
    {
      // I think we maximise the chance of catering for any
      // given combination if we arrange the choices as
      // evenly as possible and then shuffle them
      for (int j = 0; j < numVariables; j++)
      {
	currentChoice[j] = j % numChoices;
      }
      for (int j = numVariables - 1; j > 0; j--)
      {
        int target = r.nextInt(j + 1);
	int t = currentChoice[target];
	currentChoice[target] = currentChoice[j];
	currentChoice[j] = t;
      }
      initForRecursion();
      int score = countCombination(0, 0);
      if (score > bestSofar)
      {
        for (int j = 0; j < numVariables; j++)
	{
	  sofar[j] = currentChoice[j];
	}
	bestSofar = score;
      }
    }
    return sofar;
  }
  // write pointer for covered
  private int bitOffset;
  // looking at effects of adding this
  private int[] currentChoice;
  // Used to keep track of which combinations have been covered
  private BitSet covered;
  // number of times each possibility has been used in current
  // combination
  private int[] useCount;
  // number of collisions seen
  private int numCollisions;
  private void initForRecursion()
  {
    bitOffset = 0;
    Arrays.fill(useCount, 0);
    numCollisions = 0;
  }
  /** recursive routine called to count all combinations
   *  of columns with the last column chosen being lastChoice,
   *  the number of columns in the recursive stack so far being
   *  covered
   */
  private int countCombination(int nextChoice, int numChosen)
  {
    // work out last sensible choice. If we have covered all
    // but one so far, this is numChoices.length - 1
    int lastChoice = numVariables - strength + numChosen;
    int newNumChosen = numChosen + 1;
    int newNext;
    int newCovers = 0;
    for (;nextChoice <= lastChoice; nextChoice = newNext)
    {
      newNext = nextChoice + 1;
      if (useCount[currentChoice[nextChoice]]++ == 1)
      {
        numCollisions++;
      }
      if (newNumChosen == strength)
      {
	// Score one if not covered yet
        if ((numCollisions == 0) && !covered.get(bitOffset))
	{
	  newCovers++;
	}
	bitOffset++;
      }
      else
      {
        newCovers += countCombination(newNext, newNumChosen);
      }
      if (--useCount[currentChoice[nextChoice]] == 1)
      {
        numCollisions--;
      }
    }
    return newCovers;
  }
  /** recursive routine called to consider all combinations
   *  of columns with the last column chosen being lastChoice,
   *  the number of columns in the recursive stack so far being
   *  numChosen
   */
  private void checkCombination(int nextChoice, int numChosen)
  {
    // work out last sensible choice. If we have covered all
    // but one so far, this is numChoices.length - 1
    int lastChoice = numVariables - strength + numChosen;
    int newNumChosen = numChosen + 1;
    int newNext;
    for (;nextChoice <= lastChoice; nextChoice = newNext)
    {
      newNext = nextChoice + 1;
      if (useCount[currentChoice[nextChoice]]++ == 1)
      {
        numCollisions++;
      }
      if (newNumChosen == strength)
      {
	// Write out this combination within the concatenated
	// bitsets, and advance the write pointer
	/*
	System.out.println("Offset " + bitOffset + " set " +
	  newSofar);
	*/
	if (numCollisions == 0)
	{
	  covered.set(bitOffset);
	}
	bitOffset++;
      }
      else
      {
        checkCombination(newNext, newNumChosen);
      }
      if (--useCount[currentChoice[nextChoice]] == 1)
      {
        numCollisions--;
      }
    }
  }
  private boolean finishedAt(int[] choice)
  {
    for (int i = 0; i < numVariables; i++)
    {
      currentChoice[i] = choice[i];
    }
    initForRecursion();
    checkCombination(0, 0);
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
    int variables = 64;
    int choices = 2;
    int strength = 2;
    long seed = 42;
    int innerGoes = 100;
    int s1 = s.length - 1;
    try
    {
      for (int i = 0; i < s.length; i++)
      {
        sp = s[i].trim();
	if ((sp.equals("-choices")) && (i < s1))
	{
	  sp = s[++i].trim();
	  choices = Integer.parseInt(sp);
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
	else if ((sp.equals("-variables")) && (i < s1))
	{
	  sp = s[++i].trim();
	  variables = Integer.parseInt(sp);
	}
	else
	{
	  System.err.println("Could not handle flag " + sp);
	  trouble = true;
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

    if (variables < strength)
    {
      System.err.println(
        "No point trying to work with less than strength points");
      trouble = true;
    }
    if (choices < strength)
    {
      System.err.println("No solution if less than strength choices");
      trouble = true;
    }
    if (trouble)
    {
      System.err.println("Args are [-choices #] [-goes #] " +
        "[-innerGoes #] [-seed #] [-strength #] [-variables #]");
      return;
    }
    System.out.println("Choices " + choices + " variables " +
      variables);
    System.out.println("MaxGoes " + maxGoes + " seed " + seed +
      " innerGoes " + innerGoes + " strength " + strength);
    AllPerms.indefiniteGenerate(choices, variables, seed, maxGoes,
      strength, innerGoes);
  }
}
