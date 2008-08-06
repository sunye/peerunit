package fr.inria.peerunit.tree.btree;

import java.util.ArrayList;
import java.util.List;

public class BTree {
	private List<String> path=new ArrayList<String>();	
	private String parent;
	private String theRoot;
	private Comparable searchKey;
	private boolean isLeaf;
	private class BTreeNode {

		private Comparable[] keys;
		private BTreeNode[] children;		

		private BTreeNode() {
			keys =  new Comparable[2*order];
			children = new BTreeNode[2*order+1];
		}

		private BTreeNode(BTreeNode copy) {
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

		private boolean find(Comparable key) {
			searchKey=key;
			System.out.println(this);
			for (int i = 0; i < children.length; i++) {
				// BUGFIX #2 by Stefan Sprick
				// BUGFIX #3 Dietrich Schulten
				if (keys[0]==null) return false;
				if (i == keys.length || keys[i] == null) 
					return children[i].find(key);
				int cmp = keys[i].compareTo(key);
				if (cmp == 0) return true;
				else if (cmp > 0 && children[i] != null) 
					return children[i].find(key);
				else if (cmp > 0 && isLeaf())
					return false;
			}
			return false;
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

		public String toString() {
			String res = "[";
			for (int i = 0; i < keys.length; i++) {
				res = res + " " + keys[i] + " ";				
				if((keys[i]!=null)&&(!keys[i].toString().equalsIgnoreCase(searchKey.toString()))){					
					/* Look to the left */
					if( (Integer.valueOf(searchKey.toString()) < Integer.valueOf(theRoot))
							&&(Integer.valueOf(keys[i].toString())>  Integer.valueOf(searchKey.toString()))){
						parent=keys[i].toString();
					}else /* Look to the right */ 
						if((Integer.valueOf(searchKey.toString()) > Integer.valueOf(theRoot))
							&&(Integer.valueOf(keys[i].toString())<  Integer.valueOf(searchKey.toString()))){							
						parent=keys[i].toString();
					}
				}		
			}
			res += "] ";
			if(!path.contains(res))
				path.add(res);
			return res;
		}
		
		private String toString(int level) {
			String res = "";
			int i = 0;
			for (i = 0; i < level; i++) res += " ";
			res = res + this + "\n";
			for (i = 0; i < children.length; i ++){				
				if (children[i] != null){
					res += children[i].toString(level+1);				
				}
			}
			return res;
		}		
	}
	
	int order;
	static BTreeNode root = null;
	

	public BTree(int o) {
		order = o;
		root = new BTreeNode();
	}

	public boolean find(Comparable key) {		
		return root.find(key);
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
			if(theRoot==null)
				theRoot=newRootKey.toString();
		}
	}

	public String toString() {
		String res = "BTree:\n------\n";
		return (res + root.toString(0));
	}
	
	public BTreeElements getTreeElements(Integer i){		
		this.find(i);		
		System.out.println("Root is "+this.getTheRoot());
		System.out.println("Parent is "+this.getParent());
		return new BTreeElements(Integer.valueOf(parent),Integer.valueOf(theRoot));		
	}	
	
	public List<String> getPath(){
		return path;
	}
	
	public String getParent(){
		return parent;
	}
	
	public String getTheRoot(){		
		return theRoot;
	}
	
	public boolean isLeaf(){		
		return isLeaf;
	}
	
/*	public Integer getChildren(int possChildren,Integer actualKey){

		if(!isLeaf){
			int startKey=possChildren-actualKey.intValue();
			int endKey=possChildren+actualKey.intValue();
			
			if(startKey<0){
				startKey=0;
			}	
			
			if(endKey>7){
				endKey=7;
			}
			
			for(int iKey=startKey;iKey<endKey;iKey++){				
				this.find(Integer.valueOf(iKey));		
				System.out.println(iKey+" with parent "+this.getParent());
			}
			for(String s:this.getPath()){
				System.out.println("New printing");
				System.out.println(s);
			}
			return 1;
		}else
			return null;
	}*/
	
	/*public static void main(String[] args) {
		BTree btree = new BTree(2);
		//Integer[] inserts = {9,12,5,6,4,47,56,9,0,23,23,1,2,3,7,8,10,11,13,14};
		Integer[] inserts = {0,1,2,3,4,5,6,7};
		for (Integer i : inserts) {
			System.out.println("\nInsert: " + i);
			btree.insert(i);
			//System.out.println(btree);
		}
		/*Integer[] searches = {23,10,48};
		for (Integer i : searches) {
			System.out.println("\nSearching: " + i + " ---> " );
			System.out.println(btree.find(i));
		}
		
		
		Integer actualKey=7;
				
		System.out.println(btree.find(actualKey));
		System.out.println(btree);
		for(String s:btree.getPath()){
			System.out.println("Start printing");
			System.out.println(s);
		}
		
		System.out.println("Root is "+btree.getTheRoot());
		System.out.println("Parent is "+btree.getParent());
	}*/
}
