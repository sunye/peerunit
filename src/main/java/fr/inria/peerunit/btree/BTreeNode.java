package fr.inria.peerunit.btree;

import java.io.Serializable;

import fr.inria.peerunit.btreeStrategy.AbstractBTreeNode;
import fr.inria.peerunit.util.TesterUtil;

/**
 * 
 * @author Eduardo Almeida
 * @version 1.0
 * @since 1.0
 */
public class BTreeNode extends AbstractBTreeNode implements Serializable{
	private static final long serialVersionUID = 1L;
	private int id;
	private Comparable[] keys;
	private BTreeNode[] children;
	private BTreeNode parent;		
	private int order=TesterUtil.instance.getTreeOrder();
	private boolean isLeaf;
	private boolean isRoot=false;
	
	/**
	 * Constructs a new BTreeNode
	 */
	public BTreeNode() {		
		keys =  new Comparable[2*order];
		children = new BTreeNode[2*order+1];
	}

	/**
	 * Constructs a copy of the specified BTreeNode
	 * @param copy the BTreeNode to be copied
	 */
	public BTreeNode(BTreeNode copy) {
		keys = copy.keys.clone();
		children = copy.children.clone();
	}

	private void setKeys(Comparable[] k) {
		keys = k;
	}

	private void setChildren(BTreeNode[] c) {
		children = c;
	}
	
	/**
	 * Specifies which node is this node's parent
	 * @param parent the parent BTreeNode for this node
	 */
	public void setParent(BTreeNode parent){
		this.parent=parent;
	}

	/**
	 * Determines if this node is a leaf
	 * @return true if this node is a leaf (ie. doesn't have any child)
	 */
	public boolean isLeaf() {
		if(children[0] == null)
			isLeaf=true;
		else
			isLeaf=false;

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
	public void setRoot(boolean isRoot){
		this.isRoot=isRoot;
	}

	/**
	 * inserts a new Node with the associated key
	 * @param newKey the key to be added
	 * @param returnNode the node associated to the added key
	 * @return the new root's key
	 */
	Comparable insert(Comparable newKey,BTreeNode returnNode) {
		// Einfügeknoten suchen (rekursiv)
		if (!isLeaf()) {
			for (int i = 0; i < children.length; i++) {
				if (i == keys.length || keys[i] == null){
					newKey = children[i].insert(newKey,returnNode);
					break;
				}	
				int cmp = keys[i].compareTo(newKey);
				if (cmp == 0) return null;
				else if (cmp > 0) {
					newKey = children[i].insert(newKey,returnNode);
					break;
				}
			}
		}
		// Einfügeposition in Knoten suchen
		if (newKey != null) {
			int i=0;
			while (i < keys.length 
					&& keys[i] != null
					&& keys[i].compareTo(newKey) < 0) {
				i++;
			}
			
			if (i < keys.length 
					&& keys[i] != null 
					&& keys[i].compareTo(newKey) == 0) {
				return null;
			}
			
			Comparable tmpKey = null;
			BTreeNode tmpNode = null;
			BTreeNode newNode = new BTreeNode(returnNode);
			// Einfügen und Nachfolger weiterrücken
			while (i < keys.length) {
				tmpKey = keys[i];
				tmpNode = children[i+1];
				keys[i] = newKey;
				children[i+1] = (newNode == null || isLeaf()) ?
						null : newNode;
				newKey = tmpKey;
				newNode = tmpNode;
				i++;
			}
			
			// Überlaufbehandlung  
			if (newKey != null) {
				// Neuen Knoten erzeugen, Schluessel
				// kopieren und aus altem Knoten loeschen
				tmpNode = new BTreeNode();
				for (i = 0; i < order-1; i++) {
					tmpNode.keys[i] = keys[order+i+1];
					tmpNode.children[i] = children[order+i+1];
					keys[order+i+1] = null;
					children[order+i+1] = null;
				}
				// BUGFIX
				// RAUS: tmpNode.children[order-1] = children[order+i];
				tmpNode.children[order-1] = children[2*order]; // REIN
				children[2*order] = null; // REIN
				tmpNode.keys[order-1] = newKey;
				tmpNode.children[order] = (newNode == null || isLeaf()) ?
						null :
							newNode;
				
				// mittlerer Schluessel als Rueckgabewert
				//middle key as a return value
				newKey = keys[order];
				keys[order] = null;
				children[order+1] = null;
				
				// Werte des Rueckgabeknoten setzen
				// Values of the return knot place
				returnNode.setKeys(tmpNode.keys.clone());
				returnNode.setChildren(tmpNode.children.clone());
				
			}
		}
		return newKey;
	}

	@Override
	public AbstractBTreeNode[] getChildren() {
		return children;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public Comparable[] getKeys() {
		return keys;
	}

	@Override
	public AbstractBTreeNode getParent() {
		return parent;
	}

	public void setId(int id) {
		this.id = id;
	}
	
}

