package fr.inria.peerunit.test.oracle;
/**
 * 
 * @author Eduardo Almeida
 * @version 1.0
 * @since 1.0
 *
 */

public class GlobalVerdict {
	
	private Verdicts globalVerdict = null;
	private int passVerdicts = 0;
	private int incVerdicts = 0;
	private int failVerdicts = 0;
	private int index;
	
	public GlobalVerdict(int i) {
		index = i;
	}
	/**
	 * Calculates the global verdict of a test suit. </br>
	 * Once verdict is <code>FAIL</code> it will not have another verdict. 
	 * 
	 * @param localVerdict is the current verdict. 
	 * @param index is the threshold of inclusive results accepted to get a correct (PASS) global verdict.
	 * @since 1.0  
	 */
	public void addLocalVerdict(Verdicts localVerdict){
		
		switch (localVerdict) {
		case FAIL:
			failVerdicts++;
			break;
		case PASS:
			passVerdicts++;
			break;
		case INCONCLUSIVE:
			incVerdicts++;
			break;
		default:
			System.err.println("Unknown verdict");
			break;
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
			calculateVerdict();
		
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
		calculateVerdict();	
		return String.format("GlobalVerdict is %s , Local Verdicts are (Pass: %d) (Inconc.: %d) (Fail:. %d)", globalVerdict , passVerdicts ,incVerdicts, failVerdicts);
	}
	
	private void calculateVerdict() {

		if (failVerdicts > 0) {
			globalVerdict = Verdicts.FAIL;
		} else if ((((double) incVerdicts / (passVerdicts + incVerdicts)) * 100) <= index) {
			globalVerdict = Verdicts.PASS;
		} else {
			globalVerdict = Verdicts.INCONCLUSIVE;
		}
	}
	
	
}
