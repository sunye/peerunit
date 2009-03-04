package fr.inria.peerunit.test.oracle;

/**
 * 
 * @author Eduardo Almeida
 * 
 * This enumeration represents the different results returned by a test. 
 * 
 */

public enum Verdicts {
	/**
	 * The test has been correctly executed.
	 */
	PASS, 
	/**
	 * The test failed.
	 */
	FAIL,
	/**
	 * The test didn't failed but we can't be sure it works.
	 */
	INCONCLUSIVE, 
	/**
	 * The test has not been executed correctly : it's not possible to determine if the test is <code>PASS</code>, <code>FAIL</code> or <code>INCONCLUSIVE</code>. 
	 */
	ERROR;
}
