package fr.inria.mdca.mba.test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import fr.inria.mdca.core.model.BaseInstance;
import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.core.model.BaseSerieInstance;
import fr.inria.mdca.mba.BactereologicAlgorithm;
import fr.inria.mdca.mba.impl.McdaFitnessFunction;

public class TestFitness {

	
	ArrayList<BaseInstance> instances=new ArrayList<BaseInstance>();
	
	BaseModel model=new BaseModel(2,2);
	McdaFitnessFunction fn=new McdaFitnessFunction();
	
	@Before
	public void setUp() throws Exception {
		
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
		
		instances.add(instance);
		instances.add(instance1);
		instances.add(instance2);
		
		BactereologicAlgorithm b=new BactereologicAlgorithm();
		b.setModel(model);
		fn.setBactereologicAlgorithm(b);
		
	}

	@Test
	public void testFitnessArrayListOfBaseInstance() {
		float f=fn.fitness(instances);
		System.out.println(f);
		assertTrue(12==f);
	}

	@Test
	public void testFitnessArrayListOfBaseInstanceIntBoolean() {
	
		instances.get(2).change(3, 2);
		instances.get(1).change(3, 2);
		instances.get(2).change(2, 1);
		instances.get(1).change(2, 1);
		
		
		float f=fn.fitness(instances);
		System.out.println(f);
		assertTrue(11==f);
	
	}
	@Test
	public void testRelativeFitness(){
		
		BaseInstance instance2=new BaseInstance(4);
		instance2.getValues()[0]=6;
		instance2.getValues()[1]=7;
		instance2.getValues()[2]=8;
		instance2.getValues()[3]=12;
		instance2.setIndex(4);
		
		BaseSerieInstance serie=new BaseSerieInstance();
		serie.setInstanceSet(instances);
		
		fn.getBactereologicAlgorithm().setSolution(serie);
		
		float relative=fn.relativeFitness(instance2);
		System.out.println(relative);
	}

}
