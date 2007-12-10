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
			
			CoordinatorImpl cii = new CoordinatorImpl();
			Coordinator stub = (Coordinator) UnicastRemoteObject.exportObject(cii, 0);
			String servAddr="";
			if(TesterUtil.getServerAddr()==null)
				servAddr=InetAddress.getLocalHost().getHostAddress();
			else
				servAddr=TesterUtil.getServerAddr();
			
			log.info("[CoordinatorImpl] New Coordinator address is : "+servAddr);
			
			// Bind the remote object's stub in the registry
			//Registry registry = LocateRegistry.getRegistry(servAddr,1099);
			Registry registry = LocateRegistry.createRegistry(1099);
			
			//registry.rebind("Coordinator", stub);
			registry.bind("Coordinator", stub);
						
			log.info("[CoordinatorImpl] Object registered and ready");
			Thread updateThread = new Thread(cii, "StockInfoUpdate");
			updateThread.start();			
		} catch (RemoteException e) {
			log.severe("[CoordinatorImpl] I can't export the object");
			e.printStackTrace();
		} catch (UnknownHostException e) {
			log.severe("[CoordinatorImpl] InetAddress can't get the host address");
			e.printStackTrace();
		} catch (SecurityException e) {			
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			// TODO Auto-generated catch block
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
		
		log.info("[CoordinatorImpl] First Peer registration");
		while(regPeers.size() < expectedPeers.intValue()){						

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {				
				e.printStackTrace();
			}
		}		
		
		for (MethodDescription key : testerMap.keySet()){
			log.info("[CoordinatorImpl] EXEC SEQ: "+key.toString());
		}
		
		synchronized (this) {
			TesterSet testerSet=null;
			try {
						
				for (MethodDescription key : testerMap.keySet()){
						
					testerSet= testerMap.get(key);
					
					for (Tester peer : testerSet.getTesters()) {
						if(!peersInError.contains(peer)){
							log.info("[TesterSet] Method "+key.toString()+" Will execute in "+peer.getPeerName());
							executor.submit(new MethodExecute(peer, key));							
						}
					}
					expectedPeers.set(testerSet.getPeersQty());
					
					log.info("[CoordinatorImpl] Waiting to begin the next test "+key.toString());					
					while(redLight())
						Thread.sleep(1000);		
					
					log.info("Internals"+peers.intValue()+" "+expectedPeers.intValue()+" "+peersInError.size()+" "+regPeers.size());
					/*if(regPeers.size()==0){
						log.info("[CoordinatorImpl] Everybody left, I'll break ");
						break;
					}*/
				}		
				// reseting
				log.info("[CoordinatorImpl] Reseting semaphore ");				
				testerMap.clear();
			
				// waiting everyone to execute quit to give the global verdict
				//while((regPeers.size()+verdict.getJudged()) > verdict.getJudged())
				while(regPeers.size()>0){
					log.info("[CoordinatorImpl] Waiting everybody leave to judge ");
					Thread.sleep(200);
				}
				
				log.info("[CoordinatorImpl] Test Verdict with index "+relaxIndex+"% is "+verdict.toString());				
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
			log.info("[CoordinatorImpl] New Registered Peer: "+peerName+" new client " + t);			
		}		
		return peerName;
	}
	
	private boolean redLight(){		
		if(regPeers.size()==0){			
			return false;
		}else	if(peers.intValue() >= (expectedPeers.intValue()-peersInError.size())){			
			log.info("[CoordinatorImpl] Reseting semaphore ");
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
		log.info("[CoordinatorImpl] Test Case local verdict "+localVerdict.toString());
		verdict.setGlobalVerdict(localVerdict, relaxIndex);
		
		if(error){
			peersInError.add(t);
			log.info("[CoordinatorImpl] Tester quits by error "+t.toString());
		}else{			
			log.info("[CoordinatorImpl] Tester quits "+t.toString());			
		}
		log.info("[CoordinatorImpl] Expecting "+regPeers.size());
		log.info("[CoordinatorImpl] Judged "+verdict.getJudged());
	}
	
	public void put(Integer key,Object object)  throws RemoteException {
		log.info("[CoordinatorImpl] Caching global variable key "+key);			
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
