package uk.co.demon.mcdowella.algorithms;

import uk.co.demon.mcdowella.algorithms.AhoCorasick;
import uk.co.demon.mcdowella.algorithms.Callback;
import java.io.IOException;

/** Test class for AhoCorasick - simple grep
 */
public class TestCorasick implements Callback
{
    public static void main(String[] s) throws IOException
    {
        Callback[] cb = new Callback[s.length];
        int[][] seqs = new int[s.length][];
        int[] position = new int[1];
        for (int i = 0; i < s.length; i++)
        {
            seqs[i] = AhoCorasick.toSequence(s[i]);
            cb[i] = new TestCorasick(s[i], position);
        }
        AhoCorasick ahc = new AhoCorasick(seqs, cb);
        position[0] = 1;
        for(;;)
        {
            int ch = System.in.read();
            if (ch == -1)
                break;
            ahc.accept(ch);
            if (ch == '\n')
                position[0]++;
        }
    }
    private String marker;
    private int[] l;
    public TestCorasick(String s, int[] l)
    {
        marker = s;
        this.l = l;
    }
    public void callback()
    {
       System.out.println("Spotted marker <" + marker + "> in line " + l[0]);
    }
}
