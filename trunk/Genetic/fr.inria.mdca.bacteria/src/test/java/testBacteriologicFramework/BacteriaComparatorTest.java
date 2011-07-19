/*
 * Created on 29 nov. 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package testBacteriologicFramework;

import java.util.ArrayList;

import fr.irisa.triskell.bacteria.framework.*;
import junit.framework.*;
/**
 * @author bbaudry
 * 29 nov. 2004
 */
public class BacteriaComparatorTest extends TestCase {

	BacteriologicAlgorithm bacterioAlgo;
	DummyBacterium bact1;
	DummyBacterium bact2;
	DummyBacterium bact3;
	BacteriaComparator comparator;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		DummyFitness fitness = new DummyFitness();
		
		bact1 = new DummyBacterium(5);
		bact2 = new DummyBacterium(7);
		bact3 = new DummyBacterium(12);
		ArrayList medium = new ArrayList();
		medium.add(bact1);
		medium.add(bact2);
		medium.add(bact3);
		
		bacterioAlgo = new BacteriologicAlgorithm(medium,fitness,null,null,null,null);
		
		comparator = new BacteriaComparator(bacterioAlgo);
		super.setUp();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testCompare1() {
		assertTrue(comparator.compare(bact1, bact2)<0);
	}

	public void testCompare2() {
		assertTrue(comparator.compare(bact2, bact1)>0);
	}

	public void testCompare3() {
		assertTrue(comparator.compare(bact3, bact1)>0);
	}
	
	public void testCompare4() {
		assertTrue(comparator.compare(bact2, bact3)<0);
	}
	
	public void testCompare5() {
		assertTrue(comparator.compare(bact2, bact2)==0);
	}

}
