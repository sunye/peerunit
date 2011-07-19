package uk.co.demon.mcdowella.misc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.util.Date;
import java.text.DateFormat;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import java.util.Random;
import java.awt.Rectangle;
import java.text.SimpleDateFormat;
import javax.swing.Timer;

/** This class blinks a number of small circles against a
 *  black background
 */
public class Blinker extends JPanel
{
  /** x co-ordinates of stars scaled between 0 and 1 */
  private double[] xCoord = null;
  /** y co-ordinates of stars scaled between 0 and 1 */
  private double[] yCoord = null;
  /** radius of stars, in pixels */
  private int radius = 0;
  /** time to wait before clearing */
  private int waitTime = 0;
  /** deadline before clearing */
  private long clearDeadline = 0;
  /** whether waiting for a clear */
  private boolean waitingClear = false;
  /** colour of stars */
  private Color color;
  /** action coming from timer */
  private boolean timerInvoked = false;
  /** Timer used to schedule repaints */
  private final Timer paintTimer;
  public Blinker()
  {
    paintTimer = new Timer(waitTime,
    new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
	// Have come from timer. This works because everything
	// here runs on the GUI thread
	// System.err.println("Action!");
	timerInvoked = true;
        repaint();
      }
    });
  }
  /** Blink using coordinates given. Must be called on GUI thread */
  public void blink(double[] x, double[] y, Color c, int theRadius,
    int time)
  {
    xCoord = (double[])x.clone();
    yCoord = (double[])y.clone();
    radius = theRadius;
    color = c;
    waitTime = time;
    repaint();
  }
  /** paint stars, if any. Must be on gui thread */
  public void paintComponent(Graphics g)
  {
    long now = System.currentTimeMillis();
    // System.err.println("Now " + now + " deadline " + clearDeadline);
    if (waitingClear)
    {
      long toWait = clearDeadline - now;
      if (toWait <= 0)
      { // want draw to clear
        xCoord = yCoord = null;
	waitingClear = false;
	timerInvoked = false;
        paintTimer.stop();
      }
      else if (timerInvoked)
      {
        paintTimer.setDelay((int)toWait);
	timerInvoked = false;
      }
    }
    super.paintComponent(g);
    if ((xCoord != null) && (yCoord != null) && (xCoord.length > 0) &&
        (xCoord.length == yCoord.length))
    {
      Rectangle r = getBounds();
      Insets in = getInsets();
      int xStart = in.left + radius;
      int xPast = r.width - in.right - radius;
      int yStart = in.top + radius;
      int yPast = r.height - in.bottom - radius;
      double xScale = xPast - xStart;
      double yScale = yPast - yStart;
      Color oldColor = g.getColor();
      g.setColor(color);
      // g.drawLine(xStart, yStart, xPast, yPast);
      int diam = radius * 2;
      for (int i = 0; i < xCoord.length; i++)
      {
        int xPos = (int)Math.round(xStart + xCoord[i] * xScale);
        int yPos = (int)Math.round(yStart + yCoord[i] * yScale);
	g.fillArc(xPos, yPos, diam, diam, 0, 360);
      }
      g.setColor(oldColor);
      if (!waitingClear)
      {
        waitingClear = true;
	clearDeadline = now + waitTime;
	paintTimer.setDelay(waitTime);
	paintTimer.start();
      }
      // System.err.println("starred");
    }
    else
    {
      // System.err.println("Cleared");
    }
  }
  public static void main(String[] s)
  {
    JFrame jf = new JFrame();
    jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    Container cp = jf.getContentPane();
    long seed = (new Random()).nextLong();
    System.err.println("seed is " + seed);
    BlinkerTest bt = new BlinkerTest(seed);
    cp.setLayout(new BorderLayout());
    cp.add(bt, BorderLayout.CENTER);
    jf.pack();
    jf.show();
  }
}

