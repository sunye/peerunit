package fr.inria.mcda.pathcreator.test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;


import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.core.model.BaseModelElement;
import fr.inria.mdca.core.model.Tuple;
import fr.inria.mdca.core.model.path.Path;
import fr.inria.mdca.pathcreator.PathCreator;

public class TestPathCreator {

	ArrayList<BaseModelElement> elements=new ArrayList<BaseModelElement>();
	@Before
	public void setUp() throws Exception {
		BaseModelElement element0=new BaseModelElement(3,"test");
		BaseModelElement element1=new BaseModelElement(3,"test 1");
		BaseModelElement element2=new BaseModelElement(3,"test 2");
		elements.add(element0);
		elements.add(element1);
		elements.add(element2);
		
		
	}

	@Test
	public void testGenerateRandomIncluding() {
		BaseModel model=new BaseModel();
		for(BaseModelElement element:elements){
			model.addElement(element);
		}
		model.setOrder(2);
		model.setTwise(2);
		ArrayList<Path> result=PathCreator.generateRandomIncluding(model, new ArrayList<Path>(), 10,2);
		System.out.println(result);
		assertTrue(result.size()==10);
	}
	
	@Test
	public void testGenerateRandomIncludingInstance() {
		BaseModel model=new BaseModel();
		for(BaseModelElement element:elements){
			model.addElement(element);
		}
		int twise=2;
		model.setOrder(2);
		model.setTwise(2);
		
		
		ArrayList<Tuple> Eresult =new  ArrayList<Tuple>();
		
		
		int[][] s={{1,2,3},{3,2,1}};
		int[][] s1={{1,3,2},{1,3,3}};
		int[][] s2={{1,2,2},{1,2,3}};
		int[][] s3={{1,3,2},{1,1,2}};
		
		int[] i={0,1};
		int[] i1={0,1};
		int[] i2={1,2};
		int[] i3={0,1};
		
		Tuple t=new Tuple(twise,2);
		t.setTransitions(s);
		t.setIndexes(i);
		Tuple t1=new Tuple(twise,2);
		t1.setTransitions(s1);
		t1.setIndexes(i1);
		Tuple t2=new Tuple(twise,2);
		t2.setTransitions(s2);
		t2.setIndexes(i2);
		Tuple t3=new Tuple(twise,2);
		t3.setTransitions(s3);
		t3.setIndexes(i3);
		
		
		
		ArrayList<Path> result=PathCreator.generateRandomIncludingTuples(model, Eresult, 10,1);
		//System.out.println(result);
		assertTrue(result.size()==10);
	}

}
