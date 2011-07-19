import java.io.File;
import java.io.IOException;

import org.apache.log4j.BasicConfigurator;

import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.ga.FitnessFunction;
import fr.inria.mdca.ga.GeneticAlgorithm;
import fr.inria.mdca.ga.StatisticTrace;
import fr.inria.mdca.io.DataReader;
import fr.inria.mdca.io.DataWriter;
import fr.inria.mdca.mba.StatisticTrace.InterationTrace;
import fr.inria.mdca.util.WrongOrderException;


public class GAGenerator {
	public static void main(String[] args) {
		String Directory="/Users/freddy/Documents/workspaces/thesis_projects/mdca/ExperimentalDataContainer/files/";
		int num=0;
		
		
		BaseModel m=null;
		DataReader reader=new DataReader();
		DataWriter writer=new DataWriter();
		BasicConfigurator.configure();
		
		try {
			m=reader.readModel(new File(Directory+"CASmodel.txt"));
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		GeneticAlgorithm ga=new GeneticAlgorithm(m,new FitnessFunction(),400,30,200,80,0.5f,0.5f,160,1);
		ga.setTracer(new StatisticTrace());
		try {
			ga.run();
		} catch (WrongOrderException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for(fr.inria.mdca.ga.StatisticTrace.InterationTrace t:ga.getTracer().getTraces()){
			System.out.println(t.toString());
		}
		/*try {
			writer.write(ga.getSolution(), new File(Directory+"instanceGA"+num+".txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(ga.getSolution());*/
	}
}
