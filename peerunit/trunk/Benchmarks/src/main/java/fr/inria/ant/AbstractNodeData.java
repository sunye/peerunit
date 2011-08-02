/*
 * Created on 21 févr. 07
 *
 */
package fr.inria.ant;

public abstract class AbstractNodeData implements IAbstractNode {

    private String directory;

    private String user;

    private String javahome;
    
    private String passphrase;
    
    private String keyfile;
    
    private Boolean trust;

    
    private boolean checked = false;

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return this.user;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getJavahome() {
        return this.javahome;
    }

    public void setJavahome(String str) {
        this.javahome = str;
    }
    
    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public String getPassphrase() {
        return this.passphrase;
    }
    
    public void setKeyfile(String keyfile) {
        this.keyfile = keyfile;
    }

    public String getKeyFile() {
        return keyfile;
    }
    
    public void setTrust(boolean value) {
    	trust = value;
    }
    
    public Boolean getTrust() {
    	return trust;
    }

    public void verify() {
        checked = true;
    }
    
    public boolean isChecked() {
        return checked;
    }
}
