/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.peerunit.rmi.tester;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import fr.inria.peerunit.Bootstrapper;
import fr.inria.peerunit.Coordinator;
import fr.inria.peerunit.GlobalVariables;
import fr.inria.peerunit.MessageType;
import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.base.AbstractTester;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.rmi.coord.CoordinatorImpl;
import fr.inria.peerunit.test.oracle.Verdicts;
import fr.inria.peerunit.util.TesterUtil;

/**
 * The DistributedTester is both, a Tester and a Coordinator.
 * As a Tester, it has a Coordinator, it registers a test case and executes 
 * test steps when requested by its Coordinator.
 * 
 * As a coordinator, it accepts the registration of several testers
 * and asks its testers to execute test steps.
 * 
 * @author sunye
 */
public class DistributedTester extends AbstractTester implements Tester, Coordinator {
    
	private static Logger LOG = Logger.getLogger(TesterImpl.class.getName());
	
	/**
	 * The coordinator of this tester. Since DistributedTester is used in
	 * a distributed architecture, the coordinator is also a DistributedTester.
	 * 
	 */
	private Coordinator parent;
	
	/**
	 * Set of testers that are coordinated by this tester.
	 */
	private List<Tester> testers = new LinkedList<Tester>();

	/**
	 * Set of testers that have registered their methods with this tester.
	 */
	private List<Tester> registeredTesters = Collections.synchronizedList(new LinkedList<Tester>());
	
	private TesterImpl tester;
	
	private CoordinatorImpl coordinator;
	
	private TesterUtil defaults;
	
    public DistributedTester(Bootstrapper boot, GlobalVariables gv, TesterUtil tu) throws RemoteException {
        super(gv);
        int id = boot.register(this);
        this.setId(id);
        defaults = tu;
        
        this.tester = new TesterImpl(this.globalTable(), this.getId(), defaults);
        this.testers.add(tester);

    }

	/** 
	 * Sets the testers that are controlled by this tester and
	 * informs the tester that this tester is their controller
	 * 
	 * @see fr.inria.peerunit.Coordinator#registerTesters(java.util.List)
	 */
	public void registerTesters(List<Tester> testers) throws RemoteException {
		assert testers != null;
		
		this.testers.addAll(testers);
		for(Tester each : testers) {
			each.setCoordinator(this);
		}
	}

	/**
	 * 
	 */
	private void startCoordination() {
		this.coordinator = new CoordinatorImpl(testers.size(),defaults.getRelaxIndex());
	}
    
    
    /** 
     * @see fr.inria.peerunit.Coordinator#registerMethods(fr.inria.peerunit.Tester, java.util.List)
     */
    public void registerMethods(Tester tester, Collection<MethodDescription> list) throws RemoteException {
    	assert testers.contains(tester);
    	
		coordinator.registerMethods(tester, list);
		
    }

	/**
	 * @throws InterruptedException 
	 * @see fr.inria.peerunit.Tester#execute(fr.inria.peerunit.parser.MethodDescription)
	 */
	public void execute(MethodDescription md) throws RemoteException {
		
		try {
			coordinator.execute(md);
			
			// TODO retrieve information about method execution,
			// before sending a OK to parent !
			
			parent.methodExecutionFinished(this, MessageType.OK);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    /**
     * @see fr.inria.peerunit.Coordinator#methodExecutionFinished(Tester, fr.inria.peerunit.MessageType)
     */
    public void methodExecutionFinished(Tester tester, MessageType message) throws RemoteException {
    	assert coordinator != null : "Null Coordinator";
    	
    	coordinator.methodExecutionFinished(tester, message);
    }

    /** 
     * @see fr.inria.peerunit.Coordinator#quit(fr.inria.peerunit.Tester, fr.inria.peerunit.test.oracle.Verdicts)
     */
    public void quit(Tester t, Verdicts v) throws RemoteException {
    	assert parent != null : "Null Coordinator";
    	
    	coordinator.quit(t, v);
    }



	/** 
	 * @see fr.inria.peerunit.Tester#kill()
	 */
	public void kill() throws RemoteException {
		for(Tester each : testers) {
			each.kill();
		}
		
	}

	
	/** 
	 * @see fr.inria.peerunit.Tester#setCoordinator(fr.inria.peerunit.Coordinator)
	 */
	public void setCoordinator(Coordinator coord) {
		this.parent = coord;
	}
	
	
	/**
	 * Sets the test case class.
	 * 
	 * @param klass The test case class to execute
	 */
	public void setTestCaseClass(Class<? extends TestCaseImpl> klass) {
		tester.export(klass);
	}

	
	
	/**
	 * Starts the distributed tester:
	 * 		- 
	 * @throws RemoteException
	 * @throws InterruptedException 
	 */
	public void start() throws RemoteException {
		assert parent != null;
		
		LOG.entering("DistributedTester", "start()");
		
		for(Tester each : testers) {
			each.start();
		}
		try {
			this.startCoordination();
			coordinator.waitForTesterRegistration();
			this.registerWithCoordinator();
			
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			LOG.exiting("DistributedTester", "start()");
		}

	}

	
	
	private void registerWithCoordinator() throws RemoteException {
		assert parent != null;
		assert registeredTesters.size() == testers.size();
		
		Set<MethodDescription> methods = coordinator.getTesterMap().keySet();
		parent.registerMethods(this, methods);
	}
    
}
