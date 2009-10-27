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
import fr.inria.peerunit.util.TesterUtil;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
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

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testValidate() {
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

    @Test
    public void testRegister() {
        Method[] methods =  Data.class.getMethods();
        List<String> names = new ArrayList<String>(methods.length);
        List<MethodDescription> l = wrapper.register(fr.inria.peerunit.base.Data.class);
        assertTrue(l.size() == 8);

        for(Method each : methods) {
            names.add(each.getName());
        }

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
     * Test of validatePeerRange method, of class TestCaseWrapper.
     */
    @Test
    public void testValidatePeerRange() {
        System.out.println("validatePeerRange");
        int from = 0;
        int to = 0;
        TestCaseWrapper instance = null;
        boolean expResult = false;
        boolean result = instance.validatePeerRange(from, to);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMethod method, of class TestCaseWrapper.
     */
    @Test
    public void testGetMethod() {
        System.out.println("getMethod");
        MethodDescription md = null;
        TestCaseWrapper instance = null;
        Method expResult = null;
        Method result = instance.getMethod(md);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of invoke method, of class TestCaseWrapper.
     */
    @Test
    public void testInvoke() throws Exception {
        System.out.println("invoke");
        MethodDescription md = null;
        TestCaseWrapper instance = null;
        //instance.invoke(md);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isLastMethod method, of class TestCaseWrapper.
     */
    @Test
    public void testIsLastMethod() {
        System.out.println("isLastMethod");
        TestCaseWrapper instance = null;
        boolean expResult = false;
        boolean result = instance.isLastMethod();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isValid method, of class TestCaseWrapper.
     */
    @Test
    public void testIsValid_AfterClass() {
        System.out.println("isValid");
        fr.inria.peerunit.parser.AfterClass a = null;
        TestCaseWrapper instance = null;
        boolean expResult = false;
        boolean result = instance.isValid(a);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isValid method, of class TestCaseWrapper.
     */
    @Test
    public void testIsValid_BeforeClass() {
        System.out.println("isValid");
        fr.inria.peerunit.parser.BeforeClass a = null;
        TestCaseWrapper instance = null;
        boolean expResult = false;
        boolean result = instance.isValid(a);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isValid method, of class TestCaseWrapper.
     */
    @Test
    public void testIsValid_TestStep() {
        System.out.println("isValid");
        TestStep a = null;
        TestCaseWrapper instance = null;
        boolean expResult = false;
        boolean result = instance.isValid(a);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
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

    /**
     * Test of getTestcase method, of class TestCaseWrapper.
     */
    @Test
    public void testGetTestcase() {
        System.out.println("getTestcase");
        TestCaseWrapper instance = null;
        TestCase expResult = null;
        TestCase result = instance.getTestcase();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMethods method, of class TestCaseWrapper.
     */
    @Test
    public void testGetMethods() {
        System.out.println("getMethods");
        TestCaseWrapper instance = null;
        Map expResult = null;
        Map result = instance.getMethods();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
