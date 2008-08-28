package fr.inria.peerunit.btree;

import java.util.ArrayList;
import java.util.List;

public class TreeElements {	
	
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
}
