/**
 *
 */
package fr.inria.peerunit;

/**
 * The <i>test cases</i> interface. This interface must be implemented by the 
 * testing engineer  wants to write a <i>test case</i>, it allow to access the 
 * <i>tester</i> who will execute the  <i>test case</i>.
 *  
 * @author sunye
 * @author Aboubakar Koïta 
 * @version 1.0
 * @since 1.0
 * @see fr.inria.peerunit.TestCaseImpl  
 **/
public interface TestCase {

//	public void setTester(TesterImpl ti);


	/**
	 * For set the reference to the <i>tester</i> which  will execute the  <i>test case</i>
	 * in distributed  architecture. 
	 * 
	 * @param t the  reference of the subjacent <i>tester</i>
	 */		
	public void setTester(Tester t);
	
}
