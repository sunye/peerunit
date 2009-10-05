package test.btree;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import java.rmi.RemoteException;
import java.util.Properties;

import fr.inria.peerunit.btree.BootstrapperImpl;
import fr.inria.peerunit.btree.Node;
import fr.inria.peerunit.util.TesterUtil;

public class BootstrapperImplTest {

    private BootstrapperImpl bootstrapper;
    private Properties properties;

    //@Before
    public void setup() {
        properties = new Properties();
        properties.setProperty("tester.peers", "3");
        properties.setProperty("test.treeStrategy", "1");
        TesterUtil defaults = new TesterUtil(properties);
        bootstrapper = new BootstrapperImpl(defaults);
    }

    //@Test
    public void testBootstrapperIml() {
    	/*
        properties.setProperty("tester.peers", "1");
        TesterUtil defaults = new TesterUtil(properties);
        fr.inria.peerunit.Bootstrapper b = new BootstrapperImpl(defaults);
        Node node = mock(Node.class);
        try {
            b.register(node);
        } catch (RemoteException e) {
            fail(e.getMessage());
        }

        assertTrue(b != null);
        */
    }

    //@Test
    public void testRegister() {
    	/*
        int id = 0;
        try {
            for (int i = 0; i < 5; i++) {
                Node node = mock(Node.class);
                id = bootstrapper.register(node);
            }
            assertTrue(id == 4);
        } catch (RemoteException e) {
            fail(e.getMessage());
        }
        */
    }

    @Test
    public void testGetRegistered() {
        //fail("Not yet implemented");
    }

    @Test
    public void testIsRoot() {
        //fail("Not yet implemented");
    }
    
    
}
