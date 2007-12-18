package fr.inria.peerunit.parser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.Parser;
import fr.inria.peerunit.exception.AnnotationFailure;
import fr.inria.peerunit.rmi.tester.TesterImpl;

public class ParserImpl implements Parser{
	private List<MethodDescription> mList = new ArrayList<MethodDescription>();
	private static  Logger log;
	private int peerName=-1;

	public List<MethodDescription> parse(Class c) {

		for (Method m : c.getMethods()) {
			String annotation = null;

			for (Annotation a : m.getAnnotations()) {
				annotation = a.annotationType().getSimpleName();	
				MethodDescription md = new MethodDescription();
				if (annotation.equalsIgnoreCase("Test")) {
					if(!hasFailure(m.getAnnotation(Test.class).from(),m.getAnnotation(Test.class).to())
							&& verify(m,m.getAnnotation(Test.class).place(),m.getAnnotation(Test.class).from(),m.getAnnotation(Test.class).to())){					
						md.setDescription(m.getName(), m.getAnnotation(Test.class).name(),
								m.getAnnotation(Test.class).step(), annotation, m.getAnnotation(Test.class).timeout());
						mList.add(md);	
					}
				} else if (annotation.equalsIgnoreCase("BeforeClass")) {
					if(!hasFailure(m.getAnnotation(BeforeClass.class).from(),m.getAnnotation(BeforeClass.class).to())
							&& verify(m,m.getAnnotation(BeforeClass.class).place(),m.getAnnotation(BeforeClass.class).from(),m.getAnnotation(BeforeClass.class).to())){		
						md.setDescription(m.getName(), "BeforeClass",
								Integer.MIN_VALUE, "BeforeClass", m.getAnnotation(BeforeClass.class).timeout());
						mList.add(md);	
					}
				} else if (annotation.equalsIgnoreCase("AfterClass")) {
					if(!hasFailure(m.getAnnotation(AfterClass.class).from(),m.getAnnotation(AfterClass.class).to())
							&&verify(m,m.getAnnotation(AfterClass.class).place(),m.getAnnotation(AfterClass.class).from(),m.getAnnotation(AfterClass.class).to())){
						md.setDescription(m.getName(), "AfterClass",
								Integer.MAX_VALUE, "AfterClass", m.getAnnotation(AfterClass.class).timeout());
						mList.add(md);	
					}
				}
			}			
		}
		return mList;
	}
	
	private boolean hasFailure(int from, int to) {
		if ((from > -1) && (to == -1)) {
			throw new AnnotationFailure("Annotation FROM without TO");
		} else if ((from == -1) && (to > -1)) {
			throw new AnnotationFailure("Annotation TO without FROM");
		} else if ((from < -1) || (to < -1)) {
			throw new AnnotationFailure("Invalid value for FROM / TO");
		} else if ((from >= to) && (from != -1)) {
			throw new AnnotationFailure("The value of FROM must be smaller than TO");		
		} else return false;
	}	
	
	private boolean verify(Method m, int place,int from, int to) {
		if (((from > -1) && (to > -1))
				&& ((peerName >= from) && (peerName <= to))) {
			log.log(Level.FINEST,"I will execute " + m.getName());
			return true;
		} else if (peerName == place) {
			log.log(Level.FINEST,"I will execute " + m.getName());
			return true;
		} else if ((place == -1) && (from == -1) && (to == -1)) {
			log.log(Level.FINEST,"I will execute " + m.getName());
			return true;
		} else {
			log.log(Level.FINEST,"I do nothing in " + m.getName());
			return false;
		}
	}
	
	public boolean isLastMethod(String methodAnnotation) {
		if(methodAnnotation.equalsIgnoreCase("AfterClass"))
			return true;
		else
			return false;
	}

	public void setPeerName(int peerName) {
		this.peerName=peerName;		
	}
	
	public void setLogger(Logger log) {
		this.log=log;		
	}
}

