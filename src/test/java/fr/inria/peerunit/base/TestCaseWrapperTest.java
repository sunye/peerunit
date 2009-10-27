/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit.base;

import fr.inria.peerunit.GlobalVariables;
import fr.inria.peerunit.GlobalVariablesImpl;
import fr.inria.peerunit.TestCase;
import fr.inria.peerunit.exception.AnnotationFailure;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.rmi.coord.CoordinatorImpl;
import fr.inria.peerunit.rmi.tester.TesterImpl;
import fr.inria.peerunit.test.assertion.AssertionFailedError;
import fr.inria.peerunit.util.TesterUtil;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 *
 * @author sunye
 */
public class TestCaseWrapperTest {

    private static Data testcase;
    private static TestCaseWrapper wrapper;
    private static CoordinatorImpl coord;
    private static GlobalVariables globals;
    private static TesterImpl tester;
    private static Logger LOG = Logger.getLogger("test");

    public TestCaseWrapperTest() {
    }

    @BeforeClass
    public static void inititalize() {
        Properties properties = new Properties();
        properties.setProperty("tester.peers", "3");
        properties.setProperty("tester.log.dateformat", "yyyy-MM-dd");
        properties.setProperty("tester.log.timeformat", "HH:mm:ss.SSS");
        properties.setProperty("tester.log.level", "FINEST");
        properties.setProperty("tester.logfolder", "/tmp/");
        properties.setProperty("tester.log.delimiter", "|");
        properties.setProperty("tester.waitForMethod", "500");
        try {
            TesterUtil defaults = new TesterUtil(properties);
            coord = new CoordinatorImpl(defaults);
            globals = new GlobalVariablesImpl();
            new Thread(coord, "Coordinator").start();
            tester = new TesterImpl(coord, globals);
            tester.setCoordinator(coord);
            wrapper = new TestCaseWrapper(tester);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }


    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

     /**
     * Test of validatePeerRange method, of class TestCaseWrapper.
     */
    @Test
    public void testValidatePeerRange() {
        try {
            wrapper.validatePeerRange(0, -1);
            fail("Exception not catch");
        } catch (AnnotationFailure af) {
            assertEquals(af.getLocalizedMessage(), "Annotation FROM without TO");
        }
        try {
            wrapper.validatePeerRange(-1, 0);
            fail("Exception not catch");
        } catch (AnnotationFailure af) {
            assertEquals(af.getLocalizedMessage(), "Annotation TO without FROM");
        }

        try {
            wrapper.validatePeerRange(-1, -4);
            fail("Exception not catch");
        } catch (AnnotationFailure af) {
            assertEquals(af.getLocalizedMessage(), "Invalid value for FROM / TO");
        }

        try {
            wrapper.validatePeerRange(4, 0);
            fail("Exception not catch");
        } catch (AnnotationFailure af) {
            assertEquals(af.getLocalizedMessage(), "The value of FROM must be smaller than TO");
        }

    }

    /**
     * Tests the TestCaseWrapper.register() method.
     */
    @Test
    public void testRegister() {
        Method[] methods =  Data.class.getMethods();
        List<String> names = new ArrayList<String>(methods.length);
        List<MethodDescription> l = wrapper.register(fr.inria.peerunit.base.Data.class);

        // Only 8 methods should be registered for tester 0
        assertTrue(l.size() == 8);

        for(Method each : methods) {
            names.add(each.getName());
        }

        // All registered methods must belong to the test case.
        for (MethodDescription each : l) {
            assertTrue(names.contains(each.getName()));
        }
    }


     @Test
    public void testRegisterBis() throws Exception {
        Class<? extends TestCase> c = Data.class;
        List<MethodDescription> listMethodDesc = wrapper.register(c);

        //int id2 = executor.getTester().getId();
        //TestCase testcase = executor.getTestcase();

        fr.inria.peerunit.parser.TestStep t;
        fr.inria.peerunit.parser.BeforeClass bc;
        fr.inria.peerunit.parser.AfterClass ac;
        int valid = 0;

        for (Method each : c.getMethods()) {
            t = (fr.inria.peerunit.parser.TestStep) each.getAnnotation(TestStep.class);
            if (wrapper.isValid(t)) {
                valid++;
                assertTrue(listMethodDesc.contains(new MethodDescription(each, t)));
            }
        }

        for (Method each : c.getMethods()) {
            bc = (fr.inria.peerunit.parser.BeforeClass) each.getAnnotation(fr.inria.peerunit.parser.BeforeClass.class);
            if (wrapper.isValid(bc)) {
                valid++;
                assertTrue(listMethodDesc.contains(new MethodDescription(each, bc)));
            }
        }

        for (Method each : c.getMethods()) {
            ac = each.getAnnotation(fr.inria.peerunit.parser.AfterClass.class);
            if (wrapper.isValid(ac)) {
                valid++;
                assertTrue(listMethodDesc.contains(new MethodDescription(each, ac)));
                assertFalse(wrapper.isLastMethod());
            }
        }

        assertEquals(wrapper.getMethods().size(), valid);
        assertEquals(listMethodDesc.size(), valid);
    }

    /**
     * Test of getMethod method, of class TestCaseWrapper.
     */
    @Test
    public void testGetMethod() {
        List<MethodDescription> listMethodDesc = wrapper.register(Data.class);
        List<Method> methods =  java.util.Arrays.asList(Data.class.getMethods());

        for (MethodDescription each : listMethodDesc) {
            Method m = wrapper.getMethod(each);
            assertTrue(methods.contains(m));
        }
    }

    /**
     * Test of invoke method, of class TestCaseWrapper.
     */
    @Test
    public void testInvokeFailure() throws Exception {
        wrapper.register(Sample.class);
        MethodDescription md = new MethodDescription("failure","action2", 2, "TestStep", 1000);

        assertNotNull(wrapper.getMethod(md));

        try {
            wrapper.invoke(md);
            fail("Exception not thrown");
        } catch (AssertionFailedError ex) {

        } catch (Throwable t) {
            fail("Wrong exception thrown");
        }
    }
    /**
     * Test of invoke method, of class TestCaseWrapper.
     */

    @Test
    public void testInvokeFailureBis() throws Exception {
        wrapper.register(Sample.class);
        MethodDescription md = new MethodDescription("failureBis","action3", 3, "TestStep", 1000);

        assertNotNull(wrapper.getMethod(md));

        try {
            wrapper.invoke(md);
            fail("Exception not thrown");
        } catch (AssertionError ex) {

        } catch (Throwable t) {
            fail("Wrong exception thrown");
        }
    }

     /**
     * Test of invoke method, of class TestCaseWrapper.
     */
    @Test
    public void testInvokePass() throws Exception {
        wrapper.register(Sample.class);
        MethodDescription md = new MethodDescription("first","action1", 1, "TestStep", 1000);
        
        assertNotNull(wrapper.getMethod(md));
        try {
            wrapper.invoke(md);
            
        } catch (Throwable t) {
            fail("Exception thrown");
        }
    }


    /**
     * Test of isLastMethod method, of class TestCaseWrapper.
     */
    @Test
    public void testIsLastMethod() {
        wrapper.register(Sample.class);

        assertFalse(wrapper.isLastMethod());
        for(MethodDescription each : wrapper.getMethods().keySet()) {
            try {
                wrapper.invoke(each);
            } catch (Throwable ex) {}
        }
        assertTrue(wrapper.isLastMethod());
    }

    /**
     * Test of isValid method, of class TestCaseWrapper.
     */
    @Test
    public void testIsValid_AfterClass() {
        fr.inria.peerunit.parser.AfterClass a = mock(fr.inria.peerunit.parser.AfterClass.class);
        when(a.place()).thenReturn(-1);
        when(a.from()).thenReturn(0);
        when(a.to()).thenReturn(1);

        assertTrue(wrapper.isValid(a));
    }

    /**
     * Test of isValid method, of class TestCaseWrapper.
     */
    @Test
    public void testIsValid_BeforeClass() {
        fr.inria.peerunit.parser.BeforeClass bc = mock(fr.inria.peerunit.parser.BeforeClass.class);
        when(bc.place()).thenReturn(-1);
        when(bc.from()).thenReturn(0);
        when(bc.to()).thenReturn(1);

        assertTrue(wrapper.isValid(bc));
    }

    /**
     * Test of isValid method, of class TestCaseWrapper.
     */
    @Test
    public void testIsValid_TestStep() {
        fr.inria.peerunit.parser.TestStep ts = mock(fr.inria.peerunit.parser.TestStep.class);
        when(ts.place()).thenReturn(-1);
        when(ts.from()).thenReturn(0);
        when(ts.to()).thenReturn(1);

        assertTrue(wrapper.isValid(ts));
    }

    /**
     * Test of shouldIExecute method, of class TestCaseWrapper.
     */
    @Test
    public void testShouldIExecute() {
        int id = tester.getId();

        assertTrue(wrapper.shouldIExecute(id, -1, 2));
        assertFalse(wrapper.shouldIExecute(2, 1, 4));
        assertTrue(wrapper.shouldIExecute(-1, -1, -1));
        assertTrue(wrapper.shouldIExecute(id + 1, id - 2, id + 2));
        assertFalse(wrapper.shouldIExecute(id + 5, id + 2, id + 7));
    }
}
