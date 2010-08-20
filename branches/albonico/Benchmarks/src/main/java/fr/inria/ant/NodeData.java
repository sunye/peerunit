/*
 * Created on 21 févr. 07
 *
 */
package fr.inria.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.CommandlineJava;

public class NodeData extends AbstractNodeData implements INode {
    
    private String address = null;
    private String ref = null;
    private IAbstractNode parent;
    
    public NodeData(IAbstractNode node) {
        parent = node;
    }
    
    public String getIp() {
            return address;
    }

    public void setIp(String ip) {
        this.address = ip;
    }
    
    @Override
    public String getDirectory() {
        String ret = super.getDirectory();
        if (ret == null && parent != null)
            ret = parent.getDirectory();

        return ret;
    }

    @Override
    public String getJavahome() {
        String ret = super.getJavahome();
        if (ret == null && parent != null)
            ret = parent.getJavahome();

        return ret;
    }

    @Override
    public String getUser() {
        String ret = super.getUser();
        if (ret == null && parent != null)
            ret = parent.getUser();

        return ret;
    }
    
    @Override
    public String getKeyFile() {
        String ret = super.getKeyFile();
        if (ret == null && parent != null)
            ret = parent.getKeyFile();

        return ret;
    }
    
    @Override
    public String getPassphrase() {
        String ret = super.getPassphrase();
        if (ret == null && parent != null)
            ret = parent.getPassphrase();

        return ret;
    }
    
    @Override
    public Boolean getTrust() {
        Boolean ret = super.getTrust();
        if (ret == null && parent != null)
            ret = parent.getTrust();

        return ret;
    }
    
    @Override
    public String toString() {
        return String.format("Ip: %s Dir: %s User: %s", this.getIp(), this
                .getDirectory(), this.getUser());
    }

    @Override
    public void verify() {
        super.verify();
        
        if (this.getIp().equals("")) {
            throw new BuildException("The IP adresse was not specified");
        }
        if (this.getDirectory() == null) {
            throw new BuildException("Directory missing");
        }
        if (this.getUser() == null) {
            throw new BuildException("User missing");
        }
    }

    public int compareTo(INode o) {
        return getIp().compareTo(o.getIp());
    }

    public Task createDeploy(DeployTask deploy) {
        throw new RuntimeException("Should not implement");
    }

    public Task execute(ExecuteTask remote) {
        throw new RuntimeException("Should not implement");
    }


    public Task executeJava(CommandlineJava command) {
        throw new RuntimeException("Should not implement");
    }





}
