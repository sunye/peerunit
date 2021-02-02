package uk.co.demon.mcdowella.stats;

/** This class is used to keep track of the results of Monte Carlo
 *  runs. It assumes that the first sample taken is the observed
 *  value but does include it in its counts. This is because, if you
 *  run an experiment and run it via Monte Carlo, and treat the Monte
 *  Carlo runs as part of the experiment you can then derive an exact
 *  probability for the chance that the real experimental
 *  result occupies its observed rank amongst the Monte Carlo runs.
 *  So if you see that the true answer is the highest one observed then
 *  you will see that 1 out of N runs in all got at least the observed
 *  score (1 true run and 0 out of N-1 M/C runs), and you can say that
 *  the chance of observing at least this was 1/N. Ditto if the counts
 *  for >, =, < are a, b, c then the probability of seeing at least
 *  a+b counts as high as the observed is exactly (a+b)/(a+b+c)
 */
public class McCount
{
    private double observed;
    int lt;
    int eq;
    int gt;
    public McCount()
   {lt = eq = gt = 0;}

    /**
     * Absorb one value. If this is the first call after construction,
     * this is the value observed in real life, whose significance we
     * are attempting to work out. If not, it is one of a number of
     * Monte-Carlo values, which have the same distribution as the real
     * life value under the null hypothesis
     * @param s The value of the statistic
     */
    public void sample(double s)
    {
        if (eq == 0)
        {
            observed = s;
            eq = 1;
            return;
        }
        if (s < observed)
        {
            lt++;
        }
        else if (s > observed)
        {
            gt++;
        }
        else
        {
            eq++;
        }
    }
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Observation ");
        sb.append(observed);
        sb.append(" Counts inc. observation: lt = ");
        sb.append(lt);
        sb.append(" eq = ");
        sb.append(eq);
        sb.append(" gt = ");
        sb.append(gt);
        sb.append(" Prob low ");
        double scale = 1.0 / (lt + eq + gt);
        sb.append((lt + eq) * scale);
        sb.append(" Prob high ");
        sb.append((eq + gt) * scale);
        return sb.toString();
    }
}