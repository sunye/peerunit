package uk.co.demon.mcdowella.stats;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

/** This class reads in a table of observations, for example the
 *  observations of birds at different sites over a period of time.
 *  In this case, each day produces records for the same set of sites,
 *  and at each site we have counts of birds of different types seen.
 *  Each line starts with a day marker, then a row marker, then
 *  a series of pairs of ID, #. For example:
 *  16Jan Pool43 Swan 3 Mallard 6
 *  # marks a comment
 *  <br>
 *  The first job of the program is to read everything in and convert
 *  to ranks. Then it can output an index for each site, based on the
 *  sum of its rankings, ready for uk.co.demon.mcdowella.stats.Columns.
 *  It can also provide the sum of the absolute differences between
 *  ranks and the mean rank, the sum of the squares of these, and use
 *  a Monte-Carlo test for the sum of the distance between the mean
 *  vector of rankings for a site and the global mean (this can't be
 *  handled by Columns, because it isn't a sum of 1-d contributions).
 *  <br>
 *  Optionally, it can ignore all but a given set of sites, or of
 *  birds, to allow you to narrow in on stuff.
 */
public class DoubleColumns
{
  public static void main(String[] s) throws IOException
  {
    SortedSet<String> keepBirdSet = new TreeSet();
    SortedSet<String> keepSiteSet = null;
    boolean outputRanks = false;
    boolean outputPosns = false;
    int goes = 0;
    int s1 = s.length - 1;
    boolean trouble = false;
    boolean distance = false;
    boolean absRank = false;
    String argNum = null;
    long seed = 42;
    try
    {
      if (s.length > 0)
      {
	for (int i = 0; i < s.length; i++)
	{
	  if ("-absRank".equals(s[i]))
	  {
	    absRank = true;
	  }
	  else if ("-distance".equals(s[i]))
	  {
	    distance = true;
	  }
	  else if ("-goes".equals(s[i]) && (i < s1))
	  {
	    argNum = s[++i].trim();
	    goes = Integer.parseInt(argNum);
	  }
	  else if ("-posns".equals(s[i]))
	  {
	    outputPosns = true;
	  }
	  else if ("-ranks".equals(s[i]))
	  {
	    outputRanks = true;
	  }
	  else if ("-seed".equals(s[i]) && (i < s1))
	  {
	    argNum = s[++i].trim();
	    seed = Long.parseLong(argNum);
	  }
	  else if (("-sites".equals(s[i])) && (i < s1))
	  {
	    i++;
	    StringTokenizer siteList = new StringTokenizer(s[i], ",");
	    keepSiteSet = new TreeSet();
	    while (siteList.hasMoreElements())
	    {
	      keepSiteSet.add(siteList.nextToken());
	    }
	  }
	  else if (s[i].startsWith("-"))
	  {
	    System.err.println("Could not handle flag " + s[i]);
	    trouble = true;
	  }
	  else
	  {
	    keepBirdSet.add(s[i].trim());
	  }
	}
      }
    }
    catch (NumberFormatException nfe)
    {
      System.err.println("Could not read number in " + argNum);
      trouble = true;
    }
    if (trouble)
    {
      System.err.println(
  "Args are [-absRank] [-distance] [-posns] [-ranks] [-sites <site>,<site>,...] [-goes #] <bird>*");
      return;
    }
    if (keepBirdSet.isEmpty())
    {
      keepBirdSet = null;
    }
    if (keepBirdSet != null)
    {
      System.out.print("# keeping only birds");
      for (String keeper: keepBirdSet)
      {
        System.out.print(' ');
        System.out.print(keeper);
      }
      System.out.println();
    }
    if (keepSiteSet != null)
    {
      System.out.print("# keeping only sites");
      for (String keeper: keepSiteSet)
      {
        System.out.print(' ');
	System.out.print(keeper);
      }
      System.out.println();
    }
    List<RowInfo> riList = new ArrayList<RowInfo>();
    BufferedReader br = new BufferedReader(new
      InputStreamReader(System.in));
    // Just read in the data
    try
    {
      for (;;)
      {
        String line = br.readLine();
	if (line == null)
	{
	  break;
	}
	int pos = line.indexOf('#');
	if (pos >= 0)
	{
	  line = line.substring(0, pos);
	}
	line = line.trim();
	if ("".equals(line))
	{ // blank, or pure comment
	  continue;
	}
	StringTokenizer st = new StringTokenizer(line);
	if (!st.hasMoreTokens())
	{ // can we be here? - blank again if so
	  continue;
	}
	String day = st.nextToken();
	if (!st.hasMoreTokens())
	{
	  throw new IllegalArgumentException("No site info after day " +
	    day);
	}
	String site = st.nextToken();
	if ((keepSiteSet != null) && !keepSiteSet.contains(site))
	{ // ignoring this site
	  continue;
	}
	Map<String, Double> numberByBird = new HashMap<String,
	  Double>();
	while (st.hasMoreTokens())
	{
	  String bird = st.nextToken();
	  if (!st.hasMoreTokens())
	  {
	    throw new IllegalArgumentException("No number after bird "
	      + bird + " day " + day + " site " + site);
	  }
	  String num = st.nextToken().trim();
	  double number;
	  try
	  {
	    number = Double.parseDouble(num);
	  }
	  catch (NumberFormatException nfe)
	  {
	    throw new IllegalArgumentException(
	      "Cannot read number from " + num + " at day " + day +
	      " site " + site + " bird " + bird);
	  }
	  if ((keepBirdSet != null) && !keepBirdSet.contains(bird))
	  {
	    continue;
	  }
	  if (numberByBird.put(bird, number) != null)
	  {
	    throw new IllegalArgumentException(
	      "Multiple counts for at day " + day +
	      " site " + site + " bird " + bird);
	  }
	}
	RowInfo ri = new RowInfo(day, site, numberByBird);
        riList.add(ri);
      }
    }
    finally
    {
      br.close();
      br = null;
    }
    // Assign reference number to days and sites
    Set<String> days = new HashSet<String>();
    Set<String> sites = new HashSet<String>();
    Set<String> birds = new HashSet<String>();
    for (RowInfo ri: riList)
    {
      String day = ri.getDay();
      String site = ri.getSite();
      days.add(day);
      sites.add(site);
      birds.addAll(ri.getBirdCounts().keySet());
    }
    IdMap dayMap = new IdMap(days);
    IdMap siteMap = new IdMap(sites);
    IdMap birdMap = new IdMap(birds);
    // Counts by day then site then bird
    int numSites = siteMap.size();
    int numBirds = birdMap.size();
    int numDays = dayMap.size();
    double[][][] countByDaySiteBird = new double[numDays][][];
    for (int i = 0; i < countByDaySiteBird.length; i++)
    {
      countByDaySiteBird[i] = new double[numSites][];
    }
    // Populate table
    for (RowInfo ri: riList)
    {
      String day = ri.getDay();
      int dayNum = dayMap.getNumber(day);
      String site = ri.getSite();
      int siteNum = siteMap.getNumber(site);
      if (countByDaySiteBird[dayNum][siteNum] != null)
      {
	throw new IllegalArgumentException("Multiple counts for day " +
	  day + " site " + site);
      }
      double[] count = new double[numBirds];
      countByDaySiteBird[dayNum][siteNum] = count;
      for (Map.Entry<String, Double> me: ri.getBirdCounts().entrySet())
      {
        count[birdMap.getNumber(me.getKey())] = me.getValue();
      }
    }
    double[] averagePositionByDay =
      new double[countByDaySiteBird.length];
    for (int i = 0; i < countByDaySiteBird.length; i++)
    {
      double[][] row = countByDaySiteBird[i];
      double total = 0.0;
      double totalTimes = 0.0;
      for (int j = 0; j < row.length; j++)
      {
	double[] allBirds = row[j];
        if (allBirds == null)
	{
	  throw new IllegalArgumentException("No info for day " +
	    dayMap.getString(i) + " site " + siteMap.getString(j));
	}
	double totalBirds = 0.0;
	for (double seen: allBirds)
	{
	  totalBirds += seen;
	}
	totalTimes += j * totalBirds;
	total += totalBirds;
      }
      if (total > 0.0)
      {
        totalTimes = totalTimes / total;
      }
      averagePositionByDay[i] = totalTimes;
    }
    // construct ranked version
    double[][][] rankByDaySiteBird =
      new double[countByDaySiteBird.length][][];
    ForRank[] fr = new ForRank[numSites];
    for (int i = 0; i < fr.length; i++)
    {
      fr[i] = new ForRank();
    }
    for (int i = 0; i < rankByDaySiteBird.length; i++)
    {
      rankByDaySiteBird[i] = new double[numSites][];
      for (int j = 0; j < numSites; j++)
      {
        rankByDaySiteBird[i][j] = new double[numBirds];
      }
      for (int j = 0; j < numBirds; j++)
      {
        for (int k = 0; k < numSites; k++)
	{
	  fr[k].set(k, countByDaySiteBird[i][k][j]);
	}
	Arrays.sort(fr);
	int first = 0;
	double val = countByDaySiteBird[i][fr[0].getElement()][j];
	for (int k = 1; k <= numSites; k++)
	{ // look at each point to see if is different from
	  // the previous value, which then ends a range of
	  // equal ranks
	  if ((k == numSites) ||
	    (countByDaySiteBird[i][fr[k].getElement()][j] != val))
	  {
	    /*
	    if (k != numSites)
	    {
	      System.out.println("Compare " +
	        countByDaySiteBird[i][fr[k].getElement()][j] +
		  " target " +
		fr[k].getElement() + " with " + val);
	    }
	    */
	    double score = 0.5 * (first + k - 1);
	    for (int l = first; l < k; l++)
	    {
	      int target = fr[l].getElement();
	      rankByDaySiteBird[i][target][j] = score;
	      /*
	      System.out.println("day " + dayMap.getString(i) +
	        " site " + siteMap.getString(target) + " bird " +
		birdMap.getString(j) + " gets score " + score);
	      */
	    }
	    first = k;
	    if (k < numSites)
	    {
	      val = countByDaySiteBird[i][fr[k].getElement()][j];
	    }
	  }
	  // System.out.println();
	}
      }
    }
    // Now output as per-column
    System.out.print("# Sites are");
    for (int i = 0; i < siteMap.size(); i++)
    {
      System.out.print(' ');
      System.out.print(siteMap.getString(i));
    }
    System.out.println();
    System.out.print("# Birds are");
    for (int i = 0; i < birdMap.size(); i++)
    {
      System.out.print(' ');
      System.out.print(birdMap.getString(i));
    }
    System.out.println();
    int n1 = numSites - 1;
    if (outputRanks)
    {
      System.out.println("# Total rank by day and site");
      for (int i = 0; i < numDays; i++)
      {
	System.out.println("# Day " + dayMap.getString(i) +
	  " avg posn " + averagePositionByDay[i]);
	for (int j = 0; j < numSites; j++)
	{
	  double total = 0.0;
	  for (int k = 0; k < numBirds; k++)
	  {
	    /*
	    System.out.println("Day " + dayMap.getString(i) + " site " +
	      siteMap.getString(j) + " bird " + birdMap.getString(k) +
	      " rank " + rankByDaySiteBird[i][j][k]);
	    */
	    total += rankByDaySiteBird[i][j][k];
	  }
	  System.out.print(total);
	  if (j < n1)
	  {
	    System.out.print(' ');
	  }
	  else
	  {
	    System.out.println();
	  }
	}
      }
    }
    if (outputPosns)
    {
      System.out.println("# Average postion by day and site");
      // Now output average position of each bird on each day
      for (int i = 0; i < numDays; i++)
      {
	System.out.println("# Day " + dayMap.getString(i));
	String sp = "";
	for (int j = 0; j < numBirds; j++)
	{
	  double totalCount = 0.0;
	  double averageRank = 0.0;
	  for (int k = 0; k < numSites; k++)
	  {
	    double here = countByDaySiteBird[i][k][j];
	    totalCount += here;
	    averageRank += here * k;
	  }
	  System.out.print(sp);
	  sp = " ";
	  if (totalCount <= 0.0)
	  {
	    System.out.print('*');
	  }
	  else
	  {
	    System.out.print(averageRank / totalCount);
	  }
	}
	System.out.println();
      }
    }
    if (distance)
    { // score for each site is the distance from that site to the
      // mean for that day, treating each site's ranks as a vector
      // This is actually bit silly, because we already know the
      // mean for each day, because we have ranked everything. So
      // this is just recoding the rank scores, which we might
      // as well do as absolute difference between rank and mid rank
      System.out.println(
        "# Distance from day's mean of rank vector by day and site");
      double[] mean = new double[numBirds];
      for (int i = 0; i < numDays; i++)
      {
	double[][] dayInfo = rankByDaySiteBird[i];
	for (int j = 0; j < numBirds; j++)
	{
	  double total = 0.0;
	  for (int k = 0; k < numSites; k++)
	  {
	    total += dayInfo[k][j];
	  }
	  mean[j] = total / numSites;
	}
	System.out.println("# day " + dayMap.getString(i));
	String sp = "";
	for (int j = 0; j < numSites; j++)
	{
	  double total = 0.0;
	  double[] siteInfo = dayInfo[j];
	  for (int k = 0; k < numBirds; k++)
	  {
	    double diff = mean[k] - siteInfo[k];
	    total += diff * diff;
	  }
	  System.out.print(sp + total);
	  sp = " ";
	}
	System.out.println();
      }
    }
    if (absRank)
    { // score for each site is the absolute diffence from mean rank
      System.out.println(
        "# Sum of absolute distances from mean rank");
      double[] mean = new double[numBirds];
      for (int i = 0; i < numDays; i++)
      {
	double[][] dayInfo = rankByDaySiteBird[i];
	System.out.println("# day " + dayMap.getString(i));
	String sp = "";
	double meanRank = (numSites - 1.0) * 0.5;
	for (int j = 0; j < numSites; j++)
	{
	  double total = 0.0;
	  double[] siteInfo = dayInfo[j];
	  for (int k = 0; k < numBirds; k++)
	  {
	    double diff = siteInfo[k] - meanRank;
	    total += Math.abs(diff);
	  }
	  System.out.print(sp + total);
	  sp = " ";
	}
	System.out.println();
      }
    }
    if (goes > 0)
    {
      // Work out total for each site, then squared difference
      // from known mean (because we know mean from ranking)
      // Also print out summed ranks
      McCount[] counts = new McCount[numSites];
      double expectedColSum = numDays * (numSites - 1.0) * 0.5;
      for (int i = 0; i < numSites; i++)
      {
	System.out.println("# summed ranks for " +
	  siteMap.getString(i));
	String sp = "";
        counts[i] = new McCount();
	double total = 0.0;
	for (int j = 0; j < numBirds; j++)
	{
	  double sumHere = 0.0;
	  for (int k = 0; k < numDays; k++)
	  {
	    sumHere += rankByDaySiteBird[k][i][j];
	  }
	  System.out.print(sp + sumHere);
	  sp = " ";
	  double diff = sumHere - expectedColSum;
	  total += diff * diff;
	}
	System.out.println();
	counts[i].sample(total);
      }
      // Now randomly sample one site from each day to draw from
      // distribution under the null hypothesis that each site
      // is the same on any particular day
      Random ran = new Random(seed);
      double[][] poolChoice = new double[numDays][];
      for (int i = 0; i < goes; i++)
      {
	for (int j = 0; j < numDays; j++)
	{
	  int randomSite = ran.nextInt(numSites);
	  poolChoice[j] = rankByDaySiteBird[j][randomSite];
	}
        double total = 0.0;
	for (int j = 0; j < numBirds; j++)
	{
	  double sumHere = 0.0;
	  for (int k = 0; k < numDays; k++)
	  {
	    sumHere += poolChoice[k][j];
	  }
	  double diff = sumHere - expectedColSum;
	  total += diff * diff;
	}
	for (int j = 0; j < numSites; j++)
	{
	  counts[j].sample(total);
	}
      }
      System.out.println("# " + goes + " random goes with seed " +
        seed);
      for (int i = 0; i < numSites; i++)
      {
        System.out.println("# site " + siteMap.getString(i) + " " +
	  counts[i].toString());
      }
    }
  }
  /** Class to hold info read in at first. Day and Site, plus
   *  map of bird type => count
   */
  private static class RowInfo
  {
    private final String day;
    String getDay()
    {
      return day;
    }
    private final String site;
    String getSite()
    {
      return site;
    }
    private final SortedMap<String, Double> birdCounts;
    SortedMap<String, Double> getBirdCounts()
    {
      return birdCounts;
    }
    RowInfo(String forDay, String forSite, Map<String, Double>
      counts)
    {
      day = forDay;
      site = forSite;
      birdCounts = new TreeMap<String, Double>(counts);
    }
  }
  /** Class used to convert between string and integer */
  private static class IdMap
  {
    private final Map<String, Integer> intByString =
      new HashMap<String, Integer>();
    private final String[] stringByInt;
    IdMap(Collection<String> sc)
    {
      SortedSet<String> ss = new TreeSet<String>(sc);
      int wp = 0;
      stringByInt = new String[ss.size()];
      for (String s: ss)
      {
        stringByInt[wp] = s;
	intByString.put(s, wp);
	wp++;
      }
    }
    String getString(int x)
    {
      return stringByInt[x];
    }
    int getNumber(String s)
    {
      return intByString.get(s);
    }
    int size()
    {
      return stringByInt.length;
    }
  }
  /** Class used to rank */
  private static class ForRank implements Comparable<ForRank>
  {
    private double score;
    private int element;
    void set(int index, double val)
    {
      element = index;
      score = val;
    }
    int getElement()
    {
      return element;
    }
    public int compareTo(ForRank fr)
    {
      if (score < fr.score)
      {
        return -1;
      }
      if (score > fr.score)
      {
        return 1;
      }
      if (element < fr.element)
      {
        return -1;
      }
      if (element > fr.element)
      {
        return 1;
      }
      return 0;
    }
  }
}
