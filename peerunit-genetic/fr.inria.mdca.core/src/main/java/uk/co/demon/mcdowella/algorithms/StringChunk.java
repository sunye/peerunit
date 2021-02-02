package uk.co.demon.mcdowella.algorithms;

import java.util.ArrayList;
import java.io.IOException;
import java.util.List;
import java.io.Writer;

/** This class allows you to string together objects that
 *  can later be used to write out text or produce strings. In
 *  the mean time they can be appended to quickly, creating
 *  new objects without modifying old ones
 */
public abstract class StringChunk
{
  /** Left child if any */
  protected StringChunk getLeft()
  {
    return null;
  }
  /** Right child if any */
  protected StringChunk getRight()
  {
    return null;
  }
  /** Append this chunk (only) to a StringBuffer
   */
  public abstract void append(StringBuffer sb);
  /** Used to give some operator a view of every
   *  StringChunk in a tree in order
   */
  private interface Traveller
  {
    void see(StringChunk sc) throws Exception;
  }
  /** Append everything from here on to a StringBuffer
  */
  public void appendAll(final StringBuffer sb)
  {
    Traveller t = new Traveller()
    {
      public void see(StringChunk sc)
      {
        sc.append(sb);
      }
    };
    try
    {
      traverseChunk(t, this);
    }
    catch (Exception e)
    {
      throw new IllegalStateException("Unexpected exception " + e);
    }
  }
  /** Traverse the tree of StringChunks resulting from
   *  successive appends.
   *  Use an explicit stack instead of recursion because the
   *  trees produced here will probably be very unbalanced
   */
  private static void traverseChunk(Traveller t, StringChunk sc)
    throws Exception
  {
    List stack = new ArrayList();
    for (;;)
    { // Here with a fresh node to investigate: move down and left
      StringChunk next = sc.getLeft();
      // System.err.println("Got sc class " + sc.getClass().getName() +
      //   " has " + sc.hashCode());
      if (next != null)
      {
	// System.err.println("Push from left");
        stack.add(sc);
	sc = next;
	continue;
      }
      // Can't go any further left
      // System.err.println("See class " + sc.getClass().getName());
      t.see(sc);
      next = sc.getRight();
      if (next != null)
      {
	// System.err.println("Push from right");
        stack.add(sc);
	sc = next;
	continue;
      }
      for (;;)
      { // Done node sc and its descendants
	int l = stack.size();
	// System.err.println("Stack size " + l );
	if (l == 0)
	{
	  return;
	}
	next = (StringChunk)stack.remove(l - 1);
	// System.err.println("Got class " + next.getClass().getName() +
	//   " from stack code " + next.hashCode());
	if (next.getLeft() == sc)
	{ // Back up from left node
	  // System.err.println("See class " + next.getClass().getName());
	  t.see(next);
	  stack.add(next);
	  sc = next.getRight();
	  break;
	}
	// Here from right node
	sc = next;
      }
    }
  }
  /** Convert this chunk and everything appended to it to a
   *  single string
   */
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    appendAll(sb);
    return sb.toString();
  }
  /** Write out this chunk (only) to a writer */
  public abstract void print(Writer w) throws IOException;
  /** Write out everything from here on
   */
  public void printAll(final Writer w) throws IOException
  {
    Traveller t = new Traveller()
    {
      public void see(StringChunk sc) throws IOException
      {
        sc.print(w);
      }
    };
    try
    {
      traverseChunk(t, this);
    }
    catch (IOException ie)
    {
      throw ie;
    }
    catch (Exception e)
    {
      throw new IllegalStateException("Unexpected exception " + e);
    }
  }
  /** Append another StringChunk to this and return the result */
  public StringChunk append(StringChunk sc)
  {
    if (sc == null)
    {
      return this;
    }
    return new AppendedStringChunk(this, sc);
  }
  /** A StringChunk that contains a string */
  public static class StringCarrier extends StringChunk
  {
    private String contents;
    public StringCarrier(String s)
    {
      contents = s;
    }
    public void append(StringBuffer sb)
    {
      // System.err.println("Append called: contents " + contents);
      sb.append(contents);
    }
    public void print(Writer w) throws IOException
    {
      // System.err.println("Write called: contents " + contents);
      w.write(contents);
    }
  }
  /** A StringChunk resulting from an append */
  private static class AppendedStringChunk extends StringChunk
  {
    private StringChunk left;
    private StringChunk right;
    AppendedStringChunk(StringChunk l, StringChunk r)
    {
      left = l;
      right = r;
    }
    public StringChunk getLeft()
    {
      return left;
    }
    public StringChunk getRight()
    {
      return right;
    }
    public void append(StringBuffer sb)
    {
    }
    public void print(Writer w)
    {
    }
  }
}
