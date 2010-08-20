package fr.inria.peerunit.freepastrytest;

import java.io.IOException;
import java.io.File;
import java.net.UnknownHostException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.util.TesterUtil;
import java.io.FileInputStream;

public class Bootstrap {

    private static Logger log = Logger.getLogger("bootstrap");

    public static void main(String[] str) throws UnknownHostException, InterruptedException {

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
            handler = new FileHandler(defaults.getLogfolder() + "/bootstrap.log", true);
            handler.setFormatter(new LogFormat());
            log.addHandler(handler);

            Peer peer = new Peer();
            Network net = new Network();
            if (!net.joinNetwork(peer, null, true, log)) {
                throw new BootException("Can't bootstrap");
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
