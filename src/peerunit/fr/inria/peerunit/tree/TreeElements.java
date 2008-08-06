package fr.inria.peerunit.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TreeElements {
	private TreeTester parent;
	private TreeTester root;	
	private List<TreeTester> children = Collections.synchronizedList(new ArrayList<TreeTester>(2));
	
	public TreeElements(TreeTester parent, TreeTester root,  List<TreeTester> children){
		this.parent=parent;
		this.children=children;
		this.root=root;
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
}
