package fr.inria.peerunit.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 
 * This class retrieve the application properties stocked in the properties file. 
 * These properties allow for instance to parameter the testing architecture type
 * (centralized or distributed), the number of <i>testers</i> expected, etc.
 *  
 * @author Eduardo Almeida
 * @author Aboubakar Koïta
 * @author Veronique Pelleau
 * @author Jérémy Masson
 * @version 1.3
 * @since 1.0
 */
public class TesterUtil {
	/**
	 * The <tt>Properties</tt> object must containing the application properties
	 */
	private static Properties props;
	
	/**
	 * The number  of peers that will be in the simulation.
	 */	
	private static int peerName=Integer.MIN_VALUE;

	/**
	 * The <i>coordinator</i> or <i>bootstrapper</i>'s address depending on 
	 * the testing architecture is distributed or centralized.
	 */	
	private static String serverAddr;
	/**
	 * The application log file name
	 */		
	private static String logfile;
	/**
	 * The application log file folder
	 */			
	private static String logfolder;
	/**
	 * The date format used for logging
	 */				
	private static String dateformat;
	/**
	 * The time format used for logging
	 */					
	private static String timeformat;
	/**
	 * The delimiter used for logging
	 */						
	private static String delimiter;
	/**
	 * The class of the parser used for parse the <i>test case</i> actions
	 */							
	private static String parserClass;
	/**
	 * The relaxation index used for fix the tolerance to inconclusive results
	 */								
	private static int relaxIndex;
	/**
	 * The <i>coordinator</i> or <i>bootstrapper</i>'s port depending on 
	 * the testing architecture is distributed or centralized.
	 */		
	private static int port;
	/**
	 * This parameter fix the number of object to put in the Open chord or FreePastry's DHT
	 * for the testing
	 */			
	private static int objects;
	/**
	 * This parameter fix in millisecond the <i>test actions</i> inactivity time for the synchronization
	 */				
	private static int sleep;
	/**
	 * The peers's bootstrap address, may be different from <i>tester's</i> bootstrap one
	 */	
	private static String bootstrap;
	/**
	 * The peers's bootstrap port, may be different from <i>tester's</i> bootstrap one
	 */		
	private static int bootstrapPort;
	/**
	 * This parameter fix in millisecond the <i>tester's</i> waiting time for the synchronization
	 */					
	private static int  waitForMethod;
	/**
	 * The number of try of a <i>test action</i>
	 */						
	private static int  loopToFail;
	/**
	 * This properties fix a percentage of peers's number that is used by some <i>test cases</i>
	 * for  instance for choose the number of peers that join the test in first and those who join
	 * it in second.  
	 */							
	private static int  churnPercentage;
	/**
	 * This properties fix the application logging level
	 */								
	private static String logLevel;
	/**
	 * The BTree order, if we are in distributed architecture
	 */									
	private static int  treeOrder;
	/**
	 * The BTree strategy, if we are in distributed architecture
	 */									
	private static int  treeStrategy;
	/**
	 * Show all traces during station tree building
	 */
	private static int  stationTreeTrace;
	/**
	 * The testing architecture type, centralized or distributed 
	 */										
	private static int  coordType;
	/**
	 * This parameter fix in millisecond the <i>treetester's</i> a specific waiting 
	 * time for the synchronization.
	 */					
	private static int  treeWaitForMethod;
	
	/**
	 * 	This property correspond to the path of the file containing the tester's hosts addresses
	 */
	private static String hostsFilePath;	
	

	/**
	 * Return the value of the property whose the name is given as argument
	 * 
	 * @param property  the property whose we search the value
	 * @return the value of <code>property</code> property
	 * @throws Exception if the properties file can't find
	 */
	private static String getProperty(String property) throws Exception {
		if (props == null) {
			props = new Properties();
			
			// FIXME : System.getProperty("user.dir") : Portable ? 
			String propFilePath = System.getProperty("user.dir");
			String propFile = "config/tester.properties";
				
			FileInputStream fs = new FileInputStream(propFilePath+"/"+propFile);
			
			if (fs == null) {
				System.out.println("Do not find properties' file.");
				props = System.getProperties();
			} else {
				props.load(fs);
			}
		}
		return props.getProperty(property);
	}
	
