package uk.co.demon.mcdowella.stats;

import java.util.Arrays;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Random;
import java.io.StreamTokenizer;

/** This package contains a static routine to work out the distribution produced when you
 *  have an array of counts taken by throwing balls into boxes where box[i] is hit with
 *  probability prob[i], then summing up the contributions from score[i][count]
 */
public class RowDist
{
    /** Return an array of probabilities
     * @param balls the number of balls thrown. Must have balls >= 0
     * @param boxProbs[i] is the probability of a ball landing in box i. Must have
     * 0 < boxProbs[i] < 1
     * @param score[i][j] is the contribution to the total score if you end up with
     * j balls in box i. Must be >= 0.
     * @return an array such that array[i] is the probability of total score i
     */
    public static double[] rowDist(int balls, double[] boxProbs, int[][] score)
    {
       if (balls < 0)
           throw new IllegalArgumentException("balls < 0");
       // Check the parms
       double sum = 0.0;
       for (int i = 0; i < boxProbs.length; i++) 
       {
           double p = boxProbs[i];
           if (p <= 0 || p >= 1.0)
               throw new IllegalArgumentException("invalid probability " + p + " at " + i);
           sum += p;
       }
       if (sum > 1.0001 || sum < 0.9999)
           throw new IllegalArgumentException("Probabilities add to " + sum);
       if (balls == 0)
       {
           int totalScore = 0;
           for (int i = 0; i < boxProbs.length; i++)
               totalScore += score[i][0];
           double[] answer = new double[totalScore + 1];
           Arrays.fill(answer, 0.0);
           answer[totalScore] = 1.0;
           return answer;
       }
       // Work out the maximum and minimum score from each box
       BoxScore[] bs = new BoxScore[boxProbs.length];
       for (int i = 0; i < boxProbs.length;i++)
       {
           bs[i] = new BoxScore();
           bs[i].boxNo = i;
           int max = score[i][0];
           int min = max;
           for (int j = 1; j < balls; j++)
           {
               int x = score[i][j];
               if (x < min)
                   min = x;
               if (x > max)
                   max = x;
           }
           if (min < 0)
               throw new IllegalArgumentException("-ve score at " + i);
           bs[i].minScore = min;
           bs[i].maxScore = max;
       }
       Arrays.sort(bs);
       // At each step from now on we keep track of the distribution of the
       // contribution of the score from the first sofar boxes, given that there
       // are N balls in the first sofar boxes.
       int atBox = bs[0].boxNo;
       Distribution[] d = new Distribution[balls + 1];
       for (int i = 0; i <= balls; i++)
       {
           d[i] = new Distribution(score[atBox][i],score[atBox][i]);
           d[i].scoreProbs[0] = 1.0;
       }
       Distribution[] after = new Distribution[balls + 1];
       double probSofar = boxProbs[atBox]; 
       for (int sofar = 1; sofar < boxProbs.length; sofar++)
       {
           // System.err.println("Sofar = " + sofar);
           atBox = bs[sofar].boxNo;
           double myProb = boxProbs[atBox];
           probSofar += myProb;
           myProb = myProb / probSofar; // Prob of ball in this box, given ball somewhere
                                        // in contributing subset
           double otherProb = 1.0 - myProb;
           double logMe = Math.log(myProb);
           double logOther = Math.log(otherProb);
           int i = 0;
           if (sofar == boxProbs.length - 1)
               i = balls;
           for (;i <= balls; i++)
           {               
               int min = score[atBox][0] + d[i].minScore;
               int max = score[atBox][0] + d[i].maxScore;
               for (int j = 1; j <= i; j++)
               {
                   int minHere = score[atBox][j] + d[i - j].minScore;
                   if (minHere < min)
                       min = minHere;
                   int maxHere = score[atBox][j] + d[i - j].maxScore;
                   if (maxHere > max)
                       max = maxHere;
               }
               Distribution newDist = new Distribution(min, max);
               Arrays.fill(newDist.scoreProbs, 0.0);
               double logTotFact = LogFact.lF(i);
               for (int j = 0; j <= i; j++)
               {
                   int otherBalls = i - j;
                   double probHere = Math.exp(logMe * j + logOther * otherBalls +
                       + logTotFact - LogFact.lF(j) - LogFact.lF(i - j));
                   int minHere = newDist.minScore;
                   int oldMin = d[otherBalls].minScore;
                   int oldMax = d[otherBalls].maxScore;
                   int contrib = score[atBox][j];
                   for (int k = oldMin; k <= oldMax; k++)
                   {
                       newDist.scoreProbs[k - minHere + contrib] +=
                           probHere * d[otherBalls].scoreProbs[k - oldMin];
                   }
               }
               double checkTotal = 0.0;
               for (int j = newDist.minScore; j <= newDist.maxScore; j++)
                   checkTotal += newDist.scoreProbs[j - newDist.minScore];
               if (checkTotal < 0.9999 || checkTotal > 1.0001)
                   throw new IllegalArgumentException("Internal Error");
               after[i] = newDist;
           }
           if (sofar == boxProbs.length - 1)
           {
               double[] answer = new double[after[balls].maxScore + 1];
               System.arraycopy(after[balls].scoreProbs, 0, answer,
                   after[balls].minScore, after[balls].maxScore - after[balls].minScore + 1);
               if (after[balls].minScore > 0)
                   Arrays.fill(answer, 0, after[balls].minScore - 1, 0.0);
               return answer;
           }
           Distribution[] t = d;
           d = after;
           after = t;
       }
       // Should never get here
       throw new IllegalArgumentException("Internal error - off end of loop");
    }

