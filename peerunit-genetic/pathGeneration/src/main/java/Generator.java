import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.core.model.path.Path;
import fr.inria.mdca.io.DataReader;
import fr.inria.mdca.io.DataWriter;
import fr.inria.mdca.pathcreator.PathCreator;


public class Generator {

	public static void main(String[] args){
	
		BaseModel m=null;
		DataReader reader=new DataReader();
		DataWriter writer=new DataWriter();
		
		try {
			m=reader.readModel(new File("/Users/freddy/Documents/workspaces/thesis_projects/mdca/ExperimentalDataContainer/filesS/CASmodel.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<Path> paths=PathCreator.generateRandomIncluding(m, new ArrayList<Path>(), 100,1);
		try {
			writer.writePaths(paths, new File("/Users/freddy/Documents/workspaces/thesis_projects/mdca/ExperimentalDataContainer/filesS/paths.txt"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
	}
}
