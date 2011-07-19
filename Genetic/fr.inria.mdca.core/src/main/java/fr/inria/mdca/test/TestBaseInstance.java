package fr.inria.mdca.test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import fr.inria.mdca.core.model.BaseInstance;
import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.core.model.BaseModelElement;

public class TestBaseInstance {
	ArrayList<BaseModelElement> elements=new ArrayList<BaseModelElement>();
	
	BaseModel model;
	
	@Before
	public void setUp() throws Exception {
		BaseModelElement element0=new BaseModelElement(3,"test");
		BaseModelElement element1=new BaseModelElement(2,"test 1");
		BaseModelElement element2=new BaseModelElement(3,"test 2");
		elements.add(element0);
		elements.add(element1);
		elements.add(element2);
		model=new BaseModel();
		for(BaseModelElement element:elements){
			model.addElement(element);
		}
	}

	@Test
	public void testBaseInstanceChangeFalse() {
		BaseInstance instance=new BaseInstance(model);
		instance.getValues()[0]=1;
		instance.getValues()[1]=1;
		instance.getValues()[2]=1;
		
		assertFalse(instance.isChanged());
	}

	@Test
	public void testBaseInstanceChangeTrue() {
		BaseInstance instance=new BaseInstance(model);
		instance.getValues()[0]=1;
		instance.getValues()[1]=1;
		instance.getValues()[2]=1;
		
		instance.change(2, 2);
		

		BaseInstance instance1=new BaseInstance(model);
		
		instance1.getValues()[0]=2;
		instance1.getValues()[1]=1;
		instance1.getValues()[2]=2;
		
		assertTrue(instance.isChanged());
		assertTrue(2==instance.getValues()[2]);
		assertTrue(2==instance.getChangedElement());
		
		
	}
	
	@Test
	public void testBaseInstanceChangeValue() {
		BaseInstance instance=new BaseInstance(model);
		instance.getValues()[0]=1;
		instance.getValues()[1]=1;
		instance.getValues()[2]=1;
	} 
}
