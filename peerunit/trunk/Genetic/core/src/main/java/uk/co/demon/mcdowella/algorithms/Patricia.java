package uk.co.demon.mcdowella.algorithms;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.io.PrintStream;

/** 
<p>
This class provides an implementation of Patricia: an
algorithm described in "Handbook of Algorithms and Data
Structures" by Gonnet and Baeza-Yates. It allows you to
look up items in a dictionary
indexed by strings. It uses a binary tree to store the dictionary.
The depth of this tree is at most the length of the longest key 
(in bits). Here
I treat a key as a sequence of bits, presented in a Java String.
</p><p>
Patricia
demands that no key be a prefix of any other tree. You can arrange
this, for example, by prefixing each string with an encoded
length (Although this does muck up string order), or by
zero-terminating the string if the zero character it cannot otherwise
occur, or by setting the
top bit of all but the last char, if top bits are always otherwise 
clear. Routines are provided for
translating a string of chars to a string of 17-bit characters
with the top bit set in all, followed by a single zero bit, packed
contiguously into a Java string. This avoids
prefixing while preserving order, but takes time - more than the
Patricia routines themselves. Routines are also provided to turn
ints, floats, longs, and doubles into strings in ways that preserve
their order.
</p><p>
The big advantage of Patricia over, for instance, TreeMap, is that
each comparison made as you navigate the tree is a comparison of a
single bit, not a full string compare, whose cost increases with
the length of the keys. In practice, however, the gain is not enormous
(perhaps a factor of two) and can easily be swallowed up in any encoding
necessary to ensure that the keys are not prefixes of each other.
I have also added routines to keep track
of the number of entries stored below each node, allowing you to
efficiently retrieve entries by position: e.g. the 5th key-value
pair, or the 3rd key-value pair with the key having "fred" as a
prefix.
</p>
*/

public class Patricia
{
  // Table mapping the bytes 0..255 to the bit number of the
  // lowest numbered set bit, counting the msb as bit 0
  // (need to do this to get lexicographical order to coincide
  // with order of binary encoded integers)
  static private int[] tab;
  static
  {
    tab = new int[256];
    tab[0] = Integer.MIN_VALUE; // don't access!
    for (int i = 0; i < 8; i++)
    {
      int first = 1 << i;
      int past = first + first;
      for (int j = first; j < past; j++)
      {
        tab[j] = 7 - i;
      }
    }
  }
  /** @return a bit. Bits need to be numbered starting at the msb
   * so that string order coincides with binary integer order 
   */
  private static int getBit(String s, long index)
  {
    int i = (int)(index >> 4); // Java chars are 16 bits long
    return (s.charAt(i) >> (0xf - (index & 0xf))) & 1;
  }
  /** Find the leftmost bit position that shows a difference
   *  between two strings.
   *  @return the bit index of the first difference between two
   *  Strings within maxCheck bits, or a value >= maxCheck if no 
   *  difference.
   *  @param a a String of two to compare to find the first difference
   *  @param b a String of two to compare to find the first difference
   *  @param maxCheck the maximum number of bits to check for 
   *  differences
   */
  public static long firstDifference(String a, String b, long maxCheck)
  {
    int i = 0;
    char xc;
    int pastA = a.length();
    int pastB = b.length();
    int min;
    if (pastA < pastB)
    {
      min = pastA;
    }
    else
    {
      min = pastB;
    }
    int maxChars = (int)((maxCheck + 0xf) >> 4);
    if (min > maxChars)
    {
      min = maxChars;
    }
    for(;;i++)
    {
      if (i >= min)
      {
	return maxCheck;
      }
      char ca = a.charAt(i);
      char cb = b.charAt(i);
      xc = (char)(ca ^ cb);
      if (xc != 0)
      {
        break;
      }
    }
    long base = ((long)i) << 4;
    if ((xc & 0xff00) != 0)
    {
      return base + tab[xc >> 8];
    }
    return base + 8 + tab[xc];
  }