	/**
	 * Return the number of testers expected in the properties file.
	 * 
	 * @return the number of testers expected in the properties file
	 */
	private static int readProperty(){
		try {
			peerName=Integer.valueOf(TesterUtil.getProperty("tester.peers")).intValue();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return peerName;
	}

	/**
	 * Return the number of testers expected in the properties file.
	 * 
	 * @return the number of testers expected in the properties file
	 */	
	public static int getExpectedPeers(){
		return readProperty();
	}

	/**
	 * This method decrement for every call the number of testers expected in the 
	 * properties file and  return it, apart the first call where it initialize only
	 * and return it .
	 * 
	 * @return the number of testers expected that is decremented before
	 */	
	public static int getPeerName(){
		if(peerName == Integer.MIN_VALUE){
			peerName=readProperty();
		}else peerName--;
		return peerName;
	}

	/**
	 * This method decrement for every call the number of testers expected in the 
	 * properties file and  return it, apart the first call where it initialize only
	 * and return it.
	 * 
	 * @return the number of testers expected that is decremented before
	 */		
	public static String getServerAddr(){
		try {
			serverAddr=TesterUtil.getProperty("tester.server");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return serverAddr;
	}
	
	/**
	 * Return the log file name
	 * @return the log file name
	 */
	public static String getLogfile(){
		try {
			logfile=TesterUtil.getProperty("tester.logfile");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return logfile;
	}
	
	/**
	 * Return the date format used for logging. 
	 * 
	 * @return  the date format used for logging
	 */
	public static String getDateformat(){
		try {
			dateformat=TesterUtil.getProperty("tester.log.dateformat");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dateformat;
	}
	
	/**
	 * Return the time format used for logging 
	 * 
	 * @return  the time format used for logging
	 */	
	public static String getTimeformat(){
		try {
			timeformat=TesterUtil.getProperty("tester.log.timeformat");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return timeformat;
	}
	
	/**
	 * Return the delimiter used for logging 
	 * 
	 * @return  the delimiter format used for logging
	 */	
	public static String getDelimiter(){
		try {
			delimiter=TesterUtil.getProperty("tester.log.delimiter");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return delimiter;
	}
	
	/**
	 * Return the application log file folder 
	 * 
	 * @return the application log file folder
	 */		
	public static String getLogfolder(){
		try {
			logfolder=TesterUtil.getProperty("tester.logfolder");
		} catch (Exception e) {
			logfolder = ".";
			//e.printStackTrace();
		}
		return logfolder;
	}
	
	/**
	 * Return the class of the parser used for parse the <i>test case</i> actions
	 * 
	 * @return the class of the parser used for parse the <i>test case</i> actions
	 */
	public static String getParserClass(){
		try {
			parserClass=TesterUtil.getProperty("tester.parser");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return parserClass;
	}
	
	/**
	 * Return the relaxation index used for fix the tolerance to inconclusive results.
	 * 
	 * @return  the relaxation index used for fix the tolerance to inconclusive results
	 */
	public static int getRelaxIndex(){
		try {
			relaxIndex=Integer.valueOf(TesterUtil.getProperty("tester.relaxindex")).intValue();
		} catch (Exception e) {
			relaxIndex = 1;
			//e.printStackTrace();
		}
		return relaxIndex;
	}

	/**
	 * Return the <i>coordinator</i> or <i>bootstrapper</i>'s port depending on 
	 * the testing architecture is distributed or centralized.
	 * 
	 * @return the <i>coordinator</i> or <i>bootstrapper</i>'s port depending on 
	 *         the testing architecture is distributed or centralized.
	 */
	public static int getPort(){
		try {
			port=Integer.valueOf(TesterUtil.getProperty("tester.port")).intValue();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return port;
	}
	
	/**
	 * Return the number of object to put in the Open chord or FreePastry's DHT
	 * for the testing.
	 * 
	 * @return  the relaxation index used for fix the tolerance to inconclusive results
	 */	
	public static int getObjects(){
		try {
			objects=Integer.valueOf(TesterUtil.getProperty("test.objects")).intValue();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return objects;
	}
	
	/**
	 * Return the <i>test actions</i> inactivity time for the synchronization.
	 * 
	 * @return Return the <i>test actions</i> inactivity time for the synchronization
	 */
	public static int getSleep(){
		try {
			sleep=Integer.valueOf(TesterUtil.getProperty("test.sleep")).intValue();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return sleep;
	}
	
	/**
	 * Return the peers's bootstrap address, may be different from <i>tester's</i> bootstrap one.
	 * 	
	 * @return  the peers's bootstrap address, may be different from <i>tester's</i> bootstrap one
	 */
	public static String getBootstrap(){
		try {
			bootstrap=TesterUtil.getProperty("test.bootstrap");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bootstrap;
	}
	
	/**
	 * Return the peers's bootstrap port, may be different from <i>tester's</i> bootstrap one.
	 * 
	 * @return the peers's bootstrap port, may be different from <i>tester's</i> bootstrap one
	 */
	public static int getBootstrapPort(){
		try {
			bootstrapPort=Integer.valueOf(TesterUtil.getProperty("test.bootstrap.port")).intValue();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return bootstrapPort;
	}

	/**
	 * Return in millisecond the <i>tester's</i> waiting time for the synchronization.
	 * 
	 * @return in millisecond the <i>tester's</i> waiting time for the synchronization
	 */
	public static int getWaitForMethod(){
		try {
			waitForMethod=Integer.valueOf(TesterUtil.getProperty("tester.waitForMethod")).intValue();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return waitForMethod;
	}

	/**
	 * Return the number of try of a <i>test action</i>.
	 * 
	 * @return the number of try of a <i>test action</i>
	 */	
	public static int getLoopToFail(){
		try {
			loopToFail=Integer.valueOf(TesterUtil.getProperty("test.loopToFail")).intValue();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return loopToFail;
	}
	
	/**
	 * Return a percentage of peers's number that is used by some <i>test cases</i> for instance 
	 * for choose the number of peers that join the test in first and those who join it in second.
	 *    
	 * @return a percentage of peers's number that is used by some <i>test cases</i> for instance 
	 *         for choose the number of peers that join the test in first and those who join it in
	 *         second.
	 */
	public static int getChurnPercentage(){
		try {
			churnPercentage=Integer.valueOf(TesterUtil.getProperty("test.churnPercentage")).intValue();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return churnPercentage;
	}
	
	/**
	 * Return the value of the property that fix the application logging level.
	 *  	
	 * @return the property that fix the application logging level
	 */
	public static String getLogLevel(){
		try {
			logLevel=TesterUtil.getProperty("tester.log.level");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return logLevel;
	}
	
	/**
	 * Return the BTree order, if we are in distributed architecture.
	 *  	
	 * @return the BTree order, if we are in distributed architecture
	 */	
	public static int getTreeOrder(){
		try {
			treeOrder=Integer.valueOf(TesterUtil.getProperty("test.treeOrder")).intValue();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return treeOrder;
	}
	
	/**
	 * Return the BTree strategy, if we are in distributed architecture.
	 *  	
	 * @return the BTree strategy, if we are in distributed architecture
	 */	
	public static int getTreeStrategy(){
		try {
			treeStrategy=Integer.valueOf(TesterUtil.getProperty("test.treeStrategy")).intValue();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return treeStrategy;
	}
	
	/**
	 * Return the testing architecture type, centralized or distributed
	 * 
	 * @return the testing architecture type, centralized or distributed
	 */
	public static int getCoordinationType(){
		try {
			coordType=Integer.valueOf(TesterUtil.getProperty("test.coordination")).intValue();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return coordType;
	}
	
	/**
	 * Return the value of the property that fix in millisecond the <i>treetester's</i> a specific 
	 * waiting time for the synchronization.
	 *  
	 * @return  Return the value of the property that fix in millisecond the <i>treetester's</i> a
	 *          specific waiting time for the synchronization.
	 */
	public static int getTreeWaitForMethod(){
		try {
			treeWaitForMethod=Integer.valueOf(TesterUtil.getProperty("test.treeWaitForMethod")).intValue();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return treeWaitForMethod;
	}

	/** 1 to show traces during the station tree building, 0 by default.
	 * @return the stationTreeTrace
	 */
	public static int getStationTreeTrace()
	{
		stationTreeTrace = 0;
		try {
			stationTreeTrace=Integer.valueOf(TesterUtil.getProperty("tester.stationTreeTrace")).intValue();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return stationTreeTrace;
	}
	
	/**
	 * Return the value of the property that indicates the path of the file containing the tester's 
	 * hosts addresses.
	 *  
	 * @return the path of the hosts file.
	 */
	public static String getHostsFilePath()
	{
		try {
			hostsFilePath=TesterUtil.getProperty("tester.hostfile");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return hostsFilePath;
	}
}

