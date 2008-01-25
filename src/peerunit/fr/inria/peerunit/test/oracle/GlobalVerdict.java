package fr.inria.peerunit.test.oracle;


public class GlobalVerdict{
	Verdicts globalVerdict=Verdicts.INCONCLUSIVE;

	int passVerdicts=0;

	int incVerdicts=0;
	
	int failVerdicts=0;
	
	/**
	 * Once verdict is FAIL it will not have another verdict
	 * @param localVerdict
	 * @param index
	 */
	public void setGlobalVerdict(Verdicts localVerdict, int index){		
		if(localVerdict.compareTo(Verdicts.FAIL)==0){
			failVerdicts++;
			globalVerdict=Verdicts.FAIL;
		}else	if(localVerdict.compareTo(Verdicts.PASS)==0){
				passVerdicts++;
		}else	incVerdicts++;			
				
		if(globalVerdict.compareTo(Verdicts.FAIL)!=0){
			if(  (((double)incVerdicts/(passVerdicts + incVerdicts)) *100) <= index )
				globalVerdict = Verdicts.PASS;
			else
				globalVerdict = Verdicts.INCONCLUSIVE;
		}
	}
	public Verdicts getGlobalVerdict(){		
		return globalVerdict;
	}
	
	public int getJudged(){
		return passVerdicts + incVerdicts + failVerdicts;
	}
	
	@Override
	public String toString(){
		return String.format("GlobalVerdict is %s , Local Verdicts are (Pass: %d) (Inconc.: %d) (Fail:. %d)",globalVerdict , passVerdicts ,incVerdicts,failVerdicts);
	}
}