  /** abstract class of all nodes in the Patricia tree */
  private abstract static class Node
  {
   /** @return a child giving the next place to go for the given key
    * or null
    */
   abstract Node next(String s, long len);
   /**
    * the parent or null
    */
   InternalNode parent;
   /** The number of child DataNodes, counting the
    * current node if it is a DataNode
    */
   abstract int getChildren();
   /** return the lowest existing child, if any */
   abstract Node firstChild();
   /** return the highest existing child, if any */
   abstract Node lastChild();
   /** for debugging */
   abstract void println(PrintStream ps, int offset);
  }

  /** Internal nodes in the tree. Each such node always has
   * two direct child nodes
   */
  private static class InternalNode extends Node
  {
    /** left child */
    Node left;
    /** right child */
    Node right;
    /** bit to examine to determine which child to look at */
    long index;
    /** for debugging */
    void println(PrintStream ps, int offset)
    {
      for (int i = 0; i < offset; i++)
      {
        ps.print(' ');
      }
      ps.println("index = " + index);
      left.println(ps, offset + 2);
      right.println(ps, offset + 2);
    }
    /** number of descendant DataNodes */
    int children;
    /* @return the number of descendant DataNodes */
    int getChildren()
    {
      return children;
    }
    /** Create an InternalNode to replace an existing node in
     * the tree, with children a new DataNode and the existing
     * node
     * @param anIndex which bit to look at when deciding which
     * child to go down.
     * @param dn a DataNode child for this InternalNode
     * @param otherChild this Node's other child, which should
     * still be attached to its old parent
     */
    InternalNode(long anIndex, DataNode dn, Node otherChild)
    {
      if (dn == null || otherChild == null)
      {
        throw new IllegalStateException("Can't happen - null pointer");
      }
      parent = otherChild.parent;
      dn.parent = this;
      otherChild.parent = this;
      index = anIndex;
      int x = getBit(dn.key, index);
      if (x == 0)
      {
	left = dn;
	right = otherChild;
      }
      else
      {
	left = otherChild;
	right = dn;
      }
      children = otherChild.getChildren() + 1;
      if (parent == null)
      {
        return;
      }
      InternalNode p = parent;
      p.setNext(dn.key, this);
      for (; p != null; p = p.parent)
      {
	p.children++;
      }
    }
    Node next(String s, long len)
    {
      if (len <= index)
      { // could be searching for substring of actual key
        // in which case return smallest match
	return left;
      }
      int x = getBit(s, index);
      if (x == 0)
      {
	return left;
      }
      return right;
    }
    /** Accept a new child, possibly replacing an existing one,
     * in which case it should be a node linking to the old one
     */
    void setNext(String s, Node n)
    {
      if (n == null)
      {
        throw new IllegalStateException("Null pointer in setNext");
      }
      int x = getBit(s, index);
      if (x == 0)
      {
	left = n;
      }
      else
      {
	right = n;
      }
    }
    Node firstChild()
    {
      return left;
    }
    Node lastChild()
    {
      return right;
    }
  }

