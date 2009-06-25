package fr.inria.peerunit.btree;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.btree.parser.ExecutorImpl;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.test.oracle.Oracle;
import fr.inria.peerunit.test.oracle.Verdicts;
import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.util.PeerUnitLogger;
import fr.inria.peerunit.util.TesterUtil;

public class TreeTesterImpl implements TreeTester,Runnable {
	public int id;	
	MethodDescription md;
	boolean executing=true;
	boolean isLastMethod=false;
	private ExecutorImpl executor;	
	private TestCaseImpl testcase;
	private Bootstrapper boot;
	private Verdicts v= Verdicts.PASS;
	List<MethodDescription> testList;
	private static final PeerUnitLogger TESTER_LOG = new PeerUnitLogger(TreeTesterImpl.class.getName());
	String logFolder = TesterUtil.instance.getLogfolder();
	
	/**
	 * Creates a new TreeTester with the specified id, and attached
	 * to the specified Bootstrapper
	 * @param id The tester's id
	 * @param boot The tester's Bootstrapper
	 */
	public TreeTesterImpl(int id,Bootstrapper boot){
		this.id=id;
		this.boot=boot;				
		
		TESTER_LOG.createLogger(logFolder+ "/tester" + id + ".log");	
		TESTER_LOG.log(Level.INFO, "[TreeTesterImpl] Log file to use : "+logFolder+
				"/tester" + id + ".log");
		TESTER_LOG.log(Level.INFO, "[TreeTesterImpl] My Tester ID is: "+id);	
		TESTER_LOG.log(Level.FINEST, "[TreeTesterImpl] instance ");
		TESTER_LOG.log(Level.FINEST, "[TreeTesterImpl]  BOOT ID "+boot);
	}
	
	
	/**
	 * Starts this tester
	 * If the tester has been killed, it can't be started again
	 */
	public  void run() {			
		TESTER_LOG.log(Level.FINEST, "[TreeTesterImpl] start ");		
		while(executing){
			synchronized (this) {			
				try {
					this.wait();
				} catch (InterruptedException e) {
					TESTER_LOG.log(Level.SEVERE,e.toString());
				}			
			}			
		}
	}

	public  void inbox(MethodDescription md) {
		TESTER_LOG.log(Level.FINEST, "[TreeTesterImpl]  Tester "+id+" invoking");
		invoke(md);
	}
	
	/**
	 * sets the test class for this tester
	 * @param klass the test class to be processed by the tester
	 */
	public  void setClass(Class<? extends TestCaseImpl> klass){
		executor = new ExecutorImpl(this);				
		testList=executor.register(klass);		
		newInstance(klass);		
	}

	/**
	 * @param c Test class
	 * @throws IOException
	 *
	 * Creates the instances of peers and testers. Furthermore, creates the logfiles to them.
	 */
	public  void newInstance(Class<? extends TestCaseImpl> c){
		final Logger PEER_LOG = Logger.getLogger(c.getName());
		LogFormat format = new LogFormat();
		Level level = TesterUtil.instance.getLogLevel();		
		try {			
			String logFolder = TesterUtil.instance.getLogfolder();
				
			FileHandler phandler;
			phandler = new FileHandler(logFolder+"/" + c.getName()+ ".peer"+id+".log",true);
			phandler.setFormatter(format);
			PEER_LOG.addHandler(phandler);
			PEER_LOG.setLevel(level);
			
			testcase = (TestCaseImpl) c.newInstance();
			testcase.setTester(this);
		} catch (InstantiationException e) {
			TESTER_LOG.logStackTrace(e);
		} catch (IllegalAccessException e) {
			TESTER_LOG.logStackTrace(e);
		} catch (SecurityException e) {
			TESTER_LOG.logStackTrace(e);
		} catch (IOException e) {
			TESTER_LOG.logStackTrace(e);
		} 
	}
	
