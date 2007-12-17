package fr.inria.peerunit.rmi.tester;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import fr.inria.peerunit.Coordinator;
import fr.inria.peerunit.Parser;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.test.assertion.Assert;
import fr.inria.peerunit.test.oracle.Oracle;
import fr.inria.peerunit.test.oracle.Verdicts;
import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.util.TesterUtil;


public class TesterImpl extends Assert implements Tester, Serializable {

	private static final long serialVersionUID = 1L;

	private Class testClass;

	private static Logger log = Logger.getLogger(TesterImpl.class.getName());

	private Coordinator coord;

	private Object objectClass;

	private  int testerName = -1;

	private boolean stop=false;

	private boolean newMethod=false;
	
	private MethodDescription methodDescription;
	
	private ThreadGroup tg;
	
	private Thread timeoutThread;
	
	private Thread invokationThread=null;
	
	private Parser parser;
	
	private Verdicts v= Verdicts.PASS;
		
	public void run(){			
		while(!stop){
			if(newMethod){
				log.info("[TesterImpl] Lets create Invoke thread ");
				Invoke i=new Invoke(methodDescription);
				invokationThread = new Thread(tg,i);		
				invokationThread.start();
				log.info("[TesterImpl] Lets verify the timeout Invoke thread ");
				if (methodDescription.getTimeout() > 0) {
					timeoutThread = new Thread(tg,new Timeout(invokationThread, methodDescription.getTimeout()));						
					timeoutThread.start();
				}	
				newMethod=false;
				log.finest("Is Invoke thread alive?");				
			}
			try {
				Thread.sleep(TesterUtil.getWaitForMethod());
			} catch (InterruptedException e) {				
				e.printStackTrace();			
			}
		}		
		log.info("Stopping Tester ");		
		System.exit(0);
	}
	
	public void export(Class c) {
		testClass = c;
		boolean exported=false;
		try {

			objectClass = testClass.newInstance();

			Registry registry = LocateRegistry.getRegistry(TesterUtil.getServerAddr());
			UnicastRemoteObject.exportObject(this);

			coord = (Coordinator) registry.lookup("Coordinator");
			testerName = coord.namer(this);

			// Log creation
			FileHandler handler = new FileHandler(TesterUtil.getLogfolder()
					+ "tester" + testerName + ".log");
			handler.setFormatter(new LogFormat());
			log.addHandler(handler);
			
			// Parsing creation
			String parserClass=TesterUtil.getParserClass();
			log.finest("Parsing class used is " + parserClass);	
			parser = (Parser)Class.forName(parserClass).newInstance();
			parser.setPeerName(testerName);
			
			log.info("My name is tester" + testerName);			
			for (MethodDescription m : parser.parse(testClass)) {
				coord.register(this, m);
			}
			log.finest("Registration finished ");
			tg = new ThreadGroup("Thread-group"+testerName);	
			log.finest("Thread-group created ");
			exported=true;
		} catch (RemoteException e) {
			log.severe("RemoteException");
			e.printStackTrace();
		} catch (InstantiationException e) {
			log.severe("InstantiationException");
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			log.severe("IllegalAccessException");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			log.severe("ClassNotFoundException ");
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			if(!exported){
				executionInterrupt(true);
			}
		}
	}

	public synchronized void execute(MethodDescription m)
	throws RemoteException {
		log.info("[TesterImpl] Permission to execute "+m.getName());
		setMethodDescription(m);				
	}

	public int getPeerName() throws RemoteException {
		return testerName;
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
		executionInterrupt(true);		
		log.info("[TesterImpl] Test Case finished by kill ");		
	}	

	private void executionOk(String methodAnnotation) {
		try {
			coord.greenLight();	
			log.info("[TesterImpl] Executed "+methodAnnotation);
			if(parser.isLastMethod(methodAnnotation)){
				log.info("[TesterImpl] Test Case finished by annotation "+methodAnnotation);								
				executionInterrupt(false);	
			}				
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void executionInterrupt(boolean error) {
		try {
			if(v == null){
				v= Verdicts.INCONCLUSIVE;
				error=true;
			}
			
			log.info("[TesterImpl] Test Case local verdict to peer "+testerName+" is "+v.toString());
			coord.quit(this,error,v);			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		stop=true;
	}

	private void setMethodDescription(MethodDescription m){
		newMethod=true;
		this.methodDescription=m;
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
			e.printStackTrace();
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
			e.printStackTrace();
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
			e.printStackTrace();
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
	
	private class Invoke implements Runnable {

		String testCase;
		String annotation;

		public Invoke(MethodDescription md) {
			this.testCase = md.getName();
			this.annotation = md.getAnnotation();
		}

		public void run() {
			boolean error = true;
			log.info("[TesterImpl-Invoke] Peer " + testerName + " executing test case "
					+ testCase + " in " + testClass.getSimpleName());
			Method m=null;
			try {
				m = testClass.getMethod(testCase, (Class[]) null);
				if (testCase.equals(m.getName())) {						
					m.invoke(objectClass, (Object[]) null);
				}
				error = false;
			} catch (SecurityException e1) {
				log.warning("[TesterImpl-Invoke] SecurityException ");
				e1.printStackTrace();
			} catch (NoSuchMethodException e1) {
				log.warning("[TesterImpl-Invoke] NoSuchMethodException ");
				e1.printStackTrace();
			} catch (IllegalArgumentException e) {
				log.warning("[TesterImpl-Invoke] IllegalArgumentException ");
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				log.warning("[TesterImpl-Invoke] IllegalAccessException ");
				e.printStackTrace();			
			}catch (InvocationTargetException e) {
				Oracle oracle=new Oracle(e.getCause());				
				if(v==Verdicts.FAIL){
					log.info("[TesterImpl-Invoke] FAIL Verdict ");					
					error = false;
				}else{
					v=oracle.getVerdict();
					e.printStackTrace();
				}
			}catch (Exception e) {			
				log.warning("[TesterImpl-Invoke] Exception ");
				e.printStackTrace();		
			} finally {
				if (error) {				
					log.warning("[TesterImpl-Invoke] Executed in "+m.getName());
					executionInterrupt(true);		
				} else{
					log.info("[TesterImpl-Invoke] Executed "+testCase);					
					executionOk(annotation);
				}					
			}
		}
	}
}
