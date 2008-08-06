package fr.inria.peerunit.tree.btree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BTreeElements {
	private Integer parent;
	private Integer root;	
	private List<Integer> children = Collections.synchronizedList(new ArrayList<Integer>(2));
	
	public BTreeElements(Integer parent, Integer root,  List<Integer> children){
		this.parent=parent;
		this.children=children;
		this.root=root;
	}	
	
	public BTreeElements(Integer parent, Integer root){
		this.parent=parent;		
		this.root=root;
	}	
	
}
