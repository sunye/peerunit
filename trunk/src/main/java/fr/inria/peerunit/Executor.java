package fr.inria.peerunit;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import fr.inria.peerunit.parser.MethodDescription;
/**
 * This interface is used by a <i>tester</i>for parse a <i>test case</i> in order to get by dynamic
 * reflection the <i>list</i> of instances of <tt>MethodDescription</tt> class corresponding to
 * <i>test case actions</i>. 
 * 
 * @author Eduardo Almeida
 * @author Aboubakar Koita
 * @version 1.0
 * @since 1.0
 * @see fr.inria.peerunit.btree.parser
 * @see fr.inria.peerunit.parser
 */

public interface Executor {
	/**
	 * Parse the class to extract the methods to be executed 
	 * 
	 * @param c the <tt>Class</tt> instance of the <tt>test case</tt> to parse
	 * @return the <i>list</i> of methods to be executed
	 */
	public List<MethodDescription> register(Class<? extends TestCase> c) ;

	/**
	 * Verifies if the method is the last one to be executed by its annotation
	 * 
	 * @param methodAnnotation tha annotation of the method to verify 
	 * @return true if the method is the last one to be executed, false else
	 * @see MethodDescription 
	 */
	public boolean isLastMethod(String md);

    /**
     * Execute the given method description
     *
     * @param md : method description to execute
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    void invoke(MethodDescription md) throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException;

}
