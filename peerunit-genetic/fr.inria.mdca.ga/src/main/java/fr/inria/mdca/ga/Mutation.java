package fr.inria.mdca.ga;

import java.util.ArrayList;

import fr.inria.mdca.core.model.BaseInstance;
import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.util.RandomHelper;

public class Mutation {
	public BaseInstance mutate(BaseInstance a,BaseModel m){
		int length=a.getValues().length;
		int mutate=RandomHelper.randomValue(0, length-1);
		BaseInstance c=a.clone();
		int nv=RandomHelper.randomValue(1,m.getElements().get(mutate).getElementsNum());
		while(c.getValues()[mutate]==nv){
			nv=RandomHelper.randomValue(1,m.getElements().get(mutate).getElementsNum());
		}
		c.getValues()[mutate]=RandomHelper.randomValue(1,nv);
		return c;
	}
	
	public ArrayList<BaseInstance> mutate(ArrayList<BaseInstance> a,BaseModel m){
		int mutate=RandomHelper.randomValue(0, a.size()-1);
		ArrayList<BaseInstance> inst=new ArrayList<BaseInstance>();
		for(BaseInstance i:a){
			inst.add(i.clone());
		}
		inst.set(mutate, this.mutate(inst.get(mutate), m));
		return inst;
	}
}
