package fr.inria.peerunit.rmi.coord;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.Coordinator;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.test.oracle.GlobalVerdict;
import fr.inria.peerunit.test.oracle.Verdicts;
import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.util.TesterUtil;

public class CoordinatorImpl implements Coordinator , Runnable, Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Map<MethodDescription,TesterSet> testerMap=Collections.synchronizedMap(new TreeMap<MethodDescription,TesterSet>());
	
	private List<Tester> regPeers = Collections.synchronizedList(new ArrayList<Tester>());	
			
	private AtomicInteger expectedPeers=new AtomicInteger(TesterUtil.getExpectedPeers());
	
	private int relaxIndex = TesterUtil.getRelaxIndex();
	
	private int peerName=-1;
	
	private AtomicInteger peers = new AtomicInteger(0);
	
	private static final Logger log = Logger.getLogger(CoordinatorImpl.class.getName());
	
	private List<Tester> peersInError = Collections.synchronizedList(new ArrayList<Tester>());	
		
	private GlobalVerdict verdict= new GlobalVerdict();
	
	private Map<Integer,Object> cacheMap= new ConcurrentHashMap<Integer,Object>();	
	
	private ExecutorService executor = Executors.newFixedThreadPool(10);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			// Log creation
			FileHandler handler = new FileHandler(TesterUtil.getLogfile());
			handler.setFormatter(new LogFormat());
			log.addHandler(handler);			
			log.setLevel(Level.parse(TesterUtil.getLogLevel()));
			
			CoordinatorImpl cii = new CoordinatorImpl();
			Coordinator stub = (Coordinator) UnicastRemoteObject.exportObject(cii, 0);
			String servAddr="";
			if(TesterUtil.getServerAddr()==null)
				servAddr=InetAddress.getLocalHost().getHostAddress();
			else
				servAddr=TesterUtil.getServerAddr();
			
			log.log(Level.INFO,"New Coordinator address is : "+servAddr);
			
			// Bind the remote object's stub in the registry
			Registry registry = LocateRegistry.createRegistry(1099);
			
			//registry.rebind("Coordinator", stub);
			registry.bind("Coordinator", stub);
						
			Thread updateThread = new Thread(cii, "StockInfoUpdate");
			updateThread.start();			
		} catch (RemoteException e) {
			log.log(Level.SEVERE,"RemoteException",e);
			e.printStackTrace();
		} catch (UnknownHostException e) {
			log.log(Level.SEVERE,"UnknownHostException",e);
			e.printStackTrace();
		} catch (SecurityException e) {	
			log.log(Level.SEVERE,"SecurityException",e);
			e.printStackTrace();
		} catch (IOException e) {
			log.log(Level.SEVERE,"IOException",e);
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			log.log(Level.SEVERE,"AlreadyBoundException",e);
			e.printStackTrace();
		}	
	}

	public synchronized void register(Tester t, MethodDescription m) throws RemoteException {			
		synchronized (this) {
			if (!testerMap.containsKey(m)) {
				testerMap.put(m,new TesterSet());						
			}
			testerMap.get(m).add(t);
			if (!regPeers.contains(t)) {
				regPeers.add(t);	
			}					
		}
	}

	public void run() {
		while(regPeers.size() < expectedPeers.intValue()){						
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {				
				e.printStackTrace();
			}
		}		
		
		for (MethodDescription key : testerMap.keySet()){
			log.log(Level.FINEST,"Execution sequence: "+key.toString());
		}
		
		synchronized (this) {
			TesterSet testerSet=null;
			try {
						
				for (MethodDescription key : testerMap.keySet()){
						
					testerSet= testerMap.get(key);
					
					for (Tester peer : testerSet.getTesters()) {
						if(!peersInError.contains(peer)){
							log.log(Level.FINEST,"Peer : "+peer.getPeerName()+" will execute "+key.toString());
							executor.submit(new MethodExecute(peer, key));							
						}
					}
					expectedPeers.set(testerSet.getPeersQty());
					
					log.log(Level.FINEST,"Waiting to begin the next test "+key.toString());					
					while(redLight())
						Thread.sleep(1000);		
				
				}		
				// reseting
				log.log(Level.FINEST,"Reseting semaphore ");				
				testerMap.clear();
			
				// waiting everyone to execute quit to give the global verdict
				while(regPeers.size()>0){
					log.log(Level.FINEST,"Waiting everybody leave to judge ");
					Thread.sleep(200);
				}
				
				log.log(Level.INFO,"Test Verdict with index "+relaxIndex+"% is "+verdict.toString());				
				peers.set(0);
				regPeers.clear();
				executor.shutdown();
				System.exit(0);
			}catch (RemoteException e) {				
				e.printStackTrace();
			}catch (InterruptedException e1) {										
				e1.printStackTrace();
			}		
		}
	}
	/*
	 *  (non-Javadoc)
	 * 	@see callback.Coordinator#namer(callback.Tester)
	 *     Incremented with java.util.concurrent to handle the semaphore concurrency access
	 */
	public synchronized  int namer(Tester t) throws RemoteException {		
		if (t.getPeerName()==-1){
			peerName=peers.getAndIncrement();					
			log.log(Level.FINEST,"New Registered Peer: "+peerName+" new client " + t);			
		}		
		return peerName;
	}
	
	private boolean redLight(){		
		if(regPeers.size()==0){			
			return false;
		}else	if(peers.intValue() >= (expectedPeers.intValue()-peersInError.size())){			
			log.log(Level.FINEST,"Reseting semaphore ");
			peers.set(0);
			return false;
		}		
		else
			return true;
	}
	
	public  void greenLight() throws RemoteException{		
		peers.incrementAndGet();		
	}

	public void quit(Tester t,boolean error,Verdicts localVerdict) throws RemoteException {
		expectedPeers.decrementAndGet();
		regPeers.remove(t);		
		log.log(Level.INFO,"Test Case local verdict "+localVerdict.toString());
		verdict.setGlobalVerdict(localVerdict, relaxIndex);
		
		if(error){
			peersInError.add(t);
			log.log(Level.FINEST,"Tester quits by error "+t.toString());
		}else{			
			log.log(Level.INFO,"Tester finished "+t.toString());			
		}
		log.log(Level.FINEST,"Expecting "+regPeers.size());
		log.log(Level.FINEST,"Judged "+verdict.getJudged());
	}
	
	public void put(Integer key,Object object)  throws RemoteException {
		log.log(Level.FINEST,"Caching global variable key "+key);			
		cacheMap.put(key, object);		
	}
	
	public Object get(Integer key)  throws RemoteException {
		return cacheMap.get(key);
	}
	
	public Map<Integer,Object> getCollection()   throws RemoteException {
		return cacheMap;
	}
	
	public boolean containsKey(Object key)throws RemoteException {
		return cacheMap.containsKey(key);
	}

	public void clearCollection() throws RemoteException {
		cacheMap.clear();
	}
}
