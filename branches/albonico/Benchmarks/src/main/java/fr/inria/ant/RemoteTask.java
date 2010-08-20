/*
 * Created on 4 aožt 06
 *
 */
package fr.inria.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public abstract class RemoteTask extends Task {
    
    private NodeSet nodeSet;
    private Node node;

    public RemoteTask() {
        super();
    }

    public IAbstractNode getNode() {
        return this.node;
    }
    
    public void setNodeSet(NodeSet ns) {
    	this.nodeSet = ns;
    }

    public IAbstractNode getNodeSet() {
    	return this.nodeSet;
    }

     public IAbstractNode createNodeSet() {
    	NodeSet ns = new NodeSet();
    	nodeSet = ns;
    	return ns;
    }

    public IAbstractNode createNode() {
    	Node m = new Node();
    	node = m;
    	return m;
    }
    
    public void verify() {
        if(node == null && nodeSet == null){
            throw new BuildException("You must specify at least one node or node set.");
        }
        
        if(node != null && nodeSet != null){
            throw new BuildException("You can specify either a Node or a Node Set, " +
                    "but not both.");
        }

    }


    
}