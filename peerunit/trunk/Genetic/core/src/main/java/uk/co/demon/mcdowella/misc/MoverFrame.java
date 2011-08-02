package uk.co.demon.mcdowella.misc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.BorderLayout;
import javax.swing.Box;
import java.awt.Container;
import java.util.Date;
import java.text.DateFormat;
import java.awt.Dimension;
import java.util.Iterator;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.util.List;
import java.util.Random;
import java.text.SimpleDateFormat;

/** This class forms the Frame and main program in which the Mover
 *  display appears
 */
public class MoverFrame extends JPanel
{
  private long seed = System.currentTimeMillis();
  /** generate a list with points up to len with expected freq points
   *  per unit
   */
  private static List randomList(Random r, double freq, double len)
  {
    List l = new ArrayList();
    double pos = 0;
    for (;;)
    {
      pos = pos - Math.log(r.nextDouble()) / freq;
      if (pos > len)
      {
        break;
      }
      l.add(new Double(pos));
    }
    return l;
  }
  /** used in output */
  private DateFormat df = new SimpleDateFormat(
      "yyyyMMdd HH:mm:ss");
  /** random number generator */
  private Random r = new Random(seed);
  /** whether next Mover will be the second of a pair */
  private boolean nextSecond = false;
  /** if second of a pair, whether first was smooth */
  private boolean firstSmooth;
  /** create a mover by parsing text fields */
  private Mover createMover(String lengthText,
    String perFrameText, String speedText, String movingText,
    String sizeText, String expectedPerFrame)
  {
    double len;
    double eMoving;
    double pf;
    double spd;
    double mvSpeed;
    int sz;
    try
    {
      len = Double.parseDouble(length.getText().trim());
      eMoving = Double.parseDouble(expectedMoving.getText().trim());
      pf = Double.parseDouble(perFrame.getText().trim());
      spd = Double.parseDouble(speed.getText().trim());
      mvSpeed = Double.parseDouble(movingSpeed.getText().trim());
      sz = Integer.parseInt(size.getText().trim());
    }
    catch (NumberFormatException nfe)
    {
      setResult("InvalidNumber");
      return null;
    }
    List xVals = randomList(r, pf, len);
    List movingVals = randomList(r, eMoving, len);
    int staticLen = xVals.size();
    double[] xArr = new double[staticLen + movingVals.size()];
    boolean[] moving = new boolean[xArr.length];
    Arrays.fill(moving, staticLen, xArr.length, true);
    int wp = 0;
    for (Iterator i = xVals.iterator(); i.hasNext();)
    {
      xArr[wp++] = ((Double)i.next()).doubleValue();
    }
    for (Iterator i = movingVals.iterator(); i.hasNext();)
    {
      xArr[wp++] = ((Double)i.next()).doubleValue();
    }
    double[] yArr = new double[xArr.length];
    for (int i = 0; i < yArr.length; i++)
    {
      yArr[i] = r.nextDouble();
      // System.out.println("x " + xArr[i] + " y " + yArr[i]);
    }
    boolean smoothHere;
    if (nextSecond)
    {
      smoothHere = !firstSmooth;
      nextSecond = false;
    }
    else
    {
      smoothHere = (r.nextInt(2) > 0);
      firstSmooth = smoothHere;
      nextSecond = true;
    }
    return new Mover(xArr, yArr, moving, spd, mvSpeed, 
      smoothHere, sz);
  }
  /** length text is length of strip of points to generate */
  private final JTextField length = new JTextField("30");
  /** Mover appears in the panel */
  private final JPanel forMoving = new JPanel();
  /** results appear here */
  private final JLabel results = new JLabel("No results yet");
  private void setResult(String message)
  {
    results.setText(message);
    results.invalidate();
    validate();
  }
  /** text is expected number of moving points per frame */
  private final JTextField expectedMoving = new JTextField("0.2");
  /** text is expected number of static pointer per frame */
  private final JTextField perFrame = new JTextField("20");
  /** text is speed at which points move */
  private final JTextField speed = new JTextField("2");
  /** text is speed at which moving points move up and down */
  private final JTextField movingSpeed = new JTextField("0.1");
  /** text is size of squares drawn */
  private final JTextField size = new JTextField("6");
  /** text is user count */
  private final JTextField userCount = new JTextField("???");
  /** last mover */
  private Mover lastMover;
  public MoverFrame()
  {
    setLayout(new BorderLayout());
    forMoving.setPreferredSize(new Dimension(600, 400));
    forMoving.setLayout(new BorderLayout());
    add(forMoving, BorderLayout.CENTER);
    add(results, BorderLayout.SOUTH);
    JPanel controlCarrier = new JPanel();
    controlCarrier.setLayout(new BorderLayout());
    add(controlCarrier, BorderLayout.EAST);
    Box controlBox = Box.createVerticalBox();
    controlCarrier.add(controlBox, BorderLayout.NORTH);
    controlBox.add(new JLabel("Length"));
    controlBox.add(length);
    controlBox.add(new JLabel("Per Frame"));
    controlBox.add(perFrame);
    controlBox.add(new JLabel("Expected Moving Per Frame"));
    controlBox.add(expectedMoving);
    controlBox.add(new JLabel("Speed"));
    controlBox.add(speed);
    controlBox.add(new JLabel("Moving Speed"));
    controlBox.add(movingSpeed);
    controlBox.add(new JLabel("Size"));
    controlBox.add(size);
    JButton go = new JButton("Go");
    go.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        Mover newMover = createMover(length.getText(),
	  perFrame.getText(), speed.getText(), movingSpeed.getText(),
	  size.getText(), expectedMoving.getText());
	if (newMover == null)
	{
	  lastMover = null;
	  return;
	}
	forMoving.removeAll();
	lastMover = newMover;
	forMoving.add(newMover, BorderLayout.CENTER);
	forMoving.validate();
      }
    });
    controlBox.add(go);
    controlBox.add(new JLabel("User Count"));
    controlBox.add(userCount);
    JButton count = new JButton("Count");
    count.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        doCount();
      }
    });
    controlBox.add(count);
  }
  /** Compare user count with right answer */
  private void doCount()
  {
    if (lastMover == null)
    {
      setResult("No display or old display");
      return;
    }
    int userSays;
    try
    {
      userSays = Integer.parseInt(userCount.getText().trim());
    }
    catch (NumberFormatException nfe)
    {
      setResult("User count not readable");
      return;
    }
    int answer = lastMover.countMoving();
    setResult("User count " + userSays + " answer " + answer +
      " finished pair: " + !nextSecond);
    System.out.println(df.format(new Date()) + ", " + answer + ", " +
      userSays + ", " + (lastMover.getSmooth() ? 1 : 0));
    lastMover = null;
  }
  public static void main(String[] s)
  {
    MoverFrame mf = new MoverFrame();
    JFrame jf = new JFrame();
    jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    Container cp = jf.getContentPane();
    cp.add(mf);
    jf.pack();
    jf.show();
  }
}
