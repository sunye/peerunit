package fr.inria.mdca.test;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.core.model.BaseModelElement;
import fr.inria.mdca.util.TupleCounter;
import fr.inria.mdca.util.WrongOrderException;

public class TestTupleCounter {
	
	ArrayList<BaseModelElement> elements=new ArrayList<BaseModelElement>();
	BaseModel model=new BaseModel();
	@Before
	public void setUp() throws Exception {
		
		BaseModelElement element0=new BaseModelElement(3,"test");
		BaseModelElement element1=new BaseModelElement(2,"test 1");
		BaseModelElement element2=new BaseModelElement(3,"test 2");
		
		elements.add(element0);
		elements.add(element1);
		elements.add(element2);
		
		for(BaseModelElement element:elements){
			model.addElement(element);
		}
	}

	@Test
	public void testCalculateToupleNumber() {
		
		try {
			int count=TupleCounter.calculateToupleNumber(model, 2, 2);
			System.out.println(count);
		} catch (WrongOrderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
