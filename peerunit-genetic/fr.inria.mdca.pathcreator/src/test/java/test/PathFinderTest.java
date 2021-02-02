package test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import fr.inria.mdca.core.model.BaseInstance;
import fr.inria.mdca.core.model.BaseModel;
import fr.inria.mdca.core.model.BaseModelElement;
import fr.inria.mdca.core.model.path.Path;
import fr.inria.mdca.pathcreator.PathFinder;
import fr.inria.mdca.pathcreator.PathFinder.PathFindQueryReponse;

public class PathFinderTest {
	
	ArrayList<BaseModelElement> elements=new ArrayList<BaseModelElement>();
	ArrayList<BaseInstance> instances=new ArrayList<BaseInstance>();
	
	ArrayList<Path> paths=new ArrayList<Path>();
	
	BaseModel model;
	
	@Before
	public void setUp() throws Exception {
		
		BaseModelElement element0=new BaseModelElement(3,"test");
		BaseModelElement element1=new BaseModelElement(3,"test 1");
		BaseModelElement element2=new BaseModelElement(3,"test 2");
		
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
	public void testFindMissingPathsAllContained() {
		PathFinder f=new PathFinder();
		PathFindQueryReponse answer = f.findMissingPaths(instances, paths);
		assertTrue(answer.getMissing().size()==0);
		assertTrue(answer.getPathNum()==3);
	}

	@Test
	public void testFindMissingPathsMissing() {
		
		Path p0=new Path(2);
		p0.getIndexes()[0]=0;
		p0.getIndexes()[1]=1;
		p0.getValues().get(0).add(1);
		p0.getValues().get(0).add(1);
		p0.getValues().get(0).add(3);
		p0.getValues().get(1).add(2);
		p0.getValues().get(1).add(1);

		Path p1=new Path(2);
		p1.getIndexes()[0]=0;
		p1.getIndexes()[1]=2;
		p1.getValues().get(0).add(1);
		p1.getValues().get(0).add(1);
		p1.getValues().get(1).add(2);
		p1.getValues().get(1).add(2);
		p1.getValues().get(1).add(1);
		
		this.paths.add(p0);
		this.paths.add(p1);
		
		PathFinder f=new PathFinder();
		PathFindQueryReponse answer = f.findMissingPaths(instances, paths);
		assertTrue(answer.getMissing().size()==2);
		assertTrue(answer.getPathNum()==3);
		assertTrue(answer.getContained().size()==3);
	}

}
	