package fr.inria.peerunit.openchordtest.test;

import java.util.logging.Logger;

import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.TestStep;
import java.util.Arrays;
import static fr.inria.peerunit.tester.Assert.*;

public class TestInconclusiveArray extends AbstractOpenChordTest  {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    static TestInconclusiveArray test;
    private static final Logger log = Logger.getLogger(TestInconclusive.class.getName());
    private int[] testString = new int[10];


    @BeforeClass(range = "*")
    public void init() {
        log.info("[TestInc] Init");
    }

    @TestStep(range = "*", order = 1, timeout = 10000)
    public void testInconc() {

            if (test.getId() == 0) {
                for (int i = 0; i < 10; i++) {
                    testString[i] = i;
                }
                int[] teste = new int[10];
                for (int i = 0; i < 10; i++) {
                    teste[i] = i * 100;
                }

                log.info("[TestInc] Will assert");
                assertTrue(Arrays.equals(testString, teste));
            } else {
                log.info("[TestInc] Will continue");
            }


    }

    @AfterClass(range = "*")
    public void end() {
        log.info("[TestInc] Peer end");
    }
}