  private static class DataNode extends Node
  {
    String key;
    Object value;
    /** for debugging */
    void println(PrintStream ps, int offset)
    {
      for (int i = 0; i < offset; i++)
      {
        ps.print(' ');
      }
      ps.print("key=");
      printHexString(ps, key);
      ps.println(" value=" + value);
    }
    Node next(String s, long len)
    {
      return null;
    }
    DataNode(String aKey, Object aValue)
    {
      key = aKey;
      value = aValue;
    }
    int getChildren()
    {
      return 1;
    }
    Object getValue(String s, long len)
    {
      // System.out.print("looking for ");
      // printHexString(System.out, s);
      // System.out.println("At node with ");
      // printHexString(System.out, key);
      // System.out.println();
      long x = firstDifference(s, key, len);
      // System.out.println("len " + len + " x " + x);
      if (len <= x)
      {
        return value;
      }
      return null;
    }
    /** Accept a string-value pair to be inserted in the tree.
     *  Searching to the string has taken us to this DataNode,
     *  so either this value is a new value for this nodes' key
     *  or we need to insert a branch somewhere to distinguish
     *  the two.
     *  @exception IllegalArgumentException if one key is a
     *  prefix of the other and they are not identical.
     */
    void acceptPair(String s, Object aValue, Patricia pat)
    {
      if (key.equals(s))
      {
        value = aValue;
	return;
      }
      long len = ((long)s.length()) << 4;
      long x = firstDifference(s, key, len);
      if (x >= len || x >= (((long)key.length()) << 4))
      {
        throw new IllegalArgumentException(
	  "Would have one key a prefix of another");
      }
      InternalNode p = parent;
      Node prev = this;
      DataNode newChild = new DataNode(s, aValue);
      for(;;)
      {
	if (p == null)
	{
	  pat.head = new InternalNode(x, newChild, prev);
	  return;
	}
	if (p.index < x)
	{
	  new InternalNode(x, newChild, prev);
	  return;
	}
	prev = p;
	p = p.parent;
      }
    }
    /** Delete a key-value pair if it is the one held
     * at this node
     */
    Object delete(String s, Patricia pat)
    {
      if (!key.equals(s))
      {
	return null;
      }
      if (parent == null)
      {
        pat.head = null;
	// System.err.println("Null parent");
        return value;
      }
      InternalNode grandparent = parent.parent;
      Node other;
      if (parent.left == this)
      {
        other = parent.right;
      }
      else
      {
        other = parent.left;
      }
      if (grandparent == null)
      {
	pat.head = other;
      }
      else if (grandparent.left == parent)
      {
	grandparent.left = other;
      }
      else
      {
	grandparent.right = other;
      }
      other.parent = grandparent;
      while (grandparent != null)
      {
        grandparent.children--;
        grandparent = grandparent.parent;
      }
      // System.err.println("Set up");
      return value;
    }
    /** @return the number of keys held differing from this
     * nodes key only after the first len bits, assuming that
     * a search terminated here, so this is the leftmost such
     * node, if it is a match at all
     */
    int containsKeys(String s, long len)
    {
      long x = firstDifference(key, s, len);
      if (x < len)
      {
	return 0;
      }
      int sofar = 1;
      Node prev = this;
      for(InternalNode np = parent; np != null; np = np.parent)
      {
        if (np.index < len)
        {
          break;
        }
        if (np.left == prev)
        { // if came up on left branch, add total for everything
	  // in the right branch. If came up on right branch,
	  // left branch was not included in the search, so
	  // don't count it.
          sofar += np.right.getChildren();
        }
	prev = np;
      }
      return sofar;
    }
    /** @return an Iterator over all the matching entries,
     *  starting from this node, which must be the leftmost
     *  matching node since a search terminated here. Skip
     *  x values first, though
     */
    Iterator getEntries(String s, long len, int x)
    {
      long l = firstDifference(s, key, len);
      if (l < len)
      {
	return nullIterator;
      }
      if (x == 0)
      { // No skipping - return here
	return new SubIterator(this, len);
      }
      x--;
      Node n = this;
      InternalNode np;
      for(;;n = np)
      {
        np = n.parent;
        if (np == null)
        {
          return nullIterator;
        }
        if (n == np.left)
        { // Either descend into the right hand branch or
	  // subtract off its children from x
          int y = np.right.getChildren();
          if (y > x)
          {
            n = np.right;
            break;
          }
          x -= y;
        }
      }
      // Here to start from xth element of tree in n
      for(;;)
      {
        Node child = n.firstChild();
        if (child == null)
        { // reached a DataNode
          if (x != 0)
          { // can't happen - reached the wrong DataNode
            throw new IllegalStateException("Out of alignment");
          }
          return new SubIterator((DataNode)n, len);
        }
        int y = child.getChildren();
        if (y <= x)
        {
          x -= y;
          n = n.lastChild();
        }
        else
        {
          n = n.firstChild();
        }
      }
    }
    Node firstChild()
    {
      return null;
    }
    Node lastChild()
    {
      return null;
    }
  }

