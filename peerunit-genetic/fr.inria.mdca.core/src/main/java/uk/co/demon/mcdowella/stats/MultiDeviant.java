package uk.co.demon.mcdowella.stats;
/** Simple class to keep track of multivariate statistics
 */

public class MultiDeviant
{
    /** Number of samples seen so far */
    private int n;
    /** Dimension of random variable */
    private int dim;
    /** mean so far */
    private double mean[];
    /** Sum of (x_i - mean)(x_i - mean)' so far */
    private double sumSq[];
    /** min so far for each element */
    private double min[];
    /** max so far for each element */
    private double max[];
    /** old mean - sample  */
    private double diff[];
    /** forget everything except our dimension */
    public void reset()
    {
        n = 0;
        for (int i = 0; i < dim; i++)
        {
            mean[i] = 0.0;
        }
        for (int i = 0; i < dim * dim; i++)
        {
            sumSq[i] = 0;
        }
    }
    /** Standard constructor
     * @param dim the dimension of the random variables
     */
    public MultiDeviant(int dim)
    {
        this.dim = dim;
        mean = new double[dim];
        sumSq = new double[dim * dim];
        min = new double[dim];
        max = new double[dim];
        diff = new double[dim];
        reset();
    }
    /** accept a sample
     * @param s the sample to accept
     */
    public void sample(double[] s)
    {
        // Update max and min
        for (int i = 0; i < dim; i++)
        {
            double x = s[i];
            if (n == 0 || x < min[i])
                min[i] = x;
            if (n == 0 || x > max[i])
                max[i] = x;
        }
        // Update mean and var-covariance. We want to
        // keep floating point error under control and
        // make sure var-covariance estimate is at leat
        // +ve semi-definite
        // Mean goes from y to (y * n + x) / (n + 1)
        // old mean - new mean = (y - x) / (n + 1) = e
        // new sumSq is SUM ((x_i - old mean) + (old mean - new mean)) *
        //                  ((x_i - old mean) + (old mean - new mean))' +
        //                  (x - new mean) (x - new mean)'
        // which is sumSq + e SUM (x_i - old mean)' +
        //          SUM (x_i - old mean) e' + n * e e' +
        //          n^2(x - y)(x-y)'/(n + 1)^2
        // which is sumSq + n(n + 1) e e'
        // or sumSq + (y - x)(y - x)' * n / (n + 1)
        // This is not quite what Knuth 4.2.2 suggests but at least I
        // understand it.
        double scale1 = 1.0 / (n + 1.0);
        for (int i = 0; i < dim; i++)
        {
            double e = mean[i] - s[i];
            diff[i] = e;
            mean[i] -= e * scale1;
        }
        double scale2 = n * scale1;
        int offset = 0;
        for (int i = 0; i < dim; i++)
        {
            double v = diff[i] * scale2;
            for (int j = 0; j < dim; j++)
                sumSq[offset++] += v * diff[j];
        }
        n++;
    }
    /** @return the number of samples accepted so far */
    public int getN() {return n;}
    /** return the mean
     * @param x an array to set to the mean
     */
    public void getMean(double[] x)
    {
        System.arraycopy(mean, 0, x, 0, dim);
    }
    /** return the sample variance
     * @param x a matrix to set to the sample variance
     */
    public void getVariance(double[] x)
    {
        double scale = 1.0 / (n - 1.0);
        for (int i = 0; i < dim * dim; i++)
            x[i] = sumSq[i] * scale;
    }
    /** return the maximum so far
     * @param x an array to get the max of each element so far
     */
    public void getMax(double[] x)
    {
        System.arraycopy(max, 0, x, 0, dim);
    }
    /** return the minimum so far */
    public void getMin(double[] x)
    {
        System.arraycopy(min, 0, x, 0, dim);
    }
    public String toString()
    {
        StringBuffer s=new StringBuffer();
        s.append("N="+getN()+" dim = " + dim);
        if(n>0)
        {
            double[] x = new double[dim];
            getMean(x);
            s.append("\nMean=");
            for(int i = 0; i < dim; i++)
            {
                if (i > 0)
                    s.append(", ");
                s.append(x[i]);
            }
            getMin(x);
            s.append("\nMin=");
            for(int i = 0; i < dim; i++)
            {
                if (i > 0)
                    s.append(", ");
                s.append(x[i]);
            }
            getMax(x);
            s.append("\nMax=");
            for(int i = 0; i < dim; i++)
            {
                if (i > 0)
                    s.append(", ");
                s.append(x[i]);
            }
        }
        if(n>1)
        {
            double[] x = new double[dim * dim];
            getVariance(x);
            s.append("\nvariance:");
            int offset = 0;
            for (int i = 0; i < dim; i++)
            {
                s.append("\n");
                for (int j = 0; j < dim; j++)
                {
                    if (j > 0)
                        s.append(", ");
                    s.append(x[offset++]);
                }
            }
            s.append("\ncorrelation:");
            offset = 0;
            for (int i = 0; i < dim; i++)
            {
                s.append("\n");
                double xx = x[i * dim + i];
                for (int j = 0; j < dim; j++)
                {
                    if (j > 0)
                        s.append(", ");
                    double yy = xx * x[j * dim + j];
                    if (yy > 0.0)
                        yy = 1.0 / Math.sqrt(yy);
                    s.append(x[offset++] * yy);
                }
            }
        }
        return s.toString();
    }

    public static void main(String[] s)
    {
        int dim = 5;
        MultiDeviant md = new MultiDeviant(dim);
        double[] sample = new double[dim];
        for (int i = 0; i < 4; i++)
        {
            for (int j = 0; j < dim; j++)
                sample[j] = i * j + 1.0e6;
            md.sample(sample);
        }
        System.out.println(md);
    }
}