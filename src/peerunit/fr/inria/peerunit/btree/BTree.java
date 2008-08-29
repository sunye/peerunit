package fr.inria.peerunit.btree;

import java.util.HashMap;
import java.util.Map;

import fr.inria.peerunit.util.TesterUtil;

public class BTree {
	int order;
	static BTreeNode root = null;
	public Map<Integer,BTreeNode> nodes=new HashMap<Integer,BTreeNode>();
	Integer nodeKey=0;

	public BTree(int o) {
		order = o;
		root = new BTreeNode();
	}

	public void insert(Comparable newKey) {
		BTreeNode returnNode = new BTreeNode();
		Comparable newRootKey = root.insert(newKey, returnNode);		
		if (newRootKey != null) {
			BTreeNode newRoot = new BTreeNode();
			newRoot.keys[0] = newRootKey;
			newRoot.children[0] = root;
			newRoot.children[1] = returnNode;
			root = newRoot;
		}
	}
		
	public  Map<Integer,BTreeNode> getNodes(){		
		return nodes;
	}
	
	public void buildTree(){
		for (Integer i =0; i< TesterUtil.getExpectedPeers();i++) {
			System.out.println("\nInserting: " + i);
			insert(i);			
		}
		nodes.put(0, root);	
		root.setRoot(true);
		returnNode(root);
	}
	
	public void returnNode(BTreeNode parent){
		parent.id=nodeKey;		
		System.out.println("BTreeNode: " + parent);
		for(BTreeNode bt:parent.children){			
			if(bt != null){				
				nodeKey++;
				bt.setParent(parent);
				nodes.put(nodeKey, bt);								
				returnNode(bt);								
			}else
				break;			
			
		}		
	}
	
	public BTreeNode getNode(Integer i){
		return nodes.get(i);
	}
	
	public static void main(String[] args) {
		BTree btree = new BTree(2);				
		btree.buildTree();

		for(int i=0;i<btree.getNodes().size();i++){
			System.out.println("Node "+i+" : "+btree.getNodes().get(i));							
			for(Comparable x:btree.getNodes().get(i).keys){
				if(x != null){
					System.out.println("Key "+new Integer(x.toString()));
				}
			}			
		}		
	}
}
