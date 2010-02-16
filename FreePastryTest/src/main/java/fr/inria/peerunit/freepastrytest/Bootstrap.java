package fr.inria.peerunit.freepastrytest;

import java.io.IOException;
import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.util.TesterUtil;
import fr.inria.peerunit.freepastrytest.test.TestInsertJoin;
import java.io.FileInputStream;

public class Bootstrap {

    private static Logger log = Logger.getLogger("bootstrap");

    public static void main(String[] str) {

        TesterUtil defaults;
        FileHandler handler;

        try {
            if (new File("peerunit.properties").exists()) {
                String filename = "peerunit.properties";
                FileInputStream fs = new FileInputStream(filename);
                defaults = new TesterUtil(fs);
            } else {
                defaults = TesterUtil.instance;
            }
            // Log creation
            handler = new FileHandler(TesterUtil.instance.getLogfolder() + "/bootstrap.log", true);
            handler.setFormatter(new LogFormat());
            log.addHandler(handler);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Peer peer = new Peer();
        Network net = new Network();
        if (!net.joinNetwork(peer, null, true, log)) {
            throw new BootException("Can't bootstrap");
        }
    }
}
