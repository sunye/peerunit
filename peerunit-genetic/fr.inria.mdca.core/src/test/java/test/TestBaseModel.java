package test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;


import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.core.model.BaseModelElement;
import fr.inria.mdca.util.TupleCounter;
import fr.inria.mdca.util.WrongOrderException;

public class TestBaseModel {
	ArrayList<BaseModelElement> elements=new ArrayList<BaseModelElement>();
	@Before
	public void setUp() throws Exception {
		BaseModelElement element0=new BaseModelElement(3,"test");
		BaseModelElement element1=new BaseModelElement(2,"test 1");
		BaseModelElement element2=new BaseModelElement(3,"test 2");
		elements.add(element0);
		elements.add(element1);
		elements.add(element2);
	}

	@Test
	public void testAddElement() {
		BaseModel model=new BaseModel();
		for(BaseModelElement element:elements){
			model.addElement(element);
		}
		assertArrayEquals(elements.toArray(), model.getElements().toArray());
	}
	@Test
	public void testGetElements() {
		BaseModel model=new BaseModel();
		for(BaseModelElement element:elements){
			model.addElement(element);
		}
		for(BaseModelElement element:model.getElements()){
			assertEquals(model, element.getBase());
		}
	}
	@Test
	public void testGeneration(){
		BaseModel model=new BaseModel();
		for(BaseModelElement element:elements){
			model.addElement(element);
		}
		TupleCounter counter=new TupleCounter(model,2,2);
		try {
			counter.calculateTouples();
		} catch (WrongOrderException e) {
			e.printStackTrace();
		}
	}


}
