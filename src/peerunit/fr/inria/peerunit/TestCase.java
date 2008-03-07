/**
 *
 */
package fr.inria.peerunit;

import fr.inria.peerunit.rmi.tester.TesterImpl;

/**
 * @author sunye
 *
 * Common interface for all test cases.
 *
 */
public interface TestCase {

	public void setId(int i);

	public void setTester(TesterImpl ti);
}
