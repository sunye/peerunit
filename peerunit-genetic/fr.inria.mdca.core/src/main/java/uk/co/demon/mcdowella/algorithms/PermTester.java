package uk.co.demon.mcdowella.algorithms;

import java.util.ArrayList;
import uk.co.demon.mcdowella.stats.Deviant;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;
import uk.co.demon.mcdowella.stats.LogFact;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.PushbackReader;
import java.util.Random;

/** This class is a test harness for PermFHL.java. It receives input and
 * runs the algorithm of Furst, Hopcroft, and Luks, reading sets of permutations
 * separated by blank lines
 */
public class PermTester
{
    /** Main program to be used as a test harness from the CLI
     */
    public static void main(String[] s) throws IOException
    {
        PushbackReader pr = new PushbackReader(new InputStreamReader(
            System.in));       

        // These are the strategies that we will test out
        PermFHL.SavePerms heapSave = new PermFHL.HeapSavePerms();
        PermFHL.SavePerms fifoSave = new PermFHL.FIFOSavePerms();
        PermFHL.SavePerms lifoSave = new PermFHL.LIFOSavePerms();

        long seed = 42;
        int doTest = 1000;
        boolean trouble = false;
        int argp = 0;
        int runs = 10;
        String statFile = null; // comma-separated statistics go here for later
                                // analysis

        try
        {
            for (; argp < s.length; argp++)
            {
                if (argp < s.length && "-file".equals(s[argp]))
                {
                    argp++;
                    statFile = s[argp];
                }
                else if (argp < s.length - 1 && "-goes".equals(s[argp]))
                {
                    argp++;
                    doTest = Integer.parseInt(s[argp]);
                }
                else if (argp < s.length - 1 && "-runs".equals(s[argp]))
                {
                    argp++;
                    runs = Integer.parseInt(s[argp]);
                }
                else if (argp < s.length - 1 && "-seed".equals(s[argp]))
                {
                    argp++;
                    seed = Long.parseLong(s[argp]);
                }
                else
                {
                    System.err.println("Could not handle arg " + s[argp]);
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
            System.err.println("Usage is PermTester [-goes #] [-runs #] [-seed #]");
            System.exit(1);
        } 
     
        PrintWriter pw = new PrintWriter(new 
            OutputStreamWriter(System.out));
        pw.println("Mc goes " + doTest + "Mc runs " + runs + " seed " + seed);
        Random ran = new Random(seed);

        // Run with specified permutations from input file, separated
        // by blank lines
        for(int x = 0;x != -1;)
        {   // While still permutations to read in
            List perms = new ArrayList();
            int lineCount = 0;
            gatherLoop: while(x != -1)
            {   // scan for '[' marking the start of a permutation
                x = pr.read();
                if (x == -1)
                   break;
                if (x == '\n')
                {
                    if (lineCount > 0)
                        break; // blank line marks end of list of generators
                    lineCount++;
                }
                if (Character.isWhitespace((char)x))
                    continue; // ignore leading white space
                if (x == '#')
                {   // comment till end of line
                    for(;;)
                    {
                        System.out.print((char)x); // echo to output file
                        x = pr.read();
                        if (x == -1)
                            break gatherLoop;
                        if (x == '\n')
                        {
                            System.out.println();
                            break;
                        }
                    }
                    continue;
                }
                
                pr.unread(x); // put back likely '['
                // Leave readPermutation to check input and thrown if invalid
                Permutation p = Permutation.readPermutation(pr);
                if (p == null)
                    break; // EOF
                lineCount = 0;
                perms.add(p); // Save away permutation
            }
            int nPerms = perms.size();
            if (nPerms < 1)
                continue;            
            Permutation pa[] = new Permutation[nPerms];
            perms.toArray(pa);
            for (int i = 0; i < pa.length; i++)
                System.out.println("Perm " + i + " is " + pa[i]);

            // Test out each strategy in turn
            System.out.println("With heap:");
            runTest(pa, heapSave, pw, doTest, ran, null);
            System.out.println("With FIFO:");
            runTest(pa, fifoSave, pw, doTest, ran, null);
            System.out.println("With LIFO:");
            runTest(pa, lifoSave, pw, doTest, ran, null);
        }

        // Now run loads of tests with all permutations generated at random
        PrintWriter stats = null;
        if (statFile != null)
            stats = new PrintWriter(new FileWriter("perm.stats"));
        for (int go = 0; go < runs; go++)
        {
            int perms = ran.nextInt(5) + 1;
            int permLen = ran.nextInt(16) + 1;
            Permutation[] pi = new Permutation[perms];
            for (int i = 0; i < perms; i++)
            {
                pi[i] = new Permutation(permLen);
                pi[i].setRandom(ran);
            }
            for (int i = 0; i < pi.length; i++)
                System.out.println("Perm " + i + " is " + pi[i]);
            System.out.println("With heap:");
            runTest(pi, heapSave, pw, doTest, ran, stats);
            if (stats != null)
                stats.print(", ");
            System.out.println("With FIFO:");
            runTest(pi, fifoSave, pw, doTest, ran, stats);
            if (stats != null)
                stats.print(", ");
            System.out.println("With LIFO:");
            runTest(pi, lifoSave, pw, doTest, ran, stats);
            if (stats != null)
            {
                stats.println();
                stats.flush();
            }
        }
        if (stats != null)
            stats.close();
    }
    /** This runs tests for an array of permutations, using a plug-in PermFHL.SavePerms
     *  to specify a strategy intended to reduce the number of steps that would be given
     *  to generate a permutation in the subgroup.
     *  @param pa an Array of permutations generating the subgroup
     *  @param sp an object implementing the PermFHL.SavePerms interface, that can
     *   save permutations and their associated costs (in operations) and return them
     *   later in some prioritised order. Choosing an order is where the strategy comes
     *   in.
     *  @param ps Write trace output/ general info here
     *  @param doTest the number of permutations to generate at random and try and
     *   decompose.
     * @param ran the Random number generator to use to generate random permutations.
     * @param stats write comma-separated trace output here - e.g. for some statistical
     *  program to read in later to see how the strategy has done
     */
    static void runTest(Permutation[] pa, PermFHL.SavePerms sp, PrintWriter ps,
        int doTest, Random ran, PrintWriter stats)
    {
        int len = pa[0].getN();
        Swatch timer = new Swatch();
        timer.start();
        PermFHL pf = new PermFHL(pa, sp); // This is the clever bit: Furst/Hopcroft/Luks
        timer.stop();
        double lg = pf.logGroupSize();
        ps.println("Log of subgroup size is " + lg);
        if (stats != null)
            stats.print(lg);
        // Size of complete group is of course n!
        double fraction = Math.exp(LogFact.lF(len) - lg);
        ps.println("Subgroup holds one element in " + fraction);
        if (stats != null)
            stats.print(", " + fraction);
        // Get stats on expected number of steps to produce random
        // element in subgroup from generators using this table
        double[] moments = new double[2];
        pf.decompDistrib(moments);
        ps.println("Mean ops " + moments[0] + " variance " + moments[1] +
            " s.d. " + Math.sqrt(moments[1]));
        if (stats != null)
            stats.print(", " + moments[0] + ", " + moments[1]);
        ps.println("Table building took " + timer);
        if (stats != null)
            stats.print(", " + timer.millis());
        // Now try generating and decomposing random permutations from group
        for (int i = 0; i < pa.length; i++)
        {
            Permutation p = new Permutation(pa[i]);
            double cost = pf.decompose(p);
            if (p.changedFrom(0) != p.getN())
                throw new IllegalStateException("Could not decompose generator");
            ps.println("Generator " + pa[i].toString() + " cost " + cost);
        }
        int doable = 0;
        long seed = 42;
        Permutation pr = new Permutation(pa[0]);
        Deviant dev = new Deviant();
        for (int i = 0; i < doTest; i++)
        {
            pr.setRandom(ran);
            double cost = pf.decompose(pr);
            if (pr.changedFrom(0) == pr.getN())
            {
                doable++;
                dev.sample(cost);
            }
        }
        ps.println("Decomposed " + doable + " of " + doTest + " " + dev);
        if (stats != null)
            stats.print(", " + doable + ", " + doTest);
        if (stats != null)
            stats.print(", " + dev.getMean() + ", " + dev.getVariance());
        ps.flush();
     }
}