    /** Return an array of probabilities as before, but combine all probablities
     *  for values >= threshold to save time and memory
     * @param balls the number of balls thrown. Must have balls >= 0
     * @param boxProbs[i] is the probability of a ball landing in box i. Must have
     * 0 < boxProbs[i] < 1
     * @param score[i][j] is the contribution to the total score if you end up with
     * j balls in box i. Must be >= 0.
     * @param threshold combine all probabilities for values >= this
     * @return an array such that array[i] is the probability of total score i
     */
    public static double[] rowDist(int balls, double[] boxProbs, int[][] score,
        int threshold)
    {
       if (balls < 0)
           throw new IllegalArgumentException("balls < 0");
       // Check the parms
       double sum = 0.0;
       for (int i = 0; i < boxProbs.length; i++) 
       {
           double p = boxProbs[i];
           if (p <= 0 || p >= 1.0)
               throw new IllegalArgumentException("invalid probability " + p + " at " + i);
           sum += p;
       }
       if (sum > 1.0001 || sum < 0.9999)
           throw new IllegalArgumentException("Probabilities add to " + sum);
       if (balls == 0)
       {
           int totalScore = 0;
           for (int i = 0; i < boxProbs.length; i++)
               totalScore += score[i][0];
           if (totalScore > threshold)
               totalScore = threshold;
           double[] answer = new double[totalScore + 1];
           Arrays.fill(answer, 0.0);
           answer[totalScore] = 1.0;
           return answer;
       }
       // Work out the maximum and minimum score from each box
       BoxScore[] bs = new BoxScore[boxProbs.length];
       for (int i = 0; i < boxProbs.length;i++)
       {
           bs[i] = new BoxScore();
           bs[i].boxNo = i;
           int max = score[i][0];
           int min = max;
           for (int j = 1; j < balls; j++)
           {
               int x = score[i][j];
               if (x < min)
                   min = x;
               if (x > max)
                   max = x;
           }
           if (min < 0)
               throw new IllegalArgumentException("-ve score at " + i);
           if (max > threshold)
               max = threshold;
           if (min > threshold)
               min = threshold;
           bs[i].minScore = min;
           bs[i].maxScore = max;
       }
       Arrays.sort(bs);
       // At each step from now on we keep track of the distribution of the
       // contribution of the score from the first sofar boxes, given that there
       // are N balls in the first sofar boxes.
       int atBox = bs[0].boxNo;
       Distribution[] d = new Distribution[balls + 1];
       for (int i = 0; i <= balls; i++)
       {
           int scoreHere = score[atBox][i];
           if (scoreHere > threshold)
               scoreHere = threshold;
           d[i] = new Distribution(scoreHere,scoreHere);
           d[i].scoreProbs[0] = 1.0;
       }
       Distribution[] after = new Distribution[balls + 1];
       double probSofar = boxProbs[atBox]; 
       for (int sofar = 1; sofar < boxProbs.length; sofar++)
       {
           // System.err.println("Sofar = " + sofar);
           atBox = bs[sofar].boxNo;
           double myProb = boxProbs[atBox];
           probSofar += myProb;
           myProb = myProb / probSofar; // Prob of ball in this box, given ball somewhere
                                        // in contributing subset
           double otherProb = 1.0 - myProb;
           double logMe = Math.log(myProb);
           double logOther = Math.log(otherProb);
           int i = 0;
           if (sofar == boxProbs.length - 1)
               i = balls;
           for (;i <= balls; i++)
           {               
               int min = score[atBox][0] + d[i].minScore;
               int max = score[atBox][0] + d[i].maxScore;
               for (int j = 1; j <= i; j++)
               {
                   int minHere = score[atBox][j] + d[i - j].minScore;
                   if (minHere < min)
                       min = minHere;
                   int maxHere = score[atBox][j] + d[i - j].maxScore;
                   if (maxHere > max)
                       max = maxHere;
               }
               if (max > threshold)
                   max = threshold;
               if (min > threshold)
                   min = threshold;
               Distribution newDist = new Distribution(min, max);
               Arrays.fill(newDist.scoreProbs, 0.0);
               double logTotFact = LogFact.lF(i);
               for (int j = 0; j <= i; j++)
               {
                   int otherBalls = i - j;
                   double probHere = Math.exp(logMe * j + logOther * otherBalls +
                       + logTotFact - LogFact.lF(j) - LogFact.lF(i - j));
                   int minHere = newDist.minScore;
                   int oldMin = d[otherBalls].minScore;
                   int oldMax = d[otherBalls].maxScore;
                   int contrib = score[atBox][j]; // contribution from this box
                   int threshAt = threshold - contrib; // hit threshold here
                   int past = oldMax + 1;
                   if (past > threshAt)
                       past = threshAt;
                   // System.err.println("Loop 1");
                   for (int k = oldMin; k < past; k++) 
                   { // Work out everything under threshold
                       newDist.scoreProbs[contrib + k - minHere] +=
                           probHere * d[otherBalls].scoreProbs[k - oldMin];
                   }
                   // System.err.println("Loop 2");
                   if (past <= oldMax)
                   {   // Work out total prob >= threshold
                       double threshSum = 0.0;
                       if (past < oldMin)
                           past = oldMin;
                       for (int k = past; k <= oldMax; k++)
                           threshSum += d[otherBalls].scoreProbs[k - oldMin];
                       // System.err.println("After inner");
                       newDist.scoreProbs[threshold - minHere] +=
                           probHere * threshSum;
                   }
                   // System.err.println("Loop over");
               }
               double checkTotal = 0.0;
               for (int j = newDist.minScore; j <= newDist.maxScore; j++)
                   checkTotal += newDist.scoreProbs[j - newDist.minScore];
               if (checkTotal < 0.9999 || checkTotal > 1.0001)
                   throw new IllegalArgumentException("Internal Error");
               after[i] = newDist;
           }
           if (sofar == boxProbs.length - 1)
           {
               double[] answer = new double[after[balls].maxScore + 1];
               System.arraycopy(after[balls].scoreProbs, 0, answer,
                   after[balls].minScore, after[balls].maxScore - after[balls].minScore + 1);
               if (after[balls].minScore > 0)
                   Arrays.fill(answer, 0, after[balls].minScore - 1, 0.0);
               return answer;
           }
           Distribution[] t = d;
           d = after;
           after = t;
       }
       // Should never get here
       throw new IllegalArgumentException("Internal error - off end of loop");
    }

