package fr.inria.peerunit.btree.parser;


import fr.inria.peerunit.Tester;
import fr.inria.peerunit.base.AbstractExecutor;
import java.util.logging.Logger;

@Deprecated
public class ExecutorImpl extends AbstractExecutor {

	//private TreeTesterImpl tester;
	//private Class<? extends TestCase> c;

	/**
	 * Creates an ExecutorImpl
	 * @param tester : TesterImpl
	 */
	public ExecutorImpl(Tester tester, Logger l){
		super(tester, l);
	}

}

