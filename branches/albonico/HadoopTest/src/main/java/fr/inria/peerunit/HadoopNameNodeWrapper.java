/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.server.namenode.NameNode;

/**
 *
 * @author sunye
 */
public class HadoopNameNodeWrapper {

    private static final Logger LOG = Logger.getLogger(HadoopNameNodeWrapper.class.getName());
    private Thread nameNodeThread;
    private NameNode nameNode;
    private Configuration configuration;

    public HadoopNameNodeWrapper(Configuration conf) {
		this.configuration = conf;
	}
    
    
    public void start() {

        // NameNode
        LOG.info("Starting NameNode!");
				

	nameNodeThread = new Thread(new NameNodeThread());
        nameNodeThread.start();
        /*
        try {
            nameNodeThread.join();
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Logger.getLogger(HadoopNameNodeWrapper.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
         * 
         */
        //Thread.yield();
    }

    public void stop() throws IOException {

        LOG.info("Stopping NameNode...");
        // nn.stop();
        if (nameNodeThread.isAlive()) {
            nameNodeThread.interrupt();
        }
    }

    private class NameNodeThread implements Runnable {

        public void run() {
            try {
                nameNode = new NameNode(configuration);

                //Thread.sleep(5000);
            } catch (Exception e) {
                LOG.log(Level.INFO, "Exception on NameNodeThread!");
                e.printStackTrace();
            }
        }
    }
}
