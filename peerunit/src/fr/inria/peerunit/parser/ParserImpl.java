package fr.inria.peerunit.parser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import fr.inria.peerunit.Parser;
import fr.inria.peerunit.exception.AnnotationFailure;
import fr.inria.peerunit.rmi.tester.TesterImpl;

public class ParserImpl implements Parser{
	private List<MethodDescription> mList = new ArrayList<MethodDescription>();
	private static final Logger log = Logger.getLogger(TesterImpl.class
			.getName());
	private int peerName=-1;
		
	public List<MethodDescription> parse(Class c) {
		for (Method m : c.getMethods()) {
			String annotation = null;

			for (Annotation a : m.getAnnotations()) {
				annotation = a.annotationType().getSimpleName();
				if (annotation.equalsIgnoreCase("Test")) {
					verify(m, annotation, m.getAnnotation(Test.class).name(), 
							m.getAnnotation(Test.class).place(), 
							m.getAnnotation(Test.class).timeout(), 
							m.getAnnotation(Test.class).step(),
							m.getAnnotation(Test.class).from(),
							m.getAnnotation(Test.class).to());
				} else if (annotation.equalsIgnoreCase("BeforeClass")) {
					verify(m, "BeforeClass", "BeforeClass", 
							m.getAnnotation(BeforeClass.class).place(), 
							m.getAnnotation(BeforeClass.class).timeout(), 
							Integer.MIN_VALUE,
							m.getAnnotation(BeforeClass.class).from(),
							m.getAnnotation(BeforeClass.class).to());
				} else if (annotation.equalsIgnoreCase("AfterClass")) {
					verify(m, "AfterClass", "AfterClass", 
							m.getAnnotation(AfterClass.class).place(), 
							m.getAnnotation(AfterClass.class).timeout(), 
							Integer.MAX_VALUE,
							m.getAnnotation(AfterClass.class).from(),
							m.getAnnotation(AfterClass.class).to());
				} 
			}			
		}
		return mList;
	}
	
	private void verify(Method m, String annotation, String name, int place,
			int timeout, int step,int from, int to) {		
		if ((from > -1) && (to == -1)) {
			throw new AnnotationFailure("Annotation FROM without TO");
		} else if ((from == -1) && (to > -1)) {
			throw new AnnotationFailure("Annotation TO without FROM");
		} else if ((from >= to) && (from != -1)) {
			throw new AnnotationFailure(
					"The value of FROM must be smaller than TO");
		} else if (((from > -1) && (to > -1))
				&& ((peerName >= from) && (peerName <= to))) {
			log.info("[Parser] I will execute a " + m.getName());
			MethodDescription md = new MethodDescription(m.getName(), name,
					step, annotation, timeout);
			mList.add(md);
		} else if (peerName == place) {
			log.info("[Parser] I will execute b " + m.getName());
			MethodDescription md = new MethodDescription(m.getName(), name,
					step, annotation, timeout);
			mList.add(md);
		} else if ((place == -1) && (from == -1) && (to == -1)) {
			log.info("[Parser] I will execute c " + m.getName());
			MethodDescription md = new MethodDescription(m.getName(), name,
					step, annotation, timeout);
			mList.add(md);
		} else {
			log.info("[Parser] I do nothing in " + m.getName() + " place"
					+ place + " " + from + " " + to + " " + peerName);
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
}

