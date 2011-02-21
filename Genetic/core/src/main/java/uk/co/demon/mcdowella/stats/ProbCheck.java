package uk.co.demon.mcdowella.stats;
import java.util.Comparator;
import java.io.IOException;
import java.util.Random;
import java.io.PrintStream;

/** This checks RoughRandom against TabDist and RowDist
 */
public class ProbCheck
{
    public static void main(String[] s)
    {
        int goes = 100000;
        int grain = 10000;
        int range = 15;
        int samples = 50;
        long seed = 6200;
        int top = 10;
        TopN tn = new TopN(top, new TestCompare());
        int go = 0;
        int test = 0;
        boolean trouble = false;
        int argp = 0;
        try
        {
            for (; argp < s.length; argp++)
            {
                if (argp < s.length - 1 && s[argp].equals("-goes"))
                {
                    argp++;
                    goes = Integer.parseInt(s[argp]);
                }
                else if (argp < s.length - 1 && s[argp].equals("-grain"))
                {
                    argp++;
                    grain = Integer.parseInt(s[argp]);
                }
                else if (argp < s.length - 1 && s[argp].equals("-range"))
                {
                    argp++;
                    range = Integer.parseInt(s[argp]);
                }
                else if (argp < s.length - 1 && s[argp].equals("-samples"))
                {
                    argp++;
                    samples = Integer.parseInt(s[argp]);
                }
                else if (argp < s.length - 1 && s[argp].equals("-seed"))
                {
                    argp++;
                    seed = Long.parseLong(s[argp]);
                }
                else if (argp < s.length - 1 && s[argp].equals("-top"))
                {
                    argp++;
                    top = Integer.parseInt(s[argp]);
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
        if (range <= 0)
        {
            System.err.println("Range must be >0");
            trouble = true;
        }
        if (samples <= 0)
        {
            System.err.println("Samples must be >0");
            trouble = true;
        }
        if (trouble)
        {
            System.err.println("Usage is ProbCheck [-goes #] [-range #] " +
                "[-samples #] [-seed #] [-top #]");
            System.exit(1);
        }
        System.out.println("goes = " + goes + " range = " + range +
            " samples = " + samples + " seed = " + seed + " top = " + top);
        System.out.flush();
        // Make this interruptable by having another thread wait for input
        Thread waiter = new Thread() 
        {
            public void run()
            {
//                System.err.println("Before read");
                try
                {
                    System.in.read();
                }
                catch (IOException e) {}
//                System.err.println("After read");                	
            }
        };
        waiter.start();

        for(; go < goes; go++)
        {
            if (!waiter.isAlive())
                break;
            Random r = new Random(seed + go);
            int len = 0;
            while (len <= 2)
                len = 2 + r.nextInt(range);
            double[] probs = new double[len];
            for (int i = 0; i < len; i++)
            {
                probs[i] = r.nextDouble();
            }
            double sum = 0.0;
            for (int i = 0; i < len; i++)
            {
                sum += probs[i];
            }
            sum = 1.0 / sum;
            for (int i = 0; i < len; i++)
                probs[i] *= sum;
            int count[][] = new int[2][];
            double[] vals = new double[2];
            RoughRandom rr = new RoughRandom(probs, r);
            for (int side = 0; side < 2; side++)
            {
                count[side] = new int[len];
                int number = 0;
                while (number <= 0)
                    number = r.nextInt(samples + 1);
                for (int i = 0; i < number; i++)
                    count[side][rr.next()]++;
                for (int i = 0; i < len; i++)
                    System.out.println("Got " + count[side][i] + " expected " +
                       (probs[i] * number));
                System.out.flush();
                SigProb sp = RowDist.llProb(count[side], probs, grain, vals);
                System.out.println("True ll prob " + vals[0] + " grained " + vals[1]);
                System.out.println(sp);
                TestInfo ti = new TestInfo(sp, go, 1);
                tn.insert(ti);
                test++;
                sp = RowDist.trend(count[side], probs, grain, vals);
                System.out.println("True trend prob " + vals[0] + " grained " +
                   vals[1]);
                System.out.println(sp);
                ti = new TestInfo(sp, go, 2);
                tn.insert(ti);
                test++;
            }
            SigProb sp = TabDist.marginalTab(count[0], count[1], grain, vals);
            System.out.println("Marginal prob " + vals[0] + " grained " + vals[1]);
            TestInfo ti = new TestInfo(sp, go, 3);
            tn.insert(ti);
            test++;
            sp = TabDist.trendTab(count[0], count[1], grain, vals);
            System.out.println("Trend prob " + vals[0] + " grained " + vals[1]);
            ti = new TestInfo(sp, go, 3);
            tn.insert(ti);
            test++;
            sp = TabDist.llChiTab(count[0], count[1], grain, vals);
            System.out.println("LL Chi prob " + vals[0] + " grained " + vals[1]);
            ti = new TestInfo(sp, go, 3);
            tn.insert(ti);
            test++;
        }
        System.out.println("Go is " + go +" test is " + test);          
        Object[] tops = tn.contents(true);
        for (int i = 0; i < tops.length; i++)
        {
            if (tops[i] == null)
                break;
            System.out.println(tops[i]);
            ((TestInfo)tops[i]).bon(System.out, test);
        } 
        System.exit(0); // Won't happen automatically if Waiter still
                        // waiting for input
    }
}

class TestInfo
{
    SigProb sp;
    double tail;
    int go;
    int type;
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Go ");
        sb.append(go);
        sb.append(" Type ");
        sb.append(type);
        sb.append(sp.toString());
        return sb.toString();
    }
    /** Use the Bonferonni inequality to compensate for 
     * the fact that we do a lot of tests. This is
     * <pre>
     * P(A | B) = P(A) + P(B & ¬A)
     *          &lt;= P(A) + P(B)
     * </pre>
     */
    public void bon(PrintStream os, int tests)
    {
        os.println("Given " + tests +
           " tests the probability of at least one of them getting a " +
           " tail probability at least this large is <= " +
           (tail * tests));
    }
    TestInfo(SigProb sp, int go, int type)
    {
        this.sp = sp;
        this.go = go;
        this.type = type;
        if (sp.getLt() < sp.getGt())
            tail = sp.getEq() + sp.getLt();
        else
            tail = sp.getEq() + sp.getGt();
    }
}

class TestCompare implements Comparator
{
    public int compare(Object a, Object b)
    {
        if (a == null)
        {
            if (b == null)
                return 0;
            return -1;
        }
        if (b == null)
            return 1;
        TestInfo ta = (TestInfo)a;
        TestInfo tb = (TestInfo)b;
        if (ta.tail < tb.tail)
            return 1;
        if (ta.tail > tb.tail)
            return -1;
        return 0;
    }
}