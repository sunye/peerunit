package fr.inria.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * 
 */
public class DeployTask extends RemoteTask {

    private Application application;

    public void setApplication(Application application) {
        this.application = application;
    }

    public Application getApplication() {
        return this.application;
    }

    @Override
    public void execute() throws BuildException {       
        this.verify();
        
        createArchive();
        
        IAbstractNode h = (this.getNode() != null) ? this.getNode() : this.getNodeSet();
        h.verify();
        Task task = h.createDeploy(this);
        task.execute();
        deleteArchive();
    }

    /**
     * 
     */
    private void deleteArchive() {
        this.getApplication().deleteArchive();
    }

    /**
     * 
     */
    private void createArchive() {
        assert this.getApplication() != null : "Null application";
        
        this.getApplication().createArchive();
    }
    
    

    @Override
    public void verify() {
        if (this.getApplication() == null) {
            throw new BuildException("You must specify an application.");
        }
        
        super.verify();
    }
    
    
    public Application createApplication() {
        application = new Application();
        return application;
    }




}
