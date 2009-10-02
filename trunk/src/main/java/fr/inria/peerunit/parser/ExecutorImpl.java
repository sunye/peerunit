package fr.inria.peerunit.parser;


import fr.inria.peerunit.GlobalVariablesImpl;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.base.AbstractExecutor;
import fr.inria.peerunit.rmi.coord.CoordinatorImpl;
import fr.inria.peerunit.rmi.tester.TesterImpl;
import fr.inria.peerunit.util.TesterUtil;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExecutorImpl extends AbstractExecutor {

	public ExecutorImpl(Tester t, Logger l) {
            super(t,l);
	}


        public static void main(String[] argv) {
        try {
            CoordinatorImpl coord = new CoordinatorImpl(TesterUtil.instance);
            GlobalVariablesImpl globals = new GlobalVariablesImpl();
            TesterImpl tester = new TesterImpl(coord, globals);
            Logger logger = Logger.getLogger(ExecutorImpl.class.getName());
            ExecutorImpl executor = new ExecutorImpl(tester, logger);
            System.out.println(executor);
        } catch (RemoteException ex) {
            Logger.getLogger(ExecutorImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        }

}

