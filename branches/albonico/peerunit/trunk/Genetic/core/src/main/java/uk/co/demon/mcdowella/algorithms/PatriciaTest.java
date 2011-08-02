package uk.co.demon.mcdowella.algorithms;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import uk.co.demon.mcdowella.algorithms.Patricia;
import java.io.PrintStream;
import java.util.Random;
import java.util.TreeMap;

/** This class exists to test the Patricia class
*/
public class PatriciaTest
{
  public static void main(String[] s)
  {
    long seed = 42;
    int tabSize = 1000;
    boolean trouble = false;
    OrderCheck[] orderCheckArray = {new IntCheck(), new LongCheck(),
      new FloatCheck(), new DoubleCheck()};
    int goes = orderCheckArray.length;

    int argp = 0;
    StringBuffer sbb = new StringBuffer();
    Patricia.appendBinDouble(sbb, -1.0001);
    Patricia.printHexString(System.out, sbb.toString());
    System.out.println();
    sbb = new StringBuffer();
    Patricia.appendBinDouble(sbb, -1.0);
    Patricia.printHexString(System.out, sbb.toString());
    System.out.println();
    sbb = new StringBuffer();
    Patricia.appendBinDouble(sbb, 1.0);
    Patricia.printHexString(System.out, sbb.toString());
    System.out.println();
    try
    {
      for (argp = 0; argp < s.length; argp++)
      {
	if ("-goes".equals(s[argp]) && (argp < s.length - 1))
	{
	  argp++;
	  goes = Integer.parseInt(s[argp]);
	}
	else if ("-seed".equals(s[argp]) && (argp < s.length - 1))
	{
	  argp++;
	  seed = Long.parseLong(s[argp]);
	}
	else if ("-tabSize".equals(s[argp]) && (argp < s.length - 1))
	{
	  argp++;
	  tabSize = Integer.parseInt(s[argp]);
	}
	else
	{
	  System.err.println("Cannot handle flag " + s[argp]);
	  trouble = true;
	}
      }
    }
    catch(NumberFormatException nf)
    {
      System.err.println("Cannot read number in " + s[argp]);
      trouble = true;
    }
    if(trouble)
    {
      System.err.println("Usage is PatriciaTest [-goes #] [-seed #] " +
        "[-tabSize #]");
      System.exit(1);
    }

    System.out.println("goes = " + goes + " seed = " + seed +
      " tabSize = " + tabSize);

    for (int go = 0; go < goes; go++)
    {
      // Create new seed for each pass so you can repeat a failing
      // pass easily if you have to
      long realSeed = seed + go;
      Random r = new Random(realSeed);
      System.err.println("Go " + go + " seed " + realSeed);
      Patricia p = new Patricia();
      Map m = new HashMap();
      // Fill up map, checking string encoding
      for (int i = 0; i < tabSize; i++)
      {
	// p.printMap(System.out);
        String key = randomString(r);
        Integer val = new Integer(i);
	StringBuffer sb = new StringBuffer();
	Patricia.appendTopBitString(sb, key);
	String processedKey = sb.toString();
	Patricia.appendTopBitString(sb, key);
	String twice = sb.toString();
	int offset[] = new int[1];
	String backKey = Patricia.getTopBitString(twice, offset);
	if (!backKey.equals(key))
	{
	  System.out.print("Key: ");
	  Patricia.printHexString(System.out, key);
	  System.out.print(" Processed: ");
	  Patricia.printHexString(System.out, processedKey);
	  System.out.println();
	  throw new IllegalStateException(
	    "Key does not translate back");
	}
	backKey = Patricia.getTopBitString(twice, offset);
	if (!backKey.equals(key))
	{
	  throw new IllegalStateException(
	    "Key does not translate back second time");
	}
	if (offset[0] != twice.length())
	{
	  throw new IllegalStateException("Length mismatch");
	}
	p.put(processedKey, val);
	// p.printMap(System.out);
	Object back = p.get(processedKey,
	  Patricia.binStringLen(key.length()));
	if (val != back)
	{
	  System.out.print("processedKey=");
	  Patricia.printHexString(System.out, processedKey);
	  System.out.println("val=" + val);
	  // p.printMap(System.out);
	  throw new IllegalStateException("Could not get back: " +
	    back);
	}
	if (p.get(processedKey) != val)
	{
	  throw new IllegalStateException("Could not get second time");
	}
	m.put(key, val);
      }
      // Check map against HashMap
      for (Iterator i = m.entrySet().iterator(); i.hasNext();)
      {
        Map.Entry me = (Map.Entry)i.next();
	StringBuffer sb = new StringBuffer();
	String key = (String)me.getKey();
	Patricia.appendTopBitString(sb, key);
	String processedKey = sb.toString();
	Object o = p.get(processedKey);
	if (o != me.getValue())
	{
	  p.printMap(System.out);
	  System.out.println("o = " + o);
	  System.out.print("key = ");
	  Patricia.printHexString(System.out, processedKey);
	  System.out.println();
	  throw new IllegalStateException("could not retrieve at end");
	}
      }
      int size = p.size();
      if (size != m.size())
      {
        throw new IllegalStateException("Size mismatch");
      }
      // Check delete, giving it the opportunity to mess up
      // the data structure
      int toRemove = size >> 2;
      for (int i = 0; i < toRemove; i++)
      {
	String key = (String)m.keySet().iterator().next();
        StringBuffer sb = new StringBuffer();
	Patricia.appendTopBitString(sb, key);
	String encoded = sb.toString();
	Object onRemove = p.remove(encoded);
        if (onRemove != m.remove(key))
	{
	  throw new IllegalStateException("Could not remove");
	}
	Object second = p.remove(encoded);
	if (second != null)
	{
	  System.out.println("First remove: " + onRemove);
	  System.out.println("Second remove: " + second);
	  throw new IllegalStateException("second remove");
	}
      }
      int seen = 0;
      Iterator allEntries = p.getEntries("", 0, 0);
      String past = null;
      int[] offset = new int[1];
      int maxWork = 0;
      int prefixMatches = 0;
      // Go through the remaining map in sorted order
      while(allEntries.hasNext())
      {
	Iterator again = p.getEntries("", 0, seen);
        seen++;
	Map.Entry me = (Map.Entry)allEntries.next();
	Map.Entry fromCount = (Map.Entry)again.next();
	if (!me.equals(fromCount))
	{
	  throw new IllegalStateException("Mismatch with count");
	}
	String key = (String)me.getKey();
	offset[0] = 0;
	String decodedKey = Patricia.getTopBitString(key, offset);
	if (past != null && past.compareTo(decodedKey) >= 0)
	{
	  throw new IllegalStateException("Out of order");
	}
	past = decodedKey;
	if (me.getValue() != m.get(decodedKey))
	{
	  throw new IllegalStateException("Value mismatch");
	}
	long bits = Patricia.binStringLen(decodedKey.length());
	Iterator longMatch = p.getEntries(key, bits, 0);
	Map.Entry res = (Map.Entry)longMatch.next();
	if (!res.equals(me))
	{
	  throw new IllegalArgumentException("Iterator result mismatch");
	}
	if (longMatch.hasNext())
	{
	  throw new IllegalArgumentException("result has next");
	}
	int matches = p.containsKeys(key, bits);
	if (matches != 1)
	{
	  throw new IllegalStateException("Count mismatch");
	}
	// Check prefix matches
	if (maxWork < size)
	{
	  prefixMatches++;
	  long shortMatch = r.nextInt((int)bits);
	  String subKey = key.substring(0,
	    (int)((shortMatch + 0xf) >> 4));
	  matches = p.containsKeys(subKey, shortMatch);
	  Iterator allMatches = p.getEntries(subKey, shortMatch, 0);
	  int seenShort = 0;
	  for(;allMatches.hasNext();)
	  {
	    Map.Entry entry = (Map.Entry)allMatches.next();
	    String keyHere = (String)entry.getKey();
	    long diff = Patricia.firstDifference(keyHere, key,
	      Long.MAX_VALUE);
	    if (diff < shortMatch)
	    {
	      throw new IllegalStateException("cut mismatch");
	    }
	    Iterator into = p.getEntries(subKey, shortMatch, seenShort);
	    seenShort++;
	    Map.Entry intoEntry = (Map.Entry)into.next();
	    if (!intoEntry.equals(entry))
	    {
	      throw new IllegalStateException("Into entry mismatch");
	    }
	  }
	  if (seenShort != matches)
	  {
	    throw new IllegalStateException("Match count mismatch");
	  }
	  maxWork += matches;
	}
      }
      System.out.println("Found " + maxWork + " matches in " + seen +
        " searches with prefix keys");
      try
      {
        allEntries.next();
	throw new IllegalStateException("Iterator did not throw");
      }
      catch(NoSuchElementException e)
      {
      }
      if (seen != p.size())
      {
        throw new IllegalStateException(
	  "iterator size mismatch: seen " + seen + " of " + p.size());
      }
      // delete down to nothing
      for(;;)
      {
        Iterator i = m.keySet().iterator();
	if (!i.hasNext())
	{
	  Iterator pi = p.getEntries("", 0, 0);
	  if (pi.hasNext())
	  {
	    throw new IllegalStateException("Iterator says still lef");
	  }
	  break;
	}
	String key = (String)i.next();
        StringBuffer sb = new StringBuffer();
	Patricia.appendTopBitString(sb, key);
	String encoded = sb.toString();
	Object onRemove = p.remove(encoded);
        if (onRemove != m.remove(key))
	{
	  throw new IllegalStateException("Could not remove");
	}
	Object second = p.remove(encoded);
	if (second != null)
	{
	  System.out.println("First remove: " + onRemove);
	  System.out.println("Second remove: " + second);
	  throw new IllegalStateException("second remove");
	}
      }
      OrderCheck oc = orderCheckArray[go % orderCheckArray.length];
      for (int i = 0; i < tabSize; i++)
      {
        String key = oc.getValue(r);
	p.put(key, null);
      }
      p.put(oc.getMinValue(), null);
      p.put(oc.getMaxValue(), null);
      /*
      p.put(oc.getPosInf(), null);
      p.put(oc.getNegInf(), null);
      */
      String prev = null;
      for (Iterator i = p.getEntries("", 0, 0); i.hasNext();)
      {
	Map.Entry me = (Map.Entry)i.next();
        String key = (String)me.getKey();
	if ((prev != null) && (oc.compareEncoded(prev, key) >= 0))
	{
	  throw new IllegalStateException("Encoded order wrong with " +
	    oc);
	}
	prev = key;
      }
      p.clear();
      if (p.size() != 0)
      {
        throw new IllegalStateException("clear/size mismatch");
      }
      // Timing tests: random strings
      String data[] = new String[tabSize];
      for (int i = 0; i < tabSize; i++)
      {
         data[i] = randomString(r);
      }
      doTiming(data, "short random strings", System.out);
      // Timing tests: SNMP prefixed strings
      for (int i = 0; i < tabSize; i++)
      {
	StringBuffer sb = new StringBuffer();
        sb.append("1.2.3.4.5.6.7.8.9.10");
	for (int j = 0; j < 10; j++)
	{
	  sb.append('.');
	  sb.append(r.nextInt(10));
	}
	data[i] = sb.toString();
      }
      doTiming(data, "SNMP-style prefixed strings", System.out);
    }
  }
  /** Generate a random string */
  private static String randomString(Random r)
  {
    StringBuffer sb = new StringBuffer();
    for(;;)
    {
      if (r.nextInt(10) == 0)
      {
        break;
      }
      sb.append((char)r.nextInt());
    }
    return sb.toString();
  }
  /** Produce timing info */
  private static void doTiming(String[] data, String message,
    PrintStream ps)
  {
    ps.println(message);
    Patricia p = new Patricia();
    Swatch prefixTime = new Swatch();
    prefixTime.start();
    String[] prefixed = new String[data.length];
    for (int i = 0; i < data.length; i++)
    {
      StringBuffer sb = new StringBuffer();
      Patricia.appendTopBitString(sb, data[i]);
      prefixed[i] = sb.toString();
    }
    prefixTime.stop();
    ps.println("Prefix time is " + prefixTime);
    /*
    prefixTime = new Swatch();
    prefixTime.start();
    for (int i = 0; i < data.length; i++)
    {
      simpleEncode(data[i]);
    }
    prefixTime.stop();
    ps.println("Simple prefix time is " + prefixTime);
    */
    Swatch pat = new Swatch();
    pat.start();
    for (int i = 0; i < data.length; i++)
    {
      p.put(prefixed[i], data[i]);
    }
    for (int i = 0; i < data.length; i++)
    {
      p.get(data[i]);
    }
    pat.stop();
    ps.print("Patricia: ");
    ps.println(pat);
    Swatch hash = new Swatch();
    hash.start();
    Map h = new HashMap();
    for (int i = 0; i < data.length; i++)
    {
      h.put(data[i], data[i]);
    }
    for (int i = 0; i < data.length; i++)
    {
      h.get(data[i]);
    }
    hash.stop();
    ps.print("Hash: ");
    ps.println(hash);
    Swatch tree = new Swatch();
    tree.start();
    Map t = new TreeMap();
    for (int i = 0; i < data.length; i++)
    {
      t.put(data[i], data[i]);
    }
    for (int i = 0; i < data.length; i++)
    {
      t.get(data[i]);
    }
    tree.stop();
    ps.print("Tree: ");
    ps.println(tree);
  }
  /** used to check out ordering of int/long/float/double */
  private static interface OrderCheck
  {
    /** @return an encoded String */
    String getValue(Random r);
    /** reconvert two encoded values and check */
    int compareEncoded(String a, String b);
    /** maximum possible value */
    String getMaxValue();
    /** minimum possible value */
    String getMinValue();
    /** positive infinity if possible, or max value */
    String getPosInf();
    /** negative infinity if possible, or min value */
    String getNegInf();
  }
  private static class IntCheck implements OrderCheck
  {
    public String getValue(Random r)
    {
      return sendValue(r.nextInt());
    }
    public String getMaxValue()
    {
      return sendValue(Integer.MAX_VALUE);
    }
    public String getMinValue()
    {
      return sendValue(Integer.MIN_VALUE);
    }
    public String getPosInf()
    {
      return getMaxValue();
    }
    public String getNegInf()
    {
      return getMinValue();
    }
    private String sendValue(int i)
    {
      StringBuffer sb = new StringBuffer();
      Patricia.appendBinInt(sb, i);
      String result = sb.toString();
      if (result.length() != Patricia.BIN_INT_CHARS)
      {
        throw new IllegalStateException("Length mismatch");
      }
      int back = Patricia.getBinInt(result, 0);
      if (back != i)
      {
        throw new IllegalStateException("back mismatch");
      }
      return result;
    }
    public int compareEncoded(String a, String b)
    {
      int offset[] = new int[1];
      int ai = Patricia.getBinInt(a, 0);
      int bi = Patricia.getBinInt(b, 0);
      if (ai < bi)
      {
        return -1;
      }
      if (ai > bi)
      {
        return 1;
      }
      return 0;
    }
  }
  private static class LongCheck implements OrderCheck
  {
    public String getValue(Random r)
    {
      return sendValue(r.nextLong());
    }
    public String getMaxValue()
    {
      return sendValue(Long.MAX_VALUE);
    }
    public String getMinValue()
    {
      return sendValue(Long.MIN_VALUE);
    }
    public String getPosInf()
    {
      return getMaxValue();
    }
    public String getNegInf()
    {
      return getMinValue();
    }
    private String sendValue(long i)
    {
      StringBuffer sb = new StringBuffer();
      Patricia.appendBinLong(sb, i);
      String result = sb.toString();
      if (result.length() != Patricia.BIN_LONG_CHARS)
      {
        throw new IllegalStateException("Length mismatch");
      }
      long back = Patricia.getBinLong(result, 0);
      if (back != i)
      {
        throw new IllegalStateException("back mismatch");
      }
      return result;
    }
    public int compareEncoded(String a, String b)
    {
      long ai = Patricia.getBinLong(a, 0);
      long bi = Patricia.getBinLong(b, 0);
      if (ai < bi)
      {
        return -1;
      }
      if (ai > bi)
      {
        return 1;
      }
      return 0;
    }
  }
  private static class FloatCheck implements OrderCheck
  {
    public String getValue(Random r)
    {
      return sendValue((float)r.nextGaussian());
    }
    public String getMaxValue()
    {
      return sendValue(Float.MAX_VALUE);
    }
    public String getMinValue()
    {
      return sendValue(Float.MIN_VALUE);
    }
    public String getPosInf()
    {
      return sendValue(Float.POSITIVE_INFINITY);
    }
    public String getNegInf()
    {
      return sendValue(Float.NEGATIVE_INFINITY);
    }
    private String sendValue(float i)
    {
      StringBuffer sb = new StringBuffer();
      Patricia.appendBinFloat(sb, i);
      String result = sb.toString();
      if (result.length() != Patricia.BIN_FLOAT_CHARS)
      {
        throw new IllegalStateException("Length mismatch");
      }
      float back = Patricia.getBinFloat(result, 0);
      if (back != i)
      {
        throw new IllegalStateException("back mismatch");
      }
      return result;
    }
    public int compareEncoded(String a, String b)
    {
      float ai = Patricia.getBinFloat(a, 0);
      float bi = Patricia.getBinFloat(b, 0);
      if (ai < bi)
      {
        return -1;
      }
      if (ai > bi)
      {
        return 1;
      }
      return 0;
    }
  }
  private static class DoubleCheck implements OrderCheck
  {
    public String getValue(Random r)
    {
      double d = r.nextGaussian();
      // System.out.println("send " + d);
      return sendValue(d);
    }
    public String getMaxValue()
    {
      return sendValue(Double.MAX_VALUE);
    }
    public String getMinValue()
    {
      return sendValue(Double.MIN_VALUE);
    }
    public String getPosInf()
    {
      return sendValue(Double.POSITIVE_INFINITY);
    }
    public String getNegInf()
    {
      return sendValue(Double.NEGATIVE_INFINITY);
    }
    private String sendValue(double i)
    {
      StringBuffer sb = new StringBuffer();
      Patricia.appendBinDouble(sb, i);
      String result = sb.toString();
      if (result.length() != Patricia.BIN_DOUBLE_CHARS)
      {
        throw new IllegalStateException("Length mismatch");
      }
      double back = Patricia.getBinDouble(result, 0);
      if (back != i)
      {
        throw new IllegalStateException("back mismatch");
      }
      return result;
    }
    public int compareEncoded(String a, String b)
    {
      double ai = Patricia.getBinDouble(a, 0);
      double bi = Patricia.getBinDouble(b, 0);
      if (ai < bi)
      {
        return -1;
      }
      if (ai > bi)
      {
        return 1;
      }
      return 0;
    }
  }
  /** Check speed of simpler but less space-efficient
   * encoding rule: faster but not enough to make a
   * real difference
   */
  static String simpleEncode(String s)
  {
    int l = s.length();
    char[] x = new char[(l << 1) + 1];
    int wp = 0;
    for (int i = 0; i < l; i++)
    {
      x[wp++] = 1;
      x[wp++] = s.charAt(i);
    }
    x[wp++] = 0;
    return new String(x);
  }
}
