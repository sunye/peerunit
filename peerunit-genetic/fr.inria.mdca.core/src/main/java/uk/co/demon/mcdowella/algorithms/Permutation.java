package uk.co.demon.mcdowella.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.EOFException;
import java.util.HashSet;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.io.Reader;
import java.util.Set;

/** This class defines permutations, represented by an array
 *  of numbers 0..n-1. Most of the point of this is that we can
 *  check that our Permutation objects really define a permutation
 *  in the constructor and know they're sane from then on.
 */
public class Permutation
{
    /** Values mapped to */
    private int[] perm;
    /** return number of values mapped */
    public int getN() {return perm.length;}
    /** default Permutation value is the identity
     *  @param n construct the indentity permutation on n elements
     */
    public Permutation(int n)
    {
        perm = new int[n];
        for (int i = 0; i < n; i++)
            perm[i] = i;
    }
    /** Construct from an array 0..n-1 
     *  @param values the values to be assumed by 0,1,2,.. n-1 after
     *  permutation
     */
    public Permutation(int[] values)
    {
        perm = new int[values.length];
        Arrays.fill(perm, -1);
        for (int i = 0; i < values.length; i++)
        {   // check
            int x = values[i];
            if (x < 0 || x >= perm.length || perm[x] != -1)
                throw new IllegalArgumentException("Array not a permutation");
            perm[x] = 1;
        }
        System.arraycopy(values, 0, perm, 0, values.length);
    }
    /** Create from sample. Note that I could have defined the clone()
     *  method instead. I do not do this mostly because I don't want to
     *  spend time considering the implications of that. Most of the difference
     *  between clone() and this copy constructor is that clone() would return
     *  a subclass if called on a subclass, whereas a copy constructor always
     *  returns a member of the class specified at compile time. 
     *  I don't expect
     *  anybody to inherit from this class, though, so I don't think there's
     *  any big deal here. You could also take clone() to suggest that this might
     *  be just a shallow copy, which I definately don't want here, as I have
     *  modifying methods here for efficiency that we need to keep an eye on.
     *  I can guarantee
     *  that nothing is shallow copied by making this a copy constructor, not a
     *  clone() - which might later be overridden.
     *  @param p a Permutation to create a copy of
     */
    public Permutation(Permutation p)
    {
        perm = new int[p.perm.length];   
        System.arraycopy(p.perm, 0, perm, 0, p.perm.length);
    }
    /** set to inverse of argument 
     *  @param p a permutation you want this to be the inverse of
     */
    public void setInverse(Permutation p)
    {
        if (perm.length != p.perm.length)
            throw new IllegalArgumentException("permutation size mismatch");
        for (int i = 0; i < perm.length; i++)
            perm[p.perm[i]] = i; // works even for p.setInverse(p)
    }
    /** make INITIALLY INVALID permutation - so this must be private
     *  so we know we only release valid permutations to the user
     */
    private Permutation()
    {
    }
    /** Provide the inverse of a permutation. Why is this static? this is
     *  the same argument as for our copy constructor vs clone.
     * @param p a permutation to create the inverse of
     * @return the inverse of p
     */
    public static Permutation makeInverse(Permutation p)
    {
        Permutation pp = new Permutation();
        pp.perm = new int[p.perm.length];
        for (int i = 0; i < pp.perm.length; i++)
            pp.perm[p.perm[i]] = i;
        return pp;
    }
    /** Provide the product of two permutations. That is f(x)=p(q(x))
     *  @param p permutation to multiply
     *  @param q permutation to multiply with
     *  @return the product of p and q. That is f(x)=p(q(x))
     */
    public static Permutation makeProduct(Permutation p, Permutation q)
    {
        if (p.perm.length != q.perm.length)
            throw new IllegalArgumentException("permutation size mismatch");
        Permutation pp = new Permutation();
        pp.perm = new int[p.perm.length];
        for (int i = 0; i < pp.perm.length; i++)
            pp.perm[i] = p.perm[q.perm[i]];
        return pp;
    }
    /** set one permutation from another of the same size
     * @param p the permutation to set this one to
     */
    public void setPerm(Permutation p)
    {
        if (perm.length != p.perm.length)
            throw new IllegalArgumentException("permutation size mismatch");
        for (int i = 0; i < perm.length; i++)
            perm[i] = p.perm[i];
    }
    /** multiply in place, producing p * this. No aliasing allowed!
     * @param p a permutation to preMultiply this one by
     */
    public void preMultiply(Permutation p)
    {
        if (perm.length != p.perm.length)
            throw new IllegalArgumentException("permutation size mismatch");
        if (p == this)
            throw new IllegalArgumentException("aliasing");
        for (int i = 0; i < perm.length; i++)
            perm[i] = p.perm[perm[i]]; // NOT safe for aliasing
    }
    /** multiply out of place, setting this to p * q. No aliasing allowed between
     * this and p. All permutations must be on the same number of elements.
     * @param p permutation to multiply. Cannot be this.
     * @param q permutation to multiply.
     */
    public void multiply(Permutation p, Permutation q)
    {
        if (perm.length != p.perm.length || perm.length != q.perm.length)
            throw new IllegalArgumentException("permutation size mismatch");
        if (p == this)
            throw new IllegalArgumentException("aliasing");
        for (int i = 0; i < perm.length; i++)
            perm[i] = p.perm[q.perm[i]]; // Aliasing safe for q only
    }
    /** apply permutation to an element
     *  @param x the element to be permuted
     *  @return the result of permuting it
     */
    public int apply(int x) {return perm[x];}
    /** return the lowest element >= i not mapped to itself, or n if
     * none found - identity
     * @param i the first element to consider returning, of not unchanged
     * @return the index of the first element changed or n of no such
     */
    public int changedFrom(int i)
    {
        for (; i < perm.length; i++)
            if (perm[i] != i)
                return i;
        return i;
    }
    /** Turn the permutation into a human (and computer) readable string
     *  @return something legible
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("[ ");
        for (int i = 0; i < perm.length - 1; i++)
        {
            sb.append(perm[i]);
            sb.append(", ");
        }
        if (perm.length > 0)
            sb.append(perm[perm.length - 1]);
        sb.append("]");
        return sb.toString();
    }
    /** Returns permutation read from stream, null for graceful EOF,
     * or throws IO exception or NumberFormatException
     * (including EOFException for unexpected EOF).
     * Example permutation is identity on 4 elements: [0, 1, 2, 3]
     * @param r the Reader to read from
     * @return a permutation read in or null if graceful EOF 
     * @exception NumberFormatException if misformatted input (can happen if passed
     *  hugely too long string of digits as a number).
     * @exception IOException if encountered, or EOFException of EOF after apparent
     * start of permutation, or misformat caught by this code, not Integer.parseInt
     */
    public static Permutation readPermutation(Reader r) throws IOException,
        NumberFormatException
    {
        for(;;)
        {   // Scan for '['
            int x = r.read();
            if (x == -1)
                return null;
            if (Character.isWhitespace((char)x))
                continue;
            if (x == '[')
                break;
            throw new IOException("IO misformat looking for [");
        }
        List l = new ArrayList();
        Set seen = new HashSet(); // Use to detect duplicate elements ASAP
        int x;
        StringBuffer sb = new StringBuffer();
        numberLoop:for(;;)
        {
            for(;;)
            {   // look for number
                x = r.read();
                if (x == -1)
                    throw new EOFException("EOF inside permutation");
                if (Character.isWhitespace((char)x))
                    continue;
                if (Character.isDigit((char)x))
                    break;
                if (x == ']')
                {
                    if (l.size() == 0)
                        break numberLoop; // Else expect ] when looking for comma
                }
                throw new IOException("Found " + (char) x +
                   " looking for number");
            } // here with number
            sb.setLength(0);
            sb.append((char)x);
            for(;;)
            {
                x = r.read();
                if (x == -1)
                    throw new EOFException("EOF inside permutation");
                if (Character.isDigit((char)x))
                    sb.append((char)x);
                else
                    break;
            }
            Integer got = new Integer(sb.toString());
            if (got.intValue() < 0)
                throw new IOException(got + " occurs in permutation");
            if (!seen.add(got))
                throw new IOException(got + " occurs twice in permutation");
            l.add(got);
            for(;;)
            {
                if (x == ']')
                    break numberLoop;
                if (x == ',')
                    break;
                if (!Character.isWhitespace((char)x))
                    throw new IOException("got " + (char) x +
                        " looking for separator");
                x = r.read();
                if (x == -1)
                    throw new EOFException("EOF inside permutation");
            }
        }
        int[] p = new int[l.size()];
        int wp = 0;
        for (Iterator i = l.iterator(); i.hasNext();)
        {
            int v = ((Integer)i.next()).intValue();
            if (v >= p.length)
                throw new IOException("Gap in permutation");
            // System.err.println("Set " + wp + " to " + v);
            p[wp++] = v;
        }
        return new Permutation(p);
    }
    /** Set to a random permutation
     * @param r the Random number generator to use
     */
    public void setRandom(Random r)
    {
        int j;
        for (int i = perm.length; i > 1; i = j)
        {   // set rightmost unset slot to random choice of all unchosen
            // values (ignore final unchosen slot, which is forced)
            int rv = r.nextInt(i);
            j = i - 1;
            int x = perm[j];
            perm[j] = perm[rv];
            perm[rv] = x;
        }
    }
}
