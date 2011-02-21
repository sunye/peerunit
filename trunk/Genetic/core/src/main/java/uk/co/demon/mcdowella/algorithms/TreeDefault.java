package uk.co.demon.mcdowella.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/**
 * Code by A.G.McDowell. This was written for fun, and has not been
 * independently tested, so use at your own risk! Feel free to copy,
 * spindle, mutilate, or whatever - but some acknowledgement would
 * be nice.
 * </br>
 * This class, and its inner classes, can be used to store
 * attribute-value information, where the nodes owning the attributes
 * form a tree. The value associated with an attribute at a particular
 * node is the value explicitly stored at that node, or, if no such
 * attribute is explicitly stored there, at the lowest possible ancestor
 * of that node. In other words, if the node you want doesn't contain
 * the desired attribute, look in its parent, or its parent's parent,
 * and so on.
 * <br>
 * Done naively, this would take time for each query linear
 * in the distance from the queried node to the parent holding the
 * desired information, and so, in the worst case, could grow with
 * the size of the tree. However, queries
 * can be answered in logarithmic time using only linear space if we
 * are allowed to build an index from the tree to be queried, in
 * linear time. This class represents such an index.
 * <br>
 * This class assigns each node a number, writing that into the node.
 * To refer to a node during a query, you pass in the number. After
 * this class has been built, you can discard the nodes, if you want.
 * The same is true of attributes. The number assigned to a node is
 * in fact a numbering in depth first search order, starting at zero,
 * and the calls to assign numbers to nodes are made in depth first
 * search order.
 * <br>
 * This implementation numbers the nodes of the tree in depth first
 * search order, and traverses the tree in that order. While doing
 * this, it uses a stack to keep track of the current value of
 * each attribute at a node, and the values it will revert to when the
 * implementation retreats up the tree. This allows it to write down
 * the numbers of
 * the nodes at which the value changes. It answers a query by doing
 * a binary search on this list to find the highest number in this
 * list &le; the node we are querying on. Each occurrence of an explicit
 * attribute setting in the tree causes at most two entries to be made
 * in the list of node occurrences, so the size of our array - and the
 * time taken to produce it - is at most linear in the size of the
 * tree.
 * <br>
 * Objects of this class are immutable once built, but have links
 * to mutable objects (such as the Node and Attribute classes). These
 * objects should not normally be modified while the TreeDefault is
 * in use. Under these circumstances, thread safety should not be
 * a problem. However, this code contains no synchronisation calls.
 */
