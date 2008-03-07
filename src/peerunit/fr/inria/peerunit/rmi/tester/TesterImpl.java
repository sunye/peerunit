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
import fr.inria.peerunit.TestCase;
import fr.inria.peerunit.TestCaseImpl;
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

	private Class<? extends TestCase> testClass;

	private static Logger LOG = Logger.getLogger(TesterImpl.class.getName());

	private static Logger PEER_LOG;

	private Coordinator coord;

	private TestCase testcase;

	private  int id = -1;

	private boolean stop=false;

	private boolean newMethod=false;

	private MethodDescription methodDescription;

	private Thread timeoutThread;

	private Thread invokationThread=null;

	private Parser parser;

	private Verdicts v= Verdicts.PASS;


	public TesterImpl() {
		try {
			Registry registry = LocateRegistry.getRegistry(TesterUtil.getServerAddr());
			UnicastRemoteObject.exportObject(this);
			coord = (Coordinator) registry.lookup("Coordinator");

			} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}


	public void setId(int i) {
		id = i;
	}

	public void run(){
		assert id >= 0;

		while(!stop){
			if(newMethod){
				try {
					LOG.log(Level.FINEST,"Creating Invoke thread ");
					Invoke i = new Invoke(methodDescription);
					invokationThread = new Thread(i);
					invokationThread.start();
					LOG.log(Level.FINEST,"Verify the timeout of the Invoke thread ");
				} catch (RuntimeException e) {
					e.printStackTrace();
				}
				if (methodDescription.getTimeout() > 0) {
					timeoutThread = new Thread(new Timeout(invokationThread, methodDescription.getTimeout()));
					timeoutThread.start();
				}
				newMethod=false;
				LOG.log(Level.FINEST,"Is Invoke thread alive?");
			}
			try {
				Thread.sleep(TesterUtil.getWaitForMethod());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		LOG.log(Level.INFO,"Stopping Tester ");
		System.exit(0);
	}

	public void export(Class<? extends TestCaseImpl> c) {
		testClass = c;
		boolean exported=false;
		try {
			id = coord.getNewId(this);
			testcase = testClass.newInstance();
			testcase.setId(id);
			testcase.setTester(this);

			// Create the peer (sut) logger file
			PEER_LOG = Logger.getLogger(testClass.getName());
			FileHandler phandler = new FileHandler(TesterUtil.getLogfolder()+"/" + testClass.getName()+ ".log.peer"+id,true);
			phandler.setFormatter(new LogFormat());
			PEER_LOG.addHandler(phandler);

			// Create the tester logger file
			FileHandler handler = new FileHandler(TesterUtil.getLogfolder()
					+ "tester" + id + ".log");
			handler.setFormatter(new LogFormat());
			LOG.addHandler(handler);

			LOG.setLevel(Level.parse(TesterUtil.getLogLevel()));

			// Parsing creation
			//String parserClass=TesterUtil.getParserClass();
			//log.log(Level.FINEST,"Parsing class used is " + parserClass);
			// parser = (Parser)Class.forName(parserClass).newInstance();
			// parser.setPeerName(testerName);
			// parser.setLogger(log);

			parser  = new ParserImpl(id, LOG);

			LOG.log(Level.INFO,"My name is tester" + id);


			coord.register(this, parser.parse(testClass));

			LOG.log(Level.FINEST,"Registration finished ");
			LOG.log(Level.FINEST,"Thread-group created ");
			exported=true;
		} catch (RemoteException e) {
			LOG.log(Level.SEVERE,"RemoteException",e);
			e.printStackTrace();
		} catch (InstantiationException e) {
			LOG.log(Level.SEVERE,"InstantiationException",e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			LOG.log(Level.SEVERE,"IllegalAccessException",e);
			e.printStackTrace();
		} catch (SecurityException e) {
			LOG.log(Level.SEVERE,"SecurityException ",e);
			e.printStackTrace();
		} catch (IOException e) {
			LOG.log(Level.SEVERE,"IOException ",e);
			e.printStackTrace();
		} finally{
			if(!exported){
				executionInterrupt(true);
			}
		}
	}

	public synchronized void execute(MethodDescription m)
	throws RemoteException {
		LOG.log(Level.FINEST,"Permission to execute "+m.getName());
		setMethodDescription(m);
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
		executionInterrupt(true);
		LOG.log(Level.INFO,"Test Case finished by kill ");
	}

	private void executionOk(String methodAnnotation) {
		try {
			coord.greenLight();
			LOG.log(Level.FINEST,"Executed "+methodAnnotation);
			if(parser.isLastMethod(methodAnnotation)){
				LOG.log(Level.FINEST,"Test Case finished by annotation "+methodAnnotation);
				executionInterrupt(false);
			}
		} catch (RemoteException e) {
			LOG.log(Level.SEVERE,"RemoteException ",e);
			e.printStackTrace();
		}
	}

	private void executionInterrupt(boolean error) {
		try {
			if(v == null){
				v= Verdicts.INCONCLUSIVE;
				error=true;
			}

			LOG.log(Level.INFO,"Test Case local verdict to peer "+id+" is "+v.toString());
			coord.quit(this,error,v);
		} catch (RemoteException e) {
			LOG.log(Level.SEVERE,"RemoteException ",e);
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
			LOG.log(Level.SEVERE,"RemoteException ",e);
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
			LOG.log(Level.SEVERE,"RemoteException ",e);
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
			LOG.log(Level.SEVERE,"RemoteException ",e);
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
			LOG.log(Level.INFO,"Peer " + id + " executing test case "
					+ testCase + " in " + testClass.getSimpleName());
			Method m=null;
			try {
				m = testClass.getMethod(testCase, (Class[]) null);
				if (testCase.equals(m.getName())) {
					m.invoke(testcase, (Object[]) null);
				}
				error = false;
			} catch (SecurityException e) {
				LOG.log(Level.SEVERE,"SecurityException ",e);
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				LOG.log(Level.SEVERE,"NoSuchMethodException ",e);
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				LOG.log(Level.SEVERE,"IllegalArgumentException ",e);
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				LOG.log(Level.SEVERE,"IllegalAccessException ",e);
				e.printStackTrace();
			}catch (InvocationTargetException e) {
				Oracle oracle=new Oracle(e.getCause());
				if(v==Verdicts.FAIL){
					LOG.log(Level.SEVERE,"FAIL Verdict ",e);
					error = false;
				}else{
					v=oracle.getVerdict();
					e.printStackTrace();
				}
			}catch (Exception e) {
				LOG.log(Level.SEVERE,"Exception ",e);
				e.printStackTrace();
			} finally {
				if (error) {
					LOG.log(Level.WARNING," Executed in "+m.getName());
					executionInterrupt(true);
				} else{
					LOG.log(Level.INFO," Executed "+testCase);
					executionOk(annotation);
				}
			}
		}
	}
}
