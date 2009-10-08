package fr.inria.peerunit.util;

import java.io.Serializable;

/**
 * 
 * @author Eduardo Almeida
 * @version 1.0
 * @since 1.0
 */
@Deprecated
class BTreeNodeImpl implements BTreeNode, Serializable {

    private static final long serialVersionUID = 1L;
    private int id;
    @SuppressWarnings("unchecked")
	private Comparable[] keys;
    private BTreeNodeImpl[] children;
    private BTreeNodeImpl parent;
    private int order;
    private boolean isLeaf;
    private boolean isRoot = false;

    /**
     * Constructs a new BTreeNode
     */
    public BTreeNodeImpl() {
        this(TesterUtil.instance.getTreeOrder());
    }

    public BTreeNodeImpl(int i) {
        order = i;
        keys = new Comparable[2 * order];
        children = new BTreeNodeImpl[2 * order + 1];
    }

    /**
     * Constructs a copy of the specified BTreeNode
     * @param copy the BTreeNode to be copied
     */
    public BTreeNodeImpl(BTreeNodeImpl copy) {
        keys = copy.keys.clone();
        children = copy.children.clone();
    }

    @SuppressWarnings("unchecked")
	private void setKeys(Comparable[] k) {
        keys = k;
    }

    private void setChildren(BTreeNodeImpl[] c) {
        children = c;
    }

    /**
     * Specifies which node is this node's parent
     * @param parent the parent BTreeNode for this node
     */
    public void setParent(BTreeNodeImpl parent) {
        this.parent = parent;
    }

    /**
     * Determines if this node is a leaf
     * @return true if this node is a leaf (ie. doesn't have any child)
     */
    public boolean isLeaf() {
        if (children[0] == null) {
            isLeaf = true;
        } else {
            isLeaf = false;
        }

        return isLeaf;
    }

    /**
     * Determines if this node is the tree's root
     * @return true if this node is the BTree' root
     */
    public boolean isRoot() {
        return isRoot;
    }

    /**
     * Indicates that this node is the tree's root
     * @param isRoot
     */
    public void setRoot(boolean isRoot) {
        this.isRoot = isRoot;
    }

    /**
     * inserts a new Node with the associated key
     * @param newKey the key to be added
     * @param returnNode the node associated to the added key
     * @return the new root's key
     */
    @SuppressWarnings("unchecked")
	Comparable insert(Comparable newKey, BTreeNodeImpl returnNode) {
        // Search and insert node (recursive)
        if (!isLeaf()) {
            for (int i = 0; i < children.length; i++) {
                if (i == keys.length || keys[i] == null) {
                    newKey = children[i].insert(newKey, returnNode);
                    break;
                }
                int cmp = keys[i].compareTo(newKey);
                if (cmp == 0) {
                    // key already exists
                    return null;
                } else if (cmp > 0) {
                    newKey = children[i].insert(newKey, returnNode);
                    break;
                }
            }
        }


        // Einfügeposition in Knoten suchen
        if (newKey != null) {
            int i = 0;
            while (i < keys.length && keys[i] != null && keys[i].compareTo(newKey) < 0) {
                i++;
            }

            if (i < keys.length && keys[i] != null && keys[i].compareTo(newKey) == 0) {
                
                return null;
            }

            Comparable tmpKey = null;
            BTreeNodeImpl tmpNode = null;
            BTreeNodeImpl newNode = new BTreeNodeImpl(returnNode);
            // Einfügen und Nachfolger weiterrücken
            while (i < keys.length) {
                tmpKey = keys[i];
                tmpNode = children[i + 1];
                keys[i] = newKey;
                children[i + 1] = (newNode == null || isLeaf()) ? null : newNode;
                newKey = tmpKey;
                newNode = tmpNode;
                i++;
            }

            // Überlaufbehandlung
            if (newKey != null) {
                // New nodes generate keys
                // copy and delete nodes from old
                tmpNode = new BTreeNodeImpl();
                for (i = 0; i < order - 1; i++) {
                    tmpNode.keys[i] = keys[order + i + 1];
                    tmpNode.children[i] = children[order + i + 1];
                    keys[order + i + 1] = null;
                    children[order + i + 1] = null;
                }
                // BUGFIX
                // RAUS: tmpNode.children[order-1] = children[order+i];
                tmpNode.children[order - 1] = children[2 * order]; // REIN
                children[2 * order] = null; // REIN
                tmpNode.keys[order - 1] = newKey;
                tmpNode.children[order] = (newNode == null || isLeaf()) ? null : newNode;

                // mittlerer Schluessel als Rueckgabewert
                //middle key as a return value
                newKey = keys[order];
                keys[order] = null;
                children[order + 1] = null;

                // Werte des Rueckgabeknoten setzen
                // Values of the return knot place
                returnNode.setKeys(tmpNode.keys.clone());
                returnNode.setChildren(tmpNode.children.clone());

            }
        }
        return newKey;
    }


    public BTreeNode[] getChildren() {
        return children;
    }

    public int getId() {
        return id;
    }

    @SuppressWarnings("unchecked")
	public Comparable[] getKeys() {
        return keys;
    }

    public BTreeNode getParent() {
        return parent;
    }

    public void setId(int id) {
        this.id = id;
    }
}

