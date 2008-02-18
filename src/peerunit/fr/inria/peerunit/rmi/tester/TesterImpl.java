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
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.Coordinator;
import fr.inria.peerunit.Parser;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.parser.ParserImpl;
import fr.inria.peerunit.test.assertion.Assert;
import fr.inria.peerunit.test.oracle.Oracle;
import fr.inria.peerunit.test.oracle.Verdicts;
import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.util.TesterUtil;


public class TesterImpl extends Assert implements Tester, Serializable {

	private static final long serialVersionUID = 1L;

	private Class<? extends Tester> testClass;

	private static Logger log = Logger.getLogger(TesterImpl.class.getName());

	private Coordinator coord;

	private Object objectClass;

	private  int testerName = -1;

	private boolean stop=false;

	private boolean newMethod=false;

	private MethodDescription methodDescription;

	private Thread timeoutThread;

	private Thread invokationThread=null;

	private Parser parser;

	private Verdicts v= Verdicts.PASS;

	public void run(){
		while(!stop){
			if(newMethod){
				try {
					log.log(Level.FINEST,"Creating Invoke thread ");
					Invoke i = new Invoke(methodDescription);
					invokationThread = new Thread(i); 
					invokationThread.start();
					log.log(Level.FINEST,"Verify the timeout of the Invoke thread ");
				} catch (RuntimeException e) {
					e.printStackTrace(); 
				}
				if (methodDescription.getTimeout() > 0) {
					timeoutThread = new Thread(new Timeout(invokationThread, methodDescription.getTimeout()));
					timeoutThread.start();
				}
				newMethod=false;
				log.log(Level.FINEST,"Is Invoke thread alive?");
			}
			try {
				Thread.sleep(TesterUtil.getWaitForMethod());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		log.log(Level.INFO,"Stopping Tester ");
		System.exit(0);
	}

	public void export(Class<? extends Tester> c) {
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

			log.setLevel(Level.parse(TesterUtil.getLogLevel()));

			// Parsing creation
			//String parserClass=TesterUtil.getParserClass();
			//log.log(Level.FINEST,"Parsing class used is " + parserClass);
			// parser = (Parser)Class.forName(parserClass).newInstance();
			// parser.setPeerName(testerName);
			// parser.setLogger(log);

			parser  = new ParserImpl(testerName, log);

			log.log(Level.INFO,"My name is tester" + testerName);
			for (MethodDescription m : parser.parse(testClass)) {
				coord.register(this, m);
			}
			log.log(Level.FINEST,"Registration finished ");			
			log.log(Level.FINEST,"Thread-group created ");
			exported=true;
		} catch (RemoteException e) {
			log.log(Level.SEVERE,"RemoteException",e);
			e.printStackTrace();
		} catch (InstantiationException e) {
			log.log(Level.SEVERE,"InstantiationException",e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			log.log(Level.SEVERE,"IllegalAccessException",e);
			e.printStackTrace();
		} catch (SecurityException e) {
			log.log(Level.SEVERE,"SecurityException ",e);
			e.printStackTrace();
		} catch (IOException e) {
			log.log(Level.SEVERE,"IOException ",e);
			e.printStackTrace();
		} catch (NotBoundException e) {
			log.log(Level.SEVERE,"NotBoundException ",e);
			e.printStackTrace();
		} finally{
			if(!exported){
				executionInterrupt(true);
			}
		}
	}

	public synchronized void execute(MethodDescription m)
	throws RemoteException {
		log.log(Level.FINEST,"Permission to execute "+m.getName());
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
		log.log(Level.INFO,"Test Case finished by kill ");
	}

	private void executionOk(String methodAnnotation) {
		try {
			coord.greenLight();
			log.log(Level.FINEST,"Executed "+methodAnnotation);
			if(parser.isLastMethod(methodAnnotation)){
				log.log(Level.FINEST,"Test Case finished by annotation "+methodAnnotation);
				executionInterrupt(false);
			}
		} catch (RemoteException e) {
			log.log(Level.SEVERE,"RemoteException ",e);
			e.printStackTrace();
		}
	}

	private void executionInterrupt(boolean error) {
		try {
			if(v == null){
				v= Verdicts.INCONCLUSIVE;
				error=true;
			}

			log.log(Level.INFO,"Test Case local verdict to peer "+testerName+" is "+v.toString());
			coord.quit(this,error,v);
		} catch (RemoteException e) {
			log.log(Level.SEVERE,"RemoteException ",e);
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
			log.log(Level.SEVERE,"RemoteException ",e);
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
			log.log(Level.SEVERE,"RemoteException ",e);
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
			log.log(Level.SEVERE,"RemoteException ",e);
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
			log.log(Level.INFO,"Peer " + testerName + " executing test case "
					+ testCase + " in " + testClass.getSimpleName());
			Method m=null;
			try {
				m = testClass.getMethod(testCase, (Class[]) null);				
				if (testCase.equals(m.getName())) {					
					m.invoke(objectClass, (Object[]) null);
				}
				error = false;
			} catch (SecurityException e) {
				log.log(Level.SEVERE,"SecurityException ",e);
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				log.log(Level.SEVERE,"NoSuchMethodException ",e);
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				log.log(Level.SEVERE,"IllegalArgumentException ",e);
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				log.log(Level.SEVERE,"IllegalAccessException ",e);
				e.printStackTrace();
			}catch (InvocationTargetException e) {
				Oracle oracle=new Oracle(e.getCause());
				if(v==Verdicts.FAIL){
					log.log(Level.SEVERE,"FAIL Verdict ",e);
					error = false;
				}else{
					v=oracle.getVerdict();
					e.printStackTrace();
				}
			}catch (Exception e) {
				log.log(Level.SEVERE,"Exception ",e);
				e.printStackTrace();
			} finally {
				if (error) {
					log.log(Level.WARNING," Executed in "+m.getName());
					executionInterrupt(true);
				} else{
					log.log(Level.INFO," Executed "+testCase);
					executionOk(annotation);
				}
			}
		}
	}
}
