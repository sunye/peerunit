package fr.inria.peerunit.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TreeElements implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private TreeTester parent;
	private TreeTester root;	
	private List<TreeTester> children = Collections.synchronizedList(new ArrayList<TreeTester>(2));
	
	public TreeElements(TreeTester parent, TreeTester root,  List<TreeTester> children){
		this.parent=parent;
		this.children=children;
		this.root=root;
	}	
	
	public TreeElements(TreeTester parent, TreeTester root){
		this.parent=parent;		
		this.root=root;
	}	
	
	public TreeElements(){
		
	}	
	
	public TreeTester getParent(){
		return parent;
	}
	public TreeTester getRoot(){
		return root;
	}
	public List<TreeTester> getChildren(){
		return children;
	}
	public void add(TreeTester t,int id){
		children.add(t);		
		System.out.println(id+" added child: "+t);		
	}
	public  List<TreeTester>  get(){
		return children;
	}
	public boolean isEmpty(){
		return children.isEmpty();
	}	
}
