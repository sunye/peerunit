import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import fr.inria.mdca.core.model.BaseInstance;
import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.core.model.Tuple;
import fr.inria.mdca.core.model.path.Path;
import fr.inria.mdca.io.DataReader;
import fr.inria.mdca.io.DataWriter;
import fr.inria.mdca.pathcreator.PathCreator;
import fr.inria.mdca.util.TupleCounter;


public class GenerateFromMissing {
	public static void main(String[] args){
		
		BaseModel m=null;
		DataReader reader=new DataReader();
		DataWriter writer=new DataWriter();
		
		ArrayList<BaseInstance> instances=null;
		
		String fileDirectory="/Users/freddy/Documents/workspaces/thesis_projects/mdca/ExperimentalDataContainer/filesS";
		
		try {
			m=reader.readModel(new File(fileDirectory+"/CASmodel.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			instances=reader.readInstances(new File(fileDirectory+"/instanceRA0.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		TupleCounter c=new TupleCounter(m,m.getTwise(),m.getOrder());
		ArrayList<Tuple> missing = c.getMissingTuples(instances);
		
		
		
		ArrayList<Path> paths=PathCreator.generateRandomIncludingTuples(m, missing, 100, 1);
		try {
			writer.writePaths(paths, new File(fileDirectory+"/pathsM.txt"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
	}
}
