package fr.inria.mdca.ga;

import java.util.ArrayList;

import fr.inria.mdca.core.model.BaseInstance;
import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.core.model.Tuple;
import fr.inria.mdca.util.MathHelper;
import fr.inria.mdca.util.RandomHelper;
import fr.inria.mdca.util.TupleBuilder;


public class McdaFixer {
	private ArrayList<Tuple> missing;
	private  ArrayList<Tuple> all;
	
	
	//first calculate the missing tuples
	//second create 
	
	public ArrayList<Tuple> getAll() {
		return all;
	}

	public void setAll(ArrayList<Tuple> all) {
		this.all = all;
	}

	public BaseModel getModel() {
		return model;
	}

	public void setModel(BaseModel model) {
		this.model = model;
	}

	ArrayList<BaseInstance> currentSolution;
	ArrayList<ArrayList<Tuple>> selectedTuples=new ArrayList<ArrayList<Tuple>>();
	
	private BaseModel model;
	
	private void setup_(){
		int order=model.getOrder();
		if(all==null)
			all=TupleBuilder.buildAllTuples(model);
		ArrayList<Tuple> inTuples=new ArrayList<Tuple>();
		for(int i=0;i<this.currentSolution.size();i++){
			if((this.currentSolution.size()-order)>=i){
				ArrayList<BaseInstance> baseEval=new ArrayList<BaseInstance>();
				for(int k=0;k<order;k++){
					baseEval.add(this.currentSolution.get(i+k));
				}
				ArrayList<Tuple> dTuples=TupleBuilder.buildTuples(baseEval,model.getTwise());
				for(Tuple t:dTuples){
					if(!inTuples.contains(t)){
						inTuples.add(t);
					}
				}
			}
		}
		this.missing=new ArrayList<Tuple>();
		this.missing.addAll(this.all);
		for(Tuple t:inTuples){
			if(this.missing.contains(t))
				this.missing.remove(t);
			else System.out.println("ARGGG");
		}
		ArrayList<Integer> used=new ArrayList<Integer>();
		for(int i=0;i<this.missing.size()-1;i++){
			ArrayList<Tuple> thisTuples=new ArrayList<Tuple>();
			if(!used.contains(i)){
				Tuple a=this.missing.get(i);
				thisTuples.add(a);
				for(int j=i;j<this.missing.size();j++){
					boolean conflict=false;
					Tuple b=this.missing.get(j);
					for(int k=0;k<model.getTwise();k++){
						for(int t=0;t<model.getTwise();t++){
							if(a.getIndexes()[k]==b.getIndexes()[t]){
								conflict=true;
							}
						}
					}
					if(!conflict){	
						thisTuples.add(b);
						used.add(j);
					}
				}
				selectedTuples.add(thisTuples);
			}
		}
	}
	
	public void fix(ArrayList<BaseInstance> currentSolution){

		this.currentSolution=currentSolution;
		this.setup_();
		int order=this.model.getOrder();
		ArrayList<BaseInstance> elements=new ArrayList<BaseInstance>(order);
		int combinations=MathHelper.calculateCombination(model.getElements().size(), model.getTwise());
		//fill all with new tuples
		//select two 
		for(int i=0;i<order;i++){
			BaseInstance ins=new BaseInstance(model);
			elements.add(ins);
		}
		ArrayList<Tuple> selected=new ArrayList<Tuple>();
		int val=-1;
		for(ArrayList<Tuple> tuples:this.selectedTuples){
			int ss=tuples.size();
			if(ss>val){
				val=ss;
				selected=tuples;
			}
		}
		int h=0;
		for(Tuple t:selected){
			int k=0;
			if(h>combinations)
				break;
			for(BaseInstance i:elements){
				for(int j=0;j<t.getIndexes().length;j++){
					i.getValues()[t.getIndexes()[j]]=t.getTransitions()[j][k];
				}
				k++;
			}
			h++;
		}
		for(BaseInstance i:elements){
			for(int k=0;k<i.getValues().length;k++){
				if(i.getValues()[k]==0){
					i.getValues()[k]=RandomHelper.randomValue(1, model.getElements().get(k).getElementsNum());
				}
			}
		}
		currentSolution.addAll(elements);
	}
	
	
}
