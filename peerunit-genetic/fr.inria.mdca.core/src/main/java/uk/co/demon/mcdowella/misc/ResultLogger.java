package uk.co.demon.mcdowella.misc;

import java.util.Date;
import java.text.DateFormat;
import java.io.PrintWriter;

/** This class logs results from Recog */
public class ResultLogger
{
  private final String name;
  private final int rows;
  private final int cols;
  private final PrintWriter pw;
  private final DateFormat ddf;
  public ResultLogger(String theName, int theRows, int theCols,
    PrintWriter thePw, DateFormat df)
  {
    name = theName;
    rows = theRows;
    cols = theCols;
    pw = thePw;
    ddf = df;
  }
  long whenStarted;
  int clicks;
  public void started(int clicksRequired)
  {
    whenStarted = System.currentTimeMillis();
    clicks = clicksRequired;
  }
  public void finished()
  {
    long now = System.currentTimeMillis();
    long taken = now - whenStarted;
    pw.print(ddf.format(new Date(now)));
    pw.print(',');
    pw.print(name);
    pw.print(',');
    pw.print(clicks);
    pw.print(',');
    pw.println(taken);
    pw.flush();
  }
}