    /** Exact chi-squared against null hypothesis of equality. Note that the chi-squared
     * against flat random is SUM_i (x_i - mean)^2 / mean, which is basically
     * SUM_i x_i^2 with some flannel that either cancels or is affine as far as we
     * are concerned. In fact, we can even use SUM_ x_i(x_i - 1)/2 - the number of
     * repeats, which is smaller
     * @param counts the observed counts
     * @param the probability of each value counted under then null
     * hypothesis. If probs[i] != 1.0/counts.length, then the significance
     * returned by this routine is still accurate, but you're probably not
     * testing for anything very sensible.
     * @return a SigProb giving the significance
     */
    public static SigProb exactFlat(int[] counts, double[] probs)
    {
        int total = 0;
        for (int i = 0; i < counts.length; i++)
            total += counts[i];
        int[] basicScore = new int[total + 1];
        for (int i = 0; i <= total; i++)
            basicScore[i] = (i * (i - 1)) / 2;
        int[][] score = new int[counts.length][];
        int ourScore = 0;
        for (int i = 0; i < counts.length; i++)
        {
            score[i] = basicScore;
            ourScore += score[i][counts[i]];
        }
        double[] answer = rowDist(total, probs, score, ourScore + 1);
        double lt = 0.0;
        for (int i = 0; i < ourScore; i++)
            lt += answer[i];
        double gt = 0.0;
        for (int i = ourScore + 1; i < answer.length; i++)
            gt += answer[i];
        return new SigProb(lt, answer[ourScore], gt);
    }
    /** Return the semi-exact significance of the Log-likelihood
     * chi-squared against the null hypothesis that the counts are
     * generated by independent selection according to the given
     * probs.
     * @param counts The observed counts
     * @param probs holds the probability of each sort of object counted
     * under then null hypothesis
     * @param grain the range (0..grain) of integers to which the 
     * log-likelihood contributions will be scaled
     * @param vals[0] will be set to the true log-likelihood chi-squared
     * @param vals[1] will be set to the log-likelihood chi-squared
     * according to the grained calculation (to give you a rough feel for
     * how much accuracy is lost here). Inaccuracy here probably means
     * you're getting less than ideal power, but doesn't effect the
     * significance level reported
     * @return a SigProb giving the significance level attained.
     */
    public static SigProb llProb(int[] counts, double[] probs, int grain, double[] vals)
    {
        // Log-likelihood chi-squared is SUM_i x_i ln (x_i / N) - x_i ln p_i
        // and we will work out twice this to make this chi-squared
        int total = 0;
        for (int i = 0; i < counts.length; i++)
            total += counts[i];
        double[][] scores = new double[counts.length][];
        double lt = Math.log(total);
        for (int i = 0; i < counts.length; i++)
        {
            scores[i] = new double[total + 1];
            double probHere = Math.log(probs[i]);
            for (int j = 1; j <= total; j++)
                scores[i][j] = 2.0 * (j * (Math.log(j) - lt - probHere));
        }
        int grainedScores[][] = new int[scores.length][];
        for (int i = 0; i < scores.length; i++)
            grainedScores[i] = new int[scores[i].length];
        Grain g = new Grain(scores, grainedScores, grain);
        double sofar = 0.0;
        int sofarInt = 0;
        for (int i = 0; i < counts.length; i++)
        {
            int offset = counts[i];
            sofar += scores[i][offset];
            sofarInt += grainedScores[i][offset];
        }
        vals[0] = sofar;
        vals[1] = g.intToDouble(sofarInt);
        double[] res = rowDist(total, probs, grainedScores, sofarInt + 1);
        lt = 0.0;
        for (int i = 0; i < sofarInt; i++)
            lt += res[i];
        double gt = 0.0;
        for (int i = sofarInt + 1; i < res.length; i++)
            gt += res[i];
        return new SigProb(lt, res[sofarInt], gt);
    }

