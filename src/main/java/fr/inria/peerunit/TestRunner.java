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
package fr.inria.peerunit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import fr.univnantes.alma.rmilite.UnexportedException;
import java.rmi.RemoteException;

import fr.univnantes.alma.rmilite.ConfigManagerStrategy;
import fr.univnantes.alma.rmilite.ConfigManagerRMIStrategy;
//import fr.univnantes.alma.rmilite.ConfigManagerSocketStrategy;

import fr.univnantes.alma.rmilite.registry.Registry;


import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.rmi.tester.DistributedTesterImpl;
import fr.inria.peerunit.rmi.tester.TesterImpl;
import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.util.TesterUtil;

/**
 * A <i>test</i> on a peer is launched by a instance of the <tt>TestRunner</tt> class. Its role consists
 * to launch the right implementation of <i>Tester</i> interface, i.e centralized or distributed (the type
 * is set in the framework property file ), passing it as argument the <tt>Class</tt> instance corresponding
 * to the <i>test case</i> to perform.
 * 
 * @author sunye
 * @author Aboubakar Koita
 * @version 1.0
 * @since 1.0
 */
public class TestRunner {

    /**
     * The test case that will be excuted and those name was
     * passed at the command line.
     */
    private Class<?> testcase;
    private TesterUtil defaults;
    private Registry registry;
    private static final Logger log = Logger.getLogger(TesterImpl.class.getName());

    /**
     * Launch the right implementation of <i>tester</i> passing it as argument the <tt>Class</tt>
     * instance corresponding to the <i>test case</i> to execute.
     *
     * @param klass the<tt>Class</tt> instance corresponding to the <i>test case</i> to execute
     */
    public TestRunner(Class<?> klass, TesterUtil tu) throws Exception {

        defaults = tu;
        testcase = klass;

        Bootstrapper boot = null;

        registry = null;
        int times = 0;
        boolean centralized = true;

        this.initializeLogger();

        ConfigManagerStrategy cms = new ConfigManagerRMIStrategy();

        while (times < 5 && boot == null) {
            try {
                registry = cms.getNamingServer().getRegistry("127.0.0.1", 0);
                //registry = LocateRegistry.getRegistry();
                boot = (Bootstrapper) registry.lookup("Bootstrapper");
                centralized = false;

            } catch (UnexportedException ex) {
                try {
                    boot = (Bootstrapper) registry.lookup("Coordinator");
                } catch (Exception e) {
                }
            } catch (RemoteException ex) {
            }
            times++;
            if (boot == null) {
                try {
                    Thread.sleep(300*times);
                } catch (InterruptedException e) {
                }
            }
            
        }

        if (boot == null) {
            log.severe("Unable to bind");
            System.exit(1);
        }

        try {
            GlobalVariables globals = (GlobalVariables) registry.lookup("Globals");
            if (centralized) {
                log.fine("Coordinator found, using the centralized architecture.");
                TesterImpl tester = new TesterImpl(boot, globals, defaults);
                cms.getRemoteObjectProvider().exportObject(tester, 0);
                //UnicastRemoteObject.exportObject(tester);

                tester.setCoordinator((Coordinator) boot);

                tester.registerTestCase(testcase);
                tester.start();
                tester.run();

            } else {
                log.fine("Bootstrapper found, using the distributed architecture.");
                DistributedTesterImpl tester = new DistributedTesterImpl(testcase, boot, globals, defaults);
                cms.getRemoteObjectProvider().exportObject(tester, 0);
                //UnicastRemoteObject.exportObject(tester);
                tester.register();
                //tester.registerTestCase(testcase);
                //tester.run();
            }
        } catch (Exception e) {
        }

    }

    private void initializeLogger() {
        try {
            Level level = defaults.getLogLevel();
            Formatter formatter = new LogFormat();
            Logger.getLogger("fr.inria").setLevel(level);
            Logger.getLogger("").getHandlers()[0].setLevel(level);
            Logger.getLogger("").getHandlers()[0].setFormatter(formatter);

        } catch (SecurityException ex) {
            log.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * In the main method, we get the only argument corresponding to class name of
     * <i>test case</i> to perform. We load the <tt>Class</tt> corresponding and we
     * create a <code>TestRunner</tt> instance.
     *
     * @param args The only argument should be a class name of <i>test case</i> to execute
     */
    public static void main(String[] args) throws Exception {
        TesterUtil defaults;
        String filename;

        if (args.length < 1) {
            System.out.println("Usage: java TestRunner TestCaseClass [Properties File]");
        } else {
            String name = args[0];
            try {
                Class<?> klass = Class.forName(name);

                if (args.length > 1) {
                    filename = args[1];
                    FileInputStream fs = new FileInputStream(filename);
                    defaults = new TesterUtil(fs);
                } else if (new File("peerunit.properties").exists()) {
                    filename = "peerunit.properties";
                    FileInputStream fs = new FileInputStream(filename);
                    defaults = new TesterUtil(fs);
                } else {
                    defaults = TesterUtil.instance;
                }

                new TestRunner(klass, defaults);

            } catch (FileNotFoundException e) {
                System.err.println("Error: Unable to open properties file");
                System.exit(1);
            } catch (ClassCastException e) {
                System.out.println("Error: Class " + name + " does not implement TestCase interface.");
            } catch (ClassNotFoundException e) {
                System.out.println("Error: Class " + name + " not found.");
            }
        }
    }
}
