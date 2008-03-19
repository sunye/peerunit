package fr.inria.peerunit.util;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PeerUnitLogger {
	private static Logger LOG;
	
	public PeerUnitLogger(String name){
		LOG = Logger.getLogger(name);
	}
	
	public void createLogger(String pattern){
		LogFormat format = new LogFormat();
		Level level = Level.parse(TesterUtil.getLogLevel());
		FileHandler handler=null;
		try {
			handler = new FileHandler(pattern,true);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		handler.setFormatter(format);
		LOG.addHandler(handler);
		LOG.setLevel(level);
	}

	public void log(Level level, String msg){
		LOG.log(level, msg);
	}
	
}
