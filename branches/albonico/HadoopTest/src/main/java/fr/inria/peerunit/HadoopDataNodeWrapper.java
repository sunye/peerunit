package fr.inria.peerunit;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.server.datanode.DataNode;

public class HadoopDataNodeWrapper {
	private static final Logger LOG = Logger
			.getLogger(HadoopTaskTrackerWrapper.class.getName());
	private DataNode dataNode;
	private Thread dataNodeThread;
	private final Configuration cfg;
	private final String dirName;
	private final String dirData;

	public HadoopDataNodeWrapper(Configuration hdfsConf,
			String dirName, String dirData) {

		this.cfg = hdfsConf;
		this.dirName = dirName;
		this.dirData = dirData;
	}

	protected void start() throws IOException, InterruptedException {
		cfg.set("dfs.name.dir", dirName);
		cfg.set("dfs.data.dir", dirData);
		LOG.info("Starting DataNode!");
		
		dataNodeThread = new Thread(new DataNodeThread());
		dataNodeThread.start();
		dataNodeThread.join();
	}

	protected void stop() throws IOException {
		// dn.shutdown();
		if (dataNodeThread.isAlive()) {
			dataNodeThread.interrupt();
		}

	}

	private class DataNodeThread implements Runnable {

		public void run() {
			try {

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

}
