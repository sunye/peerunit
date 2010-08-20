/*
 * Created on 1 aožt 06
 *
 */
package fr.inria.ant;
import java.net.InetAddress;
import java.net.UnknownHostException;

//import org.gridbus.broker.util.SSHSession;

public class Test {

    /**
     * @param args
     */
    public static void main(String[] args) {
        
        String user = "sunye";
        String password = "volvic";
        InetAddress duke = null;
        
        try {
            duke = InetAddress.getByName("oo");
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        String ip = "172.16.9.101";
        
        System.out.println(duke);
       // SSHSession sshSession = new SSHSession(ip, user, password);
       // boolean connect = sshSession.connect();
        //System.out.println("--> Connection SSH réussie avec la machine "+ ip +" : "+connect);
        
        /** Copie du fichier jar **/
//        boolean result = sshSession.scpTo(this.getProject().getBaseDir()+"/fichiersConfiguration.jar",m.getDirectory()+"/fichiersConfiguration.jar");
//        System.out.println("--> Connection des fichiers de configuration sur la machine "+m.getIp()+" : "+result);
        
        /** Décompression du fichier JAR contenant les fichiers de configuration avec le package de gridbus ***/
 //       sshSession.executeCmd("cd "+m.getDirectory()+";jar xf ./fichiersConfiguration.jar");
        
        /** Suppression du fichier JAR **/
 //       sshSession.executeCmd("cd "+m.getDirectory()+";rm ./fichiersConfiguration.jar");

        /** Fermeture de la connection SSH **/
       // sshSession.close();         
    }

    }

