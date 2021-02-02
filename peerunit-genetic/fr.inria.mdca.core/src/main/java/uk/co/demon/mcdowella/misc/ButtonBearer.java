package uk.co.demon.mcdowella.misc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** This screen presents the user with buttons. They have to click
 *  buttons in succession to identify buttons with the same title.
 *  Not thread safe - expects everything to be on event-dispatching
 *  thread.
 */
public class ButtonBearer extends JComponent
{
  /** buttons displayed to user */
  private JButton[] buttons;
  /** ButtonInfo for each distinct title */
  private Map buttonInfoByText = new HashMap();
  private List actionListenerList = new ArrayList();
  /** button with which current button press will be compared. This
   *  will be slightly emphasised
   */
  private JButton liveButton;
  /** used to log results
   */
  private final ResultLogger forResults;
  /** holds records for buttons with the same title */
  private static class ButtonInfo
  {
    /** all buttons sharing this title */
    private Set allButtons;
    /** buttons picked out by user so far */
    private Set seenButtons = new HashSet();
    ButtonInfo(Set buttonSet)
    {
      allButtons = buttonSet;
    }
    boolean isNewDuplicate(JButton lastButton, JButton currentButton)
    {
      if (!(allButtons.contains(lastButton) &&
            allButtons.contains(currentButton)))
      {
        throw new IllegalStateException("Foreign button");
      }
      boolean result = !seenButtons.contains(currentButton);
      seenButtons.add(currentButton);
      return result;
    }
    boolean allButOneSeen()
    {
      return seenButtons.size() >= (allButtons.size() - 1);
    }
  }
  private Font weakFont;
  private Font strongFont;
  private void weakButton(JButton jb)
  {
    if (jb == null)
    {
      return;
    }
    jb.setFont(weakFont);
  }
  private void strongButton(JButton jb)
  {
    if (jb == null)
    {
      return;
    }
    if (weakFont == null)
    {
      weakFont = jb.getFont();
      strongFont = weakFont.deriveFont(Font.BOLD | Font.ITALIC);
    }
    jb.setFont(strongFont);
  }
  private ActionListener al = new ActionListener()
  {
    public void actionPerformed(ActionEvent e)
    {
      JButton source = (JButton)e.getSource();
      if (source == liveButton)
      { // user pressed twice on the same button
        return;
      }
      String sourceText = source.getText();
      ButtonInfo bi = (ButtonInfo)buttonInfoByText.get(sourceText);
      ButtonInfo before = null;
      if (liveButton != null)
      {
        before = (ButtonInfo)buttonInfoByText.get(liveButton.getText());
      }
      if ((bi == null) || (bi != before))
      { // does not match previous button pressed
        // so becomes new live button
	weakButton(liveButton);
	liveButton = source;
	strongButton(liveButton);
        return;
      }
      if (bi.isNewDuplicate(liveButton, source))
      {
        source.setEnabled(false);
      }
      if (bi.allButOneSeen())
      {
	// no live button now
	weakButton(liveButton);
        liveButton.setEnabled(false);
	liveButton = null;
	buttonInfoByText.remove(sourceText);
	checkFinished();
      }
    }
  };
  public ButtonBearer(int rows, int cols,
    String[] texts, ResultLogger rl, float fontSize)
  {
    forResults = rl;
    setLayout(new GridLayout(rows, cols));
    buttons = new JButton[rows * cols];
    Map initial = new HashMap();
    Font f = null;
    for (int i = 0; i < buttons.length; i++)
    {
      String title = texts[i];
      JButton jb = new JButton(title);
      if (fontSize > 0.0)
      {
	if (f == null)
	{
	  f = jb.getFont().deriveFont(fontSize);
	}
        jb.setFont(f);
      }
      buttons[i] = jb;
      jb.addActionListener(al);
      add(jb);
      Set l = (Set)initial.get(title);
      if (l == null)
      {
        l = new HashSet();
	initial.put(title, l);
      }
      l.add(jb);
    }
    int clicksRequired = 0;
    for (Iterator i = initial.entrySet().iterator(); i.hasNext();)
    {
      Map.Entry me = (Map.Entry)i.next();
      Set l = (Set)me.getValue();
      int len = l.size();
      if (len > 1)
      {
        buttonInfoByText.put(me.getKey(), new ButtonInfo(l));
	clicksRequired += len;
      }
    }
    totalClicks = clicksRequired;
  }
  private final int totalClicks;
  public void paint(java.awt.Graphics g)
  {
    if (!seen)
    {
      forResults.started(totalClicks);
      seen = true;
    }
    super.paint(g);
  }
  private boolean seen = false;
  public void addActionListener(ActionListener al)
  {
    actionListenerList.add(al);
    checkFinished();
  }
  private void checkFinished()
  {
    if (!buttonInfoByText.isEmpty())
    {
      return;
    }
    List fireList = new ArrayList(actionListenerList);
    ActionEvent ae = new ActionEvent(this, -1, null);
    for (Iterator i = fireList.iterator(); i.hasNext();)
    {
      ActionListener al = (ActionListener)i.next();
      al.actionPerformed(ae);
    }
    forResults.finished();
  }
}
