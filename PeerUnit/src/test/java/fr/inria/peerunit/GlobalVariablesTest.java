/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit;

import fr.inria.peerunit.util.TesterUtil;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
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
public class GlobalVariablesTest {

    private GlobalVariables globals;
    private static CoordinatorRunner runner;
    private Registry registry;

    public GlobalVariablesTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        runner = new CoordinatorRunner(TesterUtil.instance);
        runner.bindGlobals();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        runner.cleanAndUnbind();
    }

    @Before
    public void setUp() throws RemoteException, NotBoundException {
        registry = LocateRegistry.getRegistry();
        globals = (GlobalVariables) registry.lookup("Globals");
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of put method, of class GlobalVariables.
     */
    @Test
    public void testPut() throws Exception {
        for (int i = 0; i < 50; i++) {
            globals.put(new Integer(i), i);
        }

        for (int i = 0; i < 50; i++) {
            Integer value = (Integer) globals.get(i);
            assertTrue(i == value.intValue());
        }
    }

    /**
     * Test of get method, of class GlobalVariables.
     */
    @Test
    public void testGet() throws Exception {
        for (int i = 0; i < 50; i++) {
            globals.put(new Integer(i), i);
        }

        for (int i = 0; i < 50; i++) {
            Integer value = (Integer) globals.get(i);
            assertTrue(i == value.intValue());
        }
    }

    /**
     * Test of getCollection method, of class GlobalVariables.
     */
    @Test
    public void testGetCollection() throws Exception {
        for (int i = 0; i < 50; i++) {
            globals.put(new Integer(i), i);
        }

        for (int i = 0; i < 50; i++) {
            Integer value = (Integer) globals.get(i);
            assertTrue(i == value.intValue());
        }
    }

    /**
     * Test of containsKey method, of class GlobalVariables.
     */
    @Test
    public void testContainsKey() throws Exception {
        for (int i = 0; i < 50; i++) {
            globals.put(new Integer(i), i);
        }

        for (int i = 0; i < 50; i++) {
            Integer value = (Integer) globals.get(i);
            assertTrue(i == value.intValue());
        }
    }

    /**
     * Test of clearCollection method, of class GlobalVariables.
     */
    @Test
    public void testClearCollection() throws Exception {
        for (int i = 0; i < 50; i++) {
            globals.put(new Integer(i), i);
        }

        for (int i = 0; i < 50; i++) {
            Integer value = (Integer) globals.get(i);
            assertTrue(i == value.intValue());
        }
    }
}
