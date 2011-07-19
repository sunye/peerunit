package uk.co.demon.mcdowella.stats;

import java.util.Arrays;
import java.util.Random;

/** Find examples of task lists with non-obvious best schedules */
public class ElapsedExample
{
  public static void main(String[] s)
  {
    int length = 3;
    Random r = new Random(42);
    int[] schedule = new int[length * 3];
    int[] bestSchedule = new int[schedule.length];
    double[] probs = new double[schedule.length];
    double percentile = 0.95;
    ElapsedDriver.do3Point(length, probs);
    for (int go = 0;;go++)
    {
      // System.out.println("Go " + go);
      ElapsedDriver.fillIn(length, schedule, r);
      int bestSimple = Integer.MAX_VALUE;
      int[] simple = new int[4];
      // Work out best answer for cheap deterministic strategies
      for (int i = 0; i < 4; i++)
      {
	if (i == 3)
	{
	  ElapsedDriver.sortByRange(schedule);
	}
	else
	{
	  ElapsedDriver.sortByOffset(schedule, i);
	}
	int elapsed = ElapsedDriver.getCalculatedPercentile(length,
	  schedule, probs, percentile);
	simple[i] = elapsed;
        if (elapsed < bestSimple)
	{
	  bestSimple = elapsed;
	}
      }
      int bestAnswer = ElapsedDriver.getBestAnswer(length,
        schedule, probs, percentile, 0, length, bestSchedule);
      if (bestAnswer < bestSimple)
      {
        System.out.println("Non-obvious best schedule " +
	  Arrays.toString(bestSchedule));
	System.out.print(percentile + " percentile Durations:");
	for (int i = 0; i < simple.length; i++)
	{
	  System.out.print(' ');
	  System.out.print(simple[i]);
	}
	System.out.print(' ');
	System.out.println(bestAnswer);
      }
    }
  }
}
