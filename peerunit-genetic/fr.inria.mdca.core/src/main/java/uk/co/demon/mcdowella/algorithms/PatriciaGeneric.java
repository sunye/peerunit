package uk.co.demon.mcdowella.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
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
(in bits). The key must be presented as a sequence of bits, but
the key class has the option of producing those bits on demand, deriving
them from some more convenient representation.
</p><p>
Patricia
demands that no key be a prefix of any other tree. You can arrange
this, for example, by prefixing each string with an encoded
length (Although this does muck up string order), or by
zero-terminating the string if the zero character it cannot otherwise
occur, or by setting the
top bit of all but the last char, if top bits are always otherwise 
clear. A utility class is provided that presents a string as a sequence
of 17-bit chunks. The first bit of each 17-bit chunk is set if the
corresponding character exists in the original string. This encoding
therefore presents each string as a bitstring of infinite length, so
that no encoded string is a prefix of any other encoded string, except
itself. Note that this preserves the usual order.
</p><p>
The big advantage of Patricia over, for instance, TreeMap, is that
each comparison made as you navigate the tree is a comparison of a
single bit, not a full string compare, whose cost increases with
the length of the keys. In practice, however, the gain is not enormous
(perhaps a factor of two) and can easily be swallowed up in any encoding
necessary to ensure that the keys are not prefixes of each other.
</p>
<p>
A second advantage of this class is that it allows the clients to
provide callbacks to keep track
of information about a node's descendants. This could be used, for
instance, to search for closest matches in the tree. PatriciaClosest
does just this, providing one way to store a set of points with 
coordinates (as arrays of doubles) and find the closest point to a
target point without checking every candidate.
</p>
*/
public class PatriciaGeneric<K extends PatriciaGeneric.KeyInterface<K>,
  Value, DescendantInfo extends 
  PatriciaGeneric.ChildInfo<K, Value, DescendantInfo>>
  implements Iterable<Map.Entry<K, Value>>
{
  /** interface to be implemented by keys. We could have each key
   *  reformat itself to a bit-string, but having a separate interface
   *  makes life easier for queries where the conversion overhead might
   *  be higher than the cost of the search. The K parameter is 
   *  typically the implementing class. You should probably reimplement
   *  equals(), although we use firstDifference() where we can, the
   *  exception being to compare two Map.Entry<K, Value> objects.
   */
  public interface KeyInterface<K>
  {
    /** return the bit at the given offset. It is up to implementors
        to deal with offsets off the end of their object */
    int getBit(long offset);
    /** return the offset of the first bit difference to the other
     *  type, or -ve if no difference. Implementors can throw an
     *  IllegalArgumentException if one of the keys being compared
     *  is a prefix of the other, but may wish to define their
     *  bit encoding so that this never happens, for instance by
     *  making all keys virtually infinite.
     */
    long firstDifference(K compareWith) throws IllegalArgumentException;
  }
  /** KeyInterface for a String. Treats each string as consisting of
   *  chunks of 17 bits, with the first bit being set where the
   *  original string has a character. The other 16 bits are the bits
   *  of the character, if present, or 0
   */
  public static class StringKey implements KeyInterface<StringKey>
  {
    public StringKey(String s)
    {
      k = s;
    }
    private final String k;
    public int getBit(long loffset)
    {
      // Let's assume strings aren't hideously long
      // so as to avoid 64-bit division
      int offset = (int)loffset;
      int charOffset = (int)(offset / 17);
      // int bitOffset = (int)(offset % 17L);
      int bitOffset = (int)(offset - charOffset * 17);
      if (charOffset >= k.length())
      {
        return 0;
      }
      if (bitOffset == 0)
      {
        return 1;
      }
      // check: bit 1 is top bit - shift 15. Bit 16 is low bit.
      return 1 & (k.charAt(charOffset) >> (16 - bitOffset));
    }
    public long firstDifference(StringKey compareWith) 
    {
      int ourLen = k.length();
      int theirLen = compareWith.k.length();
      int minLen;
      if (ourLen < theirLen)
      {
        minLen = ourLen;
      }
      else
      {
        minLen = theirLen;
      }
      for (int i = 0; i < minLen; i++)
      {
        int diff = k.charAt(i) ^ compareWith.k.charAt(i);
	if (diff != 0)
	{
	  // check: top char bit has 16 bits above it. Low bit has 31
	  return i * 17L + Integer.numberOfLeadingZeros(diff) - 15;
	}
      }
      if (ourLen != theirLen)
      { // mismatch on virtual bit announcing start of first character
        // present in one string but not another.
        return minLen * 17L;
      }
      return -1;
    }
    /** define equals() though we use firstDifference() directly 
        instead where we can */
    @Override
    public boolean equals(Object o)
    {
      if (!(o instanceof StringKey))
      {
        return false;
      }
      StringKey other = (StringKey) o;
      return other.k.equals(k);
    }
    @Override
    public int hashCode()
    {
      return k.hashCode();
    }
    /** return original key */
    public String toString()
    {
      return k;
    }
  }
  /** KeyInterface for an array of doubles, which are treated as if
   *  turned into longs and then bit-interleaved. This only works if
   *  every key is an array of the same length, although this is not
   *  type-checked at compile time. If two keys of different length
   *  ever meet, they will probably throw an IllegalArgumentException 
   *  at run time.
   */
  public static class DoubleArrayKey 
    implements KeyInterface<DoubleArrayKey>
  {
    public DoubleArrayKey(double[] values)
    {
      longValues = new long[values.length];
      for (int i = 0; i < values.length; i++)
      {
	/*
        long l = Double.doubleToLongBits(values[i]);
	if ((l & Long.MIN_VALUE) != 0)
	{
	  // map -1 to Long.MIN_VALUE and vice versa
	  l = Long.MIN_VALUE - l - 1;
	}
        longValues[i] = l ^ Long.MIN_VALUE;
	*/
	longValues[i] = MaxInRange.toOrderedLong(values[i]);
      }
    }
    /** Hold doubles using bit representation as longs, with the top
     *  bit (sign bit) flipped and the order of the -ves reversed.
     *  Because of the rules of IEEE floating
     *  point, this means that the order will be the same 
     */
    private final long[] longValues;
    public int getBit(long loffset)
    {
      // Let's assume strings aren't hideously long
      // so as to avoid 64-bit division
      int offset = (int)loffset;
      int bitOffset = (int)(offset / longValues.length);
      int longOffset = (int)(offset - bitOffset * longValues.length);
      if (bitOffset >= 64)
      {
        return 0;
      }
      return 1 & (int)(longValues[longOffset] >> (63 - bitOffset));
    }
    public long firstDifference(DoubleArrayKey compareWith) 
    {
      if (compareWith.longValues.length != longValues.length)
      {
        throw new IllegalArgumentException(
	  "double array length mismatch");
      }
      long first = -1;
      // We will check for differences only under the mask, modifying
      // the mask to zap out differences that can't produce a smaller
      // first difference than the number so far
      long mask = -1;
      for (int i = 0; i < longValues.length; i++)
      {
        long diff = (longValues[i] ^ compareWith.longValues[i]) & mask;
	if (diff == 0)
	{
	  continue;
	}
	int lz = Long.numberOfLeadingZeros(diff);
	first = lz * (long)longValues.length + i;
	if (lz == 0)
	{ // Can't do better than a difference in the top bit
	  // and must do worse as i will increase
	  break;
	}
	else
	{
	  // zap out everything below and including the bit position
	  // we have just found things differ at as later values will
	  // have larger i. Check: lz = 1 => 1 << 63 = 0x800...
	  // => mask is ~0x7fffff = 0x80000... and we don't check
	  // anything except the top bit any more
	  mask = ~((1L << (64 - lz)) - 1);
	}
      }
      return first;
    }
    /** define equals() though we use firstDifference() directly 
        instead where we can */
    @Override
    public boolean equals(Object o)
    {
      if (!(o instanceof DoubleArrayKey))
      {
        return false;
      }
      DoubleArrayKey other = (DoubleArrayKey) o;
      return Arrays.equals(longValues, other.longValues);
    }
    /** hashCode so we can shove these in hash tables */
    @Override
    public int hashCode()
    {
      int value = 0;
      for (int i = 0; i < longValues.length; i++)
      {
	long v = longValues[i];
        value = value * 131 + (int)(v >> 32);
        value = value * 131 + (int)v;
      }
      return value;
    }
    /** return original key */
    public double[] toDouble()
    {
      double[] result = new double[longValues.length];
      for (int i = 0; i < longValues.length; i++)
      {
	/*
        long l = longValues[i] ^ Long.MIN_VALUE;
	if ((l & Long.MIN_VALUE) != 0)
	{
	  // map -1 to Long.MIN_VALUE and vice versa
	  l = Long.MIN_VALUE - l - 1;
	}
        result[i] = Double.longBitsToDouble(l);
	*/
	result[i] = MaxInRange.fromOrderedLong(longValues[i]);
      }
      return result;
    }
  }
  /** interface implemented by clients allowing them to receive
   *  info about children, which will be either value-key pairs
   *  or other DescendantInfo objects, which are of type D, typically
   *  the implementing type. Each method allows the class to return true
   *  if no change has been made to its DescendantInfo object, so that
   *  nothing need be propagated up. The class should do this because
   *  this could also be used in debugging to check that everything in
   *  the tree is up to date.
   */
  public interface ChildInfo<K, V, D>
  {
    /** called when one of two children is updated. Return true if
     *  no need to propagate change up. */
    boolean leafUpdate(K k1, V v1, K k2, V v2);
    /** called when child or DescendantInfo is updated.
      Return true if no need to propagate change to parent */
    boolean mixedUpdate(K k, V v, D di);
    /** call when one of two DescendantInfo children is updated. Return
        true if no need to propagate change to parent */
    boolean internalUpdate(D di1, D di2);
  }
  /** dummy (null) implementation of ChildInfo. See PatriciaClosest
   * for a proper implementation.
   */
  public static class DummyChildInfo<K, V> implements 
    ChildInfo<K, V, DummyChildInfo<K, V>>
  {
    public boolean leafUpdate(K k1, V v1, K k2, V v2)
    {
      return true;
    }
    public boolean mixedUpdate(K k, V v, DummyChildInfo<K, V> di)
    {
      return true;
    }
    public boolean internalUpdate(DummyChildInfo<K, V> di1,
      DummyChildInfo<K, V> di2)
    {
      return true;
    }
  }
  /** interface so we can create DescendantInfo objects */
  public interface InfoFactory<D>
  {
    D create();
  }
  /** Dummy implementation of infoFactory */
  public static class DummyInfoFactory<K, V> implements
    InfoFactory<DummyChildInfo<K, V>>
  {
    public DummyChildInfo<K, V> create()
    {
      return new DummyChildInfo<K, V>();
    }
  }

  /** abstract class of all nodes in the Patricia tree. The tree is
     made up internal nodes, which have two children and no key or
     value but do have DescendantInfo objects, and data nodes, which
     have no children nor DescendantInfo objects but do have both keys
     and values. There is always one fewer internal node than data node.
     */
  public abstract static class Node<
    K extends PatriciaGeneric.KeyInterface<K>, V,
    D extends PatriciaGeneric.ChildInfo<K, V, D>>
  {
   /** @return a child giving the next place to go for the given key
    * or null
    */
   public abstract Node<K, V, D> next(K k);
   /** return the Key or null */
   public abstract K getKey();
   /** return the value or null */
   public abstract V getValue();
   /** return the DescendantInfo or null. Nodes with non-null
    *  DescendantInfo objects are internal nodes and have two
    *  children. Nodes without DescendantInfo objects are leaf nodes and
    *  have no children but have keys (not null) and values 
    * (which may be null).
    */
   public abstract D getDescendantInfo();
   /**
    * the parent or null
    */
   InternalNode<K, V, D> parent;
   /** return the left existing child, if any */
   public abstract Node<K, V, D> firstChild();
   /** return the right existing child, if any */
   public abstract Node<K, V, D> lastChild();
   /** for debugging */
   public abstract void println(PrintStream ps, int offset);
   /** Top of double dispatch for callUpdate. We want to call one of
    *  three different methods on parent depending on the
    *  types of the two children of a node. We make two calls in
    *  succession to let the automatic method selection based on the
    *  type of the object making the call do the work for us.
    */
   abstract boolean callWithOther(D parent, Node<K, V, D> other);
   /** called in double dispatch when left side is a DataNode */
   abstract boolean callDataWithOther(D parent,
     DataNode<K, V, D> other);
   /** called in double dispatch when left side is an InternalNode */
   abstract boolean callInternalWithOther(D parent,
     InternalNode<K, V, D> other);
  }

  /** Internal nodes in the tree. Each such node always has
   * two direct child nodes
   */
  private static class InternalNode<
    K extends PatriciaGeneric.KeyInterface<K>, V,
    D extends PatriciaGeneric.ChildInfo<K, V, D>> extends Node<K, V, D>
  {
    /** internal nodes return null keys */
    public K getKey()
    {
      return null;
    }
    /** internal nodes return null values */
    public V getValue()
    {
      return null;
    }
    /** left child */
    Node<K, V, D> left;
    /** right child */
    Node<K, V, D> right;
    /** descendant info */
    private final D info;
    /** return descendant info */
    public D getDescendantInfo()
    {
      return info;
    }
    /** bit to examine to determine which child to look at */
    private final long index;
    /** for debugging */
    public void println(PrintStream ps, int offset)
    {
      for (int i = 0; i < offset; i++)
      {
        ps.print(' ');
      }
      ps.println("index = " + index);
      left.println(ps, offset + 2);
      right.println(ps, offset + 2);
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
    public InternalNode(long anIndex, DataNode<K, V, D> dn,
      Node<K, V, D> otherChild, D di)
    {
      if (dn == null || otherChild == null)
      {
        throw new IllegalStateException("Can't happen - null pointer");
      }
      info = di;
      parent = otherChild.parent;
      dn.parent = this;
      otherChild.parent = this;
      index = anIndex;
      long x = dn.key.getBit(index);
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
      // update new info
      callUpdate();
      if (parent == null)
      {
        return;
      }
      parent.setNext(dn.key, this);
      // just changed stuff under parent's feet so call its update
      for (InternalNode<K, V, D> p = parent; p != null;
        p = p.parent)
      {
	if (p.callUpdate())
	{
	  break;
	}
      }
    }
    public Node<K, V, D> next(K k)
    {
      int x = k.getBit(index);
      if (x == 0)
      {
	return left;
      }
      return right;
    }
    /** call update required for this node */
    private boolean callUpdate()
    {
      return left.callWithOther(info, right);
    }
    /** Accept a new child, possibly replacing an existing one,
     * in which case it should be a node linking to the old one
     */
    void setNext(K k, Node<K, V, D> n)
    {
      if (n == null)
      {
        throw new IllegalStateException("Null pointer in setNext");
      }
      int x = k.getBit(index);
      if (x == 0)
      {
	left = n;
      }
      else
      {
	right = n;
      }
    }
    public Node<K, V, D> firstChild()
    {
      return left;
    }
    public Node<K, V, D> lastChild()
    {
      return right;
    }
    boolean callWithOther(D parent, Node<K, V, D> other)
    {
      return other.callInternalWithOther(parent, this);
    }
    boolean callDataWithOther(D parent, DataNode<K, V, D> other)
    {
      return parent.mixedUpdate(other.key, other.value, info);
    }
    boolean callInternalWithOther(D parent, InternalNode<K, V, D> other)
    {
      return parent.internalUpdate(info, other.info);
    }
  }

  private static class DataNode<
    K extends PatriciaGeneric.KeyInterface<K>, V,
    D extends PatriciaGeneric.ChildInfo<K, V, D>> extends Node<K, V, D>
  {
    /** DescendantInfo is always null for a DataNode */
    public D getDescendantInfo()
    {
      return null;
    }
    /** key stored in this node for table lookup */
    final K key;
    /** return the key held in this leaf node */
    public K getKey()
    {
      return key;
    }
    /** value stored in this node associated with the key */
    V value;
    /** return the value associated with our key */
    public V getValue()
    {
      return value;
    }
    /** for debugging */
    public void println(PrintStream ps, int offset)
    {
      for (int i = 0; i < offset; i++)
      {
        ps.print(' ');
      }
      ps.print("key=");
      ps.print(key);
      ps.println(" value=" + value);
    }
    public Node<K, V, D> next(K s)
    {
      return null;
    }
    DataNode(K aKey, V aValue)
    {
      key = aKey;
      value = aValue;
    }
    /** return value if we match the given key */
    V getValue(K k)
    {
      // System.out.print("looking for ");
      // printHexString(System.out, s);
      // System.out.println("At node with ");
      // printHexString(System.out, key);
      // System.out.println();
      // System.out.println("len " + len + " x " + x);
      if (k.firstDifference(key) < 0)
      {
        return value;
      }
      return null;
    }
    /** Accept a key-value pair to be inserted in the tree.
     *  Searching to the key has taken us to this DataNode,
     *  so either this value is a new value for this nodes' key
     *  or we need to insert a branch somewhere to distinguish
     *  the two. Sets found to whether the key was found
     *  @exception IllegalArgumentException if one key is a
     *  prefix of the other and they are not identical (can't happen
     *  with some encodings: StringKey will never do this).
     *  @return old value
     */
    V acceptPair(K k, V aValue, PatriciaGeneric<K, V, D> pat,
      InfoFactory<D> fact, boolean[] found)
    {
      if (key.firstDifference(k) < 0)
      { // just modify existing value
	V old = value;
        value = aValue;
	found[0] = true;
	return old;
      }
      // have to create a new node because no existing value matches
      found[0] = false;
      long x = k.firstDifference(key);
      InternalNode<K, V, D> p = parent;
      Node<K, V, D> prev = this;
      DataNode<K, V, D> newChild = new DataNode<K, V, D>(k, aValue);
      for(;;)
      {
	if (p == null)
	{ // want new internal node to be at top of tree
	  pat.head = new InternalNode<K, V, D>(x, newChild, prev,
	    fact.create());
	  return null;
	}
	if (p.index < x)
	{ // substitute new internal node in instead of node prev, which
	  // becomes its other child
	  new InternalNode<K, V, D>(x, newChild, prev, fact.create());
	  return null;
	}
	prev = p;
	p = p.parent;
      }
    }
    /** Delete a key-value pair held
     * at this node. Assumes that we have already checked that we
     * really do want to delete this node.
     */
    V delete(K s, PatriciaGeneric<K, V, D> pat)
    {
      // Here we rely on the fact, by the construction of the tree,
      // that all DataNodes are leaf nodes.
      if (parent == null)
      { // here if no parent => we are the only node in the tree
        pat.head = null;
	// System.err.println("Null parent");
        return value;
      }
      // Here => we can delete our parent internalNode and make a
      // link from its parent, if any, to our sibling.
      InternalNode<K, V, D> grandparent = parent.parent;
      Node<K, V, D> other;
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
	if (grandparent.callUpdate())
	{
	  break;
	}
	grandparent = grandparent.parent;
      }
      // System.err.println("Set up");
      return value;
    }
    public Node<K, V, D> firstChild()
    {
      return null;
    }
    public Node<K, V, D> lastChild()
    {
      return null;
    }
    boolean callWithOther(D parent, Node<K, V, D> other)
    {
      return other.callDataWithOther(parent, this);
    }
    boolean callDataWithOther(D parent, DataNode<K, V, D> other)
    {
      return parent.leafUpdate(other.key, other.value, key, value);
    }
    boolean callInternalWithOther(D parent, InternalNode<K, V, D> other)
    {
      return parent.mixedUpdate(key, value, other.info);
    }
  }

  /** The head of the tree */
  private Node<K, Value, DescendantInfo> head = null;
  /** return head of tree */
  public Node<K, Value, DescendantInfo> getHead()
  {
    return head;
  }
  /** creator for DescendantInfo */
  private final InfoFactory<DescendantInfo> factory;

  /** Iterator over entries. Does not support modification, or any
      form of modification of the underlying map while iterating */
  private static class Iter<K extends PatriciaGeneric.KeyInterface<K>,
    Value,
    D extends PatriciaGeneric.ChildInfo<K, Value, D>>
    implements Iterator<Map.Entry<K, Value>>
  {
    /** stack of nodes producing key-value info */
    private final List<Node<K, Value, D>> stack =
      new ArrayList<Node<K, Value, D>>();
    Iter(Node<K, Value, D> root)
    {
      if (root != null)
      {
	stack.add(root);
      }
    }
    public boolean hasNext()
    {
      // Top entry on stack is either data node with info or
      // internal node with data nodes under it with info so
      // something to return if anything there at all
      return !stack.isEmpty();
    }
    public Map.Entry<K, Value> next()
    {
      int size = stack.size();
      if (size < 1)
      {
        throw new NoSuchElementException("Nothing left for next");
      }
      Node<K, Value, D> n = stack.remove(--size);
      for (;;)
      {
	final K k = n.getKey();
	if (k != null)
	{ // data node
	  final Value v = n.getValue();
	  return new Map.Entry<K, Value>()
	  {
	    public K getKey()
	    {
	      return k;
	    }
	    public Value getValue()
	    {
	      return v;
	    }
	    public Value setValue(Value v)
	    {
	      throw new UnsupportedOperationException(
	        "No modification");
	    }
	    @Override
	    public boolean equals(Object o)
	    {
	      if (!(o instanceof Map.Entry))
	      {
	        return false;
	      }
	      Map.Entry other = (Map.Entry) o;
	      Object otherKey = other.getKey();
	      if (otherKey == null)
	      {
	        return false;
	      }
	      // use equals here because we can only do a run-time
	      // check against Map.Entry, not Map.Entry<K, V>
	      // and so can't safely call firstDifference()
	      if (!k.equals(otherKey))
	      {
	        return false;
	      }
	      Object otherValue = other.getValue();
	      if (v == null)
	      {
	        return otherValue == null;
	      }
	      return v.equals(otherValue);
	    }
	  };
	}
	// here with internal node, which always has two children
	// Stack rightmost child so as to deal with leftmost child first
	// and we should encounter leaf nodes in the order of their
	// bit representations
	stack.add(n.lastChild());
	n = n.firstChild();
      }
    }
    public void remove()
    {
      throw new UnsupportedOperationException("remove not supported");
    }
  }

  /** return iterator over entries */
  public Iterator<Map.Entry<K, Value>> iterator()
  {
    return new Iter<K, Value, DescendantInfo>(head);
  }

  /** Create an empty PatriciaGeneric */
  public PatriciaGeneric(InfoFactory<DescendantInfo> fact)
  {
    factory = fact;
  }
  /** Create from an existing Map.
   * @param p a Map from which to read key-value pairs
   * @exception IllegalArgumentException if a prefix clash is discovered
   */
  public PatriciaGeneric(Map<K, Value> p,
    InfoFactory<DescendantInfo> fact)
  {
    factory = fact;
    putAll(p);
  }
  /** Create from an existing Patricia. It may be possible for
   * this to fail if the original Patricia contains undetected
   * pairs of keys, one of which is a prefix of the other: Patricia
   * Maps should not be used to store such sets of keys.
   * @param p a Map from which to read key-value pairs.
   * @exception IllegalArgumentException if two keys are found such
   * that one is a prefix of the other.
   */
  public PatriciaGeneric(PatriciaGeneric<K, Value, DescendantInfo> p,
    InfoFactory<DescendantInfo> fact)
  {
    factory = fact;
    putAll(p);
  }
  /** Clear all entries from the Map */
  public void clear()
  {
    head = null;
    numItems = 0;
  }
  /** find out if the Map is empty
   * @return true iff no entries in the Map
   */
  public boolean isEmpty()
  {
    return head == null;
  }
  /** Put all the mappings in a Map into the Patricia.
   * @param m a Map from which to read key-value pairs
   * @exception IllegalArgumentException if a prefix clash is
   * discovered
   */
  public void putAll(Map<K, Value> m)
  {
    for (Iterator<Map.Entry<K, Value>> i =
      m.entrySet().iterator(); i.hasNext();)
    {
      Map.Entry<K, Value> me = i.next();
      put(me.getKey(), me.getValue());
    }
  }
  /** Put all the mappings in a Patricia into the Patricia. This may
   * fail
   * if the resulting set of keys have one which is a prefix of another:
   * don't do that!
   * @param m a Patricia from which to read key-value pairs
   * @exception IllegalArgumentException if prefix found
   */
  public void putAll(PatriciaGeneric<K, Value, DescendantInfo> m)
  {
    for (Iterator<Map.Entry<K, Value>> i = m.iterator(); i.hasNext();)
    {
      Map.Entry<K, Value> me = i.next();
      put(me.getKey(), me.getValue());
    }
  }
  /** @return a DataNode bearing a key that might match the given
   * String, but has not been checked for this.
   */
  private DataNode<K, Value, DescendantInfo> search(K k)
  {
    if (head == null)
    {
      return null;
    }
    Node<K, Value, DescendantInfo> p = head;
    for(Node<K, Value, DescendantInfo> q = p.next(k);
      q != null; q = p.next(k))
    {
      p = q;
    }
    return (DataNode<K, Value, DescendantInfo>)p;
  }
  /** Retrieve a value whose key matches the string quoted, or null.
   * @param k a key to look for in the map
   */
  public Value get(K k)
  {
    // System.err.println("Before search");
    DataNode<K, Value, DescendantInfo> p = search(k);
    // System.err.println("After search");
    if (p == null)
    {
      // System.err.println("Search null");
      return null;
    }
    return p.getValue(k);
  }

  /** Insert a key-value pair, overwriting any existing exact
   * match. The key should not be a prefix of any existing key,
   * and no existing key should be a prefix of it.
   * @param key the key
   * @param value its value
   * @exception IllegalArgumentException if the key is a prefix of
   * another key, or an existing key is a prefix of this, and this
   * is detected. Such a situation is improper use, but is not
   * guaranteed to be detected, here or elsewhere. Some key classes,
   * such as StringKey, make this impossible.
   */
  public Value put(K key, Value value)
  {
    if (head == null)
    {
      head = new DataNode<K, Value, DescendantInfo>(key, value);
      numItems++;
      return null;
    }
    DataNode<K, Value, DescendantInfo> p = search(key);
    boolean[] found = new boolean[1];
    Value result = p.acceptPair(key, value, this, factory, found);
    if (!found[0])
    {
      numItems++;
    }
    return result;
  }
  /** Delete an exactly matching string.
   * @param s a string exactly matching the key of a
   * key-value pair to be deleted.
   * @return the previously associated value, or null
   */
  public Value remove(K k)
  {
    if (head == null)
    {
      return null;
    }
    DataNode<K, Value, DescendantInfo> n = search(k);
    if (n == null)
    {
      return null;
    }
    if (n.key.firstDifference(k) >= 0)
    {
      return null;
    }
    numItems--;
    return n.delete(k, this);
  }
  /** number of items in the map */
  private int numItems;
  /** Number of items in the map
  */
  public int size()
  {
    if (head == null)
    {
      return 0;
    }
    return numItems;
  }

  /** Utility: add binary value of int to StringBuilder, encoding
   * it so that string sort order is int sort order.
   * @param sb the StringBuilder to append to
   * @param y the int to encode and append
   */
  public static void appendBinInt(StringBuilder sb, int y)
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
  /** Utility: append binary encoded long to StringBuilder,
   *  encoding it so that string order is long order.
   * @param sb the StringBuilder to append to
   * @param y the value to encode and append
   */
  public static void appendBinLong(StringBuilder sb, long y)
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
  /** Utility: append binary encoded double to StringBuilder, again
   * ensuring sorting order is consistent.
   * @param sb the StringBuilder to append to
   * @param d the Double to encode and append
   */
  public static void appendBinDouble(StringBuilder sb, double d)
  {
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
    return MaxInRange.fromOrderedLong(getBinLong(s, offset));
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
  /** check invariants (traverses entire tree so slow). Assumes that
   *  all DescendantInfo objects will return true.
   */
  public void checkInvariants()
  {
    if (checkHead(head, null) != numItems)
    {
      throw new IllegalStateException("Item count mismatch");
    }
  }
  /** recursive routine to check invariants. Slow and could possibly
   *  run off top of stack for very strange input values
   */
  private int checkHead(Node<K, Value, DescendantInfo> here,
    Node<K, Value, DescendantInfo> hereParent)
  {
    if (here == null)
    {
      if (hereParent != null)
      {
        throw new IllegalStateException("null pointer in tree");
      }
      return 0;
    }
    if (here.parent != hereParent)
    {
      throw new IllegalStateException("parent pointer mismatch");
    }
    if (here.getKey() != null)
    { // one data node here
      if (!(here instanceof DataNode))
      {
        throw new IllegalStateException("Fake data node");
      }
      if (here.getDescendantInfo() != null)
      {
	throw new IllegalStateException("Both key and descendantInfo");
      }
      if (here.firstChild() != null)
      {
        throw new IllegalStateException("Data node with left child");
      }
      if (here.lastChild() != null)
      {
        throw new IllegalStateException("Data node with right child");
      }
      return 1;
    }
    DescendantInfo di = here.getDescendantInfo();
    if (di == null)
    {
      throw new IllegalStateException("Neither key nor descendantInfo");
    }
    Node<K, Value, DescendantInfo> left = here.firstChild();
    if (left == null)
    {
      throw new IllegalStateException(
        "Internal node with no left child");
    }
    Node<K, Value, DescendantInfo> right = here.lastChild();
    if (right == null)
    {
      throw new IllegalStateException(
        "Internal node with no right child");
    }
    if (!(here instanceof InternalNode))
    {
      throw new IllegalStateException("Fake internal node");
    }
    InternalNode<K, Value, DescendantInfo> internal = 
      (InternalNode<K, Value, DescendantInfo>)here;
    if (!internal.callUpdate())
    {
      throw new IllegalStateException("DescendantInfo returned false");
    }
    return checkHead(left, here) + checkHead(right, here);
  }
}
