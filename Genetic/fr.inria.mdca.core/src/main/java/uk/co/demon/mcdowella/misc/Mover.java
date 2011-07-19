package uk.co.demon.mcdowella.misc;

import java.util.Arrays;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.JComponent;
import java.awt.Rectangle;

/** This class displays a collection of points (as small rectangles)
 *  which move from left to right and possibly up and down. The idea
 *  is to see whether detecting up and down movement is easier if there
 *  is no left to right movement
 */
public class Mover extends JComponent
{
  /** x values in sorted order */
  private final double[] xv;
  /** y values corresponding to x */
  private final double[] yv;
  /** movement markers corresponding to x */
  private final boolean[] uv;
  /** number of moving points */
  private final int numMoving;
  /** return the number of moving points */
  public int countMoving()
  {
    return numMoving;
  }
  /** create from x and y coordinates and a notion of whether the
   *  object is moving up and down. Region displayed will be
   *  [0, 1] x [0, 1] after application of movement.
   */
  Mover(double[] x, double[] y, boolean[] upDown, double
    xSpeed, double ySpeed, boolean forSmooth, int forSize)
  {
    if ((x.length != y.length) || (x.length != upDown.length))
    {
      throw new IllegalArgumentException("Length mismatch");
    }
    speed = xSpeed;
    movingSpeed = ySpeed;
    smooth = forSmooth;
    squareSize = forSize;
    ToSort[] ts = new ToSort[x.length];
    for (int i = 0; i < x.length; i++)
    {
      ts[i] = new ToSort(x[i], y[i], upDown[i]);
    }
    Arrays.sort(ts);
    xv = new double[ts.length];
    yv = new double[ts.length];
    uv = new boolean[ts.length];
    int count = 0;
    for (int i = 0; i < ts.length; i++)
    {
      ToSort here = ts[i];
      xv[i] = here.getX();
      yv[i] = here.getY();
      boolean moveHere = here.getUpDown();
      uv[i] = moveHere;
      if (moveHere)
      {
        count++;
      }
    }
    numMoving = count;
  }
  /** Class used to sort into x order */
  private static class ToSort implements Comparable
  {
    private final double x;
    private final double y;
    private final boolean moving;
    ToSort(double forX, double forY, boolean isMoving)
    {
      x = forX;
      y = forY;
      moving = isMoving;
    }
    public int compareTo(Object o)
    {
      ToSort other = (ToSort)o;
      if (x < other.x)
      {
        return -1;
      }
      if (x > other.x)
      {
        return 1;
      }
      return 0;
    }
    public double getX()
    {
      return x;
    }
    public double getY()
    {
      return y;
    }
    public boolean getUpDown()
    {
      return moving;
    }
  }
  /** start time or MAX_VALUE if not yet started */
  private long startTime = Long.MAX_VALUE;
  /** speed in x units per second */
  private final double speed;
  /** whether smooth movement */
  private final boolean smooth;
  /** return whether smooth is set or not */
  public boolean getSmooth()
  {
    return smooth;
  }
  /** speed of moving points per second */
  private final double movingSpeed;
  /** side of square drawn at point */
  private final int squareSize;
  public synchronized void paint(Graphics g)
  {
    setBackground(Color.red);
    Rectangle bounds = getBounds(null);
    Insets insets = getInsets();
    int left = bounds.x + insets.left;
    int right = bounds.x + bounds.width - insets.right;
    int top = bounds.y + insets.top;
    int bottom = bounds.y + bounds.height - insets.bottom;
    long now = System.currentTimeMillis();
    double timePassed;
    if (startTime > now)
    {
      startTime = now;
    }
    timePassed = (now - startTime) * 1.0e-3;
    double xOffset = speed * timePassed;
    if (!smooth)
    {
      xOffset = Math.floor(xOffset);
      // System.out.println("X " + xOffset);
    }
    double movingOffset = movingSpeed * timePassed;
    // Will clip x at [0, 1] so first of all find leftmost
    // point >= xOffset;
    int first = 0;
    int last = xv.length - 1;
    while (first > last)
    {
      int probe = (first + last) / 2;
      // first <= probe < last
      double val = xv[probe];
      if (val < xOffset)
      { // probe too far left
        first = probe + 1;
      }
      else
      { // still changes region as probe < last
        last = probe;
      }
    }
    int width = right - left + 1;
    int height = bottom - top + 1;
    Color before = g.getColor();
    g.setColor(Color.white);
    g.fillRect(left, top, width, height);
    g.setColor(before);
    if ((width <= 0) || (height <= 0))
    {
      return;
    }
    if ((xv.length <= 0) || (xv[xv.length - 1] < xOffset))
    {
      return;
    }
    double xScale = width;
    double yScale = height;
    before = g.getColor();
    g.setColor(Color.black);
    for (int i = first; i < xv.length; i++)
    {
      double xPos = xv[i] - xOffset;
      if (xPos > 1.0)
      { // finished
        break;
      }
      double yPos = yv[i];
      if (uv[i])
      {
        yPos += movingOffset;
      }
      yPos = yPos - Math.floor(yPos);
      int xp = (int)(xPos * xScale) + left;
      int yp = (int)(yPos * yScale) + top;
      g.fillRect(xp, yp, squareSize, squareSize);
    }
    g.setColor(before);
    repaint(10);
  }
}
