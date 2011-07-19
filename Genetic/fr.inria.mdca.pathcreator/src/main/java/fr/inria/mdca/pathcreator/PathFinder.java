package fr.inria.mdca.pathcreator;

import java.util.ArrayList;

import fr.inria.mdca.core.model.BaseInstance;
import fr.inria.mdca.core.model.path.Path;

public class PathFinder {
	public class PathFindQueryReponse{
		private ArrayList<Path> missing=new ArrayList<Path>();
		private ArrayList<Path> contained=new ArrayList<Path>();
		private int pathNum=0;
		public ArrayList<Path> getMissing() {
			return missing;
		}
		public void setMissing(ArrayList<Path> missing) {
			this.missing = missing;
		}
		public ArrayList<Path> getContained() {
			return contained;
		}
		public void setContained(ArrayList<Path> contained) {
			this.contained = contained;
		}
		public int getPathNum() {
			return pathNum;
		}
		public void setPathNum(int pathNum) {
			this.pathNum = pathNum;
		}
		
	}
	
	/**
	 * returns a list of missing paths
	 * @param instances
	 * @param paths
	 * @return
	 */
	public PathFindQueryReponse findMissingPaths(ArrayList<BaseInstance> instances,ArrayList<Path> paths){
		PathFindQueryReponse reponse=new PathFindQueryReponse();
		
		
		//paths can have different length in twise and order
		
		for(int instanceIndex=0;instanceIndex<instances.size();instanceIndex++){
			for(Path p:paths){
				if(!reponse.contained.contains(p)){
					boolean hasPath=true;
					for(int i=0;i<p.getIndexes().length;i++){
						ArrayList<Integer> vals=p.getValues().get(i);
						if((instanceIndex+vals.size()-1)<instances.size()){
							for(int j=0;j<vals.size();j++){
								BaseInstance instance=instances.get(instanceIndex+j);
								int val=instance.getValues()[p.getIndexes()[i]];
								if(val!=vals.get(j)&&vals.get(j)!=-1){
									j=vals.size();
									i=p.getIndexes().length;
									hasPath=false;
								}
							}
						}
						else{
							i=p.getIndexes().length;
							hasPath=false;
						}
					}
					if(hasPath){
						reponse.pathNum++;
						reponse.contained.add(p);
					}
				}
			}
		}
		
		reponse.missing.addAll(paths);
		reponse.missing.removeAll(reponse.contained);
		
		return reponse;
	}
	
}
