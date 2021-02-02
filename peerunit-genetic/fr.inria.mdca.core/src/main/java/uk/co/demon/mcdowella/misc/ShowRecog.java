package uk.co.demon.mcdowella.misc;

import java.util.ArrayList;
import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.awt.Color;
import java.awt.Container;
import java.io.FileReader;
import java.awt.Graphics;
import java.util.HashMap;
import java.awt.Insets;
import java.io.IOException;
import java.util.Iterator;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.awt.Rectangle;
import java.util.StringTokenizer;

/** This class reads in the Recog output file and produces
 *  balanced cumulative sum lines for each type. Units are
 *  milliseconds per click required
 */
public class ShowRecog extends JPanel
{
  /** Shows numbered position of point to display and value
   *
   */
  public static class Info
  {
    final int position;
    double value;
    Info(int pos, double v)
    {
      position = pos;
      value = v;
    }
  }
  /** Map from name to List of Info */
  private Map infoListByName = new HashMap();
  /** min value in adjusted cusum. Note value will be 0 at end */
  private double min = 0.0;
  /** max value in cusum */
  private double max = 0.0;
  /** maximum position reached */
  private int pos = 0;
  public ShowRecog(BufferedReader br) throws IOException
  {
    for (pos = 0;; pos++)
    {
      String line = br.readLine();
      if (line == null)
      {
        break;
      }
      StringTokenizer st = new StringTokenizer(line, ",");
      if (st.countTokens() != 4)
      {
        throw new IOException("Wrong number of tokens in line");
      }
      st.nextToken();
      String name = st.nextToken();
      int items;
      int ms;
      try
      {
	items = Integer.parseInt(st.nextToken().trim());
        ms = Integer.parseInt(st.nextToken().trim());
      }
      catch (NumberFormatException nfe)
      {
        throw new IOException("bad number in line");
      }
      List l = (List)infoListByName.get(name);
      if (l == null)
      {
        l = new ArrayList();
	infoListByName.put(name, l);
      }
      l.add(new Info(pos, items / (double)ms));
    }
    // Adjust lines so each sums to zero
    for (Iterator i = infoListByName.values().iterator(); i.hasNext();)
    {
      List l = (List)i.next();
      double sum = 0.0;
      for (Iterator j = l.iterator(); j.hasNext();)
      {
        sum += ((Info)j.next()).value;
      }
      sum = sum / l.size();
      double sofar = 0.0;
      int count = 1;
      for (Iterator j = l.iterator(); j.hasNext();)
      {
	Info here = (Info)j.next();
	sofar += here.value;
        here.value = sofar - sum * count;
	count++;
	if (here.value < min)
	{
	  min = here.value;
	}
	if (here.value > max)
	{
	  max = here.value;
	}
      }
    }
  }
  public void paint(Graphics g)
  {
    super.paint(g);
    Graphics gg = g.create();
    try
    {
      if (pos <= 0)
      { // no data
        return;
      }
      Rectangle r = getBounds();
      Insets in = getInsets();
      int minX = r.x + in.left;
      int maxX = r.x + r.width - in.right;
      int minY = r.y + in.top;
      int maxY = r.y + r.height - in.bottom;
      double yScale = (maxY - minY) / (max - min);
      double xScale = (maxX - minX) / (pos - 1.0);
      Random ran = new Random();
      for (Iterator i = infoListByName.values().iterator();
           i.hasNext();)
      {
        List here = (List)i.next();
	int oldX = 0;
	int oldY = (int)Math.round(max * yScale) + minY;
	Color c = new Color(ran.nextInt() & 255, ran.nextInt() & 255,
	  ran.nextInt() & 255);
	g.setColor(c);
	for (Iterator j = here.iterator(); j.hasNext();)
	{
	  Info info = (Info)j.next();
	  int x = (int)Math.round(info.position * xScale) + minX;
	  int y = (int)Math.round((max - info.value) * yScale) + minY;
	  g.drawLine(oldX, oldY, x, y);
	  oldX = x;
	  oldY = y;
	}
      }
    }
    finally
    {
      gg.dispose();
    }
  }
  public static void main(String[] s) throws IOException
  {
    FileReader fr = new FileReader(s[0]);
    ShowRecog sr;
    try
    {
      sr = new ShowRecog(new BufferedReader(fr));
    }
    finally
    {
      fr.close();
    }
    JFrame jf = new JFrame();
    Container cp = jf.getContentPane();
    cp.setLayout(new BorderLayout());
    cp.add(sr, BorderLayout.CENTER);
    jf.pack();
    jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    jf.show();
  }
}
