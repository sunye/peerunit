package fr.inria.peerunit.test.oracle;

import fr.inria.peerunit.test.assertion.ArrayComparisonFailure;
import fr.inria.peerunit.test.assertion.AssertionFailedError;
import fr.inria.peerunit.test.assertion.ComparisonFailure;
import fr.inria.peerunit.test.assertion.InconclusiveFailure;

public class Oracle {

	Verdicts verdict;

	InconclusiveFailure incFailure=new InconclusiveFailure();

	public Oracle(Throwable throwable){

		if(throwable instanceof InconclusiveFailure) {
			verdict = Verdicts.INCONCLUSIVE;
		}else if(throwable instanceof ArrayComparisonFailure) {
			verdict = Verdicts.FAIL;
		}else if(throwable instanceof AssertionFailedError) {
			verdict = Verdicts.FAIL;
		}else if(throwable instanceof ComparisonFailure) {
			verdict = Verdicts.FAIL;
		}else if(throwable instanceof AssertionError) {
			verdict = Verdicts.FAIL;
		}
	}

	public Verdicts getVerdict(){
		return verdict;
	}
}