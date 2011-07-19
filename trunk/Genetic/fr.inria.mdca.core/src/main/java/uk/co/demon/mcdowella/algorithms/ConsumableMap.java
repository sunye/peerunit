package uk.co.demon.mcdowella.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;


/** This class holds a Map from keys to ListIterators. You
 *  can extract items from a listIterator by passing in its key,
 *  'consuming' it. You can ALSO call mark(), accept(), and reject()
 *  to mark the current state of consumption, accept the consumptions
 *  since the last nested mark(), or reject those consumptions
 */
public class ConsumableMap implements StackedState
{
  /** Map from list to iterators */
  private Map iteratorsByKey;
  /** Modify this Mark to record state changes: older Marks are
   *  linked to it
   */
  private Mark currentMark = null;
  /** Create from Map between keys and ListIterators.
   *  @param m a Map between keys and ListIterator that
   *  associates a key (the String) with a sequence of values
   *  (those that can be extracted from the ListIterator). Values
   *  are 'consumed' by using ListIterator.next() and retrieved
   *  by calling ListIterator.previous(). The modification methods
   *  of the ListIterator are never called: this code will work
   *  when passed a ListIterator produced from the result of
   *  Collections.unmodifiableList()
   */
  public ConsumableMap(Map m)
  {
    iteratorsByKey = m;
  }
  /* Retrieve the next value associated with the key, consuming
   * it, or throw NoSuchElementException
   * @param key the Key to lookup in the original map
   * @exception NoSuchElementException if no such key in
   * the map, or its ListIterator is exhausted
   */
  public Object next(Object key) throws NoSuchElementException
  {
    ListIterator li = (ListIterator)iteratorsByKey.get(key);
    if (li == null)
    {
      throw new NoSuchElementException("Key " + key + " not in map");
    }
    Object o = li.next();
    if (currentMark != null)
    { // record the use of this ListIterator for possible previous() on
      // reject()
      currentMark.used(li);
    }
    // System.err.println("Next returning " + o);
    return o;
  }
  /** Find out if next(key) will return successfully
   *  @param key the key to look up in the base map
   *  @return true iff there are still objects associated with the key
   */
  public boolean hasNext(Object key)
  {
    ListIterator li = (ListIterator)iteratorsByKey.get(key);
    if (li == null)
    {
      return false;
    }
    return li.hasNext();
  }
  /** Mark the current state of the map, so that we can return
   *  to it later by calling reject() if we want
   */
  public void mark()
  {
    // System.err.println("Mark: depth now " + depth);
    /*
    try
    {
      throw new IllegalStateException("trace");
    }
    catch (IllegalStateException ise)
    {
      ise.printStackTrace();
    }
    */
    currentMark = new Mark(currentMark);
  }
  /** Accept all the changes since the last nested mark()
   * @exception IllegalStateException if no matching mark()
   */
  public void accept()
  {
    // System.err.println("Accept: depth now " + depth);
    if (currentMark == null)
    {
      throw new IllegalStateException(
        "accept() without matching mark()");
    }
    currentMark = currentMark.accept();
  }
  /** Reject all the changes since the last nested mark()
   *  @exception IllegalStateException if no nested mark
   */
  public void reject()
  {
    // System.err.println("Reject: depth now " + depth);
    if (currentMark == null)
    {
      throw new IllegalStateException(
        "reject() without matching mark()");
    }
    currentMark = currentMark.reject();
  }
  /** Info recording use of ListIterator. These Records are
   *  appended to the current Mark when created, using a linked
   *  list
   */
  private static class Record
  {
    ListIterator li;
    Record next;
    Record(Record previous, ListIterator l)
    {
      li = l;
      next = previous;
    }
    /** Undo all changes in the linked list by winding back
     *  the listIterators
     */
    void reject()
    {
      for (Record r = this; r != null; r = r.next)
      {
	// System.err.println("Calling previous");
        Object o = r.li.previous();
	// System.err.println("Previous result is " + o);
      }
    }
    /** Append a chunk of Records to this, to implement accept()
    */
    void append(Record r)
    {
      if (next != null)
      {
        throw new IllegalStateException("Appending to record with tail");
      }
      next = r;
    }
  }
  /** Records the original state of the map, by storing lists of Records
   *  that can be used to wind it back to when this Map was
   *  originally created.
   */
  private class Mark
  {
    /** Pointer to previous nested mark */
    private Mark previousMark = null;
    Mark(Mark previous)
    {
      previousMark = previous;
    }
    /** Linked list of usage records */
    private Record records = null;
    private Record recordTail = null;
    /** Record the use of ListIterator */
    void used(ListIterator li)
    {
      records = new Record(records, li);
      if (recordTail == null)
      {
        recordTail = records;
      }
    }
    /** Wind back all iterators */
    Mark reject()
    {
      if (records != null)
      {
	records.reject();
      }
      return previousMark;
    }
    /** Merge with previous mark */
    Mark accept()
    {
      if (previousMark == null)
      {
        return null;
      }
      // Previous mark needs to receive all changes from this mark
      if (previousMark.recordTail == null)
      {
	previousMark.records = records;
      }
      else
      {
	previousMark.recordTail.append(records);
      }
      previousMark.recordTail = recordTail;
      return previousMark;
    }
  }
}
