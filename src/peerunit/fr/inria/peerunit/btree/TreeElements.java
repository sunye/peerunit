package fr.inria.peerunit.btree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TreeElements implements Serializable{	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Node parent;
	private List<Node> children=new ArrayList<Node>();
	private boolean isLeaf=false;	
		
	public void setParent(Node parent){
		this.parent=parent;
	}
	
	public void  setChildren(Node child){
		if(child!=null)
			children.add(child);
		else
			 isLeaf=true;	
	}	
	
	public Node getParent(){
		return parent;
	}
	
	public List<Node> getChildren(){
		return children;
	}
	
	public String toString(){
		return "Parent is: "+parent+" Children are: "+children;
	}
	
	public boolean isLeaf(){
		return isLeaf;
	}
}
