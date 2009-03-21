package fr.inria.peerunit.btree;

import java.util.HashMap;
import java.util.Map;

import fr.inria.peerunit.btreeStrategy.AbstractBTreeNode;
import fr.inria.peerunit.util.TesterUtil;

/**
 * 
 * @author Eduardo Almeida
 * @version 1.0
 * @since 1.0
 */
public class BTree { //implements TreeStrategy {
	
	int order;
	static BTreeNode root = null;
	public Map<Integer,BTreeNode> nodes = new HashMap<Integer,BTreeNode>();
	Integer nodeKey=0;

	/**
	 * Constructs a new BinaryTree, and initializes it with a single BTreeNode as root
	 * @param o
	 */
	public BTree(int o) {
		order = o;
		root = new BTreeNode();
	}

	/**
	 * Creates a new Node into this BTree, associated to the specified key
	 * @param newKey the key to be inserted
	 */
	private void insert(Comparable newKey) {
		BTreeNode returnNode = new BTreeNode();
		Comparable newRootKey = root.insert(newKey, returnNode);
		if (newRootKey != null) {
			BTreeNode newRoot = new BTreeNode();
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
	private  Map<Integer,BTreeNode> getNodes(){
		return nodes;
	}
	
	/**
	 *  Builds the BTree, based on the number of expectedPeers (Defined by TesterUtil.getExpectedPeers)
	 */
	public void buildTree(){
		for (Integer i =0; i< TesterUtil.getExpectedPeers();i++) {
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
	private void returnNode(BTreeNode parent){
		BTreeNode btParent = (BTreeNode)parent;
		btParent.setId(nodeKey);
		System.out.println("BTreeNode: " + parent);
		for (AbstractBTreeNode bt:btParent.getChildren()){
			if (bt != null){
				nodeKey++;
				BTreeNode btChildren = (BTreeNode)bt;
				btChildren.setParent((BTreeNode)parent);
				nodes.put(nodeKey, btChildren);
				returnNode(btChildren);
			} else
				break;
			
		}
	}
	
	/**
	 * Returns the node associated to the specified key
	 * @param i the key whose associated value is to be returned 
	 * @return the node to which the specified key is associated
	 */
	public AbstractBTreeNode getNode(Integer i){
		return nodes.get(i);
	}
	
	/**
	 * Test method for BTree
	 * @param args
	 */
	public static void main(String[] args) {
		BTree btree = new BTree(2);				
		btree.buildTree();

		for(int i=0;i<btree.getNodes().size();i++){
			System.out.println("Node "+i+" : "+btree.getNodes().get(i));							
			for(Comparable x:btree.getNodes().get(i).getKeys()){
				if(x != null){
					System.out.println("Key "+new Integer(x.toString()));
				}
			}			
		}		
	}

	public int getNodesSize() {
		return nodes.size();
	}
}
