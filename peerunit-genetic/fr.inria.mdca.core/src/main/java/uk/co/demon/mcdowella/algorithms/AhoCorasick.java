package uk.co.demon.mcdowella.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/** <p>
 *  This is an implementation of the Aho-Corasick Algorithm.
 *  We receive characters (in our case, integers) one by one, and make
 *  callbacks when we recognise that sequences of integers have just
 *  ended. The cost of receiving a character is (amortised) constant, plus
 *  the cost of triggering and servicing any callbacks produced. The cost
 *  does NOT depend on the number or size of sequences registered. The cost
 *  of building one of these receivers is linear in the total length of
 *  the sequences stored. Actually I play a bit fast and loose here, because
 *  I navigate round a tree choosing children by hash table lookup, where the
 *  size of the hash table is the number of possible children. So worst case
 *  behaviour could go up for very complex sets of patterns. I can still get away
 *  with this for fixed length alphabets, though, because the number of possible
 *  children is bounded by the number of characters in the alphabet.
 *  </p><p>
 *  To implement this we keep all of the sequences we know about organised
 *  as a tree. When we receive a new integer we trace our way along this
 *  tree, making callbacks as necessary. Extra links in the tree tell us
 *  how to react when the just recognised integer doesn't correspond to
 *  a branch from the current position. These point to where we would have
 *  got to if we followed along the tree starting from one place further
 *  along than our current path. If the current position records
 *  the fact that we have seen ???ABCD and we fail to match at E because
 *  no position in the tree matches ???ABCDE, then we jump to ???BCD and
 *  try to extend that match to ???BCDE. If THAT fails, we jump to ????CD
 *  and try to extend to ???CDE and so on. If there is no position in
 *  the tree to represent ???BCD the pointer we followed may actually take
 *  us straight to ???CD or even to the root ???.
 *  We also use links to recognise when the
 *  sequence we're currently tracing has just finished a subsequence that
 *  is registered for a callback.
 *  </p><p>
 *  "The Algorithm Design Manual", by Steven S. Skiena, mentions this and
 *  gives as references "Handbook of Theoretical Computer Science", Volume A
 *  (Ed. J. van Leeuwen), and A. Aho and M. Corasick. Efficient String
 *  Matching: an aid to bibliographic search. CACM 18:333-340, 1975.
 *  My knowledge of this, such as it is, comes largely from reading
 *  the source of sgrep, but this is an independent reimplementation,
 *  determined largely by my ideas of what makes for convenient Java code.
 *  The sgrep home page is at http://www.cs.helsinki.fi/~jjaakkol/sgrep.html
 *  The Aho-Corasick algorithm is also written up in section 
 *  11.2 of 'Algorithms and Theory
 *  of Computation Handbook', Edited by Mikhail J. Atallah, published by
 *  CRC press
 *  </p>
 */
