package uk.co.demon.mcdowella.algorithms;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.Collections;
import java.util.HashMap;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

/** This class is a demonstration and test harness for the
 *  various Template-related methods
 */
public class TemplateTest
{
  public static void main(String[] s) throws Exception
  {
    // System.out.println("Template is " + t);
    System.out.println("Simple template   --------------");
    BufferedReader r = openStream("TemplateTest.in1");
    ConsumableMap cm = readMap(r, new HashMap());
    Template t = new Template(r);
    r.close();
    StringChunk allResults = new StringChunk.StringCarrier("");
    StringChunk firstTemplateResult = t.tryGenerate(cm);
    BufferedWriter br = new BufferedWriter(new
      OutputStreamWriter(System.out));
    if (firstTemplateResult == null)
    {
      throw new IllegalStateException("Template failed");
    }
    else
    {
      firstTemplateResult.printAll(br);
    }
    allResults = allResults.append(firstTemplateResult);
    br.flush();
    System.out.println(
      "Use of template results within a second template----------- ");
    Map m = new HashMap();
    List l = new ArrayList();
    l.add(firstTemplateResult);
    m.put("template", l);
    r = openStream("TemplateTest.in2");
    cm = readMap(r, m);
    t = new Template(r);
    r.close();
    StringChunk secondTemplateResult = t.tryGenerate(cm);
    if (secondTemplateResult == null)
    {
      throw new IllegalStateException("Template failed");
    }
    else
    {
      secondTemplateResult.printAll(br);
    }
    allResults = allResults.append(secondTemplateResult);
    br.flush();
    System.out.println("Retrieve line number of error-----");
    StringChunk again = t.tryGenerate(new ConsumableMap(new HashMap()));
    if (again == null)
    {
      System.out.println("Got error return pointing at line " +
        t.getLastFailLine());
    }
    else
    {
      throw new IllegalStateException("Template succeeded!?!?!: " +
        again);
    }
    allResults = allResults.append(again);
    System.out.println("Generate HTML ----------------");
    // Generate a variety of input elements */
    r = openStream("TemplateTest.in3");
    t = new Template(r);
    r.close();
    List infoList = new ArrayList();
    String[][] values = {
      {"type", "text", "name", "text1", "value", "default for text1",
        "label", "First text field"},
      {"type", "text", "name", "text2", "label", "Second text field"},
      {"type", "checkbox", "name", "checkbox1", 
        "value", "checkbox1Value", "label", "Only check box"},
      {"type", "text", "name", "text2", "mandatoryMarker", "*",
      "label", "Mandatory text field"},
    };
    List fragments = new ArrayList();
    for (int i = 0; i < values.length; i++)
    {
      String[] valuesHere = values[i];
      Map insertMap = new HashMap();
      for (int j = 0; j < valuesHere.length;)
      {
	String key = valuesHere[j++];
	String result = valuesHere[j++];
	List ll = new ArrayList();
	ll.add(new StringChunk.StringCarrier(result));
	insertMap.put(key, ll.listIterator());
      }
      StringChunk created = t.tryGenerate(new ConsumableMap(insertMap));
      if (created == null)
      {
        throw new IllegalStateException("Creation failed at line " +
	  t.getLastFailLine());
      }
      fragments.add(created);
      allResults = allResults.append(created);
    }
    r = openStream("TemplateTest.in4");
    t = new Template(r);
    r.close();
    HashMap htmlMap = new HashMap();
    htmlMap.put("fragment", fragments.listIterator());
    List title = new ArrayList();
    title.add(new
      StringChunk.StringCarrier("Demo generated html page"));
    htmlMap.put("title", title.listIterator());
    StringChunk htmlPage = t.tryGenerate(new ConsumableMap(htmlMap),
      new StringChunk.StringCarrier(
      "<!DOCTYPE HTML PUBLIC \"-//W3C/DTD HTML 4.01//EN\"" +
      " \"http://www.w3.org/TR/html4/strict.dtd\">\n"));
    htmlPage.printAll(br);
    allResults = allResults.append(htmlPage);
    br.flush();
    System.out.println("ALLRESULTS");
    allResults.printAll(br);
    br.flush();
    // Now check the output so far against a hand-checked copy
    r = openStream("TemplateTest.check");
    Reader sr = new StringReader(allResults.toString());
    int line = 1;
    for(;;)
    {
      int a = r.read();
      int b = sr.read();
      if (a == '\n')
      {
        line++;
      }
      if (a != b)
      {
        throw new IllegalStateException("Mismatch at line " + line +
	  " Current output char " + (char)b + " was " + (char) a);
      }
      if (a < 0)
      {
        break;
      }
    }
  }
  private static ConsumableMap readMap(BufferedReader r, Map m)
    throws IOException
  {
    for (;;)
    {
      String ss = r.readLine();
      if (ss == null)
      {
        System.err.println("Unexpected end of file");
	System.exit(1);
      }
      ss = ss.trim();
      if (ss.equals("*EOF*"))
      {
        break;
      }
      String tt = r.readLine();
      List l = (List)m.get(ss);
      if (l == null)
      {
        l = new ArrayList();
	m.put(ss, l);
      }
      l.add(new StringChunk.StringCarrier(tt));
    }
    Map m2 = new HashMap();
    for (Iterator i = m.entrySet().iterator(); i.hasNext();)
    {
      Map.Entry me = (Map.Entry)i.next();
      // Use an UnmodifiableList to check that the modification
      // methods of the ListIterator are not called
      List l = Collections.unmodifiableList((List)me.getValue());
      m2.put(me.getKey(), l.listIterator());
    }
    return new ConsumableMap(m2);
  }
  private static BufferedReader openStream(String rname)
  {
    InputStream in = TemplateTest.class.getResourceAsStream(
      rname);
    if (in == null)
    {
      System.err.println("Could not find resource " + rname);
      System.exit(1);
    }
    BufferedReader r = new BufferedReader(new InputStreamReader(in));
    return r;
  }
}
