/*
 * Created on 21 févr. 07
 *
 */
package fr.inria.ant;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.CommandlineJava;

public interface IAbstractNode {

    public abstract void setUser(String user);

    public abstract String getUser();

    public abstract String getDirectory();

    public abstract void setDirectory(String directory);

    public abstract String getJavahome();

    public abstract void setJavahome(String str);

    public void setPassphrase(String passphrase) ;

    public String getPassphrase();
    
    public void setKeyfile(String keyfile);

    public String getKeyFile();
    
    public void setTrust(boolean value);
    
    public Boolean getTrust();
    
    public abstract void verify();
    
    public abstract boolean isChecked();

    public abstract Task createDeploy(DeployTask deploy);

    public abstract Task execute(ExecuteTask remote);
    
    public abstract Task executeJava(CommandlineJava command);

}