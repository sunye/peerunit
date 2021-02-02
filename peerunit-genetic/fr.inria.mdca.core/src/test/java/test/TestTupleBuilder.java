package test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import fr.inria.mdca.core.model.BaseInstance;
import fr.inria.mdca.core.model.Tuple;
import fr.inria.mdca.util.TupleBuilder;

public class TestTupleBuilder {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testBuildPairTouples() {
		BaseInstance instance=new BaseInstance(4);
		instance.getValues()[0]=1;
		instance.getValues()[1]=1;
		instance.getValues()[2]=1;
		instance.getValues()[3]=2;
		instance.setIndex(1);
		BaseInstance instance1=new BaseInstance(4);
		instance1.getValues()[0]=3;
		instance1.getValues()[1]=4;
		instance1.getValues()[2]=5;
		instance1.getValues()[3]=10;
		instance1.setIndex(2);
		BaseInstance instance2=new BaseInstance(4);
		instance2.getValues()[0]=6;
		instance2.getValues()[1]=7;
		instance2.getValues()[2]=8;
		instance2.getValues()[3]=12;
		instance2.setIndex(3);
		ArrayList<BaseInstance> instances=new ArrayList<BaseInstance>();
		instances.add(instance);
		instances.add(instance1);
		instances.add(instance2);
		
		int twise=3;
		
		ArrayList<Tuple> result = TupleBuilder.buildTuples(instances,twise);
		
		ArrayList<Tuple> Eresult =new  ArrayList<Tuple>();
		
		
		int[][] s={{1,3,6},{1,4,7},{1,5,8}};
		int[][] s1={{1,3,6},{1,4,7},{2,10,12}};
		int[][] s2={{1,4,7},{1,5,8},{2,10,12}};
		int[][] s3={{1,3,6},{1,5,8},{2,10,12}};
		
		int[] i={0,1,2};
		int[] i1={0,1,3};
		int[] i2={1,2,3};
		int[] i3={0,2,3};
		
		Tuple t=new Tuple(twise,instances.size());
		t.setTransitions(s);
		t.setIndexes(i);
		Tuple t1=new Tuple(twise,instances.size());
		t1.setTransitions(s1);
		t1.setIndexes(i1);
		Tuple t2=new Tuple(twise,instances.size());
		t2.setTransitions(s2);
		t2.setIndexes(i2);
		Tuple t3=new Tuple(twise,instances.size());
		t3.setTransitions(s3);
		t3.setIndexes(i3);
		
		
		Eresult.add(t);
		Eresult.add(t1);
		Eresult.add(t2);
		Eresult.add(t3);

		assertTrue(result.containsAll(Eresult));
	}

}
