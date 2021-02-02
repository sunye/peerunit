package uk.co.demon.mcdowella.stats;

import java.util.Arrays;
import java.util.Random;

/** Static methods to permute rows. Have a good look at
 * java.util.Collections.shuffle() before you decide to use
 * these. Object to generate a permuted table, preserving
 * the marginal
 */

public class Permute
{
    /** One marker per item for each sort of item recorded in
     *  the table rows */
    private int[] left;
    /** One marker per item for each sort of item recorded in
     *  the table columns */
    private int[] right;
    /** Number of cells in table (rows * cols) */
    private int tabLen;
    /** Number of table columns */
    private int cols;
    Random rr;
    /** Permute the contents of an int array using a RNG. Have
     * a good look at java.util.Collections.shuffle() before
     * using this.
     * @param a the array to permute
     * @param r the RNG to use
     */
    public static void permute(int[] a, Random r)
    {
        for (int i = a.length - 1; i > 0; i--)
        {   // Choose a[i] randomly from a[0..i]
            // a[i+1..] is already random            
            int j = r.nextInt(i + 1);
            // System.err.println("i = " + i + " j = " + j + " length " +
            //    a.length);
            int t = a[j];
            a[j] = a[i];
            a[i] = t;
        }
    }
    /** Create a source of random tables with particular marginals
     * @param table All tables created will have the same marginals as
     * this one
     * @param rows The number of rows in our sample table
     * @param cols the number of cols in our sample table
     * @param r the Random number generator to use
     */
    public Permute(int[] table, int rows, int cols, Random r)
    {
        this.rr = r;
        this.cols = cols;
        int total = 0;
        tabLen = rows * cols;
        for(int i = 0; i < tabLen; i++)
            total += table[i];
        left = new int[total];
        right = new int[total];
        int wp = 0;
        int rp = 0;
        for (int i = 0; i < rows; i++)
        {
            for (int j = 0; j < cols; j++)
            {
                int count = table[rp++];
                for(int k = 0; k < count; k++)
                {
                    left[wp] = i;
                    right[wp] = j;
                    wp++;
                }
            }
        }
    }
    /** Create a new table by randomly permuting a list of
     *  items (generated according to the table passed to the
     * constructor of this object)
     * and creating a new table of left-right counts
     *  from it
     * @param table fill in the new counts here
     */
    public void newTab(int[] table)
    {
        Arrays.fill(table, 0, tabLen, 0);
        permute(right, rr);
        for (int i = 0; i < right.length; i++)
            table[left[i] * cols + right[i]]++; 
    }
}