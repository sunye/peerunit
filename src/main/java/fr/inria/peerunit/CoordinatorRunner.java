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
import java.io.IOException;

import fr.univnantes.alma.rmilite.UnexportedException;
import java.rmi.RemoteException;

//import java.rmi.AlreadyBoundException;
//import java.rmi.NotBoundException;

import fr.univnantes.alma.rmilite.registry.NamingServer_Socket;
//import java.rmi.registry.LocateRegistry;
import fr.univnantes.alma.rmilite.registry.Registry;
//import java.rmi.registry.Registry;
//import fr.univnantes.alma.rmilite.server.RemoteObjectProvider;
//import java.rmi.server.UnicastRemoteObject;
import fr.univnantes.alma.rmilite.server.RemoteObjectProvider_Socket;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.btree.BootstrapperImpl;
import fr.inria.peerunit.rmi.coord.CoordinatorImpl;
import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.util.TesterUtil;

/**
 * @author sunye
 * 
 */
public class CoordinatorRunner {

    private static final Logger log = Logger.getLogger(CoordinatorImpl.class.getName());
    private Coordinator stub;
    private CoordinatorImpl cii;
    private GlobalVariablesImpl globals;
    private GlobalVariables globalsStub;
    private BootstrapperImpl bootstrapper;
    private TesterUtil defaults;

    /**
     * @param defaults
     */
    public CoordinatorRunner(TesterUtil tu) {
        defaults = tu;
        try {
            this.initializeLogger();
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }

    }

    /**
     * @param defaults
     * @throws IOException
     */
    private void initializeLogger() throws IOException {
        Level l = defaults.getLogLevel();
        FileHandler handler = new FileHandler("coordination.log");
        handler.setFormatter(new LogFormat());
        handler.setLevel(l);
        Logger.getLogger("").addHandler(handler);
        //Logger.getLogger("").setLevel(defaults.getLogLevel());
        Logger.getLogger("fr.inria").setLevel(l);
        Logger.getLogger("").getHandlers()[0].setLevel(l);
    }

    private void start() throws RemoteException, UnexportedException, InterruptedException, Exception {
        log.entering("CoordinatorRunner", "start()");

        // Bind the remote object's stub in the registry
        NamingServer_Socket nameServer = new NamingServer_Socket();
        Registry registry = nameServer.createRegistry(1099);
        //Registry registry = LocateRegistry.createRegistry(1099);
        globals = new GlobalVariablesImpl();

        RemoteObjectProvider_Socket rop = new RemoteObjectProvider_Socket();
        globalsStub = (GlobalVariables) rop.exportObject(globals, 0);
        //globalsStub = (GlobalVariables) UnicastRemoteObject.exportObject(globals, 0);
        registry.bind("Globals", globalsStub);

        if (defaults.getCoordinationType() == 0) {
            log.info("Using the centralized architecture");
            cii = new CoordinatorImpl(defaults);
            
            stub = (Coordinator) rop.exportObject(cii, 0);
            //stub = (Coordinator) UnicastRemoteObject.exportObject(cii, 0);
            log.info("New Coordinator address is : " + defaults.getServerAddr());
            registry.bind("Coordinator", stub);

            Thread coordination = new Thread(cii, "Coordinator");
            coordination.start();
            coordination.join();
            log.info("Coordination thread finished");
            registry.unbind("Coordinator");
        } else {
            log.info("Using the distributed architecture");
            bootstrapper = new BootstrapperImpl(defaults);
            
            Bootstrapper bootStub = (Bootstrapper) rop.exportObject(bootstrapper, 0);
            //Bootstrapper bootStub = (Bootstrapper) UnicastRemoteObject.exportObject(bootstrapper, 0);
            registry.bind("Bootstrapper", bootStub);
            Thread boot = new Thread(bootstrapper, "Bootstrapper");
            boot.start();
            boot.join();
            log.info("Bootstrap thread finished");
            registry.unbind("Bootstrapper");
            //UnicastRemoteObject.unexportObject(bootstrapper, true);
        }
        registry.unbind("Globals");
        //UnicastRemoteObject.unexportObject(globals, true);
        //UnicastRemoteObject.unexportObject(registry, true);

        Thread.sleep(3000);
        System.exit(0);
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        TesterUtil defaults;

        try {
            String filename;
            if (args.length == 1) {
                filename = args[0];
                FileInputStream fs = new FileInputStream(filename);
                defaults = new TesterUtil(fs);
            } else if (new File("peerunit.properties").exists()) {
                filename = "peerunit.properties";
                FileInputStream fs = new FileInputStream(filename);
                defaults = new TesterUtil(fs);
            } else {
                defaults = TesterUtil.instance;
            }
            CoordinatorRunner cr = new CoordinatorRunner(defaults);
            cr.start();

        } catch (RemoteException ex) {
            log.log(Level.SEVERE, null, ex);
        } catch (UnexportedException ex) {
            log.log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            log.log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException e) {
            System.err.println("Error: Unable to open properties file");
            System.exit(1);
        }

    }
}
