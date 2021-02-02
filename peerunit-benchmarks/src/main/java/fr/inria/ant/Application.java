package fr.inria.ant;

import java.io.File;
import java.util.Set;

import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.types.FileSet;

/**
 * 
 */
public class Application extends ProjectComponent {
	
	private FileSet fileSet;
	private String name;
	private Set<File> files;
	
	public FileSet getFileSet() {
		return this.fileSet;
	}
	
	public void setFileSet(FileSet fileset) {
		this.fileSet = fileset;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Set<File> getFiles(){
		return files;
	}	
	
	public FileSet createFileSet(){
        fileSet = new FileSet();
		return fileSet;
	}

	
	/** 
     * Creates the JAR archive
     * 
     **/
	public void createArchive() {
		Jar jar = (Jar) getProject().createTask("jar");
        jar.init();
		jar.setDestFile(this.getArchiveFile());
		jar.addFileset(fileSet);
 		jar.execute();
	}
	
	/** 
     * Deletes the JAR archive
     * 
     **/
	public void deleteArchive(){
		Delete delete = (Delete) getProject().createTask("delete");
        delete.init();
		delete.setFile(getArchiveFile());
		delete.execute();
	}

    public String getFullPathArchiveName() {
        return this.getProject().getBaseDir() + File.separator + this.getArchiveName(); 
    }
    
    public String getArchiveName() {
        return String.format("%s-archive.jar", getName());
    }
    
    private File getArchiveFile() {
        return new File(this.getFullPathArchiveName());
    }
}
