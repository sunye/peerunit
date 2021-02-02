package test;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.core.model.BaseModelElement;
import fr.inria.mdca.ga.FitnessFunction;
import fr.inria.mdca.ga.GeneticAlgorithm;
import fr.inria.mdca.util.WrongOrderException;


public class TestGA {
	ArrayList<BaseModelElement> elements=new ArrayList<BaseModelElement>();
	
	BaseModel model;
	
	@Before
	public void setUp() throws Exception {
	
			
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
			
			//BasicConfigurator.configure();
	}

	@Test
	public void testRun() {
		GeneticAlgorithm ga=new GeneticAlgorithm(model,new FitnessFunction(),1500,200,200,50,0.5f,0.5f,102,3);
		try {
			ga.run();
		} catch (WrongOrderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
