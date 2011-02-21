package fr.inria.mdca.util;


import java.util.ArrayList;

import fr.inria.mdca.core.model.BaseInstance;
import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.core.model.Tuple;

public class TupleCounter {
	
	private BaseModel model=null;
	private int order=0;
	private int twise=0;
	
	public TupleCounter(BaseModel model,int twise, int order){
		this.model=model;
		this.order=order;
		this.twise=twise;
	}
	
	public static int calculateToupleNumber(BaseModel model,int twise,int order) throws WrongOrderException{
		TupleCounter t = new TupleCounter(model,twise,order);
		return t.calculateTouples();
	}
	
	public int calculateTouples() throws WrongOrderException{
		int elements=this.model.getElements().size();
		if(twise>elements)
			throw new WrongOrderException(twise,elements);
		int combinations=MathHelper.calculateCombination(elements, twise);
		
		
		int[][] indexes=new int[combinations][twise];
		
		CombinationGenerator x = new CombinationGenerator (elements, twise);
		int k=0;
		while (x.hasMore ()) {
			indexes[k] = x.getNext ().clone();
			k++;
		}
		
		int total=0;
		
		for(int i=0;i<indexes.length;i++){
			
			int sub=1;
			
			for(int j=0;j<indexes[i].length;j++){
		
				int numElements=this.model.getElements().get(indexes[i][j]).getElementsNum();
				int cmb=MathHelper.calculateCombinationRep(numElements, order) + numElements;
				sub=cmb*sub;
				
			}
			
			//System.out.println("");
			total=total+sub;
		}
		//System.out.println(total);
		return total;
	}
	
	public int countTuple(ArrayList<BaseInstance> solution){
		ArrayList<Tuple> inTuples=new ArrayList<Tuple>();
		for(int i=0;i<solution.size();i++){
			if((solution.size()-order)>=i){
				ArrayList<BaseInstance> baseEval=new ArrayList<BaseInstance>();
				for(int k=0;k<order;k++){
					baseEval.add(solution.get(i+k));
				}
				ArrayList<Tuple> dTuples=TupleBuilder.buildTuples(baseEval,model.getTwise());
				for(Tuple t:dTuples){
					if(!inTuples.contains(t)){
						inTuples.add(t);
					}
				}
			}
		}
		return inTuples.size();
	}

	@SuppressWarnings("unused")
	private void print(int[][] elements){
		for(int i=0;i<elements.length;i++){
			for(int j=0;j<elements[i].length;j++){
				System.out.print(elements[i][j]+" ");
			}
			System.out.println(" ");
		}
	}
	
	public ArrayList<Tuple> getMissingTuples(ArrayList<BaseInstance> solution){
			ArrayList<Tuple> all=null;
			ArrayList<Tuple> missing;
			int order=model.getOrder();
			if(all==null)
				all=TupleBuilder.buildAllTuples(model);
			ArrayList<Tuple> inTuples=new ArrayList<Tuple>();
			for(int i=0;i<solution.size();i++){
				if((solution.size()-order)>=i){
					ArrayList<BaseInstance> baseEval=new ArrayList<BaseInstance>();
					for(int k=0;k<order;k++){
						baseEval.add(solution.get(i+k));
					}
					ArrayList<Tuple> dTuples=TupleBuilder.buildTuples(baseEval,model.getTwise());
					for(Tuple t:dTuples){
						if(!inTuples.contains(t)){
							inTuples.add(t);
						}
					}
				}
			}
			missing=new ArrayList<Tuple>();
			missing.addAll(all);
			for(Tuple t:inTuples){
				if(missing.contains(t))
					missing.remove(t);
				else System.out.println("ARGGG");
			}
			return missing;
		}
	
}
