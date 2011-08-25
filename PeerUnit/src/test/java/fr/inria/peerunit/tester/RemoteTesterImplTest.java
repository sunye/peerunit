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
package fr.inria.peerunit.tester;

import fr.inria.peerunit.remote.Coordinator;
import org.junit.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author sunye
 */
public class RemoteTesterImplTest {

    private Coordinator coordinator = null;
    private RemoteTesterImpl remoteTester = null;

    public RemoteTesterImplTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        coordinator = mock(Coordinator.class);
        remoteTester = new RemoteTesterImpl();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of setCoordinator method, of class RemoteTesterImpl.
     * @throws Exception Exception.
     */
    @Test
    public void testSetCoordinator() throws Exception {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    remoteTester.takeCoordinator();
                } catch (InterruptedException ex) {
                    fail("Thread interrupted");
                }
            }
        };
        t.start();
        t.join(1000);
        assertTrue(t.isAlive());
        remoteTester.setCoordinator(coordinator);
        assertTrue(coordinator == remoteTester.takeCoordinator());
        t.join(500);
        assertFalse(t.isAlive());
    }


    /**
     * Test of start method, of class RemoteTesterImpl.
     * @throws Exception Exception.
     */
    @Test
    public void testStart() throws Exception {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    remoteTester.waitForStart();
                } catch (InterruptedException ex) {
                    fail("Thread interrupted");
                }
            }
        };
        t.start();
        t.join(1000);
        assertTrue(t.isAlive());
        remoteTester.start();
        t.join(500);
        assertFalse(t.isAlive());
    }
}