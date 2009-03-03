package fr.inria.peerunit.tree.btree;


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
