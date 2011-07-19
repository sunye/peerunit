import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.BasicConfigurator;

import fr.inria.mdca.core.model.BaseInstance;
import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.io.DataReader;
import fr.inria.mdca.io.DataWriter;
import fr.inria.mdca.mba.BactereologicAlgorithm;
import fr.inria.mdca.mba.FilteringFunction;
import fr.inria.mdca.mba.MemorizationFunction;
import fr.inria.mdca.mba.StatisticTrace;
import fr.inria.mdca.mba.StatisticTrace.InterationTrace;
import fr.inria.mdca.mba.impl.CachedMcdaFitnessFunction;
import fr.inria.mdca.mba.impl.McdaFitnessFunction;
import fr.inria.mdca.mba.impl.McdaMutationFunction;
import fr.inria.mdca.mba.impl.McdaStoppingCriterion;
import fr.inria.mdca.util.RandomInstanceGenerator;
import fr.inria.mdca.util.TupleCounter;
import fr.inria.mdca.util.WrongOrderException;


public class BAGenerator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String Directory="/Users/freddy/Documents/workspaces/thesis_projects/mdca/ExperimentalDataContainer/files/";
		//	for(int num=15;num<40;num++){
			
			
			BaseModel m=null;
			DataReader reader=new DataReader();
			DataWriter writer=new DataWriter();
			BactereologicAlgorithm ba;
			
			try {
				m=reader.readModel(new File(Directory+"CASmodel.txt"));
			} catch (IOException e) {
				
				e.printStackTrace();
			}
	
			CachedMcdaFitnessFunction ff=new CachedMcdaFitnessFunction();
			McdaFitnessFunction gf=new McdaFitnessFunction();
			McdaMutationFunction mf=new McdaMutationFunction();
			mf.setBactNumber(10);
			mf.setMutationProb(0.4f);
			McdaStoppingCriterion sc=null;
			
			try {
				sc=new McdaStoppingCriterion(TupleCounter.calculateToupleNumber(m,m.getTwise() ,m.getOrder()));
			} catch (WrongOrderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println(sc.getExpectedFitness());
		
			MemorizationFunction mmf=new MemorizationFunction();
			
			FilteringFunction fff=new FilteringFunction(25,10,5);
		
			RandomInstanceGenerator g=new RandomInstanceGenerator(m);
			
			ArrayList<BaseInstance> instances=g.generateRandom(10);
			
			ba=new BactereologicAlgorithm(instances,ff,gf,mmf,fff,sc,mf,m);
			ba.setMaxAlgTurn(1000);
			ba.setLocalsearchProb(0.0f);
			
		
			BasicConfigurator.configure();
			//ba.setDoLocalOptimization(false);
			ba.setTracer(new StatisticTrace());
			ba.run();
			
			for(InterationTrace t:ba.getTracer().getTraces()){
				System.out.println(t.toString());
			}
			
	/*		try {
				writer.write(ba.getSolution().getInstanceSet(), new File(Directory+"instanceBA"+num+".txt"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println(ba.getSolution().getInstanceSet().toString());
		}*/
	}

}