    /** Return the semi-exact significance of a trend statistic
     * against the null hypothesis that the counts are
     * generated by independent selection according to the given
     * probs.
     * @param counts The observed counts
     * @param probs holds the probability of each sort of object counted
     * under then null hypothesis
     * @param grain the range (0..grain) of integers to which the 
     * log-likelihood contributions will be scaled
     * @param vals[0] will be set to the true log-likelihood chi-squared
     * @param vals[1] will be set to the log-likelihood chi-squared
     * according to the grained calculation (to give you a rough feel for
     * how much accuracy is lost here). Inaccuracy here probably means
     * you're getting less than ideal power, but doesn't effect the
     * significance level reported
     * @return a SigProb giving the significance level attained.
     */
    public static SigProb trend(int[] counts, double[] probs, int grain, double[] vals)
    {
        int total = 0;
        for (int i = 0; i < counts.length; i++)
            total += counts[i];
        double[][] scores = new double[counts.length][];
        double lt = Math.log(total);
        for (int i = 0; i < counts.length; i++)
        {
            scores[i] = new double[total + 1];
            double times = 2.0 * i - counts.length + 1.0;
            for (int j = 1; j <= total; j++)
                scores[i][j] = j * times;
        }
        int grainedScores[][] = new int[scores.length][];
        for (int i = 0; i < scores.length; i++)
            grainedScores[i] = new int[scores[i].length];
        Grain g = new Grain(scores, grainedScores, grain);
        double sofar = 0.0;
        int sofarInt = 0;
        for (int i = 0; i < counts.length; i++)
        {
            int offset = counts[i];
            sofar += scores[i][offset];
            sofarInt += grainedScores[i][offset];
        }
        vals[0] = sofar;
        vals[1] = g.intToDouble(sofarInt);
        double[] res = rowDist(total, probs, grainedScores, sofarInt + 1);
        lt = 0.0;
        for (int i = 0; i < sofarInt; i++)
            lt += res[i];
        double gt = 0.0;
        for (int i = sofarInt + 1; i < res.length; i++)
            gt += res[i];
        return new SigProb(lt, res[sofarInt], gt);
    }