class BlinkerTest extends JPanel
{
  /** button to press to start */
  private JButton goButton = new JButton("Go");
  /** whether next question is to be for left or right eye */
  private boolean leftNext = false;
  /** number of questions for left eye */
  private int leftQuestions = 0;
  /** number of correct answers from left eye */
  private int leftAnswers = 0;
  /** number of questions for right eye */
  private int rightQuestions = 0;
  /** number of correct answers from right eye */
  private int rightAnswers = 0;
  /** number of points placed */
  private int numPoints;
  /** shows number of points placed */
  private JLabel result = new JLabel();
  /** shows score for left eye */
  private JLabel leftTotal = new JLabel();
  /** shows score for right eye */
  private JLabel rightTotal = new JLabel();
  /** Shows which eye to use */
  JLabel eye = new JLabel("Right");
  private final Blinker b = new Blinker();
  private final JTextField delay = new JTextField("100");
  private final Timer pauseTimer;
  private final Random ran;
  private long flashDeadline;
  private boolean waitingFlash;
  private final JTextField timeBefore = new JTextField("1.0");
  private JRadioButton[] size = new JRadioButton[]
	{new JRadioButton("One"), new JRadioButton("Two"),
	 new JRadioButton("Three"), new JRadioButton("Four"),
	 new JRadioButton("Five")};
  private void setButtons(boolean enabled)
  {
    goButton.setEnabled(!enabled);
    for (int i = 0; i < size.length; i++)
    {
      size[i].setEnabled(enabled);
      if (!enabled)
      {
        size[i].setSelected(false);
      }
    }
  }
  BlinkerTest(long seed)
  {
    ran = new Random(seed);
    setLayout(new BorderLayout());
    b.setBackground(Color.black);
    b.setOpaque(true);
    add(b, BorderLayout.CENTER);
    JPanel left = new JPanel();
    left.setLayout(new BorderLayout());
    JPanel controls = new JPanel();
    controls.setLayout(new GridLayout(0, 1));
    add(left, BorderLayout.EAST);
    controls.add(eye);
    controls.add(result);
    controls.add(leftTotal);
    controls.add(rightTotal);
    controls.add(new JLabel("Visible Time"));
    controls.add(delay);
    controls.add(new JLabel("Mean wait time"));
    controls.add(timeBefore);
    setButtons(false);
    goButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        goButton();
      }
    });
    controls.add(goButton);
    for (int i = 0; i < size.length; i++)
    {
      JRadioButton here = size[i];
      final int seen = i + 1;
      here.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
	{
	  userSaw(seen);
	}
      });
      controls.add(here);
    }
    left.add(controls, BorderLayout.NORTH);
    pauseTimer = new Timer(0, new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
	timerEvent();
      }
    });
    chooseEye();
  }
  private DateFormat df = new SimpleDateFormat(
   "yyyyMMdd HH:mm:ss");
  private void userSaw(int num)
  {
    result.setText(numPoints + " points drawn");
    setButtons(false);
    if (leftNext)
    {
      leftQuestions++;
      if (num == numPoints)
      {
        leftAnswers++;
      }
      leftTotal.setText(leftAnswers + " out of " + leftQuestions +
        " for left eye");
    }
    else
    {
      rightQuestions++;
      if (num == numPoints)
      {
        rightAnswers++;
      }
      rightTotal.setText(rightAnswers + " out of " + rightQuestions +
        " for right eye");
    }
    // make choice for next go
    chooseEye();
    System.out.println(leftNext + ", " + numPoints + ", " +
      num + ", " + df.format(new Date()));
  }
  /** timer expired */
  private void timerEvent()
  {
    boolean wantMore = false;
    if (waitingFlash)
    {
      long now = System.currentTimeMillis();
      long toGo = flashDeadline - now;
      if (toGo > 0)
      {
        pauseTimer.setDelay((int)toGo);
	wantMore = true;
      }
      else
      {
	waitingFlash = false;
	int pause = 0;
	try
	{
	  pause = Integer.parseInt(delay.getText().trim());
	}
	catch (NumberFormatException nfe)
	{
	}
	numPoints = ran.nextInt(5) + 1;
	double[] xCoord = new double[numPoints];
	double[] yCoord = new double[numPoints];
	for (int i = 0; i < numPoints; i++)
	{
	  xCoord[i] = ran.nextDouble();
	  yCoord[i] = ran.nextDouble();
	}
	Color c = Color.red;
	if (ran.nextInt(2) > 0)
	{
	  c = Color.green;
	}
	b.blink(xCoord, yCoord, c, 2, pause);
	setButtons(true);
      }
    }
    if (!wantMore)
    {
      pauseTimer.stop();
    }
  }
  /** select eye */
  private void chooseEye()
  {
    int left = ran.nextInt(2);
    leftNext = left > 0;
    if (leftNext)
    {
      eye.setText("Left");
    }
    else
    {
      eye.setText("Right");
    }
  }
  /** user pushed go so flash the screen */
  private void goButton()
  {
    double delaySecs = 1000;
    try
    {
      delaySecs = Double.parseDouble(timeBefore.getText().trim());
    }
    catch (NumberFormatException nfe)
    {
    }
    // Time given is mean delay in seconds. If we take -delaySecs*ln(X)
    // where X is uniform in (0, 1] we get an exponentially distributed
    // variable with that mean. Choose exponential because then time
    // to wait given time waited for so far has the original 
    // distribution
    double next;
    for (;;)
    {
      next = ran.nextDouble();
      if (next > 0.0)
      {
        break;
      }
    }
    int ms = (int)Math.round(delaySecs * Math.log(next) * -1000.0);
    pauseTimer.setDelay(ms);
    long now = System.currentTimeMillis();
    flashDeadline = now + ms;
    waitingFlash = true;
    pauseTimer.start();
    // System.err.println("Start deadline " + ms);
  }
}
