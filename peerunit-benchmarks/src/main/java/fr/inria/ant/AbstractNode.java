package fr.inria.ant;

import org.apache.tools.ant.types.DataType;

public abstract class AbstractNode extends DataType implements IAbstractNode {

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
    
    @Override
    public boolean isChecked() {
        return getReference().isChecked();
    }

    public void verify() {
        getReference().verify();
    }
    
    protected abstract IAbstractNode getReference();
}