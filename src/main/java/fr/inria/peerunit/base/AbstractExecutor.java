/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit.base;

import fr.inria.peerunit.Executor;
import fr.inria.peerunit.TestCase;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.exception.AnnotationFailure;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.parser.TestStep;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sunye
 */
public abstract class AbstractExecutor implements Executor {

    private Map<MethodDescription, Method> methods = new TreeMap<MethodDescription, Method>();
    private TestCase testcase;
    private int testerId = -1;
    private Tester tester;

    public AbstractExecutor(Tester t, Logger l) {
        assert t != null ;

        this.tester = t;
        try {
            this.testerId = tester.getId();
        } catch (RemoteException ex) {
            Logger.getLogger(AbstractExecutor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Verify a peer range (annotation)
     *
     * @param from : number of the first peer
     * @param to : number of the last peer
     * @return boolean :
     */
    public boolean validatePeerRange(int from, int to) {
        if ((from > -1) && (to == -1)) {
            throw new AnnotationFailure("Annotation FROM without TO");
        } else if ((from == -1) && (to > -1)) {
            throw new AnnotationFailure("Annotation TO without FROM");
        } else if ((from < -1) || (to < -1)) {
            throw new AnnotationFailure("Invalid value for FROM / TO");
        } else if ((from >= to) && (from != -1)) {
            throw new AnnotationFailure("The value of FROM must be smaller than TO");
        } else {
            return false;
        }
    }

    /**
     * Get the method following methodDescription
     * @param md Methode Description
     * @return the method
     */
    public Method getMethod(MethodDescription md) {
        return methods.get(md);
    }

    /**
     * Execute the given method description
     *
     * @param md : method description to execute
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public void invoke(MethodDescription md) throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {

        assert methods.containsKey(md) : "Method should be registered";
        assert testcase != null : "Test Case instance should not be null";

        Method m = methods.get(md);
        m.invoke(testcase, (Object[]) null);
    }

    /**
     * Verifies if the method is the last one to be executed by its annotation
     * @param method
     * @return true if the method is the last one to be executed
     */
    public boolean isLastMethod(String methodAnnotation) {
        return methodAnnotation.equalsIgnoreCase("AfterClass");
    }

    /**
     * Test if a AfterClass is valid and the ExecutorImpl must execute it.
     * @param a AfterClass
     * @return
     */
    public boolean isValid(AfterClass a) {
        return (a != null) && shouldIExecute(a.place(), a.from(), a.to());
    }

    /**
     *
     * Test if a BeforeClass is valid and the ExecutorImpl must execute it.
     * @param a BeforeClass
     * @return
     */
    public boolean isValid(BeforeClass a) {
        return (a != null) && shouldIExecute(a.place(), a.from(), a.to());
    }

    /**
     *
     * Test if a Test is valid and the ExecutorImpl must execute it.
     * @param a Test
     * @return
     */
    public boolean isValid(TestStep a) {
        return (a != null) && shouldIExecute(a.place(), a.from(), a.to());
    }

    /** Verify if the tester must execute the method
     * @param place
     * @param from : number of the first tester
     * @param to : number of the last tester
     * @return
     */
    /**
     * Verify if the tester must execute the method
     * @param place
     * @param from : number of the first tester
     * @param to : number of the last tester
     * @return
     */
    public boolean shouldIExecute(int place, int from, int to) {
            assert testerId != -1 : "Tester not initialized";

        return (place == testerId) || (place == -1 && from == -1 && to == -1) || ((from <= testerId) && (to >= testerId));
    }

    /**
     * Parse the test case to extract the methods to be executed
     * @param class
     * @return List of methods to be executed
     */
    public List<MethodDescription> register(Class<? extends TestCase> c) {
        TestStep t;
        BeforeClass bc;
        AfterClass ac;
        methods.clear();




        try {
            testcase = c.newInstance();
            testcase.setTester(tester);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


        for (Method each : c.getMethods()) {
            t = each.getAnnotation(TestStep.class);
            if (this.isValid(t)) {
                methods.put(new MethodDescription(each, t), each);
            }
        }
        for (Method each : c.getMethods()) {
            bc = each.getAnnotation(BeforeClass.class);
            if (this.isValid(bc)) {
                methods.put(new MethodDescription(each, bc), each);
            }
        }
        for (Method each : c.getMethods()) {
            ac = each.getAnnotation(AfterClass.class);
            if (this.isValid(ac)) {
                methods.put(new MethodDescription(each, ac), each);
            }
        }
        return new ArrayList<MethodDescription>(methods.keySet());
    }

/*    public Tester getTester() {
        return tester;
    }
*/
    public TestCase getTestcase() {
        return testcase;
    }

    public Map<MethodDescription, Method> getMethods() {
        return methods;
    }
}
