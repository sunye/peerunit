package fr.inria.mdca.util;

import java.util.ArrayList;

import fr.inria.mdca.core.model.BaseInstance;
import fr.inria.mdca.core.model.BaseModel;

public class RandomInstanceGenerator {
	
	BaseModel model;
	
	public RandomInstanceGenerator(BaseModel model){
		this.model=model;
	}
	public ArrayList<BaseInstance> generateRandom(int size){
		ArrayList<BaseInstance> instances=new ArrayList<BaseInstance>();
		for(int i=0;i<=size;i++){
			instances.add(this.generateRandom());
		}
		return instances;
	}
	
	public BaseInstance generateRandom(){
		BaseInstance instance=new BaseInstance(model);
		int length=instance.getValues().length;
		for(int i=0;i<length;i++){
			instance.getValues()[i]=RandomHelper.randomValue(1, model.getElements().get(i).getElementsNum());
		}
		return instance;
	}
	
}
