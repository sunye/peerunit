/*
 * Created on 21 févr. 07
 *
 */
package fr.inria.ant;

import org.apache.tools.ant.Task;

public class NodeReference extends AbstractNodeReference implements INode {

    private INode reference;
    
    public NodeReference(INode node) {
        reference = node;
    }
    
    public Task createDeploy(DeployTask deploy) {
        return getReference().createDeploy(deploy);
    }

    public String getIp() {
        return getReference().getIp();
    }

    public Task execute(ExecuteTask remote) {
        return getReference().execute(remote);
    }

    public void setIp(String ip) {
        getReference().setIp(ip);
    }

    public void verify() {
        getReference().verify();
    }
    
    @Override
    protected INode getReference() {
        return reference;
    }

    public int compareTo(INode o) {
        return getReference().compareTo(o);
    }
}
