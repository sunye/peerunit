package fr.inria.peerunit.test.oracle;
/**
 * 
 * @author Eduardo Almeida
 * @version 1.0
 * @since 1.0
 *
 */

public class GlobalVerdict {
	
	private Verdicts globalVerdict = Verdicts.INCONCLUSIVE;
	private int passVerdicts = 0;
	private int incVerdicts = 0;
	private int failVerdicts = 0;
	
	/**
	 * Calculates the global verdict of a test suit. </br>
	 * Once verdict is <code>FAIL</code> it will not have another verdict. 
	 * 
	 * @param localVerdict is the current verdict. 
	 * @param index is the threshold of inclusive results accepted to get a correct (PASS) global verdict.
	 * @since 1.0  
	 */
	public void setGlobalVerdict(Verdicts localVerdict, int index){		
		if (localVerdict.compareTo(Verdicts.FAIL) == 0){
			failVerdicts++;
			globalVerdict=Verdicts.FAIL;
		} else if (localVerdict.compareTo(Verdicts.PASS)==0){
				passVerdicts++;
		} else {
			incVerdicts++;			
		}
				
		if (globalVerdict.compareTo(Verdicts.FAIL) != 0){
			if( (((double)incVerdicts/(passVerdicts + incVerdicts)) *100) <= index ){
				globalVerdict = Verdicts.PASS;
			} else {
				globalVerdict = Verdicts.INCONCLUSIVE;
			}
		}
	}
	
	/**
	 * Returns the global verdict.
	 * 
	 * @return the global verdict : <code>PASS</code>, <code>FAIL</code>, <code>INCONCLUSIVE</code>, <code>ERROR</code>.

	 * @since 1.0 
	 * 
	 */
	public Verdicts getGlobalVerdict(){		
		return globalVerdict;
	}
	
	/**
	 * Returns the number of executed testers. 
	 * 
	 * @return the number of executed testers (passed, failed and inconclusive). 
	 * @since 1.0 
	 */
	public int getJudged(){
		return passVerdicts + incVerdicts + failVerdicts;
	}
	
	@Override
	public String toString(){
		return String.format("GlobalVerdict is %s , Local Verdicts are (Pass: %d) (Inconc.: %d) (Fail:. %d)",globalVerdict , passVerdicts ,incVerdicts,failVerdicts);
	}
	
}
