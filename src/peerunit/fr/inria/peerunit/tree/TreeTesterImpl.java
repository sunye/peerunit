package fr.inria.peerunit.tree;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.test.oracle.Oracle;
import fr.inria.peerunit.test.oracle.Verdicts;
import fr.inria.peerunit.tree.parser.ExecutorImpl;
import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.util.PeerUnitLogger;
import fr.inria.peerunit.util.TesterUtil;



public class TreeTesterImpl  implements TreeTester,Serializable,Runnable{
	
	private static final long serialVersionUID = 1L;
	
	private ExecutorImpl executor;
	
	private static Logger PEER_LOG;
	
	public int id;
	
	private TreeElements tree=new TreeElements();
	
	private boolean amIRoot=false;
	
	private boolean amILeaf=true;
	
	private Long time;
	
	private Verdicts v= Verdicts.PASS;
	
	private static PeerUnitLogger log = new PeerUnitLogger(TreeTesterImpl.class
			.getName());
	
	private AtomicInteger informedByChildren = new AtomicInteger(0);
	
	final private Bootstrapper boot;
	
	public TreeTesterImpl(Bootstrapper b) throws RemoteException {
		boot = b;
		UnicastRemoteObject.exportObject(this);	
		id=boot.register(this);
		log.log(Level.INFO, "My ID is: "+id);		
	}
	
	public void run(){
		setupTree();		
		log.createLogger("/tester" + id + ".log");
		int actions=0;		
		if(amIRoot){	
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e1) {			
				e1.printStackTrace();
			}						
		}
		this.time=System.currentTimeMillis();
		while( actions < 8 ){
			try {
				if(amIRoot){
					log.log(Level.INFO, "Start action "+actions);
					dispatch(actions);
				}else{		
					synchronized(this){
						this.wait();
					}
					if(!amILeaf){
						dispatch(actions);
					}else{
						execute(actions);
					}
					talkToParent();
				}				
			} catch (InterruptedException e) {				
				e.printStackTrace();
			}
			actions++;
		}
		if(amIRoot)
			log.log(Level.INFO, "Whole execution time "+(System.currentTimeMillis()-this.time));
		else
			log.log(Level.INFO, id+" execution time "+(System.currentTimeMillis()-this.time));
		
		System.exit(0);		
	}
	
	
	/**
	 * Elements of the BTree
	 */	
	public synchronized void setTreeElements(TreeElements tree,boolean isRoot) throws RemoteException{
		this.amIRoot=isRoot;
		this.tree=tree;		
		log.log(Level.INFO, id+"  my parent is "+tree.getParent());
	}
	
	/**
	 * Going way down the tree
	 */
	public synchronized void startExecution() throws RemoteException{
		log.log(Level.INFO, id+" I'm about to execute.");		
		this.notify();				
	}
	
	/**
	 * Going way up the tree
	 */
	public synchronized void endExecution() throws RemoteException{		
		int value= informedByChildren.incrementAndGet();
		log.log(Level.INFO, id+" tester completes. Left "+(tree.getChildren().size()-value)+" children");
		if(value==tree.getChildren().size()){			
			log.log(Level.INFO, id+" will notify thread.");			
			this.notify();			
			log.log(Level.INFO, id+" has notified thread.");
			informedByChildren.set(0);
		}		
	}

	public int getId()throws RemoteException {
		return id;
	}

	public void setChildren(TreeTester tester) throws RemoteException {
		tree.add(tester,id);		
		this.amILeaf=false;
	}	
	
	private void setupTree(){
		try {
			while(tree.getParent()==null){
				Thread.sleep(1000);
			}	
			if(!amIRoot)
				tree.getParent().setChildren(this);
			
		} catch (RemoteException e) {				
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void talkToChildren(){
		for(TreeTester t:tree.getChildren()){
			log.log(Level.INFO, id+" talk to kids "+ t);		
			try {
				t.startExecution();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void talkToParent(){	
		log.log(Level.INFO, id+" talk do daddy");	
		try {
			tree.getParent().endExecution();
		} catch (RemoteException e) {
			e.printStackTrace();
		}		
	}
	
	private void execute(int action){
		log.log(Level.INFO, id+" Executing action"+ action);					
	}
	
	private synchronized void dispatch(int action) throws InterruptedException {
		log.log(Level.INFO, id+" Dispatching action "+action);		
		talkToChildren();
		execute(action);		
		this.wait();		
	}

	public void execute(MethodDescription m) throws RemoteException {
		// TODO Auto-generated method stub
		
	}
	
	public void export(Class<? extends TestCaseImpl> c) {
		
		try {			
			createLogFiles(c);
			executor = new ExecutorImpl(this,log);
			log.log(Level.INFO, "Registering actions");	
			executor.register(c);			
		} catch (SecurityException e) {
			log.logStackTrace(e);			    
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
				
		} catch (SecurityException e) {
			log.logStackTrace(e);			    
		} catch (IOException e) {
			log.logStackTrace(e);			    
		}
	}
	
	private synchronized void invoke(MethodDescription md) {
		assert executor != null : "Null executor";

		boolean error = true;
		try {
			executor.invoke(md);
			error = false;
		} catch (IllegalArgumentException e) {
			log.logStackTrace(e);		
		} catch (IllegalAccessException e) {
			log.logStackTrace(e);		
		}catch (InvocationTargetException e) {	
			Oracle oracle=new Oracle(e.getCause());
			if(oracle.isPeerUnitFailure()){
				error = false;
			}
			v=oracle.getVerdict();					
			log.logStackTrace(e);		    
		} finally {
			if (error) {
				log.log(Level.WARNING," Executed in "+md.getName());
				v= Verdicts.INCONCLUSIVE;
				//stop=true;	
			} else{
				log.log(Level.INFO," Executed "+md.getName());				
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
