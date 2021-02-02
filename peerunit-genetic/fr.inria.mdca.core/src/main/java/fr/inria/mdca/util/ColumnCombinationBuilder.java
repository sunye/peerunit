package fr.inria.mdca.util;

import java.util.ArrayList;

import fr.inria.mdca.core.model.BaseInstance;

public class ColumnCombinationBuilder {

	public static ArrayList<ArrayList<BaseInstance>> computeCombinations(ArrayList<BaseInstance> instances, int order){
		ArrayList<ArrayList<BaseInstance>> result=new ArrayList<ArrayList<BaseInstance>>();
		CombinationGenerator x = new CombinationGenerator (instances.size(), order);
		while (x.hasMore ()) {
			ArrayList<BaseInstance> comb=new ArrayList<BaseInstance>();
			for(int i:x.getNext()){
				comb.add(instances.get(i));
			}
			result.add(comb);
		}
		return result;
	}
	
	public static ArrayList<ArrayList<BaseInstance>> computeCombinationsIncluding(ArrayList<BaseInstance> instances, int order,int index){
		ArrayList<ArrayList<BaseInstance>> result=new ArrayList<ArrayList<BaseInstance>>();
		CombinationGenerator x = new CombinationGenerator (instances.size(), order);
		while (x.hasMore ()) {
			ArrayList<BaseInstance> comb=new ArrayList<BaseInstance>();
			int[] inx=x.getNext();
			boolean continue_=false;
			for(int i:inx){
				if(i==index){
					continue_=true;
				}
			}
			if(continue_){
				for(int i:inx){
					comb.add(instances.get(i));
				}
				result.add(comb);
			}
		}
		return result;
	}
}
