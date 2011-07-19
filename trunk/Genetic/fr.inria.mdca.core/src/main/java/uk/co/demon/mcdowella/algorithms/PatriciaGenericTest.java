package uk.co.demon.mcdowella.algorithms;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import uk.co.demon.mcdowella.algorithms.PatriciaGeneric;
import java.io.PrintStream;
import java.util.Random;
import java.util.TreeMap;

/** This class exists to test the PatriciaGeneric class
*/
public class PatriciaGenericTest
{
  public static void main(String[] s)
  {
    long seed = 42;
    int tabSize = 1000;
    boolean trouble = false;
    int goes = 10;
    int argp = 0;
    boolean skip = false;
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
	else if ("-skip".equals(s[argp]))
	{
	  skip = true;
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
      if (!skip)
      {
	System.err.println("Go " + go + " seed " + realSeed);
	PatriciaGeneric<PatriciaGeneric.StringKey, Integer,
	  PatriciaGeneric.DummyChildInfo<PatriciaGeneric.StringKey,
	    Integer>> p =
	  new PatriciaGeneric<PatriciaGeneric.StringKey, Integer,
	    PatriciaGeneric.DummyChildInfo<PatriciaGeneric.StringKey,
	      Integer>>(new PatriciaGeneric.DummyInfoFactory<
		PatriciaGeneric.StringKey, Integer>());
	Map<String, Integer> m = new HashMap<String, Integer>();
	// Fill up map, checking string encoding
	for (int i = 0; i < tabSize; i++)
	{
	  // p.printMap(System.out);
	  String key = randomString(r);
	  PatriciaGeneric.StringKey sk =
	    new PatriciaGeneric.StringKey(key);
	  Integer val = new Integer(i);
	  p.put(sk, val);
	  sk = new PatriciaGeneric.StringKey(key);
	  // p.printMap(System.out);
	  Object back = p.get(sk);
	  if (val != back)
	  {
	    System.out.print("key=");
	    Patricia.printHexString(System.out, key);
	    System.out.println("val=" + val);
	    // p.printMap(System.out);
	    throw new IllegalStateException("Could not get back: " +
	      back);
	  }
	  sk = new PatriciaGeneric.StringKey(key);
	  if (p.get(sk) != val)
	  {
	    throw new IllegalStateException("Could not get second time");
	  }
	  m.put(key, val);
	}
	// Check map against HashMap
	for (Iterator<Map.Entry<String, Integer>> i =
	  m.entrySet().iterator(); i.hasNext();)
	{
	  Map.Entry me = (Map.Entry)i.next();
	  String key = (String)me.getKey();
	  PatriciaGeneric.StringKey sk =
	    new PatriciaGeneric.StringKey(key);
	  Object o = p.get(sk);
	  if (o != me.getValue())
	  {
	    p.printMap(System.out);
	    System.out.println("o = " + o);
	    System.out.print("key = ");
	    Patricia.printHexString(System.out, key);
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
	  PatriciaGeneric.StringKey sk =
	    new PatriciaGeneric.StringKey(key);
	  Object onRemove = p.remove(sk);
	  if (onRemove != m.remove(key))
	  {
	    throw new IllegalStateException("Could not remove");
	  }
	  Object second = p.remove(sk);
	  if (second != null)
	  {
	    System.out.println("First remove: " + onRemove);
	    System.out.println("Second remove: " + second);
	    throw new IllegalStateException("second remove");
	  }
	}
	int seen = 0;
	Iterator<Map.Entry<PatriciaGeneric.StringKey, Integer>>
	  allEntries = p.iterator();
	String past = null;
	int[] offset = new int[1];
	int maxWork = 0;
	int prefixMatches = 0;
	// Go through the remaining map in sorted order
	while(allEntries.hasNext())
	{
	  seen++;
	  Map.Entry<PatriciaGeneric.StringKey, Integer> me =
	    allEntries.next();
	  String key = me.getKey().toString();
	  offset[0] = 0;
	  if ((past != null) && (past.compareTo(key) >= 0))
	  {
	    throw new IllegalStateException("Out of order");
	  }
	  past = key;
	  if (me.getValue() != m.get(key))
	  {
	    throw new IllegalStateException("Value mismatch");
	  }
	}
	try
	{
	  allEntries.next();
	  throw new IllegalStateException("Iterator did not throw");
	}
	catch (NoSuchElementException nsee)
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
	  Iterator<String> i = m.keySet().iterator();
	  if (!i.hasNext())
	  {
	    Iterator<Map.Entry<PatriciaGeneric.StringKey, Integer>> pi =
	      p.iterator();
	    if (pi.hasNext())
	    {
	      throw new IllegalStateException("Iterator says still lef");
	    }
	    break;
	  }
	  String key = i.next();
	  PatriciaGeneric.StringKey sk =
	    new PatriciaGeneric.StringKey(key);
	  Integer onRemove = p.remove(sk);
	  Integer mapRemove = m.remove(key);
	  // System.out.println("Removed " + onRemove + " to match " +
	  //   mapRemove);
	  if (onRemove != mapRemove)
	  {
	    throw new IllegalStateException("Could not remove");
	  }
	  Object second = p.remove(sk);
	  if (second != null)
	  {
	    System.out.println("First remove: " + onRemove);
	    System.out.println("Second remove: " + second);
	    throw new IllegalStateException("second remove");
	  }
	}
	p.clear();
	if (p.size() != 0)
	{
	  throw new IllegalStateException("clear/size mismatch");
	}
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
    PatriciaGeneric<PatriciaGeneric.StringKey, String,
      PatriciaGeneric.DummyChildInfo<PatriciaGeneric.StringKey, 
        String>> p =
      new PatriciaGeneric<PatriciaGeneric.StringKey, String,
        PatriciaGeneric.DummyChildInfo<PatriciaGeneric.StringKey,
	  String>>(new PatriciaGeneric.DummyInfoFactory<
	    PatriciaGeneric.StringKey, String>());
    Swatch pat = new Swatch();
    /*
    PatriciaGeneric.StringKey skAl[] =
      new PatriciaGeneric.StringKey[data.length];
    for (int i = 0; i < data.length; i++)
    {
      skAl[i] = new PatriciaGeneric.StringKey(data[i]);
    }
    */
    pat.start();
    {
      for (int i = 0; i < data.length; i++)
      {
	p.put(new PatriciaGeneric.StringKey(data[i]), data[i]);
	// p.put(skAl[i], data[i]);
      }
      for (int i = 0; i < data.length; i++)
      {
	p.get(new PatriciaGeneric.StringKey(data[i]));
	// p.get(skAl[i]);
      }
    }
    pat.stop();
    ps.print("Patricia: ");
    ps.println(pat);
    Swatch hash = new Swatch();
    hash.start();
    Map<String, String> h = new HashMap<String, String>();
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
    Map<String, String> t = new TreeMap<String, String>();
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
}