public class AhoCorasick
{
    /** Root node in the tree */
    private Node root;
    /** Create the searcher
     * @param sequences the sequences of ints to look for
     * @param callbacks the callbacks to make when you recognise them
     * @exception IllegalArgumentException if sequences.length != callbacks.length
     */
    public AhoCorasick(int[][] sequences, Callback[] callbacks)
    {
        if (sequences.length != callbacks.length)
            throw new IllegalArgumentException("sequences.length = " +
                sequences.length + " != callbacks.length = " +
                callbacks.length);
        // build the tree, creating parent pointers in the otherCallbacks
        // field, and level pointers in fail 
        root = new Node(null, 0);
        // List of pointers to nodes at each level
        List levelPointers = new ArrayList();
        for(int i = 0; i < sequences.length; i++)
        {
           Node n = root;
           int[] s = sequences[i];
           ListIterator level = levelPointers.listIterator();
           for (int j = 0; j < s.length; j++)
           {
               root.key = s[j]; // just to look up things
               Node next = n.getChild(root);
               if (next == null)
               {
                   next = new Node(n, s[j]);
                   n.m.put(next, next);
                   if (j >= levelPointers.size())
                   {
                       levelPointers.add(next);
                   }
                   else
                   {
                       next.fail = (Node)level.next();
                       level.set(next);
                   }
               }
               else
               {
                   level.next();
               }
               n = next;               
           }
           if (n.callbacks == null)
               n.callbacks = new ArrayList();
           n.callbacks.add(callbacks[i]);
        }
        // Now traverse each level of the tree in turn, setting
        // up the fail and callback pointers. When we traverse one
        // level, we rely on the fact that the previous level has
        // already been set up
        root.fail = root;
        root.otherCallbacks = null;
        for (ListIterator i = levelPointers.listIterator(); i.hasNext();)
        {
            for (Node current = (Node)i.next(); current != null;)
            {
                Node parent = current.otherCallbacks;
                Node next = current.fail;
                // The Fail pointer tells us where to move to if the current
                // integer doesn't match: we move to the fail pointer and
                // try again with the same integer. This amounts to going
                // to where we would go if we had matched the sequence
                // starting one step on from the current sequence. So we can
                // work this out from our parent's fail pointer
                current.fail = root;
                // System.out.println("Current node " + System.identityHashCode(current));
                // System.out.println("Parent " + System.identityHashCode(parent));
                if (parent != root)
                {
                    for (Node myFail = parent.fail;;)
                    {                  
                        Node tryThis = myFail.getChild(current);
                        if (tryThis != null)
                        {
                            current.fail = tryThis;
                            break;
                        }
                        if (myFail == root)
                            break;
                        myFail = myFail.fail;
                    }
                }
//              System.out.println("Fail to " + System.identityHashCode(current.fail));
                current.otherCallbacks = current.fail;
                if (current.otherCallbacks.callbacks == null)
                    current.otherCallbacks = current.otherCallbacks.otherCallbacks;
/*
                if (current.otherCallbacks != null)
                   System.out.println("Call to " +
                       System.identityHashCode(current.fail));
*/
                current = next;
            }
        }
        reset();
    }    
    /** The current position in the tree built from the
     *  sequences
     */
    private Node state;
    /** Reset the current state, as if starting anew. Make any callbacks registered
     *  on zero-length sequences, recognising the start of the sequence we are
     *  trying to find patterns in.
     */
    public void reset()
    {
        state = root;
        if (state.callbacks != null)
        {
            for(Iterator i = state.callbacks.iterator(); i.hasNext();)
            {
                Callback c = (Callback)i.next();
                c.callback();
            }
        }
    }
    /** Accept an integer and make all necessary callbacks */
    public void accept(int value)
    {
        root.key = value; // just for lookup
        for(;;)
        {
            // This loop (and other similar loops) looks as
            // if it might be expensive. In fact it has only
            // O(1) amortised cost. Each full iteration brings us
            // one level up the tree. We move down the tree at
            // most once per character. So on average we
            // do no more than one iteration per character.
            Node n = state.getChild(root);
            if (n != null)
            {
                state = n;
                break;
            }
            if (state == root)
                break;
            state = state.fail;
        }
        for (Node n = state; n != null;)
        {
            if(n.callbacks != null)
            {
                for(Iterator i = n.callbacks.iterator(); i.hasNext();)
                {
                    Callback c = (Callback)i.next();
                    c.callback();
                }
            }
            n = n.otherCallbacks;
       }
    }
    /** A helper method to turn a string into a sequence of
     *  ints that can be passed to our constructor
     *  @param s the String to turn into a sequence
     *  @return a sequence of ints containing the characters
     *  in the string
     */
    public static int[] toSequence(String s)
    {
        int[] seq = new int[s.length()];
        for (int i = 0; i < seq.length; i++)
            seq[i] = s.charAt(i);
        return seq;
    }
}

/** Use Node as its own Hashmap Key. This is tricky, because
 *  its hashCode is then determined only by the integer stored
 *  in it - be careful!
 */
class Node
{
    int key;

    /** This only makes sense when you use a Node as a Key */
    public int hashCode() {return key;}
    /** This only makes sense when you use a Node as a Key */
    public boolean equals(Object o)
    {
        if (!(o instanceof Node))
            return false;
        Node ko = (Node)o;
        return key == ko.key;
    }

    /** branches from this node */
    Map m; 
    /** Branch here if no other choice */
    Node fail;
    /** callbacks to make */
    List callbacks;
    /** to get to callbacks valid from higher nodes */
    Node otherCallbacks;
    /** Create a new node, but use otherCallbacks as a parent pointer
     * @param the Node's parent
     */
    public Node(Node parent, int key)
    {
//        System.out.println("New node " + System.identityHashCode(this) + 
//           " for key " + (char)key);
        m = new HashMap();
        this.key = key;
        callbacks = null;
        otherCallbacks = parent; 
/*
        if (parent != null)
            System.out.println("Parent is " + System.identityHashCode(parent));     
*/
    }
    public Node getChild(Node key)
    {
        return (Node)m.get(key);
    }
}