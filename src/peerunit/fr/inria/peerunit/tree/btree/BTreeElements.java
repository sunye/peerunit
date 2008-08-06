package fr.inria.peerunit.tree.btree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.inria.peerunit.tree.TreeTester;

public class BTreeElements {
	private Integer parent;
	private Integer root;	
	
	public BTreeElements(Integer parent, Integer root){
		this.parent=parent;		
		this.root=root;
	}	

	public Integer getParent(){
		return parent;
	}
	public Integer getRoot(){
		return root;
	}
	
}
