package uk.co.demon.mcdowella.stats;

import java.util.Arrays;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.StreamTokenizer;

/** This class contains static methods for working out the distribution of statistics
 *  to do with 2xN contingency tables
 */
public class TabDist
{
    /** Work out the minimum and maximum possible values for the cells in a
     *  2xN contingency table with the same marginals as the current one
     *  @param top the top row of the table
     *  @param bottom the bottom row of the table
     *  @param min minimum possible values go here
     *  @param max maximum possible values go here
     */
    public static void range(int[] top, int[] bottom, int[] min, int[] max)
    {
        if (top.length != bottom.length)
            throw new IllegalArgumentException("Row lengths not equal");
        int topSum = 0;
        int bottomSum = 0;
        for (int i = 0; i < top.length; i++)
        {
            topSum += top[i];
            bottomSum += bottom[i];
        }
        for (int i = 0; i < top.length; i++)
        {
            int topRest = topSum - top[i];
            int bottomRest = bottomSum - bottom[i];
            // To keep the marginals the same, we add and subtract
            // values from cells in the pattern:
            // + -
            // - +
            // So to decrease the top value, we need to be able to move counts
            // out of the bottom cells not opposite of it
            if (top[i] <= bottomRest)
                min[i] = 0;
            else
                min[i] = top[i] - bottomRest;
            if (bottom[i] <= topRest)
                max[i] = top[i] + bottom[i];
            else
                max[i] = top[i] + topRest;
//            System.err.println("min " + min[i] + " max " + max[i] + " topRest " +
//                topRest + " bottomRest " + bottomRest);
        }
    }
    /**
     * Given a 2xN table held in top and bottom, and a scoring array, work out
     * the probability of scoring <, 0, > than the observed score
     * @param top the top row of the contingency table
     * @param bottom the bottom row of the contingency table
     * @param score[][] An array such that score[i][x-min[i]] is the contribution
     * to the score if the top value in column i is x. Min[i] is the minimum possible
     * value in that cell, given the marginals
     * @param prob3 is where the probabilities are written. Prob3[0] is the probability
     * of scoring < the observed value, prob3[1] of =, prob3[2] of >.
     * @return the observed score
     */
    public static int tabDist(int[] top, int[] bottom, int[][] score, double[] prob3)
    {
        int[] min = new int[top.length];
        int[] max = new int[top.length];
        range(top, bottom, min, max);
        if (top.length == 1)
        {
            prob3[0] = 0.0;
            prob3[1] = 1.0;
            prob3[2] = 0.0;
            return score[0][0];
        }
//        System.err.println("Past range");
        int observed = 0;
        for (int i = 0; i < top.length; i++)
            observed += score[i][top[i] - min[i]];
//        System.err.println("Observed is " + observed);
        // Work out the range of possible contributions
        Info[] info = new Info[top.length];
        for (int i = 0; i < top.length; i++)
        {
            info[i] = new Info();
            int minC = score[i][0];
            int maxC = score[i][0];
            int todo = max[i] - min[i];
            for (int j = 0; j < todo; j++)
            {
                int x = score[i][j + 1];
                if (x < minC)
                    minC = x;
                if (x > maxC)
                    maxC = x;
            }
            info[i].range = maxC - minC;
            info[i].minHere = minC;
            info[i].maxHere = maxC;
            info[i].index = i;
        }
//        System.err.println("Sorting");
        // Sort into order by range
        Arrays.sort(info);
        // Work out the min and max values we need to keep track of
        info[0].minCum = info[0].minHere;
        info[0].maxCum = info[0].maxHere;
        info[0].sumHere = top[info[0].index] + bottom[info[0].index];
        for (int i = 1; i < top.length - 1; i++)
        {
            info[i].minCum = info[i - 1].minCum + info[i].minHere;
            info[i].maxCum = info[i - 1].maxCum + info[i].maxHere;
            info[i].sumHere = info[i - 1].sumHere + top[info[i].index] +
                              bottom[info[i].index];
        }
        info[top.length - 1].sumHere = info[top.length - 2].sumHere +
               top[info[top.length - 1].index] + bottom[info[top.length - 1].index];
        info[top.length - 1].minCum = observed - 1;
        info[top.length - 1].maxCum = observed + 1;
        for (int i = top.length - 2; i >= 0; i--)
        {
            int x = info[i + 1].maxCum - info[i + 1].minHere;
            if (x < info[i].maxCum) // Any more than this is not interesting
                info[i].maxCum = x;
            x = info[i + 1].minCum - info[i + 1].maxHere;
            if (x > info[i].minCum)
                info[i].minCum = x;
        }
        info[0].minMarginal = min[info[0].index];
        info[0].maxMarginal = max[info[0].index];
        for (int i = 1; i < top.length; i++)
        {
            int x = info[i].index;
            info[i].minMarginal = info[i - 1].minMarginal + min[x];
            info[i].maxMarginal = info[i - 1].maxMarginal + max[x];
        }
        int sum = 0;
        for (int i = 0; i < top.length; i++)
            sum += top[i];
        info[top.length - 1].minMarginal = info[top.length - 1].maxMarginal = sum;
        for (int i = top.length - 2; i >= 0; i--)
        {
            int x = info[i + 1].index;
            int y = info[i + 1].maxMarginal - min[x];
            if (y < info[i].maxMarginal)
                info[i].maxMarginal = y;
            y = info[i + 1].minMarginal - max[x];
            if (y > info[i].minMarginal)
                info[i].minMarginal = y;
        }
//        System.err.println("Degenerate");
        // Work out degenerate probability distributions for the contribution
        // from the first 1 columns
        int len = info[0].maxMarginal - info[0].minMarginal + 1;
        double[][] last = new double[len][];
        int x = info[0].index;
        int scoreLen = info[0].maxCum - info[0].minCum + 1;        
//        System.err.println("Min " + info[0].minCum + " max " + info[0].maxCum);
        for (int i = 0; i < len; i++)
        {   // i is the marginal sum on the top row - min possible
            // We might be able to do better by keeping track of bounds for the
            // minimum and maximum interesting score at each point given the
            // marginals there, but this code is quite complicated enough already
            last[i] = new double[scoreLen];
            int got = score[x][i + info[0].minMarginal - min[x]];
//            System.err.println("Score " + got);
            if (got < info[0].minCum)
                got = info[0].minCum;
            if (got > info[0].maxCum)
                got = info[0].maxCum;
            last[i][got - info[0].minCum] = 1.0;
        }
//        System.err.println("Done degenerates");
        // Now produce the probability distributions for the contribution from
        // the first i+1 columns, given the distributions of the contribution from
        // the first i columns
        for (int i = 1; i < top.length; i++)
        {   // i is the column
            int nextLen = info[i].maxMarginal - info[i].minMarginal + 1;
            int nextScoreLen = info[i].maxCum - info[i].minCum + 1;
            double[][] next = new double[nextLen][];
            x = info[i].index;
            for (int j = 0; j < nextLen; j++)
            {   // j is the marginal sum on the top row - min possible
                int lo = min[x]; // min possible value in this cell
                int lo2 = info[i].minMarginal + j - info[i - 1].maxMarginal;
                if (lo < lo2)
                    lo = lo2;
                int hi = max[x];
                int hi2 = info[i].minMarginal + j - info[i - 1].minMarginal;
                if (hi2 < hi)
                    hi = hi2;
                next[j] = new double[nextScoreLen];
                int contribPos = lo - min[x];
                int prevPos = info[i].minMarginal + j - lo - info[i - 1].minMarginal;              
                for (int k = lo; k <= hi; k++)
                {   // k is the value in the top row of the current column
                    int a = k;
                    int b = info[i].minMarginal + j - k;
                    int c = top[x] + bottom[x] - k;
                    int d = info[i].sumHere - a - b - c;
//                    System.err.println("a " + a + " b " + b + " c " + c + " d " + d);
                    // 'Here' specifies the current marginal and the current column,
                    // which specifies the previous marginal. We can treat this as
                    // a 2x2 chi-square to work out the probability of 'here' given
                    // the current marginal. The standard probabilities for the Fisher
                    // 2x2 chi-square gives us the probs
                    double probHere = LogFact.lF(a + b) + LogFact.lF(a + c) + 
                                      LogFact.lF(c + d) + LogFact.lF(b + d) -
                                      LogFact.lF(a) - LogFact.lF(b) -
                                      LogFact.lF(c) - LogFact.lF(d) -
                                      LogFact.lF(info[i].sumHere);
                    probHere = Math.exp(probHere);
                    int contrib = score[x][contribPos];
                    // Work out at which point in our contribution we just hit
                    // the lowest score we're interested in
                    int hitsLow = info[i].minCum - contrib - info[i - 1].minCum;
                    int past = hitsLow + 1;
                    if (past > scoreLen)
                        past = scoreLen; 
                    double total = 0.0;
                    // System.err.println("first to < " + past);
                    for(int l = 0; l < past; l++)
                        total += last[prevPos][l];
                    next[j][0] += total * probHere;
                    // Work out at which point we just hit the highest score we're
                    // interested in
                    int hitsHigh = info[i].maxCum - contrib - info[i - 1].minCum;
                    if (hitsHigh < 0)
                        hitsHigh = 0;
                    if (hitsHigh < past) // Can happen if maxCum = minCum
                        hitsHigh = past; // Need to avoid double counting
                    total = 0.0;
                    // System.err.println("Last from " + hitsHigh + " to < " + scoreLen);
                    for (int l = hitsHigh; l < scoreLen; l++)
                        total += last[prevPos][l];
                    next[j][nextScoreLen - 1] += total * probHere;
                    if (hitsHigh > 0)
                    {
                        int startHere = hitsLow + 1;
                        if (startHere < 0)
                            startHere = 0;
                        if (hitsHigh > scoreLen)
                            hitsHigh = scoreLen;
                       // System.err.println("Middle from " + startHere + " to < " +
                       //  hitsHigh);
                        for (int l = startHere; l < hitsHigh; l++)
                            next[j][l - hitsLow] += last[prevPos][l] * probHere;
                    }
                    contribPos++;
                    prevPos--;
                }
                double total = 0.0;
                for (int k = 0; k < nextScoreLen; k++)
                    total += next[j][k];
                if (total < 0.9999 || total > 1.0001)
                    throw new IllegalArgumentException("Total is " + total + " at i = " +
                                                       i + " j = " + j);  
            }
            last = next;
            scoreLen = nextScoreLen;
        }
//        System.err.println("finishing");
        prob3[0] = last[0][0];
        prob3[1] = last[0][1];
        prob3[2] = last[0][2];
        return observed;
    }
    /** Test a 2xn contingency table using as a statistic minus its log
     * likelihood given its marginals
     * @param top The counts in the top row of the table
     * @param bottom The counts in the bottom row of the table
     * @param grain When converting to scaled integers, we scale them
     * to the range 0..grain
     * @param vals[0] receives the observed minus log likelihood
     * @param vals[1] receives the observed minus log likelihood according
     * to the grained calculations, so you can get some feel as to how
     * accurate they are. Loss of accuracy here means the test is probably
     * less powerful than it should be, but doesn't invalidate the reported
     * significance.
     */
    public static SigProb marginalTab(int[] top, int[] bottom,
        int grain, double[] vals)
    {
        int topMin[] = new int[top.length];
        int topMax[] = new int[top.length];
        range(top, bottom, topMin, topMax);
        /** Probability of table given its marginals is
            Prod Ni.! Prod N.j!
             i         j
            --------------------
            N..! Prod Nij!
                  i,j
            We want minus the log of this so that very odd tables have
            large figures, just as with chi-squared
         */
        double[][] scores = new double[top.length][];
        // First of all, put in the contributions to Nij!, and each column's
        // N.j!
        int topSum = 0;
        int bottomSum = 0;
        double conTerm = 0.0;
        for (int i = 0; i < top.length; i++)
        {
            topSum += top[i];
            bottomSum += bottom[i];
            int colSum = top[i] + bottom[i];
            conTerm -= LogFact.lF(colSum);
            int len = topMax[i] - topMin[i] + 1;
            scores[i] = new double[len];
            int topV = topMin[i];
            int bottomV = colSum - topV;
            for (int j = 0; j < len; j++)
            {
//                System.err.println("TopV " + topV + " bottomV " + bottomV);
                scores[i][j] = LogFact.lF(topV) + LogFact.lF(bottomV);
                topV++;
                bottomV--;
            }
        }
        conTerm -= LogFact.lF(topSum) + LogFact.lF(bottomSum);
        conTerm += LogFact.lF(topSum + bottomSum);
        // Shove constant term into first score
        for (int i = 0; i < scores[0].length; i++)
            scores[0][i] += conTerm;
        int grainedScores[][] = new int[scores.length][];
        for (int i = 0; i < scores.length; i++)
            grainedScores[i] = new int[scores[i].length];
        Grain g = new Grain(scores, grainedScores, grain);
        double sofar = 0.0;
        int sofarInt = 0;
        for (int i = 0; i < top.length; i++)
        {
            int offset = top[i] - topMin[i];
            sofar += scores[i][offset];
            sofarInt += grainedScores[i][offset];
        }
        vals[0] = sofar;
        vals[1] = g.intToDouble(sofarInt);
        double[] probs = new double[3];
        tabDist(top, bottom, grainedScores, probs);
        return new SigProb(probs[0], probs[1], probs[2]);
    }
    /** Test a contingency table using as a statistic a trend score
     * @param top The counts in the top row of the table
     * @param bottom The counts in the bottom row of the table
     * @param grain When converting to scaled integers, we scale them
     * to the range 0..grain
     * @param vals[0] receives the observed minus log likelihood
     * @param vals[1] receives the observed minus log likelihood according
     * to the grained calculations, so you can get some feel as to how
     * accurate they are. Loss of accuracy here means the test is probably
     * less powerful than it should be, but doesn't invalidate the reported
     * significance.
     */
    public static SigProb trendTab(int[] top, int[] bottom, int grain, double[] vals)
    {
        int topMin[] = new int[top.length];
        int topMax[] = new int[top.length];
        range(top, bottom, topMin, topMax);
        double[][] scores = new double[top.length][];
        for (int i = 0; i < top.length; i++)
        {
            int len = topMax[i] - topMin[i] + 1;
            scores[i] = new double[len];
            for (int j = 0; j < len; j++)
            {
//                System.err.println("TopV " + topV + " bottomV " + bottomV);
                scores[i][j] = (j + topMin[i]) * (i * 2.0 - top.length + 1.0);
            }
        }
        int grainedScores[][] = new int[scores.length][];
        for (int i = 0; i < scores.length; i++)
            grainedScores[i] = new int[scores[i].length];
        Grain g = new Grain(scores, grainedScores, grain);
        double sofar = 0.0;
        int sofarInt = 0;
        for (int i = 0; i < top.length; i++)
        {
            int offset = top[i] - topMin[i];
            sofar += scores[i][offset];
            sofarInt += grainedScores[i][offset];
        }
        vals[0] = sofar;
        vals[1] = g.intToDouble(sofarInt);
        double[] probs = new double[3];
        tabDist(top, bottom, grainedScores, probs);
        return new SigProb(probs[0], probs[1], probs[2]);
    }
    /** Test a contingency table using as a statistic the log-likelihood
     * variant of  the chi-squared test.
     * @param top The counts in the top row of the table
     * @param bottom The counts in the bottom row of the table
     * @param grain When converting to scaled integers, we scale them
     * to the range 0..grain
     * @param vals[0] receives the observed minus log likelihood
     * @param vals[1] receives the observed minus log likelihood according
     * to the grained calculations, so you can get some feel as to how
     * accurate they are. Loss of accuracy here means the test is probably
     * less powerful than it should be, but doesn't invalidate the reported
     * significance.
     */
    public static SigProb llChiTab(int[] top, int[] bottom, int grain, double[] vals)
    {
        // Log-likelihood chi-squared is twice 
        //    SUM_ij n_ij(log(n_ij)+ log N - log(n_i) - log(n_j))
        int topMin[] = new int[top.length];
        int topMax[] = new int[top.length];
        int topCount = 0;
        int bottomCount = 0;
        for (int i = 0; i < top.length; i++)
        {
            topCount += top[i];
            bottomCount += bottom[i];
        }
        if (topCount == 0 || bottomCount == 0)
        {
            vals[0] = vals[1] = 0.0;
            return new SigProb(0.0, 1.0, 0.0);
        }
        double logTop = Math.log(topCount);
        double logBottom = Math.log(bottomCount);
        double logAll = Math.log(topCount + bottomCount);
        range(top, bottom, topMin, topMax);
        double[][] scores = new double[top.length][];
        for (int i = 0; i < top.length; i++)
        {
            int len = topMax[i] - topMin[i] + 1;
            scores[i] = new double[len];
            for (int j = 0; j < len; j++)
            {
                int here = j + topMin[i];
                int bottomHere = top[i] + bottom[i] - here;
                double sofar = 0.0;
                if (here > 0)
                    sofar += here * (Math.log(here) + logAll - Math.log(top[i] + bottom[i]) -
                                     logTop);
                if (bottomHere > 0)
                    sofar += bottomHere * (Math.log(bottomHere) + logAll - Math.log(top[i] +
                                      bottom[i]) - logBottom);
                scores[i][j] = sofar * 2.0;
            }
        }
        int grainedScores[][] = new int[scores.length][];
        for (int i = 0; i < scores.length; i++)
            grainedScores[i] = new int[scores[i].length];
        Grain g = new Grain(scores, grainedScores, grain);
        double sofar = 0.0;
        int sofarInt = 0;
        for (int i = 0; i < top.length; i++)
        {
            int offset = top[i] - topMin[i];
            sofar += scores[i][offset];
            sofarInt += grainedScores[i][offset];
        }
        vals[0] = sofar;
        vals[1] = g.intToDouble(sofarInt);
        double[] probs = new double[3];
        tabDist(top, bottom, grainedScores, probs);
        return new SigProb(probs[0], probs[1], probs[2]);
    }
    public static void main(String[] s) throws IOException
    {
        int types = 0;
        int argp = 0;
        boolean doLogProb = false;
        boolean doTrend = false;
        boolean doLLChi = false;
        boolean trouble = false;
        int grain = 1000;
        try
        {
            for (argp = 0; argp < s.length; argp++)
            {
                if ("-prob".equals(s[argp]))
                    doLogProb = true;
                else if ("-trend".equals(s[argp]))
                    doTrend = true;
                else if ("-ll".equals(s[argp]))
                    doLLChi = true;
                else if (argp < s.length - 1 && "-cols".equals(s[argp]))
                {
                    argp++;
                    types = Integer.parseInt(s[argp]);
                }
                else if (argp < s.length - 1 && "-grain".equals(s[argp]))
                {
                    argp++;
                    grain = Integer.parseInt(s[argp]);
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
        if (types <= 0)
        {
            System.err.println("Must give # of columns > 0");
            trouble = true;
        }
        if (trouble)
        {
            System.err.println("Usage is TabDist -cols # [-grain #] [-prob] [-trend]" +
                " [-ll]");
            System.exit(1);
        }
        int[] top = new int[types];
        int[] bottom = new int[types];
        StreamTokenizer st = new StreamTokenizer(
            new InputStreamReader(System.in));
        st.commentChar('#');
        for(int i = 0; i < types * 2;)
        {
            int tok = st.nextToken();
            switch(tok)
            {
                case StreamTokenizer.TT_NUMBER:
                    if (i < types)
                        top[i] = (int)st.nval;
                    else
                        bottom[i - types] = (int)st.nval;
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
        System.out.println("Grain is " + grain);
        System.out.println("Got table");
        for (int i = 0;; i++)
        {
            System.out.print(top[i]);
            if (i < types - 1)
                System.out.print(' ');
            else
            {
                System.out.println();
                break;
            }
        }
        for (int i = 0;; i++)
        {
            System.out.print(bottom[i]);
            if (i < types - 1)
                System.out.print(' ');
            else
            {
                System.out.println();
                break;
            }
        }
        double[] vals = new double[2];
        if (doLogProb)
        {
            SigProb sp = marginalTab(top, bottom, grain, vals);
            System.out.println("Minus Log prob given marginals is " +
                vals[0] + " grained version is " + vals[1]);
            System.out.println("Significance: " + sp);
        }
        if (doTrend)
        {
            SigProb sp = trendTab(top, bottom, grain, vals);
            System.out.println("Trend is " + vals[0] +
                " grained version is " + vals[1]);
            System.out.println("Significance: " + sp);
        }
        if (doLLChi)
        {
            SigProb sp = llChiTab(top, bottom, grain, vals);
            System.out.println("Log-likelihood Chi-squared is " + vals[0] +
                " grained version is " + vals[1]);
            System.out.println("Significance: " + sp);
        }
    }
}

class Info implements Comparable
{
    int range;
    int minCum;
    int maxCum;
    int minMarginal;
    int maxMarginal;
    int sumHere;
    int minHere;
    int maxHere;
    int index;
    public int compareTo(Object o)
    {
        Info i = (Info)o;
        return range - i.range; // Overflow here means we're dead anyway
    }
}

