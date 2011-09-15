package fr.inria.peerunit;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.server.datanode.DataNode;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TaskTracker;

/**
 * 
 * @author sunye Use this wrapper to start and stop Hadoop's master node. This
 *         class should not depend on the TestCase.
 */
public class HadoopWorkerWrapper {
	private static final Logger LOG = Logger
			.getLogger(HadoopWorkerWrapper.class.getName());

	private JobConf job;
	private TaskTracker taskTracker;
	private DataNode dataNode;
	private Thread dataNodeThread;
	private Thread taskTrackerThread;

	private AbstractMR amr;

	public HadoopWorkerWrapper(AbstractMR amr) {
		this.amr = amr;
	}

	protected void startWorkers() throws IOException, InterruptedException {
		// TaskTrackers
		taskTrackerThread = initTT();
		taskTrackerThread.start();
		Thread.yield();

		// DataNodes
		dataNodeThread = initDN();
		dataNodeThread.start();
		dataNodeThread.join();
	}

	protected void stopWorkers() throws IOException {
		LOG.info("Stopping Datanode...");
		// dn.shutdown();
		if (dataNodeThread.isAlive()) {
			dataNodeThread.interrupt();
		}

		LOG.info("Stopping TaskTracker...");
		// TTracker.shutdown();
		if (taskTrackerThread.isAlive()) {
			taskTrackerThread.interrupt();
		}
	}

	private class StartDataNode implements Runnable {

		public void run() {
			try {
				Configuration cfg = amr.getConfHDFS();
				String dirname = (String) amr.get(-5);
				String dirdata = (String) amr.get(-6);
				cfg.set("dfs.name.dir", dirname);
				cfg.set("dfs.data.dir", dirdata);

				String[] args = { "-rollback" };

				dataNode = DataNode.createDataNode(args, cfg);

				String serveraddr = dataNode.getNamenode();
				LOG.log(Level.INFO, "DataNode connected with NameNode: {0}",
						serveraddr);

				Thread.currentThread().join();
			} catch (IOException ioe) {
			} catch (InterruptedException ie) {
			}
		}
	}

	private Thread initDN() throws IOException, InterruptedException {
		LOG.info("Starting DataNode!");

		StartDataNode datanode = new StartDataNode();
		Thread dnT = new Thread(datanode);

		return dnT;
	}

	// TaskTracker
	private class StartTaskTracker implements Runnable {

		public void run() {
			try {
				LOG.info("Starting TaskTracker!");

				Configuration conf = amr.getConfMR();
				JobConf job = new JobConf(conf);
				taskTracker = new TaskTracker(job);
				taskTracker.run();
			} catch (IOException ioe) {
				LOG.info(ioe.toString());
			} catch (InterruptedException ie) {
				LOG.info(ie.toString());
			}
		}
	}

	private Thread initTT() throws IOException, InterruptedException {
		LOG.info("Starting TaskTracker!");

		StartTaskTracker ttracker = new StartTaskTracker();
		Thread ttT = new Thread(ttracker);

		return ttT;
	}
}
