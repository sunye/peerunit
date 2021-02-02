package uk.co.demon.mcdowella.stats;
import java.util.Random;

/** This is an implementation of Walker's method of aliases, as described in
Knuth Volume II, 3.4.1
 */
public class RoughRandom
{
    private Random r;
    private int[] alias;
    private double[] thresh;
    /** Create from a vector of probabilities or relative probabilities
     * @param probs a vector of probabilities. MUST be >= 0.0, but if they
     * don't sum to one they will be scaled so that they do
     * @exception IllegalArgumentException if any probs < 0.0 or sum = 0.0
     */
    public RoughRandom(double[] probs, Random r)
    {
        this.r = r;
        double sum = 0.0;
        for (int i = 0; i < probs.length; i++)
        {
            if (probs[i] < 0.0)
                throw new IllegalArgumentException("Probability " + i + " = " +
                    probs[i] + " < 0");
            sum += probs[i];
        }
        if (sum <= 0.0)
            throw new IllegalArgumentException("Total probability <= 0.0");
        // We will now scale the probabilities and split them into LOW and
        // HIGH values. Low if prob <= 1/probs.length, which is the probability
        // that you would get if everything was equally likely. Use longs here because
        // we don't want to have to worry about floating point inaccuracy later on.
        // 64-bit longs mean that we shouldn't loose much accuracy scaling to longs
        long[] scaled = new long[probs.length];
        int lo = 0;
        int hi = probs.length - 1;
        double scale = Long.MAX_VALUE / (2.0 * sum);
        long total = 0;
        for (int i = 0; i < probs.length; i++)
        {
            long v = Math.round(probs[i] * scale);
            scaled[i] = v;
            total += v;
        }
        // Want the result divisible by probs.length
        long remainder = total % probs.length;
        if (remainder != 0)
        {
            for (int i = 0;;i++)
            {
                if (i == probs.length)
                    i = 0;
                if (probs[i] <= 0.0)
                    continue; // Don't make the impossible possible!
                scaled[i]++;
                total++;
                remainder++;
                if (remainder == probs.length)
                    break;
            }
        }

/*
        long checkTotal = 0;
        for(int i = 0; i < probs.length; i++)
        {
            System.out.println(scaled[i]);
            checkTotal += scaled[i];
        }
        System.out.println("Check " + checkTotal + " total " + total);
        System.out.println(probs.length * (total / probs.length));
        System.out.println(total / probs.length);
*/

        // Create alias and thresh arrays. We will choose a random value
        // by picking a slot at random and choosing that slot if a uniform
        // random number is >= thresh, and the alias otherwise.
        // Repeatedly pick a low value we haven't used before. Assign it
        // to a slot and, if there is any probability left, fill it up by
        // making the alias a high value.
        alias = new int[probs.length];
        thresh = new double[probs.length]; 
        long lowVal = total / probs.length;
        int firstLow = 0;
        int firstHigh = 0;
        double over = 1.0 / lowVal;
        int nameHere = firstLow; // Normally firstLow but sometimes firstLow is
                                 // overwritten by another value
        for (int i = 0; i < probs.length; i++)
        {
            // Must be at least one value <= lowVal, because all remaining
            // values add up to an average of exactly lowVal each
            // scan for them using firstLow. Note firstLow and firstHigh never
            // move backwards, so total cost is linear in input array size
            while (scaled[firstLow] > lowVal)
            {
                firstLow++;
                nameHere = firstLow;
            }
            thresh[nameHere] = (lowVal - scaled[firstLow]) * over;
            if (scaled[firstLow] == lowVal)
            {   // Exact match
                alias[nameHere] = -1;                
                firstLow++; // Finished here
                nameHere = firstLow;
            }
            else
            {   // Must be at least one val > lowVal if here because we have
                // just seen one < lowVal
                while(scaled[firstHigh] <= lowVal)
                    firstHigh++;
                alias[nameHere] = firstHigh;
                scaled[firstHigh] -= (lowVal - scaled[firstLow]);
                if (scaled[firstHigh] <= lowVal && firstHigh < firstLow)
                { // Deal with this next: it's a new low value we won't see again
                    scaled[firstLow] = scaled[firstHigh];
                    nameHere = firstHigh;
                    firstHigh++;
                }
                else
                {
                    firstLow++; // Go on to next firstLow
                    nameHere = firstLow;
                }
            }
        }
    }
    /** Return a value from the distribution specified in the constructor
     * @return a value from the distribution specified in the constructor
     */
    public int next()
    {
        int x = r.nextInt(alias.length);
        if (alias[x] < 0)
            return x;
        if (r.nextDouble() < thresh[x])
            return alias[x];
        return x;
    }
}

