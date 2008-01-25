package fr.inria.peerunit.parser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.Parser;
import fr.inria.peerunit.exception.AnnotationFailure;
import fr.inria.peerunit.rmi.tester.TesterImpl;

public class ParserImpl implements Parser {
	//private
	private static  Logger log;
	private int testerId;

	public ParserImpl(int i, Logger l) {
		log = l;
		testerId = i;
	}

	public List<MethodDescription> parse(Class c) {
		List<MethodDescription> mList = new ArrayList<MethodDescription>();

		for (Method m : c.getMethods()) {
			String annotation = null;

			for (Annotation a : m.getAnnotations()) {
				annotation = a.annotationType().getSimpleName();
				MethodDescription md = new MethodDescription();
				if (annotation.equalsIgnoreCase("Test")) {
					if(!hasFailure(m.getAnnotation(Test.class).from(),m.getAnnotation(Test.class).to())
							&& shouldIExecute(m,m.getAnnotation(Test.class).place(),m.getAnnotation(Test.class).from(),m.getAnnotation(Test.class).to())){
						md.setDescription(m.getName(), m.getAnnotation(Test.class).name(),
								m.getAnnotation(Test.class).step(), annotation, m.getAnnotation(Test.class).timeout());
						mList.add(md);
					}
				} else if (annotation.equalsIgnoreCase("BeforeClass")) {
					if(!hasFailure(m.getAnnotation(BeforeClass.class).from(),m.getAnnotation(BeforeClass.class).to())
							&& shouldIExecute(m,m.getAnnotation(BeforeClass.class).place(),m.getAnnotation(BeforeClass.class).from(),m.getAnnotation(BeforeClass.class).to())){
						md.setDescription(m.getName(), "BeforeClass",
								Integer.MIN_VALUE, "BeforeClass", m.getAnnotation(BeforeClass.class).timeout());
						mList.add(md);
					}
				} else if (annotation.equalsIgnoreCase("AfterClass")) {
					if(!hasFailure(m.getAnnotation(AfterClass.class).from(),m.getAnnotation(AfterClass.class).to())
							&&shouldIExecute(m,m.getAnnotation(AfterClass.class).place(),m.getAnnotation(AfterClass.class).from(),m.getAnnotation(AfterClass.class).to())){
						md.setDescription(m.getName(), "AfterClass",
								Integer.MAX_VALUE, "AfterClass", m.getAnnotation(AfterClass.class).timeout());
						mList.add(md);
					}
				}
			}
		}
		return mList;
	}

	public boolean hasFailure(int from, int to) {
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

	public boolean shouldIExecute(Method m, int place,int from, int to) {

		assert m != null;
		assert log != null;



		if (((from > -1) && (to > -1))
				&& ((testerId >= from) && (testerId <= to))) {
			log.log(Level.FINEST,"I will execute " + m.getName());
			return true;
		} else if (testerId == place) {
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


	public List<MethodDescription> parseBis(Class<?> c) {
		List<MethodDescription> result = new LinkedList<MethodDescription>();
		Test t;
		BeforeClass bc;
		AfterClass ac;

		for(Method each : c.getMethods()) {
			t = each.getAnnotation(Test.class);
			if (this.isValid(t)) {
				result.add(new MethodDescription(each, t));
			}
		}

		for(Method each : c.getMethods()) {
			bc = each.getAnnotation(BeforeClass.class);
			if (this.isValid(bc)) {
				result.add(new MethodDescription(each, bc));
			}
		}

		for(Method each : c.getMethods()) {
			ac = each.getAnnotation(AfterClass.class);
			if (this.isValid(ac)) {
				System.out.println(ac.place());
				result.add(new MethodDescription(each, ac));
			}
		}

		return result;
	}



	public boolean isLastMethod(String methodAnnotation) {
		if(methodAnnotation.equalsIgnoreCase("AfterClass"))
			return true;
		else
			return false;
	}

/*	public void setPeerName(int peerName) {
		this.peerName=peerName;
	}*/

	public void setLogger(Logger l) {
		log=l;
	}

	private boolean isValid(Test a) {
		if (a == null) return false;

		return true;
	}

	private boolean isValid(BeforeClass a) {
		if (a == null) return false;

		return true;
	}

	private boolean isValid(AfterClass a) {
		if (a == null) return false;

		return true;
	}

	class IdRange {
		private int lower;
		private int upper;

		public IdRange(int place, int from, int to) {

		}
	}
}

