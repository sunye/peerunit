package uk.co.demon.mcdowella.stats;

import uk.co.demon.mcdowella.algorithms.AhoCorasick;
import java.util.Arrays;
import uk.co.demon.mcdowella.algorithms.Callback;
import java.io.PrintWriter;

/** This class reads in a file of vaguely formatted text and searches for
 * words. When a pattern is found, it writes out a list of words found
 * at various offsets from that pattern
 */
public class SelectWords
{
    /** finds the words we're after */
    private AhoCorasick finder;
    /** Current live callback. null if no callback live
     */
    private PatternFound live;
    /** Set up a PatternFound to receive characters from now
     *  until it has finished emitting its word. Settup up
     *  null resets the current setup
     */
    void setLive(PatternFound pf) {live = pf;}
    /** Return the current PatternFound or null */
    PatternFound getLive() {return live;}
    private SelectException error;
    /** Allows calbacks to register an error.
     *  If they threw one directly their interface would
     *  not be a standard callback
     */
    void setError(SelectException se)
    {
        error = se;
    }
    /** Output stream */
    private PrintWriter ps;
    /** get the output writer */
    PrintWriter getPrintWriter() {return ps;}
    /** set the output writer */
    public void setPrintWriter(PrintWriter p) {ps = p;}
    /** accept a character of input */
    public void accept(char ch) throws SelectException
    {
        if (live != null)
        {
            live.accept(ch);
            if (error != null)
            {
                ps.flush();
                SelectException se = error;
                error = null;
                throw se;
            }
        }
        finder.accept(ch);
    }
    /** Process end of file */
    public void eof() throws SelectException
    {
        if (live != null)
            live.eof();
    }
    /** Construct from arrays of words and offsets */
    public SelectWords(String[] words, int[][] offsets, PrintWriter pw)
    {
        if (words.length != offsets.length)
            throw new IllegalArgumentException("# offset arrays != # words");
        ps = pw;
        int[][] sequences = new int[words.length][];
        Callback[] callbacks = new Callback[words.length];
        for (int i = 0; i < words.length; i++)
        {
            sequences[i] = AhoCorasick.toSequence(words[i]);
            callbacks[i] = new PatternFound(words[i], offsets[i], this);
        }
        finder = new AhoCorasick(sequences, callbacks);
        live = null;
        error = null;
    }
}

/** An object of this class is activated on a pattern match
 *  and receives characters of input from then which it can
 *  use to emit words. When it has emitted all its word it
 *  detatches itself */
class PatternFound implements Callback
{
    private SelectWords caller;
    private int[] offsets;
    private int wordsSeen;
    private boolean beforeWord;
    private int atOffset;
    private StringBuffer sb;
    private String name;
    /** Construct from name (emit this as offset 0), list of
     *  word offsets, and the SelectWords object which may later
     *  call it.
     *  @throw IllegalArgumentException if -ve offset
     */
    public PatternFound(String name, int[] off, SelectWords sw)
    {
        this.name = name;
        for (int i = 0; i < off.length; i++)
        {
            if (off[i] < 0)
                throw new IllegalArgumentException("-ve offset " + off[i]);
        }   
        offsets = new int[off.length];
        System.arraycopy(off, 0, offsets, 0, off.length);
        Arrays.sort(offsets);
        caller = sw;
    }
    /** Fires when the patern first matches */
    public void callback()
    {
        // System.err.println("Callback for " + name);
        if (caller.getLive() != null)
        {
            caller.setError(new SelectException("Two words active"));
            return;
        }
        caller.setLive(this);
        wordsSeen = 1;
        beforeWord = true;
        atOffset = 0;
        PrintWriter pw = caller.getPrintWriter();
        for (;;)
        {
            if (atOffset >= offsets.length)
                break;
            if (offsets[atOffset] > 0)
                break;
            if (offsets[atOffset] == 0)
            {
                pw.println(name);
            }
            atOffset++;
        }
    }
    /** Accept a character seen after a pattern match while we are
     *  looking for words to emit
     */
    public void accept(char ch)
    {
        // System.err.println(name + " has " + ch);
        if (beforeWord)
        {
            if (Character.isWhitespace(ch))
                return;
            beforeWord = false;
            sb = new StringBuffer();
            sb.append(ch);
            return;
        }
        if (!Character.isWhitespace(ch))
        {
            sb.append(ch);
            return;
        }
        endWord();
    }
    /** Process the end of a word. */
    private void endWord()
    {
        // System.err.println(name + " end word at " + wordsSeen);
        String word = null;
        PrintWriter pw = caller.getPrintWriter();
        for(;atOffset < offsets.length;atOffset++)
        {
            if (offsets[atOffset] == wordsSeen)
            {
                if (word == null)
                    word = sb.toString();
                pw.println(word);
            }
            else if (offsets[atOffset] > wordsSeen)
                break;
        }
        sb = null;
        if (atOffset >= offsets.length) // finished
        {
            caller.setLive(null);
            // System.err.println(name + " dead ");
        }
        wordsSeen++;
        beforeWord = true;
    }
    /** Cope with end of file */
    public void eof() throws SelectException
    {
        endWord();
        if (caller.getLive() != null)
            throw new SelectException("End of file in mid-pattern");        
    }
}