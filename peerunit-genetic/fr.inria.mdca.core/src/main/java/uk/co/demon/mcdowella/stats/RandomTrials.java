package uk.co.demon.mcdowella.stats;
import java.util.Arrays;
import java.util.Random;
/** Do random trials and see what the closest is */
public class RandomTrials
{
  public static void main(String[] s)
  {
    Random r = new Random(420);
    // int mask = 0x3ff; // 10 bits => 1024 choices
    int mask = 0xff; // 10 bits => 1024 choices
    int goes = 100000000;
    // int[] samples = new int[6];
    int[] samples = new int[4];
    // int[] counts = new int[11];
    int[] counts = new int[9];
    Deviant d = new Deviant();
    for (int go = 0; go < goes; go++)
    {
      int minDistance = Integer.MAX_VALUE;
      for (int i = 0; i < samples.length; i++)
      {
	int here;
	if (true)
	{
	  here = r.nextInt() & mask;
	}
	else
	{ // not quite even but who cares?
	  int base = i * 171;
	  int top = (i + 1) * 171;
	  if (top > 1024)
	  {
	    top = 1024;
	  }
	  here = base + r.nextInt(top - base);
	}
        samples[i] = here;
	for (int j = 0; j < i; j++)
	{
	  int distance = Integer.bitCount(here ^ samples[j]);
	  if (distance < minDistance)
	  {
	    minDistance = distance;
	  }
	}
      }
      counts[minDistance]++;
      d.sample(minDistance);
    }
    System.out.println(d);
    System.out.println(goes);
    System.out.println(Arrays.toString(counts));
  }
}
