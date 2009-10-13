package fr.inria.peerunit.util;

/**
 * The HTree is an hierarchical tree.
 *
 * @author sunye
 */
public class HTree<K,V> {

    private final int order;

    private HNodeImpl<K,V> head;

    private int size = 0;

    public HTree(int b) {
        order = b;
    }

    /**
     *
     * @return the head of this HTree. Returns null if empty.
     */
    public HNode<K,V> head() {
        return head;
    }

    /**
     * Inserts an element into the tree.
     * The first inserted element becomes the header.
     * @param e
     */
    public void put(K key, V value) {
        if (head == null) {
            head = new HNodeImpl<K,V>(key, value);
        } else {
            head.put(key, value);
        }
        size++;
    }

    /**
     * Checks if an element belongs to this Tree
     * @param e the element to check
     * @return true, if the tree contains the element.
     */
    public boolean containsKey(K key) {
        return (head == null) ? false : head.containsKey(key);
    }

    public int size() {
        return size;
    }

    public void clear() {
        head = null;
        size = 0;
    }

    class HNodeImpl<K,V> implements HNode<K,V> {
        private int position = 0;

        private K key;
        private V value;

        private HNodeImpl<K,V>[] children = new HNodeImpl[order];
        
        public HNodeImpl(K k, V v) {
            key = k;
            value = v;
        }

        public K key() {
            return key;
        }
        
        public V value() {
            return value;
        }

        /**
         * Returns an array containing the children of this node.
         * If this node is a leaf, an empty array will be returned.
         * @return
         */
        public HNode<K,V>[] children() {
            int i = 0;
            HNode<K,V>[] result;
            while(i < children.length && children[i] != null ) {
                i++;
            }
            result = new HNode[i];
            System.arraycopy(children, 0, result, 0, i);
            return result;
        }

        /**
         * Checks if this node is a leaf.
         * @return true, if this node has no child.
         */
        public boolean isLeaf() {
            return  children[0] == null;
        }

        /**
         * Inserts an element into this node.
         * When this node is full (no more null child), elements
         * are inserted in the children in a equilibrated way.
         * @param e
         */
        public void put(K k, V v) {
            if(children[position] == null) {
                children[position] = new HNodeImpl<K,V>(k,v);
            } else {
                children[position].put(k,v);
            }
            position = (position + 1) % order;
        }

        /**
         * Looks up for an element, returns true if found.
         * Uses a breadth-first search (BFS) algorithm.
         * @param e the searched element
         * @return true if found, false otherwise
         */
        public boolean containsKey(K k) {
            boolean result = false;
            if(k == key) {
                result = true;
            } else {
                for (int i = 0; i < children.length && ! result && children[i] != null; i++) {
                    result = children[i].containsKey(k);
                }
            }
            return result;
        }

        @Override
        public String toString() {
            return String.format("HNode<%s,%s>", key, value);
        }
    }

}