  /** Iterate along a tree in increasing order until you reach
   *  a Node that is too high to match, because it has an index
   *  value below a cut-off point
   */
  private static class SubIterator implements Iterator
  {
    private DataNode at;
    private long len;
    SubIterator(DataNode aNode, long aLength)
    {
      at = aNode;
      len = aLength;
    }
    public boolean hasNext()
    {
      return at != null;
    }
    public Object next()
    {
      if (at == null)
      {
        throw new NoSuchElementException();
      }
      Map.Entry result = new SubIteratorEntry(at);
      Node child = at;
      for(InternalNode np = at.parent;;np = np.parent)
      {
        if (np == null || np.index < len)
        {
          at = null;
          break;
        }
        if (np.left == child)
        { // came up a left branch so next node along is
	  // the first node on the right branch
          for(Node n = np.right;; n = child)
          {
            child = n.firstChild();
            if (child == null)
            {
              at = (DataNode)n;
              break;
            }
          }
          break;
        }
        child = np;
      }
      return result;
    }
    public void remove()
    {
      throw new UnsupportedOperationException();
    }
  }

  /** Implement Map.Entry as per java Maps */
  private static class SubIteratorEntry implements Map.Entry
  {
    private DataNode at;
    public Object getKey()
    {
      return at.key;
    }
    public Object getValue()
    {
      return at.value;
    }
    public Object setValue(Object o)
    {
      Object result = at.value;
      at.value = o;
      return result;
    }
    /** as defined in the interface spec for Map.Entry */
    public boolean equals(Object o)
    {
      if (!(o instanceof Map.Entry))
      {
        return false;
      }
      Map.Entry other = (Map.Entry)o;
      // cannot have null key here
      if (!at.key.equals(other.getKey()))
      {
        return false;
      }
      if (at.value == null)
      {
        return other.getValue() == null;
      }
      return at.value.equals(other.getValue());
    }
    /** as defined in the interface spec for Map.Entry */
    public int hashCode()
    {
      if (at.value == null)
      {
        return at.key.hashCode();
      }
      return at.key.hashCode() ^ at.value.hashCode();
    }
    SubIteratorEntry(DataNode aDataNode)
    {
      at = aDataNode;
    }
  }

  /** The head of the tree */
  private Node head = null;

  /** Create an empty Patricia */
  public Patricia()
  {
  }
  /** Create from an existing Map. This may fail if the Map keys
   * are not strings, or if one is a prefix of the other.
   * @param p a Map from which to read key-value pairs
   * @exception IllegalArgumentException if a prefix clash is discovered
   */
  public Patricia(Map p)
  {
    for (Iterator i = p.entrySet().iterator(); i.hasNext();)
    {
      Map.Entry me = (Map.Entry)i.next();
      put((String)me.getKey(), me.getValue());
    }
  }
  /** Create from an existing Patricia. It may be possible for
   * this to fail if the original Patricia contains undetected
   * pairs of keys, one of which is a prefix of the other: Patricia
   * Maps should not be used to store such sets of keys.
   * @param p a Map from which to read key-value pairs.
   * @exception IllegalArgumentException if two keys are found such
   * that one is a prefix of the other.
   */
  public Patricia(Patricia p)
  {
    for (Iterator i = p.getEntries("", 0, 0); i.hasNext();)
    {
      Map.Entry me = (Map.Entry)i.next();
      put((String)me.getKey(), me.getValue());
    }
  }
  /** Clear all entries from the Map */
  public void clear()
  {
    head = null;
  }
  /** find out if the Map is empty
   * @return true iff no entries in the Map
   */
  public boolean isEmpty()
  {
    return head == null;
  }
  /** Put all the mappings in a Map into the Patricia. This may fail
   *  if the Map keys are not Strings, or if the resulting set of
   * keys have a prefix of another.
   * @param m a Map from which to read key-value pairs
   * @exception IllegalArgumentException if a prefix clash is
   * discovered
   */
  public void putAll(Map m)
  {
    for (Iterator i = m.entrySet().iterator(); i.hasNext();)
    {
      Map.Entry me = (Map.Entry)i.next();
      put((String)me.getKey(), me.getValue());
    }
  }
  /** Put all the mappings in a Patricia into the Patricia. This may
   * fail
   * if the resulting set of keys have one which is a prefix of another:
   * don't do that!
   * @param m a Patricia from which to read key-value pairs
   * @exception IllegalArgumentException if prefix found
   */
  public void putAll(Patricia m)
  {
    for (Iterator i = m.getEntries("", 0, 0); i.hasNext();)
    {
      Map.Entry me = (Map.Entry)i.next();
      put((String)me.getKey(), me.getValue());
    }
  }
  /** @return a DataNode bearing a key that might start with the given
   * String, using only the first len bits of the string
   */
  private DataNode search(String s, long len)
  {
    if (head == null)
    {
      return null;
    }
    Node p = head;
    for(Node q = p.next(s, len); q != null; q = p.next(s, len))
    {
      p = q;
    }
    return (DataNode)p;
  }
  /** Retrieve a value whose key starts with first len bits of the
   * string quoted, or null.
   * @return a value associated with a key starting with the given
   * String or null, counting only the first len bits in it. If more
   * that one match, return the first such match. Since it is illegal
   * to have two keys in the map such that one is a prefix of the other,
   * passing in a full key should return that key's value.
   * @param s a key to look for in the map
   * @param len the number of bits in the key to consider significant
   */
  public Object get(String s, long len)
  {
    // System.err.println("Before search");
    DataNode p = search(s, len);
    // System.err.println("After search");
    if (p == null)
    {
      // System.err.println("Search null");
      return null;
    }
    return p.getValue(s, len);
  }

