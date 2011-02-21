package fr.inria.mdca.util;

import java.util.ArrayList;

import fr.inria.mdca.core.model.BaseInstance;
import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.core.model.Tuple;

public class TupleBuilder {

	public static ArrayList<Tuple> buildTuples(ArrayList<BaseInstance> instances,int twise){
		
		ArrayList<Tuple> tuples=new ArrayList<Tuple>();		
		computePair(new int[twise], 0,0, twise, instances.get(0).getValues().length, instances,tuples);
		
		return tuples;
		
	}
	
	
	public static void computePair(int[] fixedrow,int fixindex,int value,final int twise,final int instanceLength,ArrayList<BaseInstance> instances,ArrayList<Tuple> tuples){
		if(fixindex==twise){
			Tuple t=new Tuple(twise,instances.size());
			int i = 0;
			for(int indexes:fixedrow){
				int j = 0;
				for(BaseInstance instance:instances){
					t.getTransitions()[i][j]=instance.getValues()[indexes];
				//	t.getIndexes()[j]=instance.getIndex();
					j++;
				//	System.out.print(" "+instance.getValues()[indexes]);
				}
				//System.out.println("");
				t.getIndexes()[i]=indexes;
				i++;
			}
			tuples.add(t);
			return;
		}
		
		for(int i=value+1;i<=instanceLength;i++){
			fixedrow[fixindex]=value;
			computePair(fixedrow, fixindex+1, i, twise, instanceLength, instances,tuples);
			value=i;
		}
	}
	
	
	public static ArrayList<Tuple> buildAllTuples(BaseModel model){
		ArrayList<ArrayList<ArrayList<Integer>>> tupleT = new ArrayList<ArrayList<ArrayList<Integer>>>(); // Variable, TUPLES, tuple
		//build the sets
		TupleBuilder.buildSets(tupleT,model);
		int elements=model.getElements().size();
		int twise=model.getTwise();
		int combinations=MathHelper.calculateCombination(elements, twise);
		CombinationGenerator x = new CombinationGenerator (elements, twise);
		int[][] indexes=new int[combinations][twise];
		
		int k=0;
		while (x.hasMore ()) {
			indexes[k] = x.getNext ().clone();
			k++;
		}
		ArrayList<Tuple> tuples=new ArrayList<Tuple>();
		for(int i=0;i<indexes.length;i++){
			TupleBuilder.buildTupleforRows(indexes[i],tupleT,0,tuples,null);

		}
		assert(tuples.size()==combinations);
		return tuples;
	}
	
	


	private static void buildSets(
			ArrayList<ArrayList<ArrayList<Integer>>> tupleT, BaseModel model) {
		int elements=model.getElements().size();
		for(int i=0;i<elements;i++){
			ArrayList<ArrayList<Integer>> variableSet=new ArrayList<ArrayList<Integer>>();
			int variableElements=model.getElements().get(i).getElementsNum();
			int order=model.getOrder();
			CombinationGenerator x = new CombinationGenerator (variableElements, order);
			
			int[][] values=new int[x.getTotal().intValue()][order];
			int[][] inverted=new int[x.getTotal().intValue()][order];
			int k=0;
			while (x.hasMore ()) {
				values[k] = x.getNext ().clone();
				for(int z=values[k].length-1;z>=0;z--){
					inverted[k][(values[k].length-1)-z]=values[k][z];
				}
				k++;
			}
			
			for(int j=0;j<values.length;j++){
				ArrayList<Integer> a=new ArrayList<Integer>();
				ArrayList<Integer> b=new ArrayList<Integer>();
				for(k=0;k<values[j].length;k++){
					a.add(values[j][k]+1);
					b.add(inverted[j][k]+1);
				}
				variableSet.add(a);
				variableSet.add(b);
			}
			for(int j=0;j<variableElements;j++){
				ArrayList<Integer> a=new ArrayList<Integer>();
				for(k=0;k<order;k++){
					a.add(j+1);
				}
				variableSet.add(a);
			}
			tupleT.add(variableSet);
		}
	}


	private static void buildTupleforRows(int[] is, ArrayList<ArrayList<ArrayList<Integer>>> tupleT,int index, ArrayList<Tuple> tuples,ArrayList<ArrayList<Integer>> elements) {
		
		
		for(ArrayList<Integer> list:tupleT.get(is[index])){

			if(elements==null){
				elements=new ArrayList<ArrayList<Integer>>();
			}
			elements.add(list);
			if(index==is.length-1){
				//build the touple...
				Tuple t=new Tuple(list.size(),is.length);
				for(int i=0;i<is.length;i++){
					t.getIndexes()[i]=is[i];
				}
				for(int i=0;i<is.length;i++){
					for(int j=0;j<list.size();j++){
						t.getTransitions()[i][j]=elements.get(i).get(j).intValue();
					}
				}
				//System.out.println("tuple: "+t);
				tuples.add(t);
			}
			else
				buildTupleforRows(is, tupleT, index+1,tuples,elements);
			elements.remove(list);
		}
		
		
	}
	
}
