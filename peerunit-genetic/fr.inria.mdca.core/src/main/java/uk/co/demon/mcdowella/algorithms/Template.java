package uk.co.demon.mcdowella.algorithms;

import java.util.ArrayList;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.text.ParseException;
import java.io.Reader;

/** This class represents a template that generates
 *  StringChunks when handed ConsumableMap objects,
 *  containing maps from Strings to ListIterators of
 *  StringChunks. StringChunks are used because they
 *  have constant-time append, so it makes sense to build
 *  StringChunks up by using the result of running one
 *  template as input to another
 */
public class Template
{
  /** Tree of nodes starting at info is the compiled form of the
   *  template
   */
  private TemplateRecord info;
  /** Map from String found in %:stringHere( to ControlTemplate
   */
  private Map controlTemplateByName;
  /** Last template line at which an attempt to look up a key
   *  in the ConsumableMap failed
   */
  private int lastFailLine;
  /** return the last template line at which an attempt to look up a key
   *  in the ConsumableMap failed - or possibly some control record
   *  failed (but none of the standard control records set this)
   *  @return the last line number in the template where a %$id%
   *  or some user-extended control template failed
   */
  public int getLastFailLine()
  {
    return lastFailLine;
  }
  /** Set the last fail line */
  private void setLastFailLine(int x)
  {
    lastFailLine = x;
  }
  public String toString()
  {
    return "Template " + info;
  }
  /** Update controlTemplateByName, checking for duplicate
   *  definitions
   */
  private void checkPut(String key, ControlTemplate ct)
  {
    if (controlTemplateByName.containsKey(key))
    {
      throw new IllegalArgumentException("Control key " +
        key + " is already defined");
    }
    controlTemplateByName.put(key, ct);
  }
  /** Create by reading in. No buffering done here, so you
   * should probably pass in a BufferedReader. Can pass
   * in a Map to define custom control templates
   */
  public Template(Reader r, Map m) throws IOException,
    ParseException
  {
    controlTemplateByName = new HashMap(m);
    checkPut("fail", new FailControl());
    checkPut("try", new TryControl());
    checkPut("ignore", new TryIgnoreControl());
    checkPut("repeat", new RepeatControl());
    checkPut("release", new ReleaseControl());
    info = createTemplateRecord(new InfoReader(r));
    controlTemplateByName = null;
  }
  /** As for Template(r, m) but with default empty hashmap */
  public Template(Reader r) throws IOException, ParseException
  {
    this(r, new HashMap());
  }
  /** recursive descent template parser */
  private TemplateRecord createTemplateRecord(InfoReader r)
    throws IOException, ParseException
  {
    StringBuffer sb = new StringBuffer();
    int x;
    /** Accumulate TemplateRecords here */
    List l = new ArrayList();
    readLoop:for (;;)
    { // Here to read characters of text to be copied through
      for (;;)
      {
	x = r.read();
	if (x == -1)
	{
	  break;
	}
	if (x == '%')
	{
	  break;
	}
	sb.append((char)x);
      }
      if (x == -1)
      {
	if (sb.length() > 0)
	{
	  l.add(new StringTemplate(this, r, sb));
	  sb = new StringBuffer();
	}
        break;
      }
      x = r.read();
      char ch = (char)x;
      if (Character.isWhitespace(ch))
      { // %<whitespace> => consume space till next non-space or EOF
        for (;;)
	{
	  x = r.read();
	  if (x == -1)
	  {
	    l.add(new StringTemplate(this, r, sb));
	    sb = new StringBuffer();
	    break;
	  }
	  ch = (char) x;
	  if (Character.isWhitespace(ch))
	  {
	    continue;
	  }
	  r.unRead(ch);
	  break;
	}
	continue;
      }
      if (ch == '<')
      {
        // %< => ignore till >% or EOF
	for (;;)
	{
	  x = r.read();
	  if (x == -1)
	  {
	    break;
	  }
	  ch = (char)x;
	  if (ch == '>')
	  {
	    ch = r.readNoEof();
	    if (ch != '%')
	    {
	      r.error("Comment start %< without comment end >%");
	    }
	    break;
	  }
	}
	continue;
      }
      if (ch == 'x')
      { // %<hexDigits>% => hex character
        StringBuffer hexDigits = new StringBuffer();
	for (;;)
	{
	  ch = r.readNoEof();
	  if (Character.digit(ch, 16) == -1)
	  {
	    if (ch != '%')
	    {
	      r.error("Hex escape not terminated by %");
	    }
	    break;
	  }
	  hexDigits.append(ch);
	}
	String digitString = hexDigits.toString();
	try
	{
	  x = Integer.parseInt(digitString, 16);
	  sb.append((char)x);
	}
	catch (NumberFormatException nfe)
	{
	  r.error("Could not read hex number in " + digitString);
	}
	continue;
      }
      if (ch == '%')
      { // %% => %
        sb.append('%');
	continue;
      }
      l.add(new StringTemplate(this, r, sb));
      sb = new StringBuffer();
      StringBuffer id;
      switch (ch)
      {
        case '$':
	  // %$<id>% => substitute identifier in
	  id = readIDToChar(r, '%');
	  l.add(new IDTemplate(id, this, r));
	  break;
	case ':':
	  // %:name( .. %) is a control template
	  id = readIDToChar(r, '(');
	  l.add(getControlTemplate(id, this, r));
	  break;
	case ')': 
	  // end of a control template in a recursive call
	  r.unRead(ch);
	  break readLoop;
	default:
	  r.error("Bad character '" + ch + "' after initial %");
      }
    }
    if (l.size() == 1)
    {
      return (TemplateRecord)l.get(0);
    }
    for (Iterator i = l.iterator(); i.hasNext();)
    {
      Object o = i.next();
    }
    return new SequentialTemplate(l, this, r);
  }
  /** Read an identifier up to some specified terminating char */
  private StringBuffer readIDToChar(InfoReader r, char term)
    throws ParseException, IOException
  {
    StringBuffer sb = new StringBuffer();
    for (;;)
    {
      char ch = r.readNoEof();
      if (ch == term)
      {
	break;
      }
      if (!Character.isJavaIdentifierPart(ch))
      {
	r.error("Bad character '" + ch +
	  "' is not a Java identifier part");
      }
      sb.append(ch);
    }
    return sb;
  }
  /** Reader that keeps track of line numbers for us and
   *  provides one character of pushback 
   */
  private static class InfoReader
  {
    private Reader r;
    int charsSeen = 0;
    int linesSeen = 0;
    boolean isSaved = false;
    char saved;
    InfoReader(Reader aReader)
    {
      r = aReader;
    }
    int read() throws IOException
    {
      int x;
      if (isSaved)
      {
        isSaved = false;
	x = saved;
      }
      else
      {
	x = r.read();
      }
      if (x != -1)
      {
        charsSeen++;
	if (x == '\n')
	{
	  linesSeen++;
	}
      }
      return x;
    }
    char readNoEof() throws IOException, ParseException
    {
      int x = read();
      if (x == -1)
      {
        error("Unexpected EOF");
      }
      return (char)x;
    }
    void unRead(char ch)
    {
      if (isSaved)
      {
        throw new IllegalStateException("Push back too far");
      }
      charsSeen--;
      if (ch == '\n')
      {
        linesSeen--;
      }
      isSaved = true;
      saved = ch;
    }
    void error(String msg) throws ParseException
    {
      throw new ParseException("Error at line " + (linesSeen + 1) +
         " : " + msg, charsSeen);
    }
    int getLine()
    {
      return linesSeen + 1;
    }
  }
  /** Template-generate, appending to StringChunk */
  public StringChunk tryGenerate(ConsumableMap cm, StringChunk sc)
  {
    return info.tryGenerate(cm, sc);
  }
  /** Default template-generate is from empty StringChunk */
  public StringChunk tryGenerate(ConsumableMap cm)
  {
    return tryGenerate(cm, new StringChunk.StringCarrier(""));
  }
  /** Node in tree parsed from template */
  public static abstract class TemplateRecord
  {
    /** Line number when parsed */
    private int line;
    /** Owning template */
    private Template owner;
    /** Set error info */
    public final void setError()
    {
      owner.setLastFailLine(line);
    }
    TemplateRecord(Template aTemplate, InfoReader r)
    {
      line = r.getLine();
      owner = aTemplate;
    }
    /** Append the result of generating with the cm to
     *  sc and return it, or null to return failure
     */
    public abstract StringChunk tryGenerate(ConsumableMap cm,
      StringChunk sc);
    /** Return a pointer to the template owning by this */
    public Template getTemplate()
    {
      return owner;
    }
    /** Return the number of the line that was parsed to create this
    */
    public int getLine()
    {
      return line;
    }
  }
  /** This template node simply writes out a fixed string */
  private static class StringTemplate extends TemplateRecord
  {
    private StringChunk sc;
    private StringTemplate(Template owner, InfoReader r,
      StringBuffer sb)
    {
      super(owner, r);
      sc = new StringChunk.StringCarrier(sb.toString());
    }
    public String toString()
    {
      return "StringTemplate string " + sc;
    }
    public StringChunk tryGenerate(ConsumableMap cm, StringChunk s)
    {
      // System.err.println("StringTemplate generate " + sc);
      return s.append(sc);
    }
  }
  /** This template node substitutes in its child nodes in sequence */
  private static class SequentialTemplate extends TemplateRecord
  {
    private List l;
    private SequentialTemplate(List ll, Template owner, InfoReader r)
    {
      super(owner, r);
      l = new ArrayList(ll);
    }
    public StringChunk tryGenerate(ConsumableMap cm, StringChunk s)
    {
      StringChunk here = s;
      cm.mark();
      for (Iterator i = l.iterator(); i.hasNext();)
      {
	// System.err.println("Sequential TryGenerate here = " + here);
        TemplateRecord n = (TemplateRecord)i.next();
	// System.err.println("Sequential act with " + n);
	here = n.tryGenerate(cm, here);
	if (here == null)
	{
	  cm.reject();
	  return null;
	}
      }
      cm.accept();
      return here;
    }
    public String toString()
    {
      StringBuffer sb = new StringBuffer();
      sb.append("Sequential Template containing (");
      for (Iterator i = l.iterator(); i.hasNext();)
      {
        Object o = i.next();
	sb.append(' ');
	sb.append(o);
      }
      sb.append(')');
      return sb.toString();
    }
  }
  /** This template node does a lookup in the ConsumableMap */
  private static class IDTemplate extends TemplateRecord
  {
    private Template t;
    private String s;
    IDTemplate(StringBuffer sb, Template owner, InfoReader r)
    {
      super(owner, r);
      s = sb.toString();
    }
    public StringChunk tryGenerate(ConsumableMap cm, StringChunk sc)
    {
      // System.err.println("Look up "+ s);
      if (!cm.hasNext(s))
      {
	setError();
	// System.err.println("Not found");
        return null;
      }
      StringChunk a = (StringChunk)cm.next(s);
      // System.err.println("Got " + a);
      StringChunk r = sc.append(a);
      // System.err.println("Append " + a + " to " + sc + " got " + r);
      return r;
    }
  }
  /** This is the interface user-definable (and other) ControlTemplates
   *  should implement
   */
  public interface ControlTemplate
  {
    /** Generate and return the resulting StringChunk if you can
     *  or return null to fail
     *  @return null or StringChunk showing results of all generation
     *  so far
     *  @param cm The ConsumableMap to generate with
     *  @param inner a TemplateRecord representing all the text
     *  within the %:ControlTemplate(....%) brackets
     *  @param sc the StringChunk so far. Typically 
     *  result = sc.append(Something generated by this using inner)
     */
    StringChunk tryGenerate(ConsumableMap cm, TemplateRecord inner,
      StringChunk sc);
  }
  /** %:try(..%). Try generating the inner text. Append that
   *  if successfull, or append nothing and restore the state
   *  of the map if not
   */
  private static class TryControl implements ControlTemplate
  {
    public StringChunk tryGenerate(ConsumableMap cm,
      TemplateRecord inner, StringChunk sc)
    {
      cm.mark();
      StringChunk next = inner.tryGenerate(cm, sc);
      if (next == null)
      {
        cm.reject();
	return sc;
      }
      cm.accept();
      return next;
    }
  }
  /** %:fail(..%) Try generating the inner text. Always generate
   *  nothing and leave the map unchanged, but succeed iff the
   *  inner generation fails
   */
  private static class FailControl implements ControlTemplate
  {
    public StringChunk tryGenerate(ConsumableMap cm,
      TemplateRecord inner, StringChunk sc)
    {
      cm.mark();
      StringChunk next = inner.tryGenerate(cm, sc);
      if (next == null)
      {
	next = sc;
      }
      else
      {
        next = null;
      }
      cm.reject();
      return next;
    }
  }
  /** %:ignore(..%). Try generating the inner text and throw it
   *  away. Success or failure, and the state of the map, is as
   *  it was when the inner text was generated
   */
  private static class TryIgnoreControl implements ControlTemplate
  {
    public StringChunk tryGenerate(ConsumableMap cm,
      TemplateRecord inner, StringChunk sc)
    {
      StringChunk next = inner.tryGenerate(cm, sc);
      if (next == null)
      {
	return null;
      }
      return sc;
    }
  }
  /** %:repeat(..%) Repeatedly generate the inner text until
   * it fails. Restore the state and text generated to as they
   * were at the end of the last successful generation
   */
  private static class RepeatControl implements ControlTemplate
  {
    public StringChunk tryGenerate(ConsumableMap cm,
      TemplateRecord inner, StringChunk sc)
    {
      for (;;)
      {
	cm.mark();
	// System.err.println("Repeat adding to " + sc);
	StringChunk next = inner.tryGenerate(cm, sc);
	if (next == null)
	{
	  cm.reject();
	  // System.err.println("Repeat returns " + sc);
	  return sc;
	}
	cm.accept();
	sc = next;
      }
    }
  }
  /** %:release(..%) Try generating the inner text. Leave
   *  the state of success/failure and generation as they
   *  were after this try, but restore the state of the map
   */
  private static class ReleaseControl implements ControlTemplate
  {
    public StringChunk tryGenerate(ConsumableMap cm,
      TemplateRecord inner, StringChunk sc)
    {
      // System.err.println("Release mark at " + sc);
      cm.mark();
      StringChunk next = inner.tryGenerate(cm, sc);
      // System.err.println("Release reject at " + next);
      cm.reject();
      return next;
    }
  }
  /** read the bracketed text inside %:???(....%) */
  private TemplateRecord getControlTemplate(StringBuffer id,
    Template t, InfoReader r) throws ParseException, IOException
  {
    String s = id.toString();
    ControlTemplate ct = (ControlTemplate)controlTemplateByName.get(s);
    // System.err.println("Got " + ct + " from " + s);
    if (ct == null)
    {
      r.error("Could understand control word " + s);
    }
    TemplateRecord inner = createTemplateRecord(r);
    if (r.read() != ')')
    {
      r.error("Control template not properly terminated");
    }
    return new ControlTemplateRecord(ct, inner, t, r);
  }
  /** This node in the Template tree runs a ControlTemplate */
  private static class ControlTemplateRecord extends TemplateRecord
  {
    private ControlTemplate ct;
    private TemplateRecord inner;
    ControlTemplateRecord(ControlTemplate act, TemplateRecord tr,
      Template t, InfoReader ir)
    {
      super(t, ir);
      ct = act;
      inner = tr;
    }
    public StringChunk tryGenerate(ConsumableMap cm, StringChunk s)
    {
      return ct.tryGenerate(cm, inner, s);
    }
    public String toString()
    {
      return "ControlTemplate with " + ct + " containing " + inner;
    }
  }
}
