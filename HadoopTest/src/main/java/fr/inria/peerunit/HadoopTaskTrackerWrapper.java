package fr.inria.peerunit;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TaskTracker;

/**
 * 
 * @author sunye Use this wrapper to start and stop Hadoop's master node. This
 *         class should not depend on the TestCase.
 */
public class HadoopTaskTrackerWrapper {
	private static final Logger LOG = Logger
			.getLogger(HadoopTaskTrackerWrapper.class.getName());

	private TaskTracker taskTracker;
	private Thread taskTrackerThread;
    private final Configuration configuration;

	public HadoopTaskTrackerWrapper(Configuration configuration) {
        

        this.configuration = configuration;

	}

	protected void start() throws IOException, InterruptedException {
		// TaskTrackers
		LOG.info("Starting TaskTracker!");

		taskTrackerThread = new Thread(new TaskTrackerThread());
		taskTrackerThread.start();
		Thread.yield();
	}

	protected void stop() throws IOException {

		LOG.info("Stopping TaskTracker...");
		// TTracker.shutdown();
		if (taskTrackerThread.isAlive()) {
			taskTrackerThread.interrupt();
                        LOG.info("TaskTracker is stopped!");
		}
	}



	// TaskTracker
	private class TaskTrackerThread implements Runnable {

		public void run() {
			try {
				LOG.info("Starting TaskTracker!");

				JobConf job = new JobConf(configuration);
				taskTracker = new TaskTracker(job);
				taskTracker.run();
			} catch (IOException ioe) {
				LOG.info(ioe.toString());
			} 
		}
	}
}
