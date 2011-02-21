package uk.co.demon.mcdowella.misc;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.text.DateFormat;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.io.PrintWriter;
import java.util.Random;
import uk.co.demon.mcdowella.stats.RoughRandom;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

public class ButtonBearerFactoryImpl implements ButtonBearerFactory
{
  private final Random r;
  private final RoughRandom chooser;
  private final ButtonBearerInfo[] choices;
  private float fontSize;
  public void setFontSize(float f)
  {
    fontSize = f;
  }
  private static class ButtonBearerInfo
  {
    final int rows;
    final int cols;
    final String name;
    final String[] strings;
    ButtonBearerInfo(String theName, int theRows, int theCols,
      List forStrings)
    {
      name = theName;
      rows = theRows;
      cols = theCols;
      String[] asArray = new String[forStrings.size()];
      strings = (String[]) forStrings.toArray(asArray);
    }
  };
  private PrintWriter pw;
  private FileWriter fw;
  public void close() throws IOException
  {
    IOException ioe = null;
    pw.close();
    pw = null;
    try
    {
      fw.close();
    }
    catch (IOException ii)
    {
      if (ioe != null)
      {
        ioe = ii;
      }
    }
    finally
    {
      fw = null;
    }
    if (ioe != null)
    {
      throw ioe;
    }
  }
  public ButtonBearerFactoryImpl(long seed, File configFile,
    File outputFile) throws IOException
  {
    List weightList = new ArrayList();
    List buttonBearerInfoList = new ArrayList();
    FileReader fr = new FileReader(configFile);
    fw = new FileWriter(outputFile, true);
    pw = new PrintWriter(fw);
    try
    {
      BufferedReader br = new BufferedReader(fr);
      for (;;)
      {
	String line = br.readLine();
	if (line == null)
	{
	  break;
	}
	StringTokenizer st = new StringTokenizer(line);
	String name = st.nextToken();
	Double w = new Double(st.nextToken().trim());
	weightList.add(w);
	System.err.println("Got " + name + " weight " + w);
	int rows = Integer.parseInt(st.nextToken().trim());
	int cols = Integer.parseInt(st.nextToken().trim());
	List l = new ArrayList();
	while (st.hasMoreElements())
	{
	  l.add(st.nextToken());
	}
	if (l.size() < (rows * cols))
	{
	  throw new IllegalArgumentException("Too few strings");
	}
	buttonBearerInfoList.add(new ButtonBearerInfo(name, rows, cols,
	  l));
      }
    }
    finally
    {
      fr.close();
    }
    r = new Random(seed);
    double[] weightArr = new double[weightList.size()];
    for (int i = 0; i < weightArr.length; i++)
    {
      weightArr[i] = ((Double)weightList.get(i)).doubleValue();
    }
    chooser = new RoughRandom(weightArr, r);
    ButtonBearerInfo[] forChoices =
      new ButtonBearerInfo[buttonBearerInfoList.size()];
    choices =
      (ButtonBearerInfo[]) buttonBearerInfoList.toArray(forChoices);
  }
  private static final DateFormat df = new SimpleDateFormat("yyMMddHHmmss");
  public ButtonBearer createButtonBearer()
  {
    ButtonBearerInfo choice = choices[chooser.next()];
    int rows = choice.rows;
    int cols = choice.cols;
    String[] text = new String[rows * cols];
    for (;;)
    {
      boolean repeated = false;
      boolean[] seen = new boolean[choice.strings.length];
      for (int i = 0; i < text.length; i++)
      {
	int pos = r.nextInt(choice.strings.length);
	text[i] = choice.strings[pos];
	if (seen[pos])
	{
	  repeated = true;
	}
	seen[pos] = true;
      }
      if (repeated)
      {
	ResultLogger rl = new ResultLogger(choice.name, rows, cols, pw, df);
	ButtonBearer bearer = new ButtonBearer(rows, cols, text, rl,
	  fontSize);
	return bearer;
      }
    }
  }
}
