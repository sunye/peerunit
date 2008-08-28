package fr.inria.peerunit.tree;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.StorageTester;
import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.test.oracle.GlobalVerdict;
import fr.inria.peerunit.test.oracle.Oracle;
import fr.inria.peerunit.test.oracle.Verdicts;
import fr.inria.peerunit.tree.parser.ExecutorImpl;
import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.util.PeerUnitLogger;
import fr.inria.peerunit.util.TesterUtil;



public class TreeTesterImpl  implements TreeTester,Serializable,Runnable, Tester, StorageTester{
	
	private static final long serialVersionUID = 1L;
	
	private ExecutorImpl executor;
	
	private static Logger PEER_LOG;
	
	private int relaxIndex = TesterUtil.getRelaxIndex();
	
	public int id;
	
	private TreeElements tree=new TreeElements();
	
	private boolean amIRoot=false;
	
	private boolean amILeaf=true;
		
	private Thread invokationThread;
	
	private Long time, buildTime;
	
	private Verdicts v= Verdicts.PASS;
	
	private GlobalVerdict verdict = new GlobalVerdict();
	
	private List<Verdicts> localVerdicts=new ArrayList<Verdicts>();
	
	private static PeerUnitLogger log = new PeerUnitLogger(TreeTesterImpl.class
			.getName());
	
	private List<MethodDescription> testList = new ArrayList<MethodDescription>();
	
	private AtomicInteger informedByChildren = new AtomicInteger(0);
	
	final private Bootstrapper boot;
			
	String logFolder = TesterUtil.getLogfolder();
	
	private boolean killed=false;
	
	public TreeTesterImpl(Bootstrapper b) throws RemoteException {
		boot = b;
		UnicastRemoteObject.exportObject(this);	
		id=boot.register(this);
		System.out.println("Log file to use : "+logFolder+ "/tester" + id + ".log");
		log.createLogger(logFolder+ "/tester" + id + ".log");	
		log.log(Level.INFO, "Log file to use : "+logFolder+ "/tester" + id + ".log");
		log.log(Level.INFO, "My ID is: "+id);		
	}
	
	public void run(){
		this.buildTime=System.currentTimeMillis();
		setupTree();			
		this.buildTime=System.currentTimeMillis()-this.buildTime;
		if(amIRoot){	
			try {
				Thread.sleep(TesterUtil.getWaitForMethod());
			} catch (InterruptedException e) {			
				log.logStackTrace(e);		
			}						
		}
		this.time=System.currentTimeMillis();
		
		for(MethodDescription md:testList){		
			try {
				verifyTree();
				if(amIRoot){
					log.log(Level.INFO, "Start action ");
					dispatch(md);
				}else{		
					synchronized(this){
						this.wait();
					}
					if(!amILeaf){
						dispatch(md);
					}else{
						execute(md);						
					}					
					talkToParent();					
				}				
			} catch (InterruptedException e) {				
				log.logStackTrace(e);		
			} catch (RemoteException e) {
				log.logStackTrace(e);					
			}			
		}
		if(amIRoot){
			log.log(Level.INFO, "# Verdicts "+localVerdicts.size());
			for(Verdicts v:localVerdicts){
				verdict.setGlobalVerdict(v, relaxIndex);
			}
			log.log(Level.INFO, "Whole execution time "+(System.currentTimeMillis()-this.time));
			log.log(Level.INFO, "Test Verdict with index " + relaxIndex
					+ "% is " + verdict.toString());
		}else{
			log.log(Level.INFO, id+" build time "+(this.buildTime));
			log.log(Level.INFO, id+" execution time "+(System.currentTimeMillis()-this.time));
		}
		
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
	public synchronized void endExecution(List<Verdicts> localVerdicts) throws RemoteException{		
		int value= informedByChildren.incrementAndGet();
		log.log(Level.INFO, id+" tester completes. Left "+(tree.getChildren().size()-value)+" children");
		for(Verdicts v:localVerdicts){
			this.localVerdicts.add(v);
		}		
		if(value==tree.getChildren().size()){			
			log.log(Level.INFO, id+" will forward # verdicts "+this.localVerdicts.size());			
			this.notify();			
			informedByChildren.set(0);
		}		
	}

	public int getId()throws RemoteException {
		return id;
	}

	public void setChildren(TreeTester child,TreeTester tester) throws RemoteException {
		if(tester!=null){
			tree.cleanTrace(tester);
		}
		tree.add(child,id);		
		this.amILeaf=false;
	}	
	
	public void setParent(TreeTester parent)throws RemoteException {
		tree.setParent(parent);
	}
	
	private void setupTree(){
		try {
			while(tree.getParent()==null){
				Thread.sleep(150);
			}	
			if(!amIRoot)
				tree.getParent().setChildren(this,null);
			
		} catch (RemoteException e) {				
			log.logStackTrace(e);		
		} catch (InterruptedException e) {
			log.logStackTrace(e);		
		}
	}
	
	private void talkToChildren(){
		for(TreeTester t:tree.getChildren()){
			log.log(Level.INFO, id+" talk to kids "+ t);		
			try {
				t.startExecution();
			} catch (RemoteException e) {
				log.logStackTrace(e);		
			}
		}
	}
	
	private void talkToParent(){	
		log.log(Level.INFO, id+" talk do daddy");	
		try {								
			tree.getParent().endExecution(localVerdicts);
		} catch (RemoteException e) {
			log.logStackTrace(e);		
		}		
	}
	
	/*private void execute(MethodDescription md){

	}*/
	
	private synchronized void dispatch(MethodDescription md) throws InterruptedException, RemoteException {
		log.log(Level.INFO, id+" Dispatching action ");		
		talkToChildren();
		execute(md);		
		this.wait();		
	}
	
	public void kill() {
		killed=true;			
	}
	
	private void verifyTree(){
		if(killed){
			log.log(Level.INFO, id+" Reorganizing tree ");
			for(TreeTester t:tree.getChildren()){								
				try {				
					tree.getParent().setChildren(t,this);
					t.setParent(tree.getParent());
				} catch (RemoteException e) {
					log.logStackTrace(e);		
				}
			}
			log.log(Level.INFO,"Test Case finished by kill ");
			System.exit(0);
		}
	}

	public void execute(MethodDescription md) throws RemoteException {
		log.log(Level.INFO, id+" Executing action");
		invokationThread = new Thread(new Invoke(md));
		invokationThread.start();		
	}
	
	public void export(Class<? extends TestCaseImpl> c) {
		
		try {			
			createLogFiles(c);
			executor = new ExecutorImpl(this,log);			
			log.log(Level.INFO, "Registering actions");	
			testList=executor.register(c);			
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
			v= Verdicts.INCONCLUSIVE;
		} catch (IllegalAccessException e) {
			log.logStackTrace(e);		
			v= Verdicts.INCONCLUSIVE;
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
			} else{
				log.log(Level.INFO," Executed "+md.getName());			
				if(executor.isLastMethod(md.getAnnotation())){
					log.log(Level.FINEST,"Test Case finished by annotation "+md.getAnnotation());			
					log.log(Level.FINEST,"Local verdict "+v);
					localVerdicts.add(v);	
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

	public int getPeerName() throws RemoteException {
		return id;
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
			log.logStackTrace(e);			    
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
			log.logStackTrace(e);			    
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
			log.logStackTrace(e);			    
		}
	}
}
