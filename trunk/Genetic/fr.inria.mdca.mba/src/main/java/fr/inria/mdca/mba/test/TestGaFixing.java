package fr.inria.mdca.mba.test;


import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import fr.inria.mdca.core.model.BaseInstance;
import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.core.model.BaseModelElement;
import fr.inria.mdca.mba.ga.McdaFixer;

public class TestGaFixing {

	ArrayList<BaseModelElement> elements=new ArrayList<BaseModelElement>();
	ArrayList<BaseInstance> instances=new ArrayList<BaseInstance>();
	BaseModel model;
	
	
	@Before
	public void setUp() throws Exception {
		BaseModelElement element0=new BaseModelElement(3,"test");
		BaseModelElement element1=new BaseModelElement(3,"test 1");
		BaseModelElement element2=new BaseModelElement(3,"test 2");
		BaseModelElement element3=new BaseModelElement(3,"test 3");
		
		elements.add(element0);
		elements.add(element1);
		elements.add(element2);
		elements.add(element3);
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
		instance.getValues()[2]=1;
		instance.getValues()[3]=1;
		
		
		BaseInstance instance2=new BaseInstance(model);
		instance2.getValues()[0]=1;
		instance2.getValues()[1]=2;
		instance2.getValues()[2]=1;
		instance2.getValues()[3]=1;
		
		BaseInstance instance3=new BaseInstance(model);
		instance3.getValues()[0]=2;
		instance3.getValues()[1]=1;
		instance3.getValues()[2]=2;
		instance3.getValues()[3]=2;
		
		
		instances.add(instance);
		instances.add(instance2);
		instances.add(instance3);
		

	}

	@Test
	public void testFix() {
		McdaFixer ga=new McdaFixer();
		ga.setModel(model);
		ga.fix(instances);
		assertTrue(instances.size()==5);
	}

}
