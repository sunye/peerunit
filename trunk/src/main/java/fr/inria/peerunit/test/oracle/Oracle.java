/*
    This file is part of PeerUnit.

    Foobar is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    PeerUnit is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with PeerUnit.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.inria.peerunit.test.oracle;

import fr.inria.peerunit.test.assertion.ArrayComparisonFailure;
import fr.inria.peerunit.test.assertion.AssertionFailedError;
import fr.inria.peerunit.test.assertion.ComparisonFailure;
import fr.inria.peerunit.test.assertion.InconclusiveFailure;

/**
 * 
 * @author Eduardo Almeida, Thomas Cerqueus
 * @version 1.1
 * @since 1.0
 *
 */

public class Oracle {

	private Verdicts verdict;
	private boolean isFailure = false;
	
	/**
	 * Calculates the verdict of a incorrect test. 
	 * 
	 * @param t represents the exception threw during the test.
	 * @since 1.0 
	 * 
	 */
	public Oracle(Throwable t){
		verdict = peerUnitException(t);
	}
	
	/**
	 * Determines if the test failed. 
	 * 
	 * @since 1.0
	 */
	public boolean isPeerUnitFailure(){
		return isFailure;	
	}
	
	/**
	 * Returns the verdict. 
	 * 
	 * @return the verdict. Possible values are <code>FAIL</code> or <code>INCONCLUSIVE</code>. 
	 * @since 1.0
	 */
	public Verdicts getVerdict(){
		return verdict;
	}
	
	private Verdicts peerUnitException(Throwable t){
		if(t instanceof InconclusiveFailure) {
			isFailure=true;
			return Verdicts.INCONCLUSIVE;
		} else if(t instanceof ArrayComparisonFailure) {
			isFailure=true;
			return Verdicts.FAIL;
		} else if(t instanceof AssertionFailedError) {
			isFailure=true;
			return Verdicts.FAIL;
		} else if(t instanceof ComparisonFailure) {
			isFailure=true;
			return Verdicts.FAIL;
		} else if(t instanceof AssertionError) {
			isFailure=true;
			return Verdicts.FAIL;
		} else {
			return Verdicts.INCONCLUSIVE;
		}
	}
}