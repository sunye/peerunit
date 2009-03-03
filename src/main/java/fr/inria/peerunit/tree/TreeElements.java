package fr.inria.peerunit.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tree Element container used for the build to tree.
 * Contains the root, parent and children elements.
 * @author Eduardo
 * 
 */
public class TreeElements implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * tester parent
	 */
	private TreeTester parent;
	
	/**
	 * tester root
	 */
	private TreeTester root;	
	
	/**
	 * tester children list of the tester parent 
	 */
	private List<TreeTester> children = Collections.synchronizedList(new ArrayList<TreeTester>(2));
	
	/**
	 * Constructor with parent, root, list of children
	 * @param parent TreeTester parent
	 * @param root	TreeTester root
	 * @param children	List of TreeTester children
	 */
	public TreeElements(TreeTester parent, TreeTester root,  List<TreeTester> children){
		this.parent=parent;
		this.children=children;
		this.root=root;
	}	
	
	/**
	 * Constructor without children
	 * @param parent	TreeTester parent
	 * @param root	TreeTester root
	 */
	public TreeElements(TreeTester parent, TreeTester root){
		this.parent=parent;		
		this.root=root;
	}	
	
	/**
	 * Constructor by default
	 */
	public TreeElements(){
		
	}	
	
	/**
	 * return the parent tester of the tree
	 * @return TreeTester
	 */
	public TreeTester getParent(){
		return parent;
	}
	
	/**
	 * return a root tester of the tree
	 * @return TreeTester
	 */
	public TreeTester getRoot(){
		return root;
	}
	
	/**
	 * return the children tester list. This list contains some objects TreeTester.
	 */
	public List<TreeTester> getChildren(){
		return children;
	}
	
	/**
	 * Add a children tester inside the children list following his ID. 
	 */
	public void add(TreeTester t,int id){
		if(!children.contains(t)){
			children.add(t);		
		}
		System.out.println(id+" added child: "+t);		
	}
	
	/**
	 * return a children tester list. This list contains some objects TreeTester.
	 */
	public  List<TreeTester>  get(){
		return children;
	}
	
	/**
	 * Test if the children tester list is empty.
	 */
	public boolean isEmpty(){
		return children.isEmpty();
	}	
	
	/**
	 * Define the parent tester
	 */
	public void setParent(TreeTester parent){
		this.parent=parent;
	}
	
	/**
	 * Delete the children tester of the children list
	 */
	public void cleanTrace(TreeTester t){
		if(children.contains(t)){
			children.remove(t);		
		}
	}
}
