package test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;


import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.core.model.BaseModelElement;

public class TestModelElement {

	private BaseModelElement element;
	@Before
	public void setUp() throws Exception {
		this.element=new BaseModelElement();
	}

	@Test
	public void testGetElementsNum() {
		int num=4;
		element.setElementsNum(num);
		assertTrue(element.getElementsNum()==num);
	}

	@Test
	public void testGetName() {
		String name="test";
		element.setName(name);
		assertEquals(element.getName(), name);
	}
	
	@Test
	public void testGetBase() {
		BaseModel model=new  BaseModel();
		element.setBase(model);
		assertEquals(element.getBase(), model);
	}

}