	private  void invoke(MethodDescription md) {
		assert executor != null : "Null executor";
		if(testList.contains(md)){
			boolean error = true;
			try {	
				Method m = executor.getMethod(md);
				m.invoke(testcase, (Object[]) null);
				error = false;
			} catch (IllegalArgumentException e) {
				TESTER_LOG.logStackTrace(e);		
				v= Verdicts.INCONCLUSIVE;
			} catch (IllegalAccessException e) {
				TESTER_LOG.logStackTrace(e);
				v= Verdicts.INCONCLUSIVE;
			}catch (InvocationTargetException e) {	
				Oracle oracle=new Oracle(e.getCause());
				if(oracle.isPeerUnitFailure()){
					error = false;
				}
				v=oracle.getVerdict();					
				TESTER_LOG.logStackTrace(e);
			} finally {
				if (error) {
					TESTER_LOG.log(Level.WARNING,"[TreeTesterImpl]  Executed in "+md.getName());									
				} else{
					TESTER_LOG.log(Level.INFO,"[TreeTesterImpl]  Executed "+md.getName());			
					if(executor.isLastMethod(md.getAnnotation())){
						TESTER_LOG.log(Level.FINEST,"[TreeTesterImpl] Test Case finished by annotation "+md.getAnnotation());			
						TESTER_LOG.log(Level.FINEST,"Local verdict "+v);				
						isLastMethod=true;
					}
				}
			}
		}
	}
	
	/**
	 * Returns this tester's verdict
	 * @return the tester's verdict
	 */
	public Verdicts getVerdict(){
		return v;
	}
	
	/**
	 * Determines if the last method has been invoked
	 * @return true if the last method has been invoked
	 */
	public boolean isLastMethod(){
		return isLastMethod;
	}
	
	/**
	 * Returns this tester's id
	 * @return the tester's id
	 */
	public int getID(){
		TESTER_LOG.log(Level.FINEST,"[TreeTesterImpl]  Tester ID "+id);
		return this.id;
	}
	
	/**
	 * Kills the tester, preventing it from processing any other treatment
	 */
	public void kill() {
		executing=false;		
		synchronized (this) {			
			this.notify();
		}
	}	
	/**
	 * Used to cache testing global variables
	 * @param key the global variable's key to be set
	 * @param object the global variable's value to be set
	 * @throws RemoteException
	 */
	public void put(Integer key,Object object) {
		try {
			boot.put(key, object);
		} catch (RemoteException e) {
			TESTER_LOG.logStackTrace(e);			    
		}
	}

	/**
	 * Used to clear the Collection of testing global variables
	 *
	 * @throws RemoteException
	 */
	public void clear() {
		try {
			boot.clearCollection();
		} catch (RemoteException e) {
			TESTER_LOG.logStackTrace(e);
		}
	}

	/**
	 * Used to retrieve the testing global variables associated 
	 * to the specified key
	 * @param key
	 * @return Object
	 * @throws RemoteException
	 */
	public Object get(Integer key)  {
		Object object=null;
		try {
			object = boot.get(key);
		} catch (RemoteException e) {
			TESTER_LOG.logStackTrace(e);    
		}
		return object;
	}

	/**
	 * Returns the testing global variables
	 * @return the testing global variables
	 * @throws java.rmi.RemoteException
	 */
	public Map<Integer,Object> getCollection() throws RemoteException {
		return  boot.getCollection();
	}

	
	/**
	 * Determines if the specified key is mapped to a testing global variable
	 * @param key
	 * @return true if a testing global variable is associated to the key
	 * @throws java.rmi.RemoteException
	 */
	public boolean containsKey(Object key) throws RemoteException{
		return  boot.containsKey(key);
	}
	
	public String toString() {
		return "Tester: "+id;
	}


	public void execute(MethodDescription m) throws RemoteException {
		this.inbox(m);
	}


	public int getPeerName() throws RemoteException {
		return this.getID();
	}
}
