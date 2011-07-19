package fr.inria.mdca.mba.test;

import java.util.ArrayList;

import org.apache.log4j.BasicConfigurator;

import fr.inria.mdca.core.model.BaseInstance;
import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.core.model.BaseModelElement;
import fr.inria.mdca.mba.BactereologicAlgorithm;
import fr.inria.mdca.mba.FilteringFunction;
import fr.inria.mdca.mba.MemorizationFunction;
import fr.inria.mdca.mba.impl.CachedMcdaFitnessFunction;
import fr.inria.mdca.mba.impl.McdaFitnessFunction;
import fr.inria.mdca.mba.impl.McdaMutationFunction;
import fr.inria.mdca.mba.impl.McdaStoppingCriterion;
import fr.inria.mdca.util.TupleCounter;
import fr.inria.mdca.util.WrongOrderException;

public class MainTest2 {

	/**
	 * @param args
	 * @throws WrongOrderException 
	 */
	public static void main(String[] args) throws WrongOrderException {
		ArrayList<BaseModelElement> elements=new ArrayList<BaseModelElement>();
		
		BaseModel model;
		
		BactereologicAlgorithm ba;
			
			BaseModelElement element0=new BaseModelElement(3,"test");
			BaseModelElement element1=new BaseModelElement(3,"test 1");
			BaseModelElement element2=new BaseModelElement(3,"test 2");
		//	BaseModelElement element3=new BaseModelElement(3,"test 3");
		//	BaseModelElement element4=new BaseModelElement(3,"test 4");
		//	BaseModelElement element5=new BaseModelElement(3,"test 5");
			
			elements.add(element0);
			elements.add(element1);
			elements.add(element2);
		//	elements.add(element3);
		//	elements.add(element4);
		//	elements.add(element5);
			
			model=new BaseModel();
			
			int order=2;
			int twise=2;
			
			model.setOrder(order);
			model.setTwise(twise);
			
			for(BaseModelElement element:elements){
				model.addElement(element);
			}
			
			
			BaseInstance instance=new BaseInstance(model);
			instance.getValues()[0]=1;
			instance.getValues()[1]=1;
			instance.getValues()[2]=3;
		//	instance.getValues()[3]=1;
		//	instance.getValues()[4]=1;
		//	instance.getValues()[5]=1;
			
			
			BaseInstance instance2=new BaseInstance(model);
			instance2.getValues()[0]=1;
			instance2.getValues()[1]=2;
			instance2.getValues()[2]=1;
		//	instance2.getValues()[3]=3;
		//	instance2.getValues()[4]=2;
		//	instance2.getValues()[5]=1;
			
			
			BaseInstance instance3=new BaseInstance(model);
			instance3.getValues()[0]=2;
			instance3.getValues()[1]=1;
			instance3.getValues()[2]=2;
		//	instance3.getValues()[3]=2;
		//	instance3.getValues()[4]=3;
		//	instance3.getValues()[5]=2;
			
			
			ArrayList<BaseInstance> instances=new ArrayList<BaseInstance>();
			instances.add(instance);
			instances.add(instance2);
			instances.add(instance3);
			
			
			
			CachedMcdaFitnessFunction ff=new CachedMcdaFitnessFunction();
			McdaFitnessFunction gf=new McdaFitnessFunction();
			McdaMutationFunction mf=new McdaMutationFunction();
			mf.setBactNumber(10);
			mf.setMutationProb(0.2f);
			
			McdaStoppingCriterion sc=null;
			
			sc=new McdaStoppingCriterion(TupleCounter.calculateToupleNumber(model,twise ,order));
			
			System.out.println(sc.getExpectedFitness());
		
			MemorizationFunction mmf=new MemorizationFunction();
			mmf.setMemorizationThreshold(0);
			
			FilteringFunction fff=new FilteringFunction(20,10,2);
		
		
			ba=new BactereologicAlgorithm(instances,ff,gf,mmf,fff,sc,mf,model);
			ba.setMaxAlgTurn(100000);
			ba.setLocalsearchProb(0.2f);
			BasicConfigurator.configure();
			ba.run();
			System.out.println(ba.getSolution().getInstanceSet().toString());
			System.out.println(ba.getSolution().getInstanceSet().size());

	}

}
