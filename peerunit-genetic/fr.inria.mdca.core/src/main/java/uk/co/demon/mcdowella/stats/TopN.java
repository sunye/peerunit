package uk.co.demon.mcdowella.stats;

import java.util.Comparator;

/**
 * This class is used to keep track of the top N of any type of object,
 * ordered by some sort of comparison function. So that we can keep track
 * of the top N items, the heap normally has its smallest element at the root, and
 * the object at x will be at least as small as either of the objects at 2x+1, 2x+2
 */
public class TopN
{
  /**
   * Create a TopN structure, initially filled with null objects
   * @param n the number of objects the structure can hold
   * @param cmp the comparator used to compare two objects. This
   * Comparator will be called with one or more arguments null - when
   * the heap is created it is filled with null pointers. You probably
   * want these to compare low, so they get thrown away as proper elements
   * are added. You probably also want to make sure you don't try and put
   * null pointers into the heap, as you can't really tell if they're
   * there.
   */
  public TopN(int n, Comparator cmp)
  {
    v=new Object[n];
    c=cmp;
  }
  /**
   * insert a new object into the topN structure, returning the
   * object deleted to make room for it (may be the object you tried to insert,
   * which means that it was smaller than all the other objects in the heap
   * @param o the object to insert
   * @return the previous smallest object, or the object you tried to insert if smallest
   */
  public Object insert(Object o)
  {
    if(c.compare(o,v[0])<=0) // No point keeping this object in the heap
      return o;
    // New object replaces old worst
    Object old=v[0];
    v[0]=o;
    // Fix heap structure
    for(int p=0;;)
    {
      int q=2*p+1;
      if(q>=v.length) // No descendants in heap - structure is OK
        return old;
      int r=q+1; // Work out smallest of two descendants, if there are two
      if(r<v.length&&c.compare(v[r],v[q])<0)
        q=r;
      if(c.compare(v[p],v[q])<=0) // Structure OK?
        return old;
      // Fix up
      Object t=v[q];
      v[q]=v[p];
      v[p]=t;
      p=q;
    }
  }
  /**
   * @return the current contents of the heap, possibly in sorted order, highest first
   */
  public Object[] contents(boolean sorted)
  {
    int n=v.length;
    Object w[]=new Object[n];
    for(int i=0;i<n;i++)
    {
      w[i]=v[i];
    }
    // Don't lose much by ignoring existing heap structure because the heap is
    // built in linear time - the nlog n behaviour comes from extractions
    if(sorted) 
    {
      HeapSort.sort(w,0,n,c);
      for(int i=0;;i++)
      {
        int j=n-1-i;
        if(j<=i)
          break;
        Object o=w[i];
        w[i]=w[j];
        w[j]=o;
      }
    }
    return w;
  }
  private Object v[];
  private Comparator c;
}
