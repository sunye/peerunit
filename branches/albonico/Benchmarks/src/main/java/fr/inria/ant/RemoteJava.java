/*
 * Created on 22 févr. 07
 *
 */
package fr.inria.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.CommandlineJava;

public class RemoteJava extends Java {
    
    private NodeSet nodeSet;
    private Node node;

    public RemoteJava() {
        super();
    }

    
    @Override
    public void execute() throws BuildException {
        this.setFork(true);
        CommandlineJava command = getCommandLine();
        Task task;
        IAbstractNode h = (node != null) ? node : nodeSet;
        h.verify();
        task = h.executeJava(command);
        task.execute();

    }
    
    public IAbstractNode createNodeSet() {
        nodeSet = new NodeSet();
        return nodeSet;
    }

    public IAbstractNode createNode() {
        node = new Node();
        return node;
    }
}
