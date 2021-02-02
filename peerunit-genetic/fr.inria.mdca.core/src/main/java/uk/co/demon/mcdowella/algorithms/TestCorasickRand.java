package uk.co.demon.mcdowella.algorithms;

import uk.co.demon.mcdowella.algorithms.AhoCorasick;
import java.util.Arrays;
import uk.co.demon.mcdowella.algorithms.Callback;
import java.util.Random;

/** Test class for AhoCorasick - random test */
public class TestCorasickRand
{
    /** Total matches */
    private int matchesSoFar;
    /** return total matches */
    public int getMatches() {return matchesSoFar;}
    /** attempts to force a match */   
    private int forces;
    /** return attempts to force a match */
    public int getForces() {return forces;}
    /** Random number generator */
    private Random r;
    /** AhoCorasick under test */
    private AhoCorasick ac;
    /** Sequences to be matched */
    private int[][] sequence;
    /** Holds previous characters to check sequences */
    private int[] buffer;
    /** position of next write in buffer */
    int bufPos;
    /** set to record patterns seen */
    private boolean[] seen;
    /** Maximum value */
    private int maxVal;
    /** Get ready to run a test
     * @param maxSeq the maximum number of random sequences
     * to generate
     * @param maxLen the maximum length of any sequence
     * @param r the Random number generator
     * @param maxVal the maximum absolute value in a sequence
     */
    public TestCorasickRand(int maxSeq, int maxLen, Random r,
        int maxVal)
    {
        matchesSoFar = 0;
        forces = 0;
        this.r = r;
        this.maxVal = maxVal;
        sequence = new int[r.nextInt(maxSeq + 1)][];
        Callback[] cb = new MyCallback[sequence.length];
        buffer = new int[maxLen];
        Arrays.fill(buffer, maxVal + 1);
        seen = new boolean[sequence.length];
        bufPos = 0;
        for (int i = 0; i < sequence.length; i++)
        {
            sequence[i] = new int[r.nextInt(maxLen + 1)];
            for (int j = 0; j < sequence[i].length; j++)
                sequence[i][j] = r.nextInt(maxVal + 1) *
                     (2 * r.nextInt(2) - 1);
            cb[i] = new MyCallback(this, i);
        }        
        ac = new AhoCorasick(sequence, cb);
    }
    /** receive a character */
    public void takeChar(int x)
    {
       buffer[bufPos] = x;
       // buffer[bufPos] = x + 1;  uncomment to force error just to show we detect them
       bufPos++;
       if (bufPos >= buffer.length)
           bufPos = 0;           
       ac.accept(x);           
    }
    /** Run a test of N characters */
    public void testChars(int num)
    {
        for (int i = 0; i < num; i++)
        {
            int x = r.nextInt(maxVal + 1) *
                    (2 * r.nextInt(1) - 1);
            takeChar(x);
        }
    }
    /** Accept a callback */
    void callback(int i)
    {
        matchesSoFar++;
        int[] seq = sequence[i];
        int pos = bufPos - 1;
        for (int j = seq.length - 1; j >= 0; j--)
        {
            if (pos < 0)
                pos += buffer.length;
            if (seq[j] != buffer[pos])
                throw new IllegalArgumentException("False match of sequence " + i +
                    " at offset " + j);
            pos--;
        }
        seen[i] = true;
    }
    /** Force a callback */
    void force()
    {
        forces++;
        int x = r.nextInt(sequence.length);
        int[] seq = sequence[x];
        for (int i = 0; i < seq.length - 1; i++)
            takeChar(seq[i]);
        int finalEl;
        if (seq.length > 0)
        {
            finalEl = seq[seq.length - 1];
            // Uncomment below to force an error just to show
            // we detect them - assuming we get here of course!
            // finalEl++;
        }
        else
        {
            finalEl = r.nextInt(maxVal + 1) *
                  (2 * r.nextInt(2) - 1);
        }
        seen[x] = false;
        takeChar(finalEl);
        if (seen[x] != true)
            throw new IllegalArgumentException("No match found for sequence " + x +
                " len " + seq.length);
    }
    public void reset()
    {
        ac.reset();
        bufPos = 0;
        Arrays.fill(buffer, maxVal + 1);
    }
    public static void main(String[] s)
    {
        int maxSeqs = 100;
        int maxLen = 100;
        int maxVal = 5;
        long seed = 32;
        int testLen = 15000;
        int passes = 20;
        boolean trouble = false;

        int argp = 0;
        try
        {
            for(;argp < s.length; argp++)
            {
                if ("-maxLen".equals(s[argp]) && argp < s.length - 1)
                {
                    argp++;
                    maxLen = Integer.parseInt(s[argp]);
                }
                else if ("-maxSeqs".equals(s[argp]) && argp < s.length - 1)
                {
                    argp++;
                    maxSeqs = Integer.parseInt(s[argp]);
                }
                else if ("-maxVal".equals(s[argp]) && argp < s.length - 1)
                {
                    argp++;
                    maxVal = Integer.parseInt(s[argp]);
                }
                else if ("-passes".equals(s[argp]) && argp < s.length - 1)
                {
                    argp++;
                    passes = Integer.parseInt(s[argp]);
                }
                else if ("-seed".equals(s[argp]) && argp < s.length - 1)
                {
                    argp++;
                    seed = Long.parseLong(s[argp]);
                }
                else if ("-testLen".equals(s[argp]) && argp < s.length - 1)
                {
                    argp++;
                    testLen = Integer.parseInt(s[argp]);
                }
                else
                {
                    System.err.println("Cannot handle flag " + s[argp]);
                    trouble = true;
                }
            }
        }
        catch (NumberFormatException ne)
        {
            System.err.println("Cannot read number in " + s[argp]);
            trouble = true;
        }
        if (trouble)
        {
            System.err.println("Usage is TestCorasickRand [-maxLen #] " +
                "[-maxSeqs #] [-maxVal #] [-passes #] [-seed #] [-testLen #]");
            System.exit(1);
        }

        System.out.println("maxLen = " + maxLen);
        System.out.println("maxSeqs = " + maxSeqs);
        System.out.println("maxVal = " + maxVal);
        System.out.println("passes = " + passes);
        System.out.println("seed = " + seed);
        System.out.println("testLen = " + testLen);

        Random r = new Random(seed);
        TestCorasickRand tcr = new TestCorasickRand(maxSeqs, maxLen, r, 5);
        for(int pass = 0; pass < passes; pass++)
        {
            System.err.println("Pass " + pass + " of " + passes);
            tcr.testChars(r.nextInt(testLen));
            tcr.force(); 
            tcr.reset();
            tcr.testChars(r.nextInt(testLen));
            tcr.force(); 
        }
        System.out.println("Matches " + tcr.getMatches() + " of which " +
           tcr.getForces() + " forced ");
    }
}

class MyCallback implements Callback
{
    private TestCorasickRand tcr;
    private int i;
    MyCallback(TestCorasickRand t, int i)
    {
        tcr = t;
        this.i = i;
    }
    public void callback()
    {
        tcr.callback(i);
    }
}