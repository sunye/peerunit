package fr.inria.peerunit.util;

import java.io.InputStream;
import java.util.Properties;

public class TesterUtil {
	private static Properties props;
	private static int peerName=Integer.MIN_VALUE;
	private static String serverAddr;
	private static String logfile;
	private static String logfolder;
	private static String dateformat;
	private static String timeformat;
	private static String delimiter;
	private static String parserClass;
	private static int relaxIndex;
	private static int port;
	private static int objects;
	private static int sleep;
	private static String bootstrap;
	private static int bootstrapPort;
	private static int  waitForMethod;
	private static int  loopToFail;
	private static int  churnPercentage;
	private static String logLevel;
	private static int  treeOrder;
	private static int  coordType;

	private static String getProperty(String property) throws Exception {
		if (props == null) {
			props = new Properties();
			//String propFile=System.getProperty("user.dir")+"tester.properties";
			String propFile="tester.properties";
			System.out.println("Prop file is: "+propFile);
			InputStream is = ClassLoader.getSystemResourceAsStream(propFile);
			if (is == null) {
				props = System.getProperties();
			} else {
				props.load(is);
			}
		}
		return props.getProperty(property);
	}

	private static int readProperty(){
		try {
			peerName=Integer.valueOf(TesterUtil.getProperty("tester.peers")).intValue();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return peerName;
	}

	public static int getExpectedPeers(){
		return readProperty();
	}

	public static int getPeerName(){
		if(peerName == Integer.MIN_VALUE){
			peerName=readProperty();
		}else peerName--;
		return peerName;
	}

	public static String getServerAddr(){
		try {
			serverAddr=TesterUtil.getProperty("tester.server");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return serverAddr;
	}
	public static String getLogfile(){
		try {
			logfile=TesterUtil.getProperty("tester.logfile");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return logfile;
	}
	public static String getDateformat(){
		try {
			dateformat=TesterUtil.getProperty("tester.log.dateformat");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dateformat;
	}
	public static String getTimeformat(){
		try {
			timeformat=TesterUtil.getProperty("tester.log.timeformat");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return timeformat;
	}
	public static String getDelimiter(){
		try {
			delimiter=TesterUtil.getProperty("tester.log.delimiter");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return delimiter;
	}
	public static String getLogfolder(){
		try {
			logfolder=TesterUtil.getProperty("tester.logfolder");
		} catch (Exception e) {
			logfolder = ".";
			//e.printStackTrace();
		}
		return logfolder;
	}
	public static String getParserClass(){
		try {
			parserClass=TesterUtil.getProperty("tester.parser");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return parserClass;
	}
	public static int getRelaxIndex(){
		try {
			relaxIndex=Integer.valueOf(TesterUtil.getProperty("tester.relaxindex")).intValue();
		} catch (Exception e) {
			relaxIndex = 1;
			//e.printStackTrace();
		}
		return relaxIndex;
	}
	public static int getPort(){
		try {
			port=Integer.valueOf(TesterUtil.getProperty("tester.port")).intValue();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return port;
	}
	public static int getObjects(){
		try {
			objects=Integer.valueOf(TesterUtil.getProperty("test.objects")).intValue();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return objects;
	}
	public static int getSleep(){
		try {
			sleep=Integer.valueOf(TesterUtil.getProperty("test.sleep")).intValue();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return sleep;
	}
	public static String getBootstrap(){
		try {
			bootstrap=TesterUtil.getProperty("test.bootstrap");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bootstrap;
	}
	public static int getBootstrapPort(){
		try {
			bootstrapPort=Integer.valueOf(TesterUtil.getProperty("test.bootstrap.port")).intValue();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return bootstrapPort;
	}

	public static int getWaitForMethod(){
		try {
			waitForMethod=Integer.valueOf(TesterUtil.getProperty("tester.waitForMethod")).intValue();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return waitForMethod;
	}

	public static int getLoopToFail(){
		try {
			loopToFail=Integer.valueOf(TesterUtil.getProperty("test.loopToFail")).intValue();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return loopToFail;
	}
	public static int getChurnPercentage(){
		try {
			churnPercentage=Integer.valueOf(TesterUtil.getProperty("test.churnPercentage")).intValue();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return churnPercentage;
	}
	public static String getLogLevel(){
		try {
			logLevel=TesterUtil.getProperty("tester.log.level");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return logLevel;
	}
	public static int getTreeOrder(){
		try {
			treeOrder=Integer.valueOf(TesterUtil.getProperty("test.treeOrder")).intValue();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return treeOrder;
	}
	
	public static int getCoordinationType(){
		try {
			coordType=Integer.valueOf(TesterUtil.getProperty("test.coordination")).intValue();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return coordType;
	}
	
}
