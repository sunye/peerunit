package fr.inria.peerunit.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Node {
	
	private List<TreeTester> children = Collections.synchronizedList(new ArrayList<TreeTester>(2));
	
	public void insert(TreeTester t){
		children.add(t);
	}
	
	public void remove(TreeTester t){
		children.remove(t);
	}
	
	public void send(Message m){
		for(TreeTester  t: children)
			t.send(m);
	}
	
	public void receive(Message m){
		for(TreeTester  t: children)
			t.receive(m);
	}
}
