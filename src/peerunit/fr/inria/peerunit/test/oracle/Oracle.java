package fr.inria.peerunit.test.oracle;

import fr.inria.peerunit.exception.PeerUnitFailure;
import fr.inria.peerunit.test.assertion.ArrayComparisonFailure;
import fr.inria.peerunit.test.assertion.AssertionFailedError;
import fr.inria.peerunit.test.assertion.ComparisonFailure;
import fr.inria.peerunit.test.assertion.InconclusiveFailure;

public class Oracle {

	Verdicts verdict;

	InconclusiveFailure incFailure=new InconclusiveFailure();
	
	Throwable throwable;
	
	public Oracle(Throwable t){
		throwable=t;
		verdict = peerUnitException();
	}
	
	public boolean isPeerUnitFailure(){
		return throwable instanceof PeerUnitFailure;
	}
	
	private Verdicts peerUnitException(){
		/**
		 * PeerUnit 
		 */
		if(throwable instanceof InconclusiveFailure) {
			return Verdicts.INCONCLUSIVE;
		}else if(throwable instanceof ArrayComparisonFailure) {
			return Verdicts.FAIL;
		}else if(throwable instanceof AssertionFailedError) {
			return Verdicts.FAIL;
		}else if(throwable instanceof ComparisonFailure) {
			return Verdicts.FAIL;
		}else if(throwable instanceof AssertionError) {
			return Verdicts.FAIL;
		}else{
			return Verdicts.INCONCLUSIVE;
		}
	}
	
	public Verdicts getVerdict(){
		return verdict;
	}
}