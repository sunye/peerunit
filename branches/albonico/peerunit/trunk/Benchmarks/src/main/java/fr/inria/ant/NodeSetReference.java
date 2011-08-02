/*
 * Created on 21 févr. 07
 *
 */
package fr.inria.ant;

import java.util.Set;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Ant.Reference;

public class NodeSetReference extends AbstractNodeReference implements INodeSet {

    private INodeSet reference;

    public NodeSetReference(INodeSet ns) {
        reference = ns;
    }

    public String getFrom() {
        return getReference().getFrom();
    }

    public void setFrom(String ip_from) {
        throw new RuntimeException("Should not Implement");
    }

    public String getTo() {
        return getReference().getTo();
    }

    public void setTo(String ip_to) {
        throw new RuntimeException("Should not Implement");
    }

    public Set<IAbstractNode> getNodes() {
        return getReference().getNodes();
    }

 /*   public Include createInclude() {
        return getReference().createInclude();
    } */

    public Task execute(ExecuteTask remote) {
        throw new RuntimeException("Should not Implement");
    }

    public void verify() {
        System.out.println("NodeSetReference::verify()");
        getReference().verify();
    }

    public void setRefid(final Reference ref) {
        throw new RuntimeException("Should not Implement");
    }

    public Task createDeploy(DeployTask deploy) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected INodeSet getReference() {
        return reference;
    }

    public void addNode(Node n) {
        throw new RuntimeException("Should not Implement"); 
    }
}
