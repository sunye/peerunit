package fr.inria.peerunit.rmi.tester;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.Coordinator;
import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.parser.ExecutorImpl;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.test.oracle.Oracle;
import fr.inria.peerunit.test.oracle.Verdicts;
import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.util.PeerUnitLogger;
import fr.inria.peerunit.util.TesterUtil;


public class TesterImpl extends Object implements Tester, Serializable, Runnable {

	private static final long serialVersionUID = 1L;

	private static PeerUnitLogger LOG = new PeerUnitLogger(TesterImpl.class.getName());

	private static Logger PEER_LOG;

	final private Coordinator coord;

	final private int id;

	private boolean stop=false;

	private Thread timeoutThread;

	private Thread invokationThread;

	private ExecutorImpl executor;

	private Verdicts v= Verdicts.PASS;

	private BlockingQueue<MethodDescription> executionQueue = new ArrayBlockingQueue<MethodDescription>(2);

	public TesterImpl(Coordinator c) throws RemoteException {
		coord = c;
		id = coord.getNewId(this);
	}


	public void run() {
		while (!stop) {
			MethodDescription md=null;
			try {
				md = executionQueue.poll(TesterUtil.getWaitForMethod(),TimeUnit.MILLISECONDS);
				if(md != null){
					invokationThread = new Thread(new Invoke(md));
					invokationThread.start();
					if (md.getTimeout() > 0) {
						timeoutThread = new Thread(new Timeout(invokationThread,
								md.getTimeout()));
						timeoutThread.start();
					}				
				}			
			} catch (InterruptedException e) {
				LOG.logStackTrace(e);			    
			}
		}
		LOG.log(Level.INFO,"Stopping Tester ");
		System.exit(0);
	}

	public void export(Class<? extends TestCaseImpl> c) {

		boolean exported = false;
		try {
			createLogFiles(c);
			executor = new ExecutorImpl(this,LOG);
			coord.register(this, executor.register(c));
			exported = true;
		} catch (RemoteException e) {
			LOG.logStackTrace(e);			    
		} catch (SecurityException e) {
			LOG.logStackTrace(e);			    
		} finally {
			if (!exported) {
				executionInterrupt();
			}
		}
	}


	/**
	 * @param c
	 * @throws IOException
	 *
	 * Creates the peer and the tester log files.
	 */
	private void createLogFiles(Class<? extends TestCaseImpl> c) {

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
			
			LOG.createLogger(logFolder+ "/tester" + id + ".log");
		} catch (SecurityException e) {
			LOG.logStackTrace(e);			    
		} catch (IOException e) {
			LOG.logStackTrace(e);			    
		}

	}

	public synchronized void execute(MethodDescription md)
	throws RemoteException {
		LOG.log(Level.FINEST,"Starting TesterImpl::execute(MethodDescription) with: " + md);
		try {
			executionQueue.put(md);			
		} catch (InterruptedException e) {
			LOG.logStackTrace(e);	  
		}
	}

	public int getPeerName() throws RemoteException {
		return id;
	}

	public int getId() {
		return id;
	}

	/**
	 * An example how to kill a peer
	 * <code> YourTestClass test = new YourTestClass();
	 * test.export(test.getClass());
	 * test.run();
	 * ...	 // code
	 * test.kill(); </code>
	 */
	public void kill() {
		executionInterrupt();
		LOG.log(Level.INFO,"Test Case finished by kill ");
	}

	private void executionOk(String methodAnnotation) {
		try {
			coord.greenLight();
			LOG.log(Level.FINEST,"Executed "+methodAnnotation);
			if(executor.isLastMethod(methodAnnotation)){
				LOG.log(Level.FINEST,"Test Case finished by annotation "+methodAnnotation);
				executionInterrupt();
			}
		} catch (RemoteException e) {
			LOG.logStackTrace(e);			    
		}
	}

	//public void executionInterrupt(boolean error) {
	public void executionInterrupt() {
		try {
			if(v == null){
				v= Verdicts.INCONCLUSIVE;
				//error=true;
			}
			executionQueue.clear();
			LOG.log(Level.INFO,"Test Case local verdict to peer "+id+" is "+v.toString());
			//coord.quit(this,error,v);
			coord.quit(this,v);
		} catch (RemoteException e) {
			LOG.logStackTrace(e);			    
		} finally{
			stop=true;	
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
			coord.put(key, object);
		} catch (RemoteException e) {
			LOG.logStackTrace(e);			    
		}
	}

	/**
	 * Used to clear the Collection of testing global variables
	 *
	 * @throws RemoteException
	 */
	public void clear() {
		try {
			coord.clearCollection();
		} catch (RemoteException e) {
			LOG.logStackTrace(e);			    
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
			object = coord.get(key);
		} catch (RemoteException e) {
			LOG.logStackTrace(e);			    
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
		return  coord.getCollection();
	}

	public boolean containsKey(Object key)throws RemoteException{
		return  coord.containsKey(key);
	}

	private synchronized void invoke(MethodDescription md) {
		assert executor != null : "Null executor";

		boolean error = true;
		try {
			executor.invoke(md);
			error = false;
		} catch (IllegalArgumentException e) {
			LOG.logStackTrace(e);		
		} catch (IllegalAccessException e) {
			LOG.logStackTrace(e);		
		}catch (InvocationTargetException e) {	
			Oracle oracle=new Oracle(e.getCause());
			if(oracle.isPeerUnitFailure()){
				error = false;
			}
			v=oracle.getVerdict();					
			LOG.logStackTrace(e);		    
		} finally {
			if (error) {
				LOG.log(Level.WARNING," Executed in "+md.getName());
				executionInterrupt();
			} else{
				LOG.log(Level.INFO," Executed "+md.getName());
				executionOk(md.getAnnotation());
			}
		}
	}

	private class Invoke implements Runnable {

		MethodDescription md;

		public Invoke(MethodDescription md) {
			this.md = md;
		}

		public void run() {
			invoke(md);
		}
	}
	

}
