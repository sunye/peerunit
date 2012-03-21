package fr.inria.peerunit;

import java.io.IOException;
import java.security.Security;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TaskAttemptID;
import org.apache.hadoop.mapred.TaskTracker;
import org.hsqldb.Session;

/**
 * 
 * @author sunye Use this wrapper to start and stop Hadoop's master node. This
 *         class should not depend on the TestCase.
 */
public class HadoopTaskTrackerWrapper {
	private static final Logger LOG = Logger
			.getLogger(HadoopTaskTrackerWrapper.class.getName());

	private static TaskTracker taskTracker;
	private static Thread taskTrackerThread;
        private final Configuration configuration;

	public HadoopTaskTrackerWrapper(Configuration configuration) {
        
            this.configuration = configuration;

	}

	protected void start() throws IOException, InterruptedException {
	
                // Create a thread to initialize TaskTracker.
		taskTrackerThread = new Thread(new TaskTrackerThread());
		taskTrackerThread.start();
               // taskTracker.wait(5000);
		Thread.yield();
                
	}
        
        protected void kill() throws Exception {
            
            try {
                    taskTrackerThread.stop();
                } catch (Exception e) {
                    LOG.info("Error on kill TaskTracker (stop()).");
                    e.printStackTrace();
                }
            
            try {
                taskTrackerThread.interrupt();
            } catch (Exception e) {
                 LOG.info("Error on kill TaskTracker (interrupt()).");
                e.printStackTrace();
            }
            
        }

	protected void stop() throws IOException, Exception {

		LOG.info("Stopping TaskTracker...");
               
                if (taskTrackerThread.isAlive()) {
                   taskTracker.shutdown();
                   taskTrackerThread.interrupt();
                } else {
                   LOG.info("TaskTracker thread is not alive in this Tester!");
                }
                
	
         /**
                if (taskTrackerThread.isAlive()) {
			taskTrackerThread.interrupt();
                        LOG.info("TaskTracker is stopped!");
		} else {
                    LOG.info("TaskTracker is not alive.");
                    throw new Exception(); 
                }
          */      
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
