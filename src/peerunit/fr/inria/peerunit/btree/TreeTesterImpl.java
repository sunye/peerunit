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
import fr.inria.peerunit.VolatileTester;
import fr.inria.peerunit.btree.parser.ExecutorImpl;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.test.oracle.Oracle;
import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.util.TesterUtil;

public class TreeTesterImpl extends Thread implements TreeTester,VolatileTester {
	public int id;	
	MethodDescription md;
	boolean executing=true;
	private ExecutorImpl executor;	
	private static Logger PEER_LOG;
	private TestCaseImpl testcase;	
	Thread invokationThread;
	private Bootstrapper boot;
	List<MethodDescription> testList;
	//private static PeerUnitLogger log = new PeerUnitLogger(TreeTesterImpl.class.getName());
	String logFolder = TesterUtil.getLogfolder();
	public TreeTesterImpl(int id,Bootstrapper boot){
		this.id=id;
		this.boot=boot;
		System.out.println("[TreeTesterImpl]  BOOT ID "+boot);
		System.out.println("[TreeTesterImpl]  My Tester ID "+id);
		/*log.createLogger(logFolder+ "/tester" + id + ".log");	
		log.log(Level.INFO, "[TreeTesterImpl] Log file to use : "+logFolder+
				"/tester" + id + ".log");
		log.log(Level.INFO, "[TreeTesterImpl] My Tester ID is: "+id);	
		log.log(Level.INFO, "[TreeTesterImpl] instance ");*/
		//inbox=new Inbox(log);
		invokationThread=Thread.currentThread();
	}
	
	public void run() {			
		//log.log(Level.INFO, "[TreeTesterImpl] start ");		
		while(executing){
			synchronized (invokationThread) {			
				try {
					invokationThread.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}			
			}			
		}
	}

	public void inbox(MethodDescription md) {
		System.out.println("[TreeTesterImpl]  Tester "+id+" invoking");
		invoke(md);
	}
	
	/*public void setExecutor(ExecutorImpl executor){
		this.executor=executor;		
		this.executor.newInstance(this);		
	}*/
	
	public void setClass(Class<? extends TestCaseImpl> klass){
		executor = new ExecutorImpl();				
		testList=executor.register(klass);		
		newInstance(klass);		
	}

	/**
	 * @param c
	 * @throws IOException
	 *
	 * Creates the instances of peers and testers. Furthermore, creates the logfiles to them.
	 */
	public void newInstance(Class<? extends TestCaseImpl> c){		
		LogFormat format = new LogFormat();
		Level level = Level.parse(TesterUtil.getLogLevel());		
		try {			
			String logFolder = TesterUtil.getLogfolder();
			
			PEER_LOG = Logger.getLogger(c.getName());
			FileHandler phandler;
			phandler = new FileHandler(logFolder+"/" + c.getName()+ ".peer"+id+".log",true);
			phandler.setFormatter(format);
			PEER_LOG.addHandler(phandler);
			PEER_LOG.setLevel(level);
			
			testcase = (TestCaseImpl) c.newInstance();
			testcase.setTester(this);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}			
	}
	
/*	public void invoke(MethodDescription md) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {

		assert methods.containsKey(md) : "Method should be registered";
		assert testcase != null : "Test Case instance should not be null";

		Method m = methods.get(md);
		m.invoke(testcase, (Object[]) null);
	}*/
	
	private  void invoke(MethodDescription md) {
		assert executor != null : "Null executor";
		if(testList.contains(md)){
			boolean error = true;
			try {
				//invoke(md);
				Method m = executor.getMethod(md);
				m.invoke(testcase, (Object[]) null);
				error = false;
			} catch (IllegalArgumentException e) {
				//log.logStackTrace(e);		
				//v= Verdicts.INCONCLUSIVE;
			} catch (IllegalAccessException e) {
				//log.logStackTrace(e);		
				//v= Verdicts.INCONCLUSIVE;
			}catch (InvocationTargetException e) {	
				Oracle oracle=new Oracle(e.getCause());
				if(oracle.isPeerUnitFailure()){
					error = false;
				}
				//v=oracle.getVerdict();					
				//log.logStackTrace(e);		    
			} finally {
				if (error) {
					//log.log(Level.WARNING,"[TreeTesterImpl]  Executed in "+md.getName());									
				} else{
					//log.log(Level.INFO,"[TreeTesterImpl]  Executed "+md.getName());			
					if(executor.isLastMethod(md.getAnnotation())){
						//log.log(Level.FINEST,"[TreeTesterImpl] Test Case finished by annotation "+md.getAnnotation());			
						//log.log(Level.FINEST,"Local verdict "+v);
						//localVerdicts.add(v);	
					}
				}
			}
		}
	}
	
	public int getID(){
		System.out.println("[TreeTesterImpl]  Tester ID "+id);
		return this.id;
	}
	
	public void kill() {
		executing=false;		
		synchronized (invokationThread) {			
			invokationThread.notify();
		}
	}	
	/**
	 * Used to cache testing global variables
	 * @param key
	 * @param object
	 * @throws RemoteException
	 */
	public void put(Integer key,Object object) {
		try {
			boot.put(key, object);
		} catch (RemoteException e) {
			//LOG.logStackTrace(e);			    
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
			//LOG.logStackTrace(e);			    
		}
	}

	/**
	 *  Used to retrieve testing global variables
	 * @param key
	 * @return Object
	 * @throws RemoteException
	 */
	public Object get(Integer key)  {
		Object object=null;
		try {
			object = boot.get(key);
		} catch (RemoteException e) {
			//LOG.logStackTrace(e);			    
		}
		return object;
	}

	/**
	 *  Used to retrieve all the keys of the testing global variables
	 * @return Collection<Object>
	 * @throws RemoteException
	 * @throws RemoteException
	 */
	public  Map<Integer,Object> getCollection() throws RemoteException {
		return  boot.getCollection();
	}

	public boolean containsKey(Object key)throws RemoteException{
		return  boot.containsKey(key);
	}
	
}
