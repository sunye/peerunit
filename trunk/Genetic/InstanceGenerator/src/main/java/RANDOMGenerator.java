import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import fr.inria.mdca.core.model.BaseInstance;
import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.io.DataReader;
import fr.inria.mdca.io.DataWriter;
import fr.inria.mdca.util.RandomInstanceGenerator;


public class RANDOMGenerator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BaseModel m=null;
		DataReader reader=new DataReader();
		DataWriter writer=new DataWriter();
		String Directory="/Users/freddy/Documents/workspaces/thesis_projects/mdca/ExperimentalDataContainer/filesS/";
		
		for(int num=0;num<40;num++){
			try {
				m=reader.readModel(new File(Directory+"/CASmodel.txt"));
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			
			RandomInstanceGenerator g=new RandomInstanceGenerator(m);
			
			ArrayList<BaseInstance> instances=g.generateRandom(90);
			
			
			try {
				writer.write(instances, new File(Directory+"/instanceRG"+num+".txt"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
