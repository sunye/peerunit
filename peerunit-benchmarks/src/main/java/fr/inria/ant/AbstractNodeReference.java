
package fr.inria.ant;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.CommandlineJava;


public abstract class AbstractNodeReference {
    
    public void setUser(String user) {
        getReference().setUser(user);
    }

    public String getUser() {
        return getReference().getUser();
    }

    public String getDirectory() {
        return getReference().getDirectory();
    }

    public void setDirectory(String directory) {
        getReference().setDirectory(directory);
    }

    public String getJavahome() {
        return getReference().getJavahome();
    }

    public void setJavahome(String str) {
        getReference().setJavahome(str);
    }
    
    public void setPassphrase(String passphrase) {
        getReference().setPassphrase(passphrase);
    }

    public String getPassphrase() {
        return getReference().getPassphrase();
    }
    
    public void setKeyfile(String keyfile) {
        getReference().setKeyfile(keyfile);
    }

    public String getKeyFile() {
        return getReference().getKeyFile();
    }
    
    public void setTrust(boolean value) {
    	getReference().setTrust(value);
    }
    
    public Boolean getTrust() {
    	return getReference().getTrust();
    }
    
    public boolean isChecked() {
        return getReference().isChecked();
    }
    
    public Task executeJava(CommandlineJava command) {
        return getReference().executeJava(command);
    }
    
    protected abstract IAbstractNode getReference();
 
}
