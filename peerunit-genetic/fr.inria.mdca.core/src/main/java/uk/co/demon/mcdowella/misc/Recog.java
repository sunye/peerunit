package uk.co.demon.mcdowella.misc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Container;
import java.text.DecimalFormat;
import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.text.NumberFormat;
import java.util.Random;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** This class provides an application that presents the user with
 *  screens of buttons. They click on buttons to select pairs of
 *  buttons with the same titles. The idea is to exercise working
 *  memory but to make chunking and mnemonics less profitable.
 *  This class just provides a framework within which the button-
 *  bearing screen lives
 */
public class Recog extends JFrame
{
  private ButtonBearer currentBearer;
  private ButtonBearerFactory bbf;
  private Container bbCarrier;
  /** display time spent so far */
  private JLabel timeTaken = new JLabel();
  /** When this run started */
  private long started = System.currentTimeMillis();
  NumberFormat nf = new DecimalFormat("00.##");
  /** set time taken so far */
  private void setTimeTaken()
  {
    long taken = System.currentTimeMillis() - started;
    long minutes = taken / 60000;
    double seconds = (taken - minutes * 60000) * 1.0E-3;
    timeTaken.setText(Long.toString(minutes) + ":" +
      nf.format(seconds));
  }
  public Recog(ButtonBearerFactory fact)
  {
    bbCarrier = getContentPane();
    bbCarrier.setLayout(new BorderLayout());
    setTimeTaken();
    bbCarrier.add(timeTaken, BorderLayout.SOUTH);
    bbf = fact;
    newButtons();
    addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent we)
      {
	try
	{
	  bbf.close();
	}
	catch (IOException ioe)
	{
	  System.err.println("Exception on close: " + ioe);
	}
	System.exit(0);
      }
    });
  }
  private ActionListener onEnd = new ActionListener()
  {
    public void actionPerformed(ActionEvent ae)
    {
      newButtons();
    }
  };
  private void newButtons()
  {
    if (currentBearer != null)
    {
      bbCarrier.remove(currentBearer);
    }
    setTimeTaken();
    currentBearer = bbf.createButtonBearer();
    currentBearer.addActionListener(onEnd);
    bbCarrier.add(currentBearer, BorderLayout.CENTER);
    bbCarrier.validate();
    pack();
  }
  public static void main(String[] s) throws IOException
  {
    File configFile = null;
    File outputFile = null;
    int argp = 0;
    boolean trouble = false;
    float fontSize = 16.0f;
    int s1 = s.length - 1;
    try
    {
      for (;argp < s1; argp++)
      {
        if ((argp < s1) && "-config".equals(s[argp]))
	{
	  configFile = new File(s[++argp]);
	}
	else if ((argp < s1) && "-font".equals(s[argp]))
	{
	  fontSize = Float.parseFloat(s[++argp].trim());
	}
        else if ((argp < s1) && "-log".equals(s[argp]))
	{
	  outputFile = new File(s[++argp]);
	}
	else
	{
	  System.err.println("Cannot handle flag " + s[argp]);
	  trouble = true;
	}
      }
    }
    catch (Exception ie)
    {
      System.err.println("Got exception " + ie + " parsing arg " +
        s[argp]);
      trouble = true;
    }
    if (configFile == null)
    {
      System.err.println("Must specify config file");
      trouble = true;
    }
    if (outputFile == null)
    {
      System.err.println("Must specify log file");
      trouble = true;
    }
    if (trouble)
    {
      System.err.println(
        "Args are -config <file> [-font #] -log <file>");
      return;
    }
    Random ran = new Random();
    long seed = ran.nextLong();
    ButtonBearerFactory bbf = new ButtonBearerFactoryImpl(seed,
      configFile, outputFile);
    if (fontSize > 0.0)
    {
      bbf.setFontSize(fontSize);
    }
    Recog r = new Recog(bbf);
    r.pack();
    r.show();
  }
}
