package fr.inria.peerunit.tutorial;

import java.util.logging.Logger;

import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.SetId;
import fr.inria.peerunit.parser.TestStep;
import java.util.Arrays;
import static fr.inria.peerunit.tester.Assert.*;

public class TestInconclusiveArray  {

    private static final Logger LOG = Logger.getLogger(TestInconclusiveArray.class.getName());
    private int[] testString = new int[10];
    private int id;

    @SetId
    public void setId(int i) {
        id = i;
    }

    @BeforeClass(range = "*", timeout = 1000000)
    public void init() {
        LOG.info("[TestInc] Init");
    }

    @TestStep(order = 1, timeout = 10000000, range = "*")
    public void testFailure() {

        if (id == 0) {
            for (int i = 0; i < 10; i++) {
                testString[i] = i;
            }
            int[] teste = new int[10];
            for (int i = 0; i < 10; i++) {
                teste[i] = i * 100;
            }

            LOG.info("[Tester " + id + "] Will assert");
            assertTrue(Arrays.equals(testString, teste));
        } else {
            LOG.info("[Test " + id + "] Will continue");
        }


    }

    @AfterClass(range = "*", timeout = 1000000)
    public void end() {
        LOG.info("[TestInc] Peer end");
    }
}


