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
package test.remote;

import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import fr.inria.peerunit.GlobalVariablesImpl;
import fr.inria.peerunit.base.Sample;
import fr.inria.peerunit.common.MethodDescription;
import fr.inria.peerunit.coordinator.CoordinatorImpl;
import fr.inria.peerunit.remote.Bootstrapper;
import fr.inria.peerunit.remote.Coordinator;
import fr.inria.peerunit.remote.GlobalVariables;
import fr.inria.peerunit.remote.Tester;
import fr.inria.peerunit.tester.TestCaseWrapper;
import fr.inria.peerunit.tester.TesterImpl;
import fr.inria.peerunit.util.TesterUtil;

public class TesterImplTest {

    private static TestCaseWrapper wrapper;
    private static CoordinatorImpl coord;
    private static Coordinator remoteCoordinator;
    private static Bootstrapper bootstrapper;
    private static GlobalVariables globals;
    private static TesterImpl tester0, tester1, tester2;

    /**
     * FIXME Tests not working correctly in all platforms.
     * @throws InterruptedException
     */
    @BeforeClass
    public static void inititalize() throws InterruptedException, RemoteException {
        // Coordinator initialization
        coord = new CoordinatorImpl(3, 100);
        remoteCoordinator = coord.getRemoteCoordinator();
        bootstrapper = coord.getRemoteBootstrapper();
        new Thread(coord, "Coordinator").start();
        Thread.yield();

        // Testers initialization
        globals = new GlobalVariablesImpl();
        tester0 = new TesterImpl(bootstrapper, globals);
        tester1 = new TesterImpl(bootstrapper, globals);
        tester2 = new TesterImpl(bootstrapper, globals);

        tester0.setCoordinator(remoteCoordinator);
        tester1.setCoordinator(remoteCoordinator);
        tester2.setCoordinator(remoteCoordinator);

        tester0.registerTestCase(Sample.class);
        tester1.registerTestCase(Sample.class);
        tester2.registerTestCase(Sample.class);


        wrapper = new TestCaseWrapper(tester0);

        Thread.yield();

        tester0.start();
        tester1.start();
        tester2.start();

        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    coord.waitForTesterRegistration();
                } catch (InterruptedException ex) {
                    Logger.getLogger(TesterImplTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };

        t.start();
        t.join();

        //new Thread(tester0, "Tester 0").start();

    }

    //@Test
    public void testTesterImpl() {
        fail("Not yet implemented");
    }

    //@Test
    public void testRun() {
        fail("Not yet implemented");
    }

    @Test
    public void testRegister() {
        MethodDescription md = new MethodDescription("first", 1, 1000);

        assertEquals(3, coord.getSchedule().size());

        assertTrue(coord.getSchedule().containsMethod(md));
        assertTrue(coord.getSchedule().testersFor(md).contains(tester0));
        assertTrue(coord.getSchedule().testersFor(md).contains(tester1));
        assertTrue(coord.getSchedule().testersFor(md).contains(tester2));

    }

    /**
     * FIXME
     */
    //@Test
    public void testExecute() {
        try {
            Bootstrapper boot = mock(Bootstrapper.class);
            TesterImpl tester = new TesterImpl(boot, globals);
            tester.setCoordinator(remoteCoordinator);
            tester.registerTestCase(Sample.class);

            System.setProperty("executed", "nok");
            List<MethodDescription> methods = wrapper.register(Sample.class);
            Thread tt = new Thread(tester, "Test Execute");
            tt.start();

            for (MethodDescription md : methods) {
                tester.execute(md);
            }
            Thread.sleep(1200);
            Thread.yield();
            tester.quit();
            assertEquals("ok", System.getProperty("executed"));
        } catch (RemoteException e) {
            fail("RemoteException");
        } catch (InterruptedException e) {
            fail("InterruptedException");
        }

    }

    @Test
    public void testGetId() {
        assertEquals(0, tester0.getId());
        assertEquals(1, tester1.getId());
        assertEquals(2, tester2.getId());
    }

    //@Test
    public void testKill() {
        fail("Not yet implemented");
    }

    @Test
    public void testPut() {
        try {
            tester0.put(0, "zero");
            tester1.put(1, "one");
            tester2.put(2, "two");
            assertEquals("zero", tester2.get(0));
            assertEquals("one", tester0.get(1));
            assertEquals("two", tester1.get(2));
        } catch (RemoteException ex) {
            fail("Communication error");
        }
    }

    //@Test
    public void testClear() {
        fail("Not yet implemented");
    }

    //@Test
    public void testGet() {
        fail("Not yet implemented");
    }

    //@Test
    public void testGetCollection() {
        fail("Not yet implemented");
    }

    //@Test
    public void testContainsKey() {

        fail("Not yet implemented");
    }

    @Test
    public void mysets() {
        Set<Tester> myset = new HashSet<Tester>();

        assertTrue(myset.add(tester0));
        assertTrue(!myset.add(tester0));
        assertTrue(!myset.add(tester0));

    }
}
