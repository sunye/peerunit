package fr.inria.mdca.ga;

import java.util.ArrayList;

import fr.inria.mdca.core.model.BaseInstance;
import fr.inria.mdca.util.RandomHelper;

public class Crossover {
	
	public BaseInstance crossover(BaseInstance a,BaseInstance b){
		int length=a.getValues().length;
		int cut=RandomHelper.randomValue(1, length-1);
		
		BaseInstance c=a.clone();
		
		for(int i=cut;i<length;i++){
			c.getValues()[i]=b.getValues()[i];
		}
		
		return c;
	}
	
	public ArrayList<BaseInstance> permute(ArrayList<BaseInstance> instances){
		int a=RandomHelper.randomValue(0, instances.size()-1);
		int b=RandomHelper.randomValue(0, instances.size()-1);
		while(a==b){
			a=RandomHelper.randomValue(0, instances.size()-1);
			b=RandomHelper.randomValue(0, instances.size()-1);
		}
		ArrayList<BaseInstance> inst=new ArrayList<BaseInstance>();
		for(BaseInstance i:instances){
			inst.add(i.clone());
		}
		BaseInstance tmp0=inst.get(a);
		inst.set(a, inst.get(b));
		inst.set(b, tmp0);
		return inst;
	}

	public ArrayList<BaseInstance> crossover(ArrayList<BaseInstance> a,ArrayList<BaseInstance> b){
		int length=a.size();
		int cut=RandomHelper.randomValue(1, length-1);
		
		ArrayList<BaseInstance> inst=new ArrayList<BaseInstance>();
		for(int i=0;i<length;i++){
			if(i<cut){
				inst.add(a.get(i).clone());
			}
			else{
				inst.add(b.get(i).clone());
			}
		}
		
		return inst;
	}
}
