package fr.inria.ant;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Sequential;
import org.apache.tools.ant.taskdefs.optional.ssh.SSHExec;
import org.apache.tools.ant.taskdefs.optional.ssh.Scp;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.CommandlineJava;

/**
 * Single IP Address
 */
public class Node extends AbstractNode implements INode {
    
    private INode reference;
    private IAbstractNode parent;
    
    public Node() {}
    
    public Node(IAbstractNode node) {
        parent = node;
    }

    public String getIp() {
        return getReference().getIp();
    }

    public void setIp(String ip) {
        getReference().setIp(ip);
    }

    /**
     * @param task
     *            TODO
     * @param command
     * @return
     */
    public Task execute(ExecuteTask remote) {
        assert isChecked() : "[getSSHExecTask] Node not checked";

        String command = String.format("cd %s; %s/bin/java %s", this
                .getDirectory(), this.getJavahome(), remote.getClassname());

        SSHExec sshexec = createSSHExecTask();
        sshexec.setCommand(command);
        return sshexec;
    }

    public Task createDeploy(DeployTask deploy) {
        assert isChecked() : "[Node::createDeploy] Node not checked";
        assert getProject() != null: "[Node::createDeploy] Null project";
        
        String commandLine;
        Sequential seq = (Sequential) this.getProject()
                .createTask("sequential");
        
        SSHExec mkdir = this.createSSHExecTask();



        
        if (seq == null)
            throw new BuildException("Task Sequencial not found");



        seq.init();
        
        commandLine = "mkdir -p " + this.getDirectory();
        //log(commandLine);
        mkdir.setCommand(commandLine);
        seq.addTask(mkdir);
        
        Scp scp = (Scp) this.getProject().createTask("scp");
        if (scp == null)
            throw new BuildException("Task Scp not found");
        scp.init();
        scp.setLocalFile(deploy.getApplication().getFullPathArchiveName());
        scp.setKeyfile(getKeyFile());
        scp.setPassphrase(getPassphrase());
        scp.setTodir(this.getUser() + "@" + getIp() + ":" + getDirectory());
        scp.setVerbose(true);
        if (getTrust() != null) {
        	scp.setTrust(getTrust());
        }
        
        seq.addTask(scp);

        commandLine = String.format("cd %s; %s/bin/jar xvf %s", this.getDirectory(), this
                .getJavahome(), deploy.getApplication().getArchiveName());
        SSHExec unjar = this.createSSHExecTask();
        unjar.setCommand(commandLine);
        seq.addTask(unjar);

        return seq;
    }
    
    public int compareTo(INode n) {
        return getReference().compareTo(n);
    }
    
    @Override
    protected INode getReference() {
        if (reference == null) {
            if(isReference()) {
                reference =  new NodeReference((INode) this.getRefid().getReferencedObject());
            } else {
                reference = new NodeData(parent);
            }
        }
        return reference;
    }

    /**
     * Steps : 
     * - correct jdk path
     * - add remote dir to class path
     * 
     */
    public Task executeJava(CommandlineJava command) {
        assert isChecked() : "Node not checked";
        
        List<String> l = new ArrayList<String>(20);
        ListIterator<String> it = l.listIterator();
       
        if (getJavahome() != null)
            command.setVm(this.getJavahome()+"/bin/java");
        command.getClasspath().setPath(getDirectory()); 
        
        command.getVmCommand().addCommandToList(it);
        command.getSystemProperties().addDefinitionsToList(it);
        
        if (command.getClasspath().size() > 0) {
            it.add("-classpath");           
            String base = this.getProject().getBaseDir().toString();
            String classpath = command.getClasspath().toString();
            // replace current dir with remote dir
            it.add(classpath.replaceAll(base, getDirectory()));
        }
        
        if(command.getJar() != null) {
            it.add("-jar");
        }
        
        command.getJavaCommand().addCommandToList(it);
        String commandLine = Commandline.toString(l.toArray(new String[0]));
        log(commandLine);

        SSHExec sshexec = createSSHExecTask();
        sshexec.setCommand(commandLine);
        return sshexec;
    }

	private SSHExec createSSHExecTask() {
		SSHExec sshexec = (SSHExec) this.getProject().createTask("sshexec");
		
        if (sshexec == null)
            throw new BuildException("Task SSHExec not found");
		
        sshexec.init();
        sshexec.setVerbose(true);
        sshexec.setHost(getIp());
        sshexec.setUsername(getUser());
        sshexec.setKeyfile(getKeyFile());
        sshexec.setPassphrase(getPassphrase());
        if (getTrust() != null) {
        	sshexec.setTrust(getTrust());
        }
		return sshexec;
	}


}
