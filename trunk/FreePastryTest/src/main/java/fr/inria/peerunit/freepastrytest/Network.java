package fr.inria.peerunit.freepastrytest;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import rice.environment.Environment;
import fr.inria.peerunit.freepastrytest.util.FreeLocalPort;
import fr.inria.peerunit.util.TesterUtil;
import java.io.File;
import java.io.FileInputStream;

public class Network {

    private InetSocketAddress bootadd;
    private TesterUtil defaults;

    public Network() {
        try {
            if (new File("peerunit.properties").exists()) {
                String filename = "peerunit.properties";
                FileInputStream fs = new FileInputStream(filename);
                defaults = new TesterUtil(fs);
            } else {
                defaults = TesterUtil.instance;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean joinNetwork(Peer peer, InetSocketAddress bootaddress, boolean createNetwork, Logger log) {
        bootadd = bootaddress;
        Environment env = new Environment();

        // the port to use locally
        //int bindport = port.getPort();
        int bootport = TesterUtil.instance.getBootstrapPort();

        // build the bootaddress from the command line args
        InetAddress bootIP = null;
        try {
            bootIP = InetAddress.getByName(defaults.getBootstrap());
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (bootadd == null) {
            bootadd = new InetSocketAddress(bootIP, bootport);
        }

        boolean joined = false;
        try {

            int usedPort = 0;
            if (createNetwork) {
                usedPort = bootport;
            } else {
                FreeLocalPort port = new FreeLocalPort();
                usedPort = port.getPort();
            }

            if (!peer.join(usedPort, bootadd, env, log, createNetwork)) {
                joined = false;
            } else {
                joined = true;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return joined;
    }

    public InetSocketAddress getInetSocketAddress() {
        return bootadd;
    }
}
