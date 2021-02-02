package uk.co.demon.mcdowella.stats;

/** This class is used to convert between real-valued and integer-valued (grained)
 * versions of statistics
 */
public class Grain
{
    /** to integer, result is round((real + addTo) * multTo)
     *  from integer, result is (real * multFrom) - addTo
     */
    double addTo;
    double multTo;
    double multFrom;
    /** Convert from double to int, using our choice of scale integer
     * representation */
    public int doubleToInt(double x) {return (int)Math.round((x + addTo) * multTo);}
    /** convert from our scaled integers to double */
    public double intToDouble(int x) {return x * multFrom - addTo;}
    /** Create a Grainer and transform a batch of scores at the same time
     * @param scores[i][j] are contributions to the final statistic. Each
     *  final result is the sum of one j value for each i.
     * @param transformed[i][j] is set so that using these scores instead
     *  produces the grained version of the scores statistic.
     * All transformed[i][j]
     *  will be >=0, and the minimum in each column will be 0
     * @param maxGrain is the maximum value to put in transformed[i][j].
     * The final maximum may be much less than that: we check the gcd of
     * the whole lot at the end and will divide by it if > 1. If grain <=
     * 0, don't multiply the values, just shift them to make the lowest
     * ones zero. This probably only makes sense if the values are known to
     * be integer valued already. 
     */
    public Grain(double[][] scores, int[][] transformed, int maxGrain)
    {
        double[] minVal = new double[scores.length];
        double[] maxVal = new double[scores.length];
        addTo = 0.0;
        boolean isInt = true;
        for (int i = 0; i < scores.length; i++)
        {
            double min = scores[i][0];
            double max = min;
            int past = scores[i].length;
            for (int j = 1; j < past; j++)
            {
                double x = scores[i][j];
                if (x < min)
                    min = x;
                if (x > max)
                    max = x;
                if (x != Math.round(x))
                    isInt = false;
            }
            minVal[i] = min;
            maxVal[i] = max;
            addTo -= min;
        }
        double maxDiff = maxVal[0] - minVal[0];
        for (int i = 1; i < scores.length; i++)
        {
            double x = maxVal[i] - minVal[i];
            if (x > maxDiff)
                maxDiff = x;
        }
        if (maxGrain >= 0)
            multFrom = maxDiff / maxGrain;
        else multFrom = 1.0;
 // Be sensible if everything is an int already
        if (isInt && maxDiff <= maxGrain)
            multFrom = 1.0;
        if (multFrom > 0.0)
            multTo = 1.0 / multFrom;
        else
            multTo = 0.0;
        int sofar = 0;
        for (int i = 0; i < scores.length; i++)
        {
            int past = scores[i].length;
            double min = minVal[i];
            for (int j = 0; j < past; j++)
            {
                int t = (int)Math.round((scores[i][j] - min) * multTo);
                transformed[i][j] = t;
                // System.err.println("i: " + i + " j: " + j + " from " +
                //    scores[i][j] + " to " + t);
                sofar = gcd(sofar, t);
            }
        }
        if (sofar > 1)
        {
            multTo = multTo / sofar;
            multFrom = multFrom * sofar;
            for (int i = 0; i < scores.length; i++)
            {
                int past = scores[i].length;
                for (int j = 0; j < past; j++)
                    transformed[i][j] = transformed[i][j] / sofar;
            }
        }
    }

    /** Greatest Common Divisor */
    public static int gcd(int a, int b)
    {
        if (a < 0)
            a = -a;
        if (b < 0)
            b = -b;
        if (a == 1 || b == 1)
            return 1;
        if (a == 0)
            return b;
        if (b == 0)
            return a;
        for(;;)
        {
            b = b % a;
            if (b == 0)
                return a;
            a = a % b;
            if (a == 0)
                return b;
        }
    }
}