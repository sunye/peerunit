/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import fr.inria.peerunit.util.HTree;

/**
 *
 * @author sunye
 */
public class HTreeTest {

    public HTreeTest() {
    }

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
    }

    @org.junit.AfterClass
    public static void tearDownClass() throws Exception {
    }


    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of insert method, of class HTree.
     */
    @Test
    public void testInsert() {
        HTree<Integer,String> tree = new HTree<Integer, String>(5);

        for (int i = 0; i < 1000; i++) {
            Integer e = new Integer(i);
            tree.put(e,e.toString());
            assertTrue(tree.containsKey(e));
        }
    }

    @Test
    public void testHead() {
         HTree<Integer, String> tree = new HTree<Integer,String>(5);

        for (int i = 0; i < 1000; i++) {
            Integer e = new Integer(i);
            tree.put(e,e.toString());
        }

        assertEquals(tree.head().key(), new Integer(0));
    }
}