package uk.co.demon.mcdowella.stats;

import java.util.Comparator;

/** Heapsort routine. This code is largely legacy, now
 * that Java 1.2/2 has Collections.sort. You should probably
 * use that instead - which is why this isn't public.
 */
class HeapSort
{
  private HeapSort() {}
  // Invariant is that array[x] >= array[x*2-first+1], array[x*2-first+2]
  static final void sort(Object array[], int first, int past,
                         Comparator cmp)
  {
    int offset= -first+1;
    // Build heap
    for(int i=(first+past)/2;i>=first;i--)
    {
      int p=i;
      for(;;)
      {
        int j=2*p+offset;
        if (j>=past)
          break;
        int k=j+1;
        if(k<past&&cmp.compare(array[j],array[k])<0)
          j=k;
        if(cmp.compare(array[j],array[p])<=0)
          break;
        Object t=array[j];
        array[j]=array[p];
        array[p]=t;
        p=j;
      }
    }
    // Destroy heap
    for(int i=past-1;i>first;i--)
    {
      Object t=array[i];
      array[i]=array[first];
      array[first]=t;
      int p=first;
      for(;;)
      {
        int j=2*p+offset;
        if(j>=i)
          break;
        int k=j+1;
        if(k<i&&cmp.compare(array[j],array[k])<0)
          j=k;
        if(cmp.compare(array[j],array[p])<=0)
          break;
        t=array[j];
        array[j]=array[p];
        array[p]=t;
        p=j;
      }
    }
  }
  // Invariant is that array[x] >= array[x*2-first+1], array[x*2-first+2]
  static void sort(double array[], int first, int past)
  {
    int offset= -first+1;
    // Build heap
    for(int i=(first+past)/2;i>=first;i--)
    {
      int p=i;
      for(;;)
      {
        int j=2*p+offset;
        if (j>=past)
          break;
        int k=j+1;
        if(k<past&&array[j]<array[k])
          j=k;
        if(array[j]<=array[p])
          break;
        double t=array[j];
        array[j]=array[p];
        array[p]=t;
        p=j;
      }
    }
    // Destroy heap
    for(int i=past-1;i>first;i--)
    {
      double t=array[i];
      array[i]=array[first];
      array[first]=t;
      int p=first;
      for(;;)
      {
        int j=2*p+offset;
        if(j>=i)
          break;
        int k=j+1;
        if(k<i&&array[j]<array[k])
          j=k;
        if(array[j]<=array[p])
          break;
        t=array[j];
        array[j]=array[p];
        array[p]=t;
        p=j;
      }
    }
  }
  // Turn doubles into ranks
  static double[] rank(double x[])
  {
    RankPair r[]=new RankPair[x.length];
    for(int i=0;i<x.length;i++)
    {
      r[i]=new RankPair();
      r[i].value=x[i];
      r[i].position=i;
    }
    sort(r,0,x.length,new RankCmp());
    int i,j;
    double y[]=new double[x.length];
    for(i=0,j=1;i<x.length;i=j)
    {
      while(j<x.length && r[j].value==r[i].value)
        j++;
      double s=(i+j-1)*0.5;
      for(int k=i;k<j;k++)
        y[r[k].position]=s;
    }
    return y;
  }

  public static void main(String args[])
  {
    int len=1000;
    int offset=0;
    Double a[]=new Double[len];
    for(int i=offset;i<len;i++)
      a[i]=new Double(Math.random());
    HeapSort.sort(a,offset,len,new DoubleCmp());
    int i;
    for(i=offset+1;i<len;i++)
      if(a[i].doubleValue()<a[i-1].doubleValue())
      {
        System.out.println("Trouble!! offset "+i);
        break;
      }
    if(i>=len)
      for(i=offset;i<len;i++)
        System.out.println(a[i]);
  }
}

class DoubleCmp implements Comparator
{
  public int compare(Object a, Object b)
  {
    double da=((Double)a).doubleValue();
    double db=((Double)b).doubleValue();
    if(da<db)
      return -1;
    if(da>db)
      return 1;
    return 0;
  }
}

class RankPair
{
  double value;
  int position;
}

class RankCmp implements Comparator
{
  public int compare(Object a, Object b)
  {
    RankPair ra=(RankPair)a;
    RankPair rb=(RankPair)b;
    if(ra.value<rb.value)
      return -1;
    if(ra.value>rb.value)
      return 1;
    return 0;
  }
}
