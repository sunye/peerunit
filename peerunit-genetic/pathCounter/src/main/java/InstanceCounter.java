import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import fr.inria.mdca.core.model.BaseInstance;
import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.core.model.Tuple;
import fr.inria.mdca.core.model.path.Path;
import fr.inria.mdca.io.DataReader;
import fr.inria.mdca.io.DataWriter;
import fr.inria.mdca.pathcreator.PathFinder;
import fr.inria.mdca.pathcreator.PathFinder.PathFindQueryReponse;
import fr.inria.mdca.util.TupleCounter;


public class InstanceCounter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DataReader reader=new DataReader();
		BaseModel m=null;
		ArrayList<Path> paths=null;
		ArrayList<BaseInstance> instances=null;
		ArrayList<BaseInstance> instances2=null;
		
		String fileDirectory="/Users/freddy/Documents/workspaces/thesis_projects/mdca/ExperimentalDataContainer/filesS/";
		
		ArrayList<ArrayList<Integer>> values=new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> sizes=new ArrayList<ArrayList<Integer>>();
		
		values.add(new ArrayList<Integer>());
		values.add(new ArrayList<Integer>());
		
		sizes.add(new ArrayList<Integer>());
		sizes.add(new ArrayList<Integer>());
		
		
		for(int num=0;num<40;num++){
			try {
				m=reader.readModel(new File(fileDirectory+"/CASmodel.txt"));
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			
			
			try {
				paths=reader.readPaths(new File(fileDirectory+"/paths.txt"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				instances=reader.readInstances(new File(fileDirectory+"/instanceBA"+num+".txt"));
				instances2=reader.readInstances(new File(fileDirectory+"/instanceRG"+num+".txt"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			PathFinder finder=new PathFinder();
			
			PathFindQueryReponse result = finder.findMissingPaths(instances, paths);
			PathFindQueryReponse result2 = finder.findMissingPaths(instances2, paths);
			
			TupleCounter c=new TupleCounter(m,m.getTwise(),m.getOrder());
			int r0=c.countTuple(instances);
			int r1=c.countTuple(instances2);
			
		//	ArrayList<Tuple> missing = c.getMissingTuples(instances2);
			values.get(0).add(result.getPathNum());
			values.get(1).add(result2.getPathNum());
			
			sizes.get(0).add(instances.size());
			sizes.get(1).add(instances2.size());
			
			System.out.println("BA"+num+","+result.getPathNum()+"/"+paths.size()+","+r0+","+instances.size());
			System.out.println("RG"+num+","+result2.getPathNum()+"/"+paths.size()+ ","+r1+","+instances2.size());
		//	System.out.println(missing);
		}
		
		System.out.print("BApath<-c(");
		for(Integer i:values.get(0)){
			System.out.print(i+",");
		}
		System.out.print(")");
		System.out.println();
		
		System.out.print("RGpath<-c(");
		for(Integer i:values.get(1)){
			System.out.print(i+",");
		}
		System.out.print(")");
		System.out.println();
		
		System.out.print("BAsize<-c(");
		for(Integer i:sizes.get(0)){
			System.out.print(i+",");
		}
		System.out.print(")");
		System.out.println();
		
	}

}
