/*
This file is part of PeerUnit.

PeerUnit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

PeerUnit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with PeerUnit.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.inria.peerunit.base;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.TestCase;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.AfterClassMethod;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.BeforeClassMethod;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.parser.TestStepMethod;

/**
 *
 * @author sunye
 */
public class TestCaseWrapper {

    private static Logger LOG = Logger.getLogger(TestCaseWrapper.class.getName());
    private Map<MethodDescription, Method> methods = new TreeMap<MethodDescription, Method>();
    private List<MethodDescription> remainingMethods;
    private TestCase testcase;
    private int testerId = -1;
    private Tester tester;

    public TestCaseWrapper(Tester t) {
        assert t != null;

        this.tester = t;
        try {
            this.testerId = tester.getId();
        } catch (RemoteException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Get the method following methodDescription
     * @param md Method Description
     * @return the method
     */
    public Method getMethod(MethodDescription md) {
        return methods.get(md);
    }

    /**
     * Execute the given method description
     *
     * @param md : method description to execute
     * @throws Throwable if any exception is thrown
     */
    public void invoke(MethodDescription md) throws Throwable {

        assert methods.containsKey(md) : "Method should be registered";
        assert remainingMethods.contains(md) : "Method already executed";
        assert testcase != null : "Test Case instance should not be null";

        Method m = methods.get(md);
        try {
            m.invoke(testcase, (Object[]) null);
        } catch (IllegalAccessException e) {
            e.fillInStackTrace();
        } catch (InvocationTargetException e) {
            e.fillInStackTrace();
            throw e.getCause();
        } finally {
            remainingMethods.remove(md);
        }
    }

    /**
     * Verifies if no more methods remain.
     * 
     * @return true if the method is the last one to be executed
     */
    public boolean isLastMethod() {

        return remainingMethods.isEmpty();
    }


    /**
     * Parse the test case to extract the methods to be executed
     * @param class
     * @return List of methods to be executed
     */
    public List<MethodDescription> register(Class<? extends TestCase> c) {

        ClassFilter filter = new ClassFilter(c);
        methods.clear();
        
        //TestStep
        for(TestStepMethod each : filter.stepMethods()) {
            if (each.range().includes(testerId)) {
                methods.put(new MethodDescription(each), each.method());
            }
        }

        //AfterClass
        //TODO: At most 1 AfterClass method.
         for(AfterClassMethod each : filter.afterMethods()) {
            if (each.range().includes(testerId)) {
                methods.put(new MethodDescription(each), each.method());
            }
        }

        //BeforeClass
        //TODO: At most 1 AfterClass method.
          for(BeforeClassMethod each : filter.beforeMethods()) {
            if (each.range().includes(testerId)) {
                methods.put(new MethodDescription(each), each.method());
            }
        }

        
        //TestCase instantiation
        try {
            testcase = c.newInstance();
            testcase.setTester(tester);
        } catch (InstantiationException e) {
            LOG.log(Level.SEVERE, "Instantiation Exception", e);
        } catch (IllegalAccessException e) {
            LOG.log(Level.SEVERE, "Illegal Access Exception", e);
        }

        remainingMethods = new ArrayList<MethodDescription>(methods.keySet());
        return remainingMethods;
    }

    public TestCase getTestcase() {
        return testcase;
    }

    public Map<MethodDescription, Method> getMethods() {
        return methods;
    }
}

class ClassFilter {

    private List<TestStepMethod> stepMethods = new ArrayList<TestStepMethod>();
    private List<BeforeClassMethod> beforeMethods = new ArrayList<BeforeClassMethod>();
    private List<AfterClassMethod> afterMethods = new ArrayList<AfterClassMethod>();

    public ClassFilter(Class<?> c) {

        Method[] methods = c.getMethods();
        for (Method each : methods) {
            if (each.isAnnotationPresent(TestStep.class)) {
                stepMethods.add(new TestStepMethod(each));
            } else if (each.isAnnotationPresent(BeforeClass.class)) {
                beforeMethods.add(new BeforeClassMethod(each));
            } else if (each.isAnnotationPresent(AfterClass.class)) {
                afterMethods.add(new AfterClassMethod(each));
            }
        }
    }

    public List<TestStepMethod> stepMethods() {
        return stepMethods;
    }

    public List<BeforeClassMethod> beforeMethods() {
        return beforeMethods;
    }

    public List<AfterClassMethod> afterMethods() {
        return afterMethods;
    }

}