    public static void main(String[] s) throws IOException
    {
        int types = 0;
        int argp = 0;
        boolean doRpt = false;
        boolean doChi = false;
        boolean trouble = false;
        boolean readProbs = false;
        boolean doTrend = false;
        int grain = 1000;
        long seed = 123;
        int mc = 0;
        try
        {
            for (argp = 0; argp < s.length; argp++)
            {
                if ("-rpt".equals(s[argp]))
                    doRpt = true;
                else if ("-trend".equals(s[argp]))
                    doTrend = true;
                else if ("-chi".equals(s[argp]))
                    doChi = true;
                else if ("-probs".equals(s[argp]))
                    readProbs = true;
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
                else if (argp < s.length - 1 && "-mc".equals(s[argp]))
                {
                    argp++;
                    mc = Integer.parseInt(s[argp]);
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
        if (types <= 0)
        {
            System.err.println("Must give # of columns > 0");
            trouble = true;
        }
        if (trouble)
        {
            System.err.println("Usage is TabDist -cols # [-grain #] [-probs] [-rpt] " +
                "[-trend] [-chi] [-mc #]");
            System.exit(1);
        }
        int[] top = new int[types];
        double[] probs = new double[types];
        if (!readProbs)
            Arrays.fill(probs, 1.0 / types);
        StreamTokenizer st = new StreamTokenizer(
            new InputStreamReader(System.in));
        st.commentChar('#');
        loop:for(int i = 0;;)
        {
            int tok = st.nextToken();
            switch(tok)
            {
                case StreamTokenizer.TT_NUMBER:
                    if (i < types && readProbs)
                        probs[i] = st.nval;
                    else 
                        top[i] = (int)st.nval;                   
                    i++;
                    if (i >= types)
                    {
                        if (!readProbs)
                            break loop;
                        readProbs = false;
                        i = 0;
                    }
                    break;
                case StreamTokenizer.TT_EOF:
                    System.err.println("Unexpected end of file");
                    System.exit(1);
                default:
                    System.err.println("Unexpected token " + tok);
                    System.exit(1);        
            }
        }
        System.out.println("Got Counts:");
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
        double totProb = 0.0;
        boolean over = false;
        for (int i = 0; i < types; i++)
        {
            if (probs[i] <= 0.0)
            {
                System.err.println("Seen prob " + probs[i] + " <= 0.0");
                System.exit(1);
            }
            if (probs[i] >= 1.0)
                over = true;
            totProb += probs[i];
        }
        if (over)
        {
            System.out.println("Rescaling probs");
            totProb = 1.0 / totProb;
            for (int i = 0; i < types; i++)
                probs[i] *= totProb;
        }
        else if (totProb < 0.9999 || totProb > 1.0001)
        {
            System.err.println("Probs sum to " + totProb);
            System.exit(1);
        }
        System.out.println("Probs are:");
        for (int i = 0;; i++)
        {
            System.out.print(probs[i]);
            if (i < types - 1)
                System.out.print(' ');
            else
            {
                System.out.println();
                break;
            }
        }
        if (doRpt)
        {
            System.out.flush();
            SigProb sp = exactFlat(top, probs);
            System.out.println("Rpt significance is " + sp);
        }
        if (doChi)
        {
            System.out.flush();
            double[] vals = new double[2];
            SigProb sp = llProb(top, probs, grain, vals);
            System.out.println("LL-chi squared is " + vals[0] +
                " grained " + vals[1]);
            System.out.println("LL significance is " + sp);
        }
        if (doTrend)
        {
            System.out.flush();
            double[] vals = new double[2];
            SigProb sp = trend(top, probs, grain, vals);
            System.out.println("Trend is " + vals[0] +
                " grained " + vals[1]);
            System.out.println("Trend significance is " + sp);
        }
        if (mc > 0)
        {
            System.out.println("M/C seed " + seed + " runs: " + mc);
            System.out.flush();
            int forMc[] = new int[types];
            System.arraycopy(top, 0, forMc, 0, types);
            McCount rpt = new McCount();
            McCount chi = new McCount();
            McCount trend = new McCount();
            int total = 0;
            for (int i = 0; i < types; i++)
                total += forMc[i];
            Random rr = new Random(seed);
            RoughRandom ap = new RoughRandom(probs, rr);
            for (int mcGo = 0;;)
            {
                // Might as well get the stats anyway, and this way lets
                // the user run MC-only
                // if (doRpt) 
                {
                    double sofar = 0.0;
                    for (int i = 0; i < types; i++)
                        sofar += forMc[i] * (forMc[i] - 1.0);
                    rpt.sample(sofar * 0.5);
                }
                // if (doChi)
                {
                    double sofar = 0.0;
                    for (int i = 0; i < types; i++)
                    {
                        if (forMc[i] == 0)
                            continue;
                        sofar += forMc[i] * Math.log(forMc[i] /
                                                   (total * probs[i]));
                    }
                    chi.sample(sofar * 2.0);
                }
                // if (doTrend)
                {
                    double sofar = 0.0;
                    for (int i = 0; i < types; i++)
                        sofar += forMc[i] * (i * 2.0 - types + 1.0);
                    trend.sample(sofar);
                }
                mcGo++;
                if (mcGo >= mc)
                    break;
                Arrays.fill(forMc, 0);
                for (int i = 0; i < total; i++)
                    forMc[ap.next()]++;
            }
            // if (doRpt)
            {
                System.out.println("Rpt M/C: " + rpt);
            }
            // if (doChi)
            {
                System.out.println("Log-Likelihood Chi M/C: " + chi);
            }
            // if (doTrend)
            {
                System.out.println("Trend M/C: " + trend);
            }

        }
    }
}

class BoxScore implements Comparable
{
    int maxScore;
    int minScore;
    int boxNo;
    public int compareTo(Object o)
    {
        BoxScore other = (BoxScore) o;
        // If anything overflows here our range was too big anyway
        return maxScore - minScore - other.maxScore + other.minScore; 
    }
}

class Distribution
{
    int minScore;
    int maxScore;
    double[] scoreProbs;
    public Distribution(int min, int max)
    {
        minScore = min;
        scoreProbs = new double[max - min + 1];
        maxScore = max;
    }
}