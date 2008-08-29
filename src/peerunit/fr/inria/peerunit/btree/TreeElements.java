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
		
	public void setParent(Node parent){
		this.parent=parent;
	}
	
	public void  setChildren(Node child){
		children.add(child);
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
}
