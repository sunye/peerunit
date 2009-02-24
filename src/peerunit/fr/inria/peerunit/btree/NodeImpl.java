package fr.inria.peerunit.btree;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.btree.parser.ExecutorImpl;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.test.oracle.GlobalVerdict;
import fr.inria.peerunit.test.oracle.Verdicts;
import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.util.TesterUtil;

public class NodeImpl  implements Node,Serializable,Runnable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Vector<TreeTesterImpl> testers=new Vector<TreeTesterImpl>();	
	
	private List<MethodDescription> testList = new ArrayList<MethodDescription>();
	
	private Bootstrapper boot;
	
	private ExecutorImpl executor;

	private static Logger log;
	
	public int id;
	
	private boolean amIRoot=false;
	
	private boolean amILeaf=false;
	
	private boolean isLastMethod=false;
	
	int numberOfChildren=0;
	
	private TreeElements tree= new TreeElements();
	
	String logFolder = TesterUtil.getLogfolder();
		
	BTreeNode bt;
		
	private AtomicInteger childrenTalk = new AtomicInteger(0);
	
	private Long time;
		
	MethodDescription mdToExecute;
	
	int treeWaitForMethod=TesterUtil.getTreeWaitForMethod();
	
	Class<? extends TestCaseImpl> klass;
	
	private List<Verdicts> localVerdicts=new Vector<Verdicts>();
	
	public NodeImpl( Bootstrapper b) throws RemoteException {
		boot=b;
		UnicastRemoteObject.exportObject(this);	
		id=boot.register(this);
		if(id==0)
			amIRoot=true;
		else if (id==Integer.MAX_VALUE) {
			/**
			 * All Nodes are taken
			 */
			System.exit(0);			
		}
		
		System.out.println("Log file to use : "+logFolder+ "/Node" + id + ".log");		
		
		/**
		 * Creating logfile
		 */
		LogFormat format = new LogFormat();
		Level level = Level.parse(TesterUtil.getLogLevel());		
					
		String logFolder = TesterUtil.getLogfolder();
		log = Logger.getLogger(NodeImpl.class.getName());
		FileHandler phandler;
		try {
			phandler = new FileHandler(logFolder+ "/Node" + id + ".log",true);
			phandler.setFormatter(format);
			log.addHandler(phandler);
			log.setLevel(level);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void export(Class<? extends TestCaseImpl> c) {
		
		try {			
			log.log(Level.INFO, "[NodeImpl] Registering actions");			
			executor = new ExecutorImpl();				
			testList=executor.register(c);		
			klass=c;
		} catch (SecurityException e) {
			log.log(Level.SEVERE,e.toString());
		} 
	}
			
	public void run() {
		/**
		 * Now starting the Testers
		 */
		startTesters();			
		if(amIRoot){	
			try {
				Thread.sleep(TesterUtil.getWaitForMethod());
			} catch (InterruptedException e) {			
				log.log(Level.SEVERE,e.toString());
			}
		}
		this.time=System.currentTimeMillis();
		log.log(Level.FINEST, "[NodeImpl] START EXECUTION ");
		for(MethodDescription md:testList){		
			mdToExecute=md;
			log.log(Level.FINEST, "[NodeImpl] METHOD "+mdToExecute);			
			try {					
				if(amIRoot){
					log.log(Level.FINEST, "[NodeImpl] Start action ");
					dispatch();
				}else{		
					/**
					 * Wait for parent
					 */
					synchronized(this){
						this.wait();
					}
					log.log(Level.FINEST, "[NodeImpl] I'm about to execute "+md);			
					if(!amILeaf){
						dispatch();
					}else{
						execute();						
					}					
					talkToParent();					
				}				
			} catch (InterruptedException e) {				
				log.log(Level.SEVERE,e.toString());
			} 		
		}
		log.log(Level.INFO, "Whole execution time "+(System.currentTimeMillis()-this.time));
		if(amIRoot){
			GlobalVerdict verdict = new GlobalVerdict();
			for(Verdicts v:localVerdicts){
				verdict.setGlobalVerdict(v, TesterUtil.getRelaxIndex());
			}
			log.log(Level.INFO, "Final verdict "+verdict);
		}
		System.exit(0);	
	}
	
	private void dispatch() throws InterruptedException {
		log.log(Level.INFO, id+"[NodeImpl] Dispatching action "+mdToExecute);		
		talkToChildren();		
		execute();		
		/**
		 * Wait for children
		 */
		synchronized(this){
			this.wait();
		}	
	}
	
	private void execute(){		
		for(TreeTesterImpl t:testers){
			log.log(Level.INFO, id+"[NodeImpl] Tester "+t.getID()+" Executing action "+mdToExecute);
			synchronized(t){
				t.inbox(mdToExecute);
			}
			if(t.isLastMethod()){
				isLastMethod=t.isLastMethod();
				localVerdicts.add(t.getVerdict());
			}
		}					
	}
	
	private void talkToChildren(){		
		for(Node child:tree.getChildren()){
			log.log(Level.FINEST, id+"[NodeImpl] talk to kids "+ child);
			log.log(Level.FINEST, id+"[NodeImpl] Sending them "+ mdToExecute);		
			try {
				/**
				 * Talk to children
				 */
				child.send(MessageType.EXECUTE,mdToExecute);
			} catch (RemoteException e) {
				log.log(Level.SEVERE,e.toString());
			}
		}
	}
	
	private void talkToParent(){	
		log.log(Level.FINEST, id+"[NodeImpl] talk do daddy");	
		try {					
			/**
			 * Talk to parent
			 */
			Thread.sleep(treeWaitForMethod);
			if(isLastMethod){
				tree.getParent().sendVerdict(localVerdicts);
			}
			tree.getParent().send(MessageType.OK,mdToExecute);	
		} catch (RemoteException e) {
			log.log(Level.SEVERE,e.toString());
		} catch (InterruptedException e) {
			log.log(Level.SEVERE,e.toString());
		}		
	}	

	/**
	 * Receive a message from another Node.
	 * OK are sent only to Nodes (way up the tree)
	 * EXECUTE are sent to both Testers and Nodes (way down the tree)
 	 * FAIL and ERROR are sent only to Testers
 	 * REGISTER used by Testers to get their ID and by Nodes to store their Testers
	 * @param t
	 * @param message
	 * @throws RemoteException
	 */
	public void send(MessageType message,MethodDescription mdToExecute) throws RemoteException {
		log.log(Level.FINEST, id+"[NodeImpl] Daddy asked me to execute "+ mdToExecute);		
		this.mdToExecute=mdToExecute;
		/**
		 * Way up 
		 */
		int talked;
		if (message.equals(MessageType.OK)) {
			talked=childrenTalk.incrementAndGet();
			log.log(Level.FINEST, id+"[NodeImpl]  I finished the execution. Waiting "+
					((numberOfChildren-talked)+1)+" of my "+numberOfChildren+" children ");			
			
			/**
			 * I have to wait for my children 
			 */			
			if(talked==numberOfChildren){
				synchronized (this) {
					this.notify();
				}
				childrenTalk.set(0);
			}
			
			
			/**
			 * now EXECUTE messages
			 */
		}else {
			/**
			 * Way down
			 */
			if (message.equals(MessageType.EXECUTE)) {				
				log.log(Level.FINEST, id+"[NodeImpl]  I'm about to execute.");		
				synchronized (this) {
					this.notify();	
				}						
			}
		}			
	}
	
	public void sendVerdict(List<Verdicts> localVerdicts) throws RemoteException {
		for(Verdicts l:localVerdicts){
			this.localVerdicts.add(l);
		}
	}

	public void setElements(BTreeNode bt,TreeElements tree) throws RemoteException {		
		log.log(Level.FINEST, "[NodeImpl] id "+id+" bt "+bt+" tree "+tree);		
		this.tree=tree;
		this.bt=bt;		
		for(BTreeNode child:this.bt.children){
			if(child!=null)
				numberOfChildren++;
		}
		log.log(Level.FINEST, "[NodeImpl] I have these number of children: "+numberOfChildren);
		
		amILeaf=bt.isLeaf();
		synchronized (this) {
			this.notify();	
		}		
	}
	
	public int getId(){
		return id;
	}	
	
	public String toString(){
		return "Node id: "+id;
	}
	
	private synchronized void startTesters(){
		/**
		 * Initially we wait for the tree construction
		 */
		try {
			this.wait();
		} catch (InterruptedException e) {
			log.log(Level.SEVERE,e.toString());
		}				
		
		log.log(Level.INFO, "[NodeImpl] Starting "+bt.keys+" Testers ");
		/**
		 * Using bt Node acknowledge the testers it must control, then start them
		 */		
		
		for(Comparable key:bt.keys){			
			if(key != null){
				int peerID=new Integer(key.toString());				
				log.log(Level.FINEST, "[NodeImpl] Tester "+key.toString());				
				testers.add(new TreeTesterImpl(peerID,boot));
			}
		}	
		
		/**
		 * Let's start testers
		 */		
		for(TreeTesterImpl t:testers){
			log.log(Level.FINEST, "[NodeImpl] Starting Tester "+t);
			t.setClass(klass);
			new Thread(t).start();
		}
		
		log.log(Level.FINEST, "[NodeImpl] Testers added: "+testers.size());
	}
}