public class TreeDefault
{
  /** Return the value associated with the given attribute
   *  at the numbered node quoted, or some ancestor of it, or null
   *  if no such value can be found.
   */
  public Object getValue(int nodeNumber, int attributeNumber)
  {
    // array of node numbers for this attribute
    AttributeOccurrence[] ao = attributeArray[attributeNumber];
    // find the highest numbered node &le; the given node number -
    // this is the last change seen if we traverse the tree from
    // the start to the given node
    int first = 0;
    int past = ao.length;
    Object maybe = null;
    while (first < past)
    {
      // Probe value is >= first and < past
      // (check: worst possible cases are (0, 1) => 0 and
      // (1, 2) => 1
      int probe = (first + past) >> 1;
      AttributeOccurrence sample = ao[probe];
      int num = sample.nodeNumber;
      if (num > nodeNumber)
      { // probe is too far along
        past = probe;
      }
      else if (num < nodeNumber)
      { // may be a match
        maybe = sample.value;
	first = probe + 1;
      }
      else
      { // exact match
        return sample.value;
      }
      // If here, distance betwen first and past has decreased
    }
    return maybe;
  }
  /** Utility routine to fetch the value associated with a given
   *  DefaultAttribute from a given DefaultNode
   */
  public Object getValueWithDefaults(DefaultNode node,
    DefaultAttribute attribute)
  {
    int atNum = attribute.getNumber();
    if (atNum == -1)
    { // never seen in tree
      return null;
    }
    return getValue(node.getNumber(), attribute.getNumber());
  }
  /** This interface represents a Node */
  public interface Node
  {
    /** Will be called to assign to it its number */
    void setNumber(int num);
    /** Should return an iterator providing objects implementing
     *  java.util.Map.Entry - these are the attributes and values
     *  at this node. The key, which is the attribute, must
     *  implement the interface Attribute.
     */
    Iterator getAttributeValueIterator();
    /** should return an iterator providing its child nodes */
    Iterator getChildrenIterator();
  }
  /** this interface represents an Attribute */
  public interface Attribute
  {
    /** set a number identifying this attribute */
    void setNumber(int num);
    /** must return the last value set by setNumber(). Need not
     *  return any particular value before setNumber() is called, but
     *  must not throw an exception. However, attributes never
     *  encountered in the tree are never numbered, so you may wish
     *  find some way of distinguishing them for yourself. The
     *  number used is a 1-up number starting from 0, so all attribute
     *  numbers we assign should be &ge; 0.
     */
    int getNumber();
  }
  /** This class holds the info we need about an occurrence of
   *  an attribute
   */
  private static class AttributeOccurrence
  {
    /** The number of the node at which this value comes into force.
     *  This could either be an explicit set, or the result of
     *  the value reverting after returning up from a node at which
     *  an explicit set was made.
     */
    final int nodeNumber;
    /** The associated value */
    final Object value;
    AttributeOccurrence(int number, Object v)
    {
      nodeNumber = number;
      value = v;
    }
  }
  /** This class holds the info we need about an attribute while
   *  we build our main datastructures.
   */
  private static class AttributeInfo
  {
    /** Attribute in question */
    final Attribute thisAttribute;
    /** List of AttributeOccurrences, in order */
    final List occurrenceList;
    /** Stack of AttributeOccurrences seen in nodes directly above
     *  the current node
     */
    final List stackedOccurrences;
    AttributeInfo(Attribute at)
    {
      thisAttribute = at;
      occurrenceList = new ArrayList();
      stackedOccurrences = new ArrayList();
    }
  }
  /** This is the attribute occurrence information in the form used
   *  to answer queries. attributeInfo[atNo] holds an array of
   *  AttributeInfo structures for the attribute numbered atNo.
   *  They are held in order of nodeNumber (and also created in this
   *  order, so they don't need to be sorted).
   */
  private final AttributeOccurrence[][] attributeArray;
  /** Return the number of attributes created */
  public int getNumAttributes()
  {
    return attributeArray.length;
  }
  /** Use this to number the nodes in depth first search order */
  private int nextNodeNumber = 0;
  /** List of Attributes encountered, numbered in the order in which
   *  they are encountered. Set to null on exit from constructor.
   */
  private List attributeInfoList = new ArrayList();
  /** This is the information we need for our explicit recursion
   *  stack
   */
  private static class RecursionStackEntry
  {
    final Node n;
    final Iterator i;
    RecursionStackEntry(Node theNode, Iterator it)
    {
      n = theNode;
      i = it;
    }
  }
  /** Add an attribute to the end of an attributeInfo list and
   *  replace any attribute already there with the same number. This
   *  can happen on exit if a child and its parent both set the
   *  same attribute, or on entry if two siblings set the same
   *  attribute.
   */
  private static void addAttributeOccurrence(List attributeInfoList,
    AttributeOccurrence ao)
  {
    int pos = attributeInfoList.size() - 1;
    if (pos > 0)
    {
      AttributeOccurrence current =
        (AttributeOccurrence)attributeInfoList.get(pos);
      if (current.nodeNumber == ao.nodeNumber)
      {
        attributeInfoList.remove(pos);
      }
    }
    attributeInfoList.add(ao);
  }
  /** This gets called when we enter a node in depth first search */
  private void handleNodeEntry(RecursionStackEntry rse)
  {
    Node n = rse.n;
    int thisNumber = nextNodeNumber++;
    n.setNumber(thisNumber);
    for (Iterator i = n.getAttributeValueIterator(); i.hasNext();)
    {
      Map.Entry me = (Map.Entry)i.next();
      Attribute attribute = (Attribute)me.getKey();
      int atNumber = attribute.getNumber();
      AttributeInfo ai;
      if ((atNumber >= attributeInfoList.size()) ||
          (atNumber < 0) ||
          (((AttributeInfo)attributeInfoList.get(atNumber)).
	    thisAttribute != attribute))
      { // have not seen this attribute before
	attribute.setNumber(attributeInfoList.size());
	ai = new AttributeInfo(attribute);
	attributeInfoList.add(ai);
      }
      else
      {
        ai = (AttributeInfo)attributeInfoList.get(atNumber);
      }
      AttributeOccurrence occurrence =
        new AttributeOccurrence(thisNumber, me.getValue());
      addAttributeOccurrence(ai.occurrenceList, occurrence);
      ai.stackedOccurrences.add(occurrence);
    }
  }
  /** This gets called when we exit a node in depth first search 
  */
  private void handleNodeExit(RecursionStackEntry rse)
  {
    Node n = rse.n;
    for (Iterator i = n.getAttributeValueIterator(); i.hasNext();)
    {
      Attribute at = (Attribute)((Map.Entry)i.next()).getKey();
      AttributeInfo ai = 
        (AttributeInfo)attributeInfoList.get(at.getNumber());
      // remove our value from the list of stacked occurrences
      int pos = ai.stackedOccurrences.size() - 1;
      ai.stackedOccurrences.remove(pos--);
      // Find the previous occurrence, if any
      Object value = null;
      if (pos >= 0)
      {
        AttributeOccurrence prev =
	  (AttributeOccurrence)ai.stackedOccurrences.get(pos);
	value = prev.value;
      }
      // Add an attributeOccurrence for the next node, cancelling
      // out the value added here
      addAttributeOccurrence(ai.occurrenceList,
	new AttributeOccurrence(nextNodeNumber, value));
    }
  }
  /** Construct from the root of a tree of nodes */
  public TreeDefault(Node root)
  {
    if (root != null)
    {
      // The first stage is a depth-first search of the tree. By
      // keeping a stack of values per attribute, we can keep track
      // of the values assigned to each attribute at each node as we
      // traverse the tree, and note down in ArrayLists where these
      // values change. For this depth-first search we use an explicit
      // stack, rather than recursion, because we cannot control the
      // depth of the tree, which determines the depth of the recursion.
      List stack = new ArrayList();
      RecursionStackEntry rse = new RecursionStackEntry(root,
	root.getChildrenIterator());
      handleNodeEntry(rse);
      for (;;)
      {
	if (rse.i.hasNext())
	{
	  Node lowerNode = (Node)rse.i.next();
	  RecursionStackEntry lower = new RecursionStackEntry(
	    lowerNode, lowerNode.getChildrenIterator());
	  handleNodeEntry(lower);
	  if (lower.i.hasNext())
	  {
	    stack.add(rse);
	    rse = lower;
	  }
	  else
	  {
	    handleNodeExit(lower);
	  }
	}
	else
	{
	  handleNodeExit(rse);
	  int len = stack.size();
	  if (len <= 0)
	  {
	    break;
	  }
	  len--;
	  rse = (RecursionStackEntry)stack.remove(len);
	}
      }
    }
    // The last stage is to populate attributeArray from 
    // attributeInfoList
    attributeArray =
      new AttributeOccurrence[attributeInfoList.size()][];
    for (int i = 0; i < attributeArray.length; i++)
    {
      AttributeInfo ai = (AttributeInfo)attributeInfoList.get(i);
      List l = ai.occurrenceList;
      AttributeOccurrence[] ao = new AttributeOccurrence[l.size()];
      attributeArray[i] = (AttributeOccurrence[])l.toArray(ao);
    }
    // no longer need attributeInfoList
    attributeInfoList = null;
  }
  /** Utility implementation of Node */
  public static class DefaultNode implements Node
  {
    private int number;
    public void setNumber(int num)
    {
      number = num;
    }
    /** returns the number assigned to this node */
    public int getNumber()
    {
      return number;
    }
    private final Map valueByAttribute = new HashMap();
    private final List children = new ArrayList();
    /** Add a child to this node */
    public void addChild(Node n)
    {
      children.add(n);
    }
    /** return number of children */
    public int getNumChildren()
    {
      return children.size();
    }
    /** return child at given location */
    public DefaultNode getChild(int index)
    {
      return (DefaultNode)children.get(index);
    }
    /** Associate an attribute-value pair with this node */
    public void put(Attribute attribute, Object value)
    {
      valueByAttribute.put(attribute, value);
    }
    /** retrieve from local attribute-value info */
    public Object get(Attribute attribute)
    {
      return valueByAttribute.get(attribute);
    }
    public Iterator getAttributeValueIterator()
    {
      return valueByAttribute.entrySet().iterator();
    }
    public Iterator getChildrenIterator()
    {
      return children.iterator();
    }
    /** return an unmodifiable copy of the Map of attribute-value
     *  settings held at this node (just this node, so does not
     *  say anything about attribute-value settings held at its
     *  parents).
     */
    public Map getUnmodifiableAttributeMap()
    {
      return Collections.unmodifiableMap(valueByAttribute);
    }
  }
  /** Utility implementation of Attribute */
  public static class DefaultAttribute implements Attribute
  {
    /** Use -1 to mean "never seen" */
    private int number = -1;
    public void setNumber(int num)
    {
      number = num;
    }
    public int getNumber()
    {
      return number;
    }
  }
  /** For debugging only */
  void dump()
  {
    for (int i = 0; i < attributeArray.length; i++)
    {
      System.out.println("Attribute " + i);
      AttributeOccurrence[] oca = attributeArray[i];
      for (int j = 0; j < oca.length; j++)
      {
	AttributeOccurrence here = oca[j];
        System.out.println("Value at " + here.nodeNumber + " is " +
	  here.value);
      }
    }
  }
}
