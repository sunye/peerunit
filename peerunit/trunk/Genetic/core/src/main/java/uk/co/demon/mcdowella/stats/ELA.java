package uk.co.demon.mcdowella.stats;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.io.Writer;

/** This class represents the result of clustering via
 *  Elementary Linkage Analysis */
public class ELA
{
    private int[] root;
    private int[] highCorr;
    private double[] corr;
    private int[] head;
    private int[] next;
    
    public int getRoot(int rootNo)
    {
        if (rootNo < root.length)
            return root[rootNo];
        return -1;
    }
    public int getHighCorr(int x)
    {
        return highCorr[x];
    }
    public double getHighCorrVal(int x)
    {
        return corr[x];
    }
    public int getFirstChild(int x)
    {
        return head[x];
    }
    public int getNextChild(int x)
    {
        return next[x];
    }
    public ELA(int n, double[] corrs)
    {
        if (n <= 1)
            throw new IllegalArgumentException("n = " + n + " <= 1");
        highCorr = new int[n];
        head = new int[n];
        next = new int[n];
        corr = new double[n];
        for (int i = 0; i < n; i++)
            head[i] = next[i] = -1;
        int rootCount = 0;
        for (int i = 0; i < n; i++)
        {   // Everything works out the element closest to it.
            // If A is closest to B and B is closest to A, we call
            // A, B roots.
            // 'closest to' forms a single pointer from each node. So we
            // have a forest of trees, possibly with rings at the top. Suppose
            // we look at a closest link in this ring: A -> B. If no other link
            // in this ring is as close, then B -> A and we have a pair of roots.
            // If B -> C, then this distance must be exactly the same as A -> B, so
            // any ring must have all links the same distance.
            // Let X be the left hand side of such a closest link with lowest number.
            // Then X -> Y, but also Y -> X because the distance is the same and X will
            // be scanned before any Z in Y -> Z. So we can only ever get trees topped
            // by roots.
            int closest = 0;
            if (i == 0)
                closest = 1;
            int off = i * n;
            double ac = Math.abs(corrs[off + closest]);
            int other = i;
            for (int j = 0; j < n; j++, other += n)
            {
                if (j == i)
                    continue;
                double here = corrs[off + j];
                if (here != corrs[other])
                    throw new IllegalArgumentException("Assymetric correlation matrix");
                double d = Math.abs(here);
                if (d > ac)
                {
                    ac = d;
                    closest = j;
                }
            }
            highCorr[i] = closest;
            corr[i] = corrs[off + closest];
            if (closest < i && highCorr[closest] == i)
                rootCount++;
            // For each element, keep track of its children: those things
            // nominating it as closest
            next[i] = head[closest];
            head[closest] = i; 
        }
        // Sort roots into decreasing order of distance
        root = new int[rootCount * 2];
        int wp = 0;
        for (int i = 0; i < n; i++)
        {
            int closest = highCorr[i];
            if (closest < i && highCorr[closest] == i)
            {
                root[wp++] = closest;
                root[wp++] = i;
            }
        }
        int[] vals = new int[n];
        double[] key = new double[n];
        wp = 0;
        for (int i = 0; i < root.length; i += 2)
        {
            vals[wp] = i;
            key[wp] = Math.abs(corr[root[i]]);
            wp++;
        }
        mysort(vals, key, wp);
        int[] vals2 = new int[n];
        wp = 0;
        for (int i = 0; i < root.length; i += 2)
        {
            int x = vals[wp++];
            vals2[i] = root[x];
            vals2[i + 1] = root[x + 1];
        }
        System.arraycopy(vals2, 0, root, 0, root.length);
        // Sort children ditto
        for (int i = 0; i < n; i++)
        {
            wp = 0;
            for (int k = head[i]; k >= 0; k = next[k])
            {
                vals[wp] = k;
                key[wp] = Math.abs(corr[k]);
                wp++;
            }
            mysort(vals, key, wp);
            head[i] = -1;
            for (int j = wp - 1; j >= 0; j--)
            {
                int x = vals[j];
                next[x] = head[i];
                head[i] = x;
            }
        }
    }
    /** heapsort into decreasing order by key. This is a bit silly:
     *  should really either use trivial insertion sort (already read
     *  n^2 numbers, or java library stuff
     */
    private void mysort(int[] vals, double[] key, int length)
    {
        for (int i = length >> 1; i >= 0; i--)
        {
            for(int j = i;;)
            {
                int p = 2 * j + 1;
                if (p >= length)
                    break;
                int q = p + 1;
                if (q < length && key[q] < key[p])
                    p = q;
                if (key[p] >= key[j])
                    break;
                int t = vals[j];
                vals[j] = vals[p];
                vals[p] = t;
                double d = key[j];
                key[j] = key[p];
                key[p] = d;
                j = p;
            }
        }
        for (int i = length - 1; i > 0; i--)
        {
            int t = vals[0];
            vals[0] = vals[i];
            vals[i] = t;
            double d = key[0];
            key[0] = key[i];
            key[i] = d;
            for(int j = 0;;)
            {
                int p = 2 * j + 1;
                if (p >= i)
                    break;
                int q = p + 1;
                if (q < i && key[q] < key[p])
                    p = q;
                if (key[p] >= key[j])
                    break;
                t = vals[j];
                vals[j] = vals[p];
                vals[p] = t;
                d = key[j];
                key[j] = key[p];
                key[p] = d;
                j = p;                
            }
        }
    }
    public void printTree(int node, PrintWriter w, int offset)
    {
        int root = getHighCorr(node);
        if (getHighCorr(root) != node)
            root = -1;
        for(int i = getFirstChild(node); i >= 0; i = getNextChild(i))
        {
            if (i == root) // i and node are roots
                continue;
            for (int j = 0; j < offset; j++)
                w.print(' ');
            w.println(node + ", " + i + " correlation " + getHighCorrVal(i));
            printTree(i, w, offset + 2);            
        }
    }
    public static void main(String[] s) throws IOException
    {
        List l = new ArrayList();
        StreamTokenizer st = new StreamTokenizer(new BufferedReader(
            new InputStreamReader(System.in)));
        for(;;)
        {
            int token = st.nextToken();
            if (token == StreamTokenizer.TT_EOF)
                break;
            if (token == StreamTokenizer.TT_NUMBER)
                l.add(new Double(st.nval));
            else if (token != StreamTokenizer.TT_EOL)
            {
                System.err.println("Could not handle token " + token + " " +
                    st.sval);
                System.exit(1);
            }
        }
        int x = l.size();
        int n = 0;
        for (; n * n < x; n++);
        if (n * n != x)
        {
            System.err.println("Only " + x + " numbers - not square!");
            System.exit(1);
        }
        if (x <= 1)
        {
            System.err.println("Need at least a 2x2 matrix of correlations");
            System.exit(1);
        } 
        double[] corrs = new double[x];
        int p = 0;
        for (Iterator it = l.iterator(); it.hasNext();)
            corrs[p++] = ((Double)it.next()).doubleValue();
        ELA result = new ELA(n, corrs);
        PrintWriter w = new PrintWriter(new BufferedWriter(
            new OutputStreamWriter(System.out)));
        for (int i = 0;;)
        {
            int r1 = result.getRoot(i++);
            if (r1 < 0)
                break;
            int r2 = result.getRoot(i++);
            if (result.getHighCorr(r1) != r2 ||
                result.getHighCorr(r2) != r1 ||
                result.getHighCorrVal(r1) != corrs[r1 * n + r2] ||
                result.getHighCorrVal(r2) != corrs[r2 * n + r1])
            {
                System.err.println("Trouble I");
                System.exit(1);
            }
            w.println("Roots " + r1 + ", " + r2 + " correlation " +
                result.getHighCorrVal(r2));
            result.printTree(r1, w, 2);
            result.printTree(r2, w, 2);
        }
        w.flush();
    }
}