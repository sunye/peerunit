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

	protected void start() throws IOException{
		//cfg.set("dfs.name.dir", dirName);
		//cfg.set("dfs.data.dir", dirData);
		LOG.info("Starting DataNode!");
		
		dataNodeThread = new Thread(new DataNodeThread());
		dataNodeThread.start();
                
        try {
            dataNodeThread.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            Logger.getLogger(HadoopDataNodeWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
                 
             //   Thread.yield();
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
                                //System.out.println("DataNodeThread.cfg="+cfg.toString());
				dataNode = DataNode.createDataNode(args, cfg);

				String serveraddr = dataNode.getNameNodeAddr().toString();
				LOG.log(Level.INFO, "DataNode connected with NameNode: {0}",
						serveraddr);

				//Thread.currentThread().join();
			} catch (Exception e) {
                            LOG.log(Level.INFO, "Exception on DataNodeThread!");
                            e.printStackTrace();
			}
		}
	}

}
