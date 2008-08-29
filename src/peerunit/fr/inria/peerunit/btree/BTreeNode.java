package fr.inria.peerunit.btree;

import java.io.Serializable;

import fr.inria.peerunit.util.TesterUtil;


public class BTreeNode implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int id;
	public Comparable[] keys;
	public BTreeNode[] children;		
	private int order=TesterUtil.getTreeOrder();
	private boolean isLeaf;
	public BTreeNode() {		
		keys =  new Comparable[2*order];
		children = new BTreeNode[2*order+1];
	}

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

	private boolean isLeaf() {
		if(children[0] == null)
			isLeaf=true;
		else
			isLeaf=false;

		return isLeaf;
	}

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
					&& keys[i].compareTo(newKey) < 0) 
				i++;
			if (i < keys.length 
					&& keys[i] != null 
					&& keys[i].compareTo(newKey) == 0) 
				return null;
			Comparable tmpKey = null;
			BTreeNode tmpNode = null;
			BTreeNode newNode = new BTreeNode(returnNode);
			// Einfügen und Nachfolger weiterrücken
			while (i < keys.length) {
				tmpKey = keys[i];
				tmpNode = children[i+1];
				keys[i] = newKey;
				children[i+1] = (newNode == null || isLeaf()) ?
						null :
							newNode;
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
				newKey = keys[order];
				keys[order] = null;
				children[order+1] = null;
				// Werte des Rueckgabeknoten setzen
				returnNode.setKeys(tmpNode.keys.clone());
				returnNode.setChildren(tmpNode.children.clone());
			}
		}
		return newKey;
	}
}

