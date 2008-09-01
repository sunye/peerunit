package fr.inria.peerunit.btree;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.logging.Level;

import fr.inria.peerunit.StorageTester;
import fr.inria.peerunit.btree.parser.ExecutorImpl;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.test.oracle.Oracle;
import fr.inria.peerunit.util.PeerUnitLogger;
import fr.inria.peerunit.util.TesterUtil;

public class TreeTesterImpl extends Thread implements TreeTester,StorageTester {
	public int id;	
	MethodDescription md;
	boolean executing=true;
	private ExecutorImpl executor;
	//private Inbox inbox;
	Thread invokationThread;
	private static PeerUnitLogger log = new PeerUnitLogger(TreeTesterImpl.class
			.getName());
	String logFolder = TesterUtil.getLogfolder();
	public TreeTesterImpl(int id){
		this.id=id;
		log.createLogger(logFolder+ "/tester" + id + ".log");	
		log.log(Level.INFO, "[TreeTesterImpl] Log file to use : "+logFolder+
				"/tester" + id + ".log");
		log.log(Level.INFO, "[TreeTesterImpl] My Tester ID is: "+id);	
		log.log(Level.INFO, "[TreeTesterImpl] instance ");
		//inbox=new Inbox(log);
	}
	
	public void run() {			
		log.log(Level.INFO, "[TreeTesterImpl] start ");		
		while(executing){
			synchronized (this) {			
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}			
			}
			//executing=inbox.execute(this);
			invokationThread = new Thread(new Invoke(md));
			invokationThread.start();	
		}
	}

	public synchronized void inbox(MethodDescription md) {		
		this.md=md;
		log.log(Level.INFO, "[TreeTesterImpl] Executing "+md);
		this.notify();
	}
	
	public void setExecutor(ExecutorImpl executor){
		this.executor=executor;		
		this.executor.newInstance(this);
	}

	private synchronized void invoke(MethodDescription md) {
		assert executor != null : "Null executor";

		boolean error = true;
		try {
			executor.invoke(md);
			error = false;
		} catch (IllegalArgumentException e) {
			log.logStackTrace(e);		
			//v= Verdicts.INCONCLUSIVE;
		} catch (IllegalAccessException e) {
			log.logStackTrace(e);		
			//v= Verdicts.INCONCLUSIVE;
		}catch (InvocationTargetException e) {	
			Oracle oracle=new Oracle(e.getCause());
			if(oracle.isPeerUnitFailure()){
				error = false;
			}
			//v=oracle.getVerdict();					
			log.logStackTrace(e);		    
		} finally {
			if (error) {
				log.log(Level.WARNING,"[TreeTesterImpl]  Executed in "+md.getName());									
			} else{
				log.log(Level.INFO,"[TreeTesterImpl]  Executed "+md.getName());			
				if(executor.isLastMethod(md.getAnnotation())){
					log.log(Level.FINEST,"[TreeTesterImpl] Test Case finished by annotation "+md.getAnnotation());			
					//log.log(Level.FINEST,"Local verdict "+v);
					//localVerdicts.add(v);	
				}
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
	
	public int getID(){
		return this.id;
	}
	
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	public boolean containsKey(Object key) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	public Object get(Integer key) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<Integer, Object> getCollection() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public void kill() {
		// TODO Auto-generated method stub
		
	}

	public void put(Integer key, Object object) {
		// TODO Auto-generated method stub
		
	}

}
