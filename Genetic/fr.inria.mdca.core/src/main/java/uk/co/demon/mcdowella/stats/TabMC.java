package uk.co.demon.mcdowella.stats;

import java.util.Arrays;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Random;
import java.io.StreamTokenizer;

/** This class contains a main program to run Monte-Carlo tests on
 *  contingency tables
 */
public class TabMC
{
    public static void main(String[] s) throws IOException
    {
        int cols = 0; // table columns to read
        int rows = 0; // table rows to read
        int argp = 0; // used to read arguments
        boolean trouble = false;
        int goes = 10000; // Number of Monte Carlo runs
        long seed = 123;  // RNG seed
        try
        {
            for (argp = 0; argp < s.length; argp++)
            {
                if (argp < s.length - 1 && "-cols".equals(s[argp]))
                {
                    argp++;
                    cols = Integer.parseInt(s[argp]);
                }
                else if (argp < s.length - 1 && "-rows".equals(s[argp]))
                {
                    argp++;
                    rows = Integer.parseInt(s[argp]);
                }
                else if (argp < s.length - 1 && "-goes".equals(s[argp]))
                {
                    argp++;
                    goes = Integer.parseInt(s[argp]);
                }
                else if (argp < s.length - 1 && "-seed".equals(s[argp]))
                {
                    argp++;
                    seed = Long.parseLong(s[argp]);
                }
                else
                {
                    System.err.println("Could not handle flag " + s[argp]);
                    trouble = true;
                }
            }
        }
        catch (NumberFormatException nf)
        {
            System.err.println("Could not read number in " + s[argp]);
            trouble = true;
        }
        if (cols <= 0 || rows <= 0)
        {
            System.err.println("Must give # of rows and columns > 0");
            trouble = true;
        }
        if (trouble)
        {
            System.err.println("Usage is TabDist -rows # -cols # " +
                " [-goes #] [-seed #]");
            System.exit(1);
        }
        // Read in table
        int[] table = new int[rows * cols];
        StreamTokenizer st = new StreamTokenizer(
                  new InputStreamReader(System.in));
        st.commentChar('#');
        for(int i = 0; i < rows * cols;)
        {
            int tok = st.nextToken();
            switch(tok)
            {
                case StreamTokenizer.TT_NUMBER:
                    table[i] = (int)st.nval;
                    i++;
                    break;
                case StreamTokenizer.TT_EOF:
                    System.err.println("Unexpected end of file");
                    System.exit(1);
                default:
                    System.err.println("Unexpected token " + tok);
                    System.exit(1);        
            }
        }
        System.out.println("Seed is " + seed + " goes is " + goes);
        System.out.println("Got table");
        for (int i = 0; i < rows; i++)
        {
            for (int j = 0;;j++)
            {
                System.out.print(table[i * cols + j]);
                if (j < cols - 1)
                    System.out.print(' ');
                else
                {
                    System.out.println();
                    break;
                }
            }
        }
        // Marginal totals indexed by column
        int[] colTot = new int[cols]; // Java initialises to zero
        // Marginal totals indexed by row
        int[] rowTot = new int[rows];
        int total = 0;
        for (int row = 0; row < rows; row++)
        {
            int rowSum = 0;
            for (int col = 0; col < cols; col++)
            {
                int x = table[row * cols + col];
                colTot[col] += x;
                rowSum += x;
            }
            rowTot[row] = rowSum;
            total += rowSum;
        }
        Random rr = new Random(seed);
        // Create a source of random tables with these marginals
        Permute pm = new Permute(table, rows, cols, rr);
        // Monte Carlo results for:
        // (-Log of ) Probability of table given marginals. This is
        // Prod_i Ni.! Prod_j N.j!
        // -----------------------
        // N..! Prod_ij Nij!
        McCount logProb = new McCount();
        // Log-Likelihood Chi-Squared against independence
        // This is 2.0 * Prod_ij Nij ln(N * Nij/ (Ni. * N.j))
        McCount llChi = new McCount();
        // Sum of squared trend statistic along each row
        // This is SUM_i (SUM_j Nij.(2j - cols + 1)^2
        McCount trend = new McCount();
        // Statistic to pick out trend along both rows and columns
        // This is SUM_ij Nij.(2j - cols + 1).(2i - rows + 1)
        McCount biTrend = new McCount();
        // Constant contributions to Log probability statistics
        double logPBase = -LogFact.lF(total);
        for (int col = 0; col < cols; col++)
        {
            logPBase += LogFact.lF(colTot[col]);
        }
        for (int row = 0; row < rows; row++)
        {
            logPBase += LogFact.lF(rowTot[row]);
        }
        for (int go = 0; go < goes; go++)
        {
            double logPSum = logPBase;
            double llSum = 0.0;
            double trendSum = 0.0;
            double biSum = 0.0;
            for (int row = 0; row < rows; row++)
            {
                double rs = rowTot[row];
                double ts = 0.0;
                // Factor to pick out trend along rows
                double rowFact = row * 2.0 - rows + 1.0;
                for (int col = 0; col < cols; col++)
                {
                    int x = table[row * cols + col];
                    logPSum -= LogFact.lF(x);
                    if (x > 0)
                        llSum += x * Math.log((total * (double) x) /
                                              (rs * colTot[col]));
                    // Factor to pick out trend along columns
                    double colFact = col * 2.0 - cols + 1.0;
                    double xc = x * colFact;
                    ts += xc;
                    biSum += xc * rowFact;
                }
                trendSum += ts * ts;
            }
            // System.err.println("trendSum " + trendSum);
            logProb.sample(-logPSum); // Minus log prob
            llChi.sample(llSum * 2.0);
            trend.sample(trendSum);
            biTrend.sample(biSum); 
            if (go < goes - 1)
            {
                pm.newTab(table);
            }
        }
        // Want minus log prob so large numbers mean odd tables
        System.out.println("Minus Log Prob: " + logProb);
        System.out.println("G2: " + llChi);
        System.out.println("Squared sum of trends along rows: " + trend);
        System.out.println("BiTrend: " + biTrend);
    }
}
