package fr.inria.mdca.pathcreator;

import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.core.model.Tuple;
import fr.inria.mdca.core.model.path.Path;
import fr.inria.mdca.util.RandomHelper;

import java.util.ArrayList;

public class PathCreator {

	
	
	public static ArrayList<Path> generateRandomIncluding(BaseModel model, ArrayList<Path> paths,int number,int radix){
		
		ArrayList<Path> return_=new ArrayList<Path>();
		
		int rnumber=number-paths.size();
		
		int order=model.getOrder();
		
		int twise=model.getTwise();
		
		for(int i=0;i<rnumber;i++){
			
		
			
			int limit=2;
			if(twise+radix<=model.getElements().size())
				limit=RandomHelper.randomValue(2, twise+radix);
			else limit=RandomHelper.randomValue(2, twise);
			Path p=new  Path(limit);
			int thisIndex=-1;
			
			for(int j=0;j<limit;j++){
				
				int thisIndex_=RandomHelper.randomValue(0, model.getElements().size()-1);
				
				while(thisIndex==thisIndex_){
					thisIndex_=RandomHelper.randomValue(0, model.getElements().size()-1);
				}
				
				thisIndex=thisIndex_;
				
				p.getIndexes()[j]=thisIndex;
				int olimit=order;
				float r = (float) Math.random();
				if(r>0.8)
					olimit=RandomHelper.randomValue(order, order+radix);
			
				for(int k=0;k<olimit;k++){
					r = (float) Math.random();
					if(r<0.2 && k<olimit-2)
						p.getValues().get(j).add(-1);
					else{
						p.getValues().get(j).add(RandomHelper.randomValue(1, model.getElements().get(thisIndex).getElementsNum()));
					}
						
				}
			}
			return_.add(p);
		}
		return_.addAll(paths);
		return return_;
	}

	public static ArrayList<Path> generateRandomIncludingTuples(BaseModel model, ArrayList<Tuple> tuples,int number,int radix) {
		
		
		ArrayList<Path> return_=new ArrayList<Path>();

		
		int order=model.getOrder();
		
		int twise=model.getTwise();
		
		int numeral=tuples.size()/2;
		if(numeral>number)
			numeral=number;
		
		for(int i=0;i<numeral;i++){
			int limit=2;
			if(twise+radix<=model.getElements().size())
				limit=RandomHelper.randomValue(twise, twise+radix);
			else limit=RandomHelper.randomValue(2, twise);
			
			Path p=new  Path(limit);
			
			int thisIndex=-1;
			
			
			for(int j=0;j<limit;j++){
				int thisIndex_=0;
				if(j>tuples.get(i).getIndexes().length-1){
					thisIndex_=RandomHelper.randomValue(0, model.getElements().size()-1);
					while(thisIndex==thisIndex_){
						thisIndex_=RandomHelper.randomValue(0, model.getElements().size()-1);
					}
				}
				else{
					thisIndex_=tuples.get(i).getIndexes()[j];
				}
				
				thisIndex=thisIndex_;
				
				p.getIndexes()[j]=thisIndex;

				int olimit=2;
				
				if(order-radix > 1)
					olimit=RandomHelper.randomValue(order, order+radix);
				else
					olimit=RandomHelper.randomValue(2, order+radix);
			
				for(int k=0;k<olimit;k++){
					if(j>tuples.get(i).getIndexes().length-1){
							float r = (float) Math.random();
							if(r<0.2 && k<olimit-1)
								p.getValues().get(j).add(-1);
							else{
								p.getValues().get(j).add(RandomHelper.randomValue(1, model.getElements().get(thisIndex).getElementsNum()));
							}
					}
					else{
						if(k>tuples.get(i).getTransitions()[j].length-1){
							float r = (float) Math.random();
							if(r<0.2 && k<olimit-1)
								p.getValues().get(j).add(-1);
							else{
								p.getValues().get(j).add(RandomHelper.randomValue(1, model.getElements().get(thisIndex).getElementsNum()));
							}
						}
						else{
							int v=tuples.get(i).getTransitions()[j][k];
							p.getValues().get(j).add(v);
						}
					}
						
				}
			}
			return_.add(p);
		}
		
		
		if(return_.size()<number){
			int rnumber=number-return_.size();
			for(int i=0;i<rnumber;i++){
				
				
				
				int limit=2;
				if(twise+radix<=model.getElements().size())
					limit=RandomHelper.randomValue(2, twise+radix);
				else limit=RandomHelper.randomValue(2, twise);
				Path p=new  Path(limit);
				int thisIndex=-1;
				
				for(int j=0;j<limit;j++){
					
					int thisIndex_=RandomHelper.randomValue(0, model.getElements().size()-1);
					
					while(thisIndex==thisIndex_){
						thisIndex_=RandomHelper.randomValue(0, model.getElements().size()-1);
					}
					
					thisIndex=thisIndex_;
					
					p.getIndexes()[j]=thisIndex;
					int olimit=2;
					if(order-radix > 0)
						olimit=RandomHelper.randomValue(order-radix, order+radix);
					else
						olimit=RandomHelper.randomValue(2, order+radix);
				
					for(int k=0;k<olimit;k++){
						float r = (float) Math.random();
						if(r<0.2 && k<olimit-1)
							p.getValues().get(j).add(-1);
						else{
							p.getValues().get(j).add(RandomHelper.randomValue(1, model.getElements().get(thisIndex).getElementsNum()));
						}
							
					}
				}
				return_.add(p);
			}
		}
		return return_;
	}

}