  /** Retrieve a value associated with a key starting with the string
   * quoted, or null.
   * @return an object associated with a key starting with the
   * given String or null. Since it is illegal
   * to have two keys in the map such that one is a prefix of the other,
   * passing in a full key should return that key's value.
   * @param s the string to look for
   */
  public Object get(String s)
  {
    return get(s, ((long)s.length()) << 4);
  }
  /** Insert a key-value pair, overwriting any existing exact
   * match. The key should not be a prefix of any existing key,
   * and no existing key should be a prefix of it.
   * @param key the key
   * @param value its value
   * @exception IllegalArgumentException if the key is a prefix of
   * another key, or an existing key is a prefix of this, and this
   * is detected. Such a situation is improper use, but is not
   * guaranteed to be detected, here or elsewhere.
   */
  public void put(String key, Object value)
  {
    if (head == null)
    {
      head = new DataNode(key, value);
      return;
    }
    DataNode p = search(key, ((long)key.length()) << 4);
    p.acceptPair(key, value, this);
  }
  /** Delete an exactly matching string.
   * @param s a string exactly matching the key of a
   * key-value pair to be deleted.
   * @return the previously associated value, or null
   */
  public Object remove(String s)
  {
    if (head == null)
    {
      return null;
    }
    DataNode n = search(s, ((long)s.length()) << 4);
    if (n == null)
    {
      return null;
    }
    return n.delete(s, this);
  }
  /** Returns the number of keys in the map starting
   * with the given substring. Node that no key in the
   * map is a substring of any other key, so this returns
   * 1 if substring is a key. Use only the first len bits
   */
  public int containsKeys(String substring, long len)
  {
    DataNode n = search(substring, len);
    // if any key starting with substring exists in the map,
    // n must point to one
    if (n == null)
    {
      return 0;
    }
    return n.containsKeys(substring, len);
  }
  /** Return an Iterator of Map.Entry items, where the key
   *  startsWith substring counting at most the first len bits,
   *  after skipping x such items
   */
  public Iterator getEntries(String substring, long len, int x)
  {
    DataNode n = search(substring, len);
    if (n == null)
    {
      return nullIterator;
    }
    return n.getEntries(substring, len, x);
  }
  /** Number of items in the map */
  public int size()
  {
    if (head == null)
    {
      return 0;
    }
    return head.getChildren();
  }

  /** An iterator over no items */
  private static class NullIterator implements Iterator
  {
    public boolean hasNext()
    {
      return false;
    }
    public Object next()
    {
      throw new NoSuchElementException();
    }
    public void remove()
    {
      throw new UnsupportedOperationException();
    }
  }
  private static NullIterator nullIterator = new NullIterator();

