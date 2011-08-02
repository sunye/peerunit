package fr.inria.ant;

import java.io.PrintStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;


/**
 * This task is used to execute remote applications.
 * 
 */
public class ExecuteTask extends RemoteTask {

	private String classname;
    private PrintStream ps = System.out;
 
    //Getters and Setters Method
	
	public void setClassname(String command){
		this.classname = command;
	}
	
	public String getClassname(){
		return classname;
	}
    
    
    @Override
    public void execute() throws BuildException {
        this.verify();

        Task task;
        IAbstractNode h = (this.getNode() != null) ? this.getNode() : this.getNodeSet();
        h.verify();
        task = h.execute(this);
        task.execute();

    }
	
}
