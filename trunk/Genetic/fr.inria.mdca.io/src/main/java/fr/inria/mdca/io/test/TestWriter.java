package fr.inria.mdca.io.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import fr.inria.mdca.core.model.BaseInstance;
import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.core.model.BaseModelElement;
import fr.inria.mdca.core.model.path.Path;
import fr.inria.mdca.io.DataReader;
import fr.inria.mdca.io.DataWriter;

public class TestWriter {
	DataWriter writer=new DataWriter();
	DataReader reader=new DataReader();
	ArrayList<BaseModelElement> elements=new ArrayList<BaseModelElement>();
	ArrayList<BaseInstance> instances=new ArrayList<BaseInstance>();
	
	ArrayList<Path> paths=new ArrayList<Path>();
	
	BaseModel model;
	@Before
	public void setUp() throws Exception {
		BaseModelElement element0=new BaseModelElement(3,"test 1");
		BaseModelElement element1=new BaseModelElement(3,"test 2");
		BaseModelElement element2=new BaseModelElement(3,"test 3");
		
		elements.add(element0);
		elements.add(element1);
		elements.add(element2);
		model=new BaseModel();
		
		model.setOrder(2);
		model.setTwise(2);
		
		for(BaseModelElement element:elements){
			model.addElement(element);
		}
		
		
		BaseInstance instance=new BaseInstance(model);
		instance.getValues()[0]=1;
		instance.getValues()[1]=1;
		instance.getValues()[2]=1;
		
		
		BaseInstance instance2=new BaseInstance(model);
		instance2.getValues()[0]=1;
		instance2.getValues()[1]=2;
		instance2.getValues()[2]=1;
		
		
		BaseInstance instance3=new BaseInstance(model);
		instance3.getValues()[0]=2;
		instance3.getValues()[1]=1;
		instance3.getValues()[2]=2;

		BaseInstance instance4=new BaseInstance(model);
		instance4.getValues()[0]=3;
		instance4.getValues()[1]=1;
		instance4.getValues()[2]=2;
		
		BaseInstance instance5=new BaseInstance(model);
		instance5.getValues()[0]=2;
		instance5.getValues()[1]=3;
		instance5.getValues()[2]=1;
		
		instances.add(instance);
		instances.add(instance2);
		instances.add(instance3);
		instances.add(instance4);
		instances.add(instance5);
		
		Path p0=new Path(2);
		p0.getIndexes()[0]=0;
		p0.getIndexes()[1]=1;
		p0.getValues().get(0).add(1);
		p0.getValues().get(0).add(2);
		p0.getValues().get(0).add(3);
		p0.getValues().get(1).add(2);
		p0.getValues().get(1).add(1);

		Path p1=new Path(2);
		p1.getIndexes()[0]=1;
		p1.getIndexes()[1]=2;
		p1.getValues().get(0).add(1);
		p1.getValues().get(0).add(1);
		p1.getValues().get(1).add(2);
		p1.getValues().get(1).add(2);
		p1.getValues().get(1).add(1);
		
		
		Path p2=new Path(3);
		p2.getIndexes()[0]=0;
		p2.getIndexes()[1]=1;
		p2.getIndexes()[2]=2;
		p2.getValues().get(0).add(1);
		p2.getValues().get(0).add(1);
		p2.getValues().get(1).add(-1);
		p2.getValues().get(1).add(2);
		p2.getValues().get(1).add(1);
		p2.getValues().get(2).add(-1);
		p2.getValues().get(2).add(1);
		p2.getValues().get(2).add(2);
		p2.getValues().get(2).add(2);
		
		this.paths.add(p0);
		this.paths.add(p1);
		this.paths.add(p2);
		
	}

	@Test
	public void testWriteModel() {
		BaseModel m=null;
		
		String file="testFiles/modelWrite.txt";
		
		File f=new File(file);
		if(f.exists()){
			f.delete();
		}
		try {
			writer.write(model, new File(file));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			m=reader.readModel(new File(file));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assert(m!=null);
	}

	@Test
	public void testWriteInstances() {
		
			ArrayList<BaseInstance> instances=null;
			
			String file="testFiles/instancesWrite.txt";
			
			File f=new File(file);
			if(f.exists()){
				f.delete();
			}
			try {
				writer.write(this.instances, new File(file));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				instances=reader.readInstances(new File(file));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			assert(instances!=null);
	}

	@Test
	public void testWritePaths() {
		ArrayList<Path> paths=null;
		
		String file="testFiles/pathWrite.txt";
		
		File f=new File(file);
		if(f.exists()){
			f.delete();
		}
		try {
			writer.writePaths(this.paths, new File(file));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			paths=reader.readPaths(new File(file));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assert(paths!=null);
	}

}
