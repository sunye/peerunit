/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.server.namenode.NameNode;
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
public class HadoopMasterWrapper {

	private static final Logger LOG = Logger
			.getLogger(HadoopMasterWrapper.class.getName());
	private  Thread nameNodeThread;
	private  Thread jobTrackerThread;

	private  StartNameNode nnode;
	private  JobConf job;
    private NameNode nameNode;
    
	private JobTracker jobTracker;
    
   
	/**
	 * @TODO: Find a way to remove this attribute.
	 */
	//private AbstractMR amr;

	public HadoopMasterWrapper(AbstractMR amr) {
		//this.amr = amr;
	}

	public void startMaster() throws RemoteException, IOException,
			InterruptedException {

		// NameNode
		nameNodeThread = initNameNode();
		nameNodeThread.start();
		nameNodeThread.join();

		// JobTracker
		jobTrackerThread = initJobTracker();
		jobTrackerThread.start();
		Thread.sleep(5000);
		Thread.yield();
	}

	public void stopMaster() throws IOException {
		LOG.info("Stopping JobTracker...");
		// JTracker.stopTracker();
		if (jobTrackerThread.isAlive()) {
			jobTrackerThread.interrupt();
		}

		LOG.info("Stopping NameNode...");
		// nn.stop();
		if (nameNodeThread.isAlive()) {
			nameNodeThread.interrupt();
		}
	}

	private Thread initJobTracker() throws IOException, InterruptedException {
		LOG.info("Starting JobTracker!");

		StartJobTracker jtracker = new StartJobTracker();
		Thread jtT = new Thread(jtracker);

		return jtT;
	}

	private Thread initNameNode() throws IOException, InterruptedException {
		LOG.info("Starting NameNode!");

		amr.setHadoopProperties();
		nnode = new StartNameNode();
		Thread nnT = new Thread(nnode);

		return nnT;
	}

	private class StartNameNode implements Runnable {

		public void run() {
			try {
				amr.setHadoopProperties();
				Configuration conf = amr.getConfHDFS();
				nameNode = new NameNode(conf);
				// nameNode = nn;

				Thread.sleep(5000);
			} catch (IOException ioe) {
			} catch (InterruptedException ie) {
			}
		}
	}

	// JobTracker
	private class StartJobTracker implements Runnable {

		public void run() {
			try {
				LOG.info("Starting JobTracker!");

				Configuration conf = amr.getConfMR();
				job = new JobConf(conf);
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
