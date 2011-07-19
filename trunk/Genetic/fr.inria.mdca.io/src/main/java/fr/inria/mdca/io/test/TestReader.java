package fr.inria.mdca.io.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import fr.inria.mdca.core.model.BaseInstance;
import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.core.model.path.Path;
import fr.inria.mdca.io.DataReader;

public class TestReader {

	DataReader reader=new DataReader();
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testReadModel() {
		BaseModel m=null;
		try {
			m=reader.readModel(new File("testFiles/model.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue(m!=null);
	}

	@Test
	public void testReadInstances() {
		ArrayList<BaseInstance> instance = null;
		
		try {
			instance = reader.readInstances(new File("testFiles/instances.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue(instance!=null);
	}

	@Test
	public void testReadPaths() {
		ArrayList<Path> paths = null;
		
		try {
			paths=reader.readPaths(new File("testFiles/paths.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue(paths!=null);
		assertTrue(paths.size()==4);
	}

}
