package fr.inria.peerunit.btree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
/**
 * 
 * @author Eduardo Almeida
 * @version 1.0
 * @since 1.0
 */
public class TreeElements implements Serializable{	
	
	private static final long serialVersionUID = 1L;
	private Node parent;
	private List<Node> children=new ArrayList<Node>();
	private boolean isLeaf=false;	
		
	/**
	 * specifies this element's parent
	 * @param parent the node to be set as this element's parent
	 */
	public void setParent(Node parent){
		this.parent=parent;
	}
	
	/**
	 * adds a new child to this element
	 * @param child the child node to be added to this element
	 */
	public void  setChildren(Node child){
		if(child!=null)
			children.add(child);
		else
			 isLeaf=true;	
	}	
	
	/**
	 * Returns this element's parent
	 * @return the element's parent
	 */
	public Node getParent(){
		return parent;
	}
	
	/**
	 * Returns this element's children
	 * @return the element's children
	 */
	public List<Node> getChildren(){
		return children;
	}
	
	@Override
	public String toString(){
		return "Parent is: "+parent+" Children are: "+children;
	}
	
	/**
	 * Determines is this element is a leaf
	 * @return true is this element is a leaf
	 */
	public boolean isLeaf(){
		return isLeaf;
	}
}
