package fr.inria.mdca.test;


import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import fr.inria.mdca.core.model.Tuple;

public class TestTouple {

	@Before
	public void setUp() throws Exception {
	}
	
	@Test
	public void testEquals(){
		Tuple t=new Tuple(2,2);
		t.getTransitions()[0][0]=1;
		t.getTransitions()[0][1]=2;
		t.getTransitions()[1][0]=3;
		t.getTransitions()[1][1]=4;
	
		Tuple t1=new Tuple(2,2);
		t1.getTransitions()[0][0]=1;
		t1.getTransitions()[0][1]=2;
		t1.getTransitions()[1][0]=3;
		t1.getTransitions()[1][1]=4;
		
		assertTrue(t.equals(t1));
	}

}
