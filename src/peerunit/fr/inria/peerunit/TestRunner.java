/**
 *
 */
package fr.inria.peerunit;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import fr.inria.peerunit.rmi.tester.TesterImpl;
import fr.inria.peerunit.util.TesterUtil;

/**
 * @author sunye
 *
 * Main class. Should be used to execute all test cases.
 *
 */
public class TestRunner {

	/**
	 * The test case that will be excuted and those name was
	 * passed at the command line.
	 */
	private Class <? extends TestCase> testcase;

	/**
	 * The tester, which should communicate with the Coordinator and
	 * control the test case execution.
	 */
	private TesterImpl tester;


	public TestRunner(Class <? extends TestCase> klass) {
		testcase = klass;
		tester = new TesterImpl();
		tester.export(klass);
		tester.run();
	}

	/**
	 * @param args The only argument should be a class name.
	 */

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: java TestRunner <Test Case Class>");
		} else {
			String name = args[0];
			try {
				Class<?> klass = Class.forName(name);
				Class<? extends TestCase> tklass = klass.asSubclass(TestCase.class);
				new TestRunner(tklass);
			} catch (ClassCastException e) {
				System.out.println("Error: Class "+name+ " should implement TestCase interface.");
			} catch (ClassNotFoundException e) {
				System.out.println("Error: Class "+name+ " not found.");
			}
		}
	}

}