  /** Utility: add binary value of int to StringBuffer, encoding
   * it so that string sort order is int sort order.
   * @param sb the StringBuffer to append to
   * @param y the int to encode and append
   */
  public static void appendBinInt(StringBuffer sb, int y)
  {
    // String orders as unsigned: make order work with signed
    int x = y ^ Integer.MIN_VALUE;
    sb.append((char)(x >> 16));
    sb.append((char)x);
  }
  /** Utility: retrieve binary int from string
   * @param s the String to read from
   * @param offset the offset in the string at which to start
   * reading
   * @return the int read
   */
  public static int getBinInt(String s, int offset)
  {
    int x = (((int)s.charAt(offset)) << 16) + s.charAt(offset + 1);
    return x ^ Integer.MIN_VALUE;
  }
  /** Number of chars in binary encoded int */
  public static final int BIN_INT_CHARS = 2;
  /** Utility: append binary encoded long to StringBuffer,
   *  encoding it so that string order is long order.
   * @param sb the StringBuffer to append to
   * @param y the value to encode and append
   */
  public static void appendBinLong(StringBuffer sb, long y)
  {
    // String orders as unsigned: make order work with signed
    long x = y ^ Long.MIN_VALUE;
    sb.append((char)(x >> 48));
    sb.append((char)(x >> 32));
    sb.append((char)(x >> 16));
    sb.append((char)x);
  }
  /** Number of chars in binary encoded long */
  public static final int BIN_LONG_CHARS = 4;
  /** Utility: retrieve binary long from string.
   *  @param s the String to read from
   *  @param offset the offset at which to start reading
   *  @return the long read
   */
  public static long getBinLong(String s, int offset)
  {
    long x = ((long)(s.charAt(offset)) << 48) +
           ((long)(s.charAt(offset + 1)) << 32) +
           ((long)(s.charAt(offset + 2)) << 16) +
           s.charAt(offset + 3);
    return x ^ Long.MIN_VALUE;
  }
  /** Utility: append binary encoded float to StringBuffer,
   * again ensuring sorting order is consistent.
   * @param sb the StringBuffer to append to
   * @param f the float to encode and append
   */
  public static void appendBinFloat(StringBuffer sb, float f)
  {
    // IEEE/Java floating point spec means order of floats
    // is the same as ints except for result of sign bit
    int l = Float.floatToIntBits(f);
    if ((l & Long.MIN_VALUE) != 0)
    {
      // map -1 to Integer.MIN_VALUE and vice versa
      l = Integer.MIN_VALUE - l - 1;
    }
    appendBinInt(sb, l);
  }
  /** Number of chars in binary encoded float */
  public static final int BIN_FLOAT_CHARS = 2;
  /** Utility: get binary encoded float from string
   *  @param s the String to read from
   *  @param offset the offset at which to start reading
   *  @return the float read
   */
  public static float getBinFloat(String s, int offset)
  {
    int l = getBinInt(s, offset);
    if ((l & Long.MIN_VALUE) != 0)
    {
      // map -1 to Integer.MIN_VALUE and vice versa
      l = Integer.MIN_VALUE - l - 1;
    }
    return Float.intBitsToFloat(l);
  }
  /** Utility: append binary encoded double to StringBuffer, again
   * ensuring sorting order is consistent.
   * @param sb the StringBuffer to append to
   * @param d the Double to encode and append
   */
  public static void appendBinDouble(StringBuffer sb, double d)
  {
    // Java/IEEE double spec means order of doubles is the same
    // as longs, except that we need to reverse the order of the
    // -ve numbers (top bit set)
    /*
    long l = Double.doubleToLongBits(d);
    if ((l & Long.MIN_VALUE) != 0)
    {
      // map -1 to Long.MIN_VALUE and vice versa
      l = Long.MIN_VALUE - l - 1;
    }
    */
    appendBinLong(sb, MaxInRange.toOrderedLong(d));
  }
  /** Number of chars in binary encoded double */
  public static final int BIN_DOUBLE_CHARS = 4;
  /** Read an encoded double from a String
   *  @param s the String to read from
   *  @param offset the offset at which to start reading
   *  @return the double read
   */
  public static double getBinDouble(String s, int offset)
  {
    /*
    long l = getBinLong(s, offset);
    if ((l & Long.MIN_VALUE) != 0)
    {
      // map -1 to Long.MIN_VALUE and vice versa
      l = Long.MIN_VALUE - l - 1;
    }
    return Double.longBitsToDouble(l);
    */
    return MaxInRange.fromOrderedLong(getBinLong(s, offset));
  }
  /** Utility: append string encoded so that no string is a prefix
   * of any other, but string order still works: reserve top bit
   * as a "not finished" marker, expanding length by 1/16th + 1 bit.
   * @param target the StringBuffer to append to
   * @param s the String to encode and append
   */
  public static void appendTopBitString(StringBuffer target, String
   s)
  {
    int len = s.length();
    long holding = 0;
    int bitsIn = 0;
    int i;
    for (i = 0; i < len; i++)
    {
      holding = (holding << 17) + 0x10000 +
		s.charAt(i);
      bitsIn += 17;
      // System.out.println("bits " + bitsIn + " got " +
      //   Long.toHexString(holding));
      while(bitsIn >= 16)
      {
	target.append((char)(holding >> (bitsIn - 16)));
	bitsIn -= 16;
	// System.out.println("bits " + bitsIn + " got " +
	//   Long.toHexString(holding));
      }
    }
    // Shove in final 0 end marker
    holding = holding << 1;
    bitsIn++;
    while(bitsIn >= 16)
    {
      target.append((char)(holding >> (bitsIn - 16)));
      bitsIn -= 16;
      // System.out.println("bits " + bitsIn + " got " +
      //   Long.toHexString(holding));
    }
    if (bitsIn > 0)
    {
      target.append((char)(holding << (16 - bitsIn)));
      // System.out.println("bits " + bitsIn + " got " +
      //   Long.toHexString(holding));
    }
  }
  /** Utility: retrieve string encoded with appendTopBitString,
  * incrementing offset[0] to mark number of chars consumed
  * @param s the String to read from
  * @param offset an array whose 0th element is the offset to
  * start reading from at entry, and at exit is the first character
  * not read from after the String read
  * @return the encoded string
  */
  public static String getTopBitString(String s, int[] offset)
  {
    int off = offset[0];
    StringBuffer sb = new StringBuffer();
    int bitsIn = 0;
    long holding = 0;
    loop:for(;;)
    {
      for(;;)
      {
	if (bitsIn > 0)
	{
	  if ((holding & (1 << (bitsIn - 1))) == 0)
	  {
	    break loop;
	  }
	  bitsIn--;
	  break;
	}
	holding = (holding << 16) + s.charAt(off++);
	bitsIn += 16;
      }
      for(;;)
      {
	if (bitsIn >= 16)
	{
	  sb.append((char)(holding >> (bitsIn - 16)));
	  bitsIn -= 16;
	  break;
	}
	holding = (holding << 16) + s.charAt(off++);
	bitsIn += 16;
      }
    }
    offset[0] = off;
    return sb.toString();
  }
  /** Utility: return the number of bits produced by encoding a string
   * of a given length (this depends only on the length of the string)
   * @param len the length in characters of a String
   * @return the length in bits that an encoding of that String would
   * take.
   */
  public static long binStringLen(int len)
  {
    long l = len;
    return 1 + (l << 4) + l;
  }
  /** Mostly for debuging - print the Patricia datastructure
   * @param ps the PrintStream to print to
   */
  public void printMap(PrintStream ps)
  {
    ps.println("Patricia:");
    if (head != null)
    {
     head.println(ps, 0);
    }
  }
  /** Utility to print out a String as a sequence of hex digits
   * @param ps a PrintStream to print to
   * @param s the String to print out
   */
  public static void printHexString(PrintStream ps, String s)
  {
    for (int i = 0; i < s.length(); i++)
    {
      String t = Integer.toHexString(s.charAt(i));
      for (int j = t.length(); j < 4; j++)
      {
	ps.print('0');
      }
      ps.print(t);
    }
  }
}
