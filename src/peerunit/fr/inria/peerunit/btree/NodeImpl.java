package fr.inria.peerunit.btree;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.btree.parser.ExecutorImpl;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.util.PeerUnitLogger;
import fr.inria.peerunit.util.TesterUtil;

public class NodeImpl  implements Node,Serializable,Runnable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger PEER_LOG;
	
	private List<TreeTester> testers=new ArrayList<TreeTester>();	
	
	private List<MethodDescription> testList = new ArrayList<MethodDescription>();
	
	final private Bootstrapper boot;
	
	private ExecutorImpl executor;

	private static PeerUnitLogger log = new PeerUnitLogger(NodeImpl.class
			.getName());
	
	public int id;
	
	private boolean amIRoot=false;
	
	private TreeElements tree= new TreeElements();
	
	String logFolder = TesterUtil.getLogfolder();
	
	public NodeImpl( Bootstrapper b) throws RemoteException {
		boot=b;
		UnicastRemoteObject.exportObject(this);	
		id=boot.register(this);
		if(id==0)
			amIRoot=true;
		
		System.out.println("Log file to use : "+logFolder+ "/tester" + id + ".log");
		log.createLogger(logFolder+ "/tester" + id + ".log");	
		log.log(Level.INFO, "Log file to use : "+logFolder+ "/tester" + id + ".log");
		log.log(Level.INFO, "My ID is: "+id);		
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
			
	public void run() {
		while(true){
			synchronized (this) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}			
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
	public void send(MessageType message) throws RemoteException {
		if (message.equals(MessageType.OK)) {		
			// way up
			
			/**
			 * now EXECUTE, ERROR and FAIL messages
			 */
		}else {
			for(TreeTester t:testers){
				t.inbox(message);		
			}	
			if (message.equals(MessageType.EXECUTE)) {			
				// way down
			}
		}			
	}

	public void setElements(BTreeNode bt,TreeElements tree) throws RemoteException {		
				
		log.log(Level.INFO, "id "+id+" bt "+bt+" tree "+tree);
		
		/**
		 * Using bt Node acknowledge the testers it must control, then start them
		 */
		for(Comparable key:bt.keys){
			if(key != null){				
				TreeTester t=new TreeTesterImpl(new Integer(key.toString()));
				t.run();
				testers.add(t);	
			}
		}
				
		/**
		 * Children are set. Now Node can talk to them to set parents.
		 */
		this.tree=tree;
		for(Node node:tree.getChildren()){
			node.setParent(this);			
		}
		this.notify();
	}
	public void setParent(Node parent)throws RemoteException {
		tree.setParent(parent);
	}
	
	public int getId(){
		return id;
	}
	
	public String toString(){
		return "Node id: "+id;
	}
}
