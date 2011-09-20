/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.JobTracker;

/**
 * 
 * @author sunye
 * 
 *         Use this wrapper to start and stop Hadoop's master node.
 *         This class should not depend on the TestCase.
 * 
 */
public class HadoopJobTrackerWrapper {

	private static final Logger LOG = Logger
			.getLogger(HadoopJobTrackerWrapper.class.getName());
	private  Thread nameNodeThread;
	private  Thread jobTrackerThread;


	private  JobConf job;

    
	private JobTracker jobTracker;
    private Configuration configuration;
    
   
	/**
	 * @TODO: Find a way to remove this attribute.
	 */
	//private AbstractMR amr;

	public HadoopJobTrackerWrapper(Configuration conf) {
		this.configuration = conf;
	}

	public void start() throws RemoteException, IOException,
			InterruptedException {

		// JobTracker
		jobTrackerThread = new Thread(new StartJobTracker());
		jobTrackerThread.start();
		Thread.sleep(5000);
		Thread.yield();
	}

	public void stop() throws IOException {
		LOG.info("Stopping JobTracker...");
		// JTracker.stopTracker();
		if (jobTrackerThread.isAlive()) {
			jobTrackerThread.interrupt();
		}
	}


	// JobTracker
	private class StartJobTracker implements Runnable {

		public void run() {
			try {
				LOG.info("Starting JobTracker!");
				job = new JobConf(configuration);
				jobTracker = JobTracker.startTracker(job);
				jobTracker.offerService();
			} catch (IOException ioe) {
				LOG.info(ioe.toString());
			} catch (InterruptedException ie) {
				LOG.info(ie.toString());
			}
		}
	}
}
