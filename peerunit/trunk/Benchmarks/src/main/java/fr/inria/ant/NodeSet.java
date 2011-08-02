package fr.inria.ant;

import java.util.HashSet;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Parallel;
import org.apache.tools.ant.types.CommandlineJava;

/**
 * 
 */
public class NodeSet extends AbstractNode implements INodeSet, Cloneable {

    private INodeSet reference;

    private Set<String> excludes = new HashSet<String>();

    public String getFrom() {
        return getReference().getFrom();
    }

    public void setFrom(String ip_from) {
        getReference().setFrom(ip_from);
    }

    public String getTo() {
        return getReference().getTo();
    }

    public void setTo(String ip_to) {
        getReference().setTo(ip_to);
    }

    public Set<IAbstractNode> getNodes() {
        return getReference().getNodes();
    }
    
    public Node createNode() {
        Node n = new Node(this);
        getNodes().add(n);
        return n;
    }

    @Override
    public void verify() {
        if (getTo() != null && getFrom() != null)
            this.createHostSet();       
        getReference().verify();
     }

    @Override
    public boolean isChecked() {
        return getReference().isChecked();
    }

    public Task createDeploy(DeployTask deploy) {
        log("NodeSet::createDeploy()");
        log("Nodes: " + getNodes().size());
        assert getProject() != null : "NodeSet::createDeploy() - Null project";
        assert isChecked() : "NodeSet::createDeploy() - Node set not checked";
        assert deploy != null : "NodeSet::createDeploy() - Null deploy";

        Parallel par = (Parallel) this.getProject().createTask("parallel");
        for (IAbstractNode h : this.getNodes()) {
            par.addTask(h.createDeploy(deploy));
        }
        return par;
    }

    public Task execute(ExecuteTask remote) {
        log("getSSHExecTask()");
        assert isChecked() : "Node set not checked";

        Parallel par = (Parallel) this.getProject().createTask("parallel");
        for (IAbstractNode h : this.getNodes()) {
            par.addTask(h.execute(remote));
        }
        return par;
    }
    
    public Task executeJava(CommandlineJava command) {
        log("executejavba()");
        assert isChecked() : "Node set not checked";

        Parallel par = (Parallel) this.getProject().createTask("parallel");
        for (IAbstractNode h : this.getNodes()) {
            par.addTask(h.executeJava(command));
        }
        return par;
    }
    
    @Override
    protected INodeSet getReference() {
        if (reference == null) {
            if (isReference()) {
                reference = new NodeSetReference((INodeSet) this.getRefid()
                        .getReferencedObject());
                log("creating a reference");
            } else {
                reference = new NodeSetData();
                log("creating an implementation");
            }
        }
        return reference;
    }

    private void createHostSet() {
        log("createHostSet");
        assert getFrom() != null    : "Null from ip";
        assert getTo() != null      : "Null to ip";
        assert getFrom().split("\\.").length == 4 : "Invalid from IP";
        assert getTo().split("\\.").length == 4 : "Invalid to IP";
        
        
        String[] from = getFrom().split("\\.");
        String[] to = getTo().split("\\.");
        
        
        if (!from[0].equals(to[0]) || !from[1].equals(to[1])
                || !from[2].equals(to[2])) {
            throw new BuildException("IP range not supported ");
        }
        
        int x = Integer.parseInt(from[3]);
        int y = Integer.parseInt(to[3]);
        int min = x < y ? x : y;
        int max = x > y ? x : y;
 
        for (int i = min; i <= max; i++) {
            String ipTemp = from[0] + "." + from[1] + "." + from[2] + "." + i;
            Node n = new Node(this);
            n.setIp(ipTemp);
            n.setProject(getProject());
            getNodes().add(n);
        } 
     }

}
