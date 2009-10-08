package fr.inria.peerunit.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Eduardo Almeida
 * @version 1.0
 * @since 1.0
 */
@Deprecated
public class BTreeImpl implements BTree { //implements TreeStrategy {

    private static BTreeNodeImpl root = null;
    private Map<Integer, BTreeNodeImpl> nodes = new HashMap<Integer, BTreeNodeImpl>();
    private Integer nodeKey = 0;

    /**
     * Constructs a new BinaryTree, and initializes it with a single BTreeNode as root
     * @param o
     */
    public BTreeImpl(int o) {
        root = new BTreeNodeImpl();
    }

    /**
     * Creates a new Node into this BTree, associated to the specified key
     * @param newKey the key to be inserted
     */
    @SuppressWarnings("unchecked")
	private void insert(Comparable newKey) {
        BTreeNodeImpl returnNode = new BTreeNodeImpl();
        Comparable newRootKey = root.insert(newKey, returnNode);
        if (newRootKey != null) {
            BTreeNodeImpl newRoot = new BTreeNodeImpl();
            newRoot.getKeys()[0] = newRootKey;
            newRoot.getChildren()[0] = root;
            newRoot.getChildren()[1] = returnNode;
            root = newRoot;

        }
    }

    /**
     * Retrieves all the nodes contained in this BTree
     * @return all the nodes contained in this BTree
     */
    private Map<Integer, BTreeNodeImpl> getNodes() {
        return nodes;
    }

    /**
     *  Builds the BTree, based on the number of expectedPeers (Defined by TesterUtil.getExpectedPeers)
     */
    public void buildTree() {
        for (Integer i = 0; i < TesterUtil.instance.getExpectedTesters(); i++) {
            System.out.println("\nInserting: " + i);
            insert(i);
        }
        nodes.put(0, root);
        root.setRoot(true);
        returnNode(root);
    }

    /**
     * Defines the BTree hierarchy : indicates recursively to each node who its parent is
     * @param parent
     */
    private void returnNode(BTreeNodeImpl parent) {
        BTreeNodeImpl btParent = (BTreeNodeImpl) parent;
        btParent.setId(nodeKey);
        System.out.println("BTreeNode: " + parent);
        for (BTreeNode bt : btParent.getChildren()) {
            if (bt != null) {
                nodeKey++;
                BTreeNodeImpl btChildren = (BTreeNodeImpl) bt;
                btChildren.setParent((BTreeNodeImpl) parent);
                nodes.put(nodeKey, btChildren);
                returnNode(btChildren);
            } else {
                break;
            }

        }
    }

    /**
     * Returns the node associated to the specified key
     * @param i the key whose associated value is to be returned
     * @return the node to which the specified key is associated
     */
    public BTreeNode getNode(Integer i) {
        return nodes.get(i);
    }

    /**
     * Test method for BTree
     * @param args
     */
    @SuppressWarnings("unchecked")
	public static void main(String[] args) {
        BTreeImpl btree = new BTreeImpl(2);
        btree.buildTree();

        for (int i = 0; i < btree.getNodes().size(); i++) {
            System.out.println("Node " + i + " : " + btree.getNodes().get(i));
            for (Comparable x : btree.getNodes().get(i).getKeys()) {
                if (x != null) {
                    System.out.println("Key " + new Integer(x.toString()));
                }
            }
        }
    }

    public int getNodesSize() {
        return nodes.size();
    }
}
