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
package fr.inria.peerunit.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.logging.Level;

/**
 * This class retrieve the application properties stocked in the properties file.
 * These properties allow for instance to parameter the testing architecture type
 * (centralized or distributed), the number of <i>testers</i> expected, etc.
 *
 * @author Eduardo Almeida
 * @author Aboubakar Koïta
 * @author Veronique Pelleau
 * @author Jérémy Masson
 * @version 1.3
 * @since 1.0
 */
public class TesterUtil {

    /**
     * The <tt>Properties</tt> object must containing the application properties
     */
    private Properties props = null;
    final public static TesterUtil instance = new TesterUtil();

    private TesterUtil() {
        try {
            Properties defaults = new Properties();
            InputStream is = this.getClass().getResourceAsStream("/peerunit.properties");
            defaults.load(is);
            is.close();
            props = new Properties(defaults);
            if (new File("peerunit.properties").exists()) {
                String filename = "peerunit.properties";
                FileInputStream fs = new FileInputStream(filename);
                props.load(fs);
                fs.close();
            }
        } catch (IOException e) {
            System.err.println("Could not find default properties' resource.");
            System.exit(1);
        }
    }

    public TesterUtil(InputStream is) {
        this();
        try {
            props.load(is);
        } catch (IOException e) {
            System.err.println("Could not find properties' file.");
            System.exit(1);
        }
    }

    public TesterUtil(Properties p) {
        this();
        props.putAll(p);
    }

    /**
     * Return the value of the property whose the name is given as argument
     *
     * @param property the property whose we search the value
     * @return the value of <code>property</code> property
     */
    private String getProperty(String property) {
        String value = props.getProperty(property);
        assert value != null : "Property " + property + " is undefined";
        return value;
    }

    /**
     * Return the number of testers expected in the properties file.
     *
     * @return the number of testers expected in the properties file
     */
    public int getExpectedTesters() {
        return Integer.valueOf(this.getProperty("tester.peers"));
    }

    /**
     * This method return the Tester's Bootstrap addresses.
     *
     * @return a ip addresses
     */
    public String getServerAddress() {
        String address;
        address = this.getProperty("tester.server");
        if (address == null) {
            try {
                address = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        return address;
    }

    /**
     * Return the date format used for logging.
     *
     * @return the date format used for logging
     */
    public String getDateFormat() {
        return this.getProperty("tester.log.dateformat");
    }

    /**
     * Return the time format used for logging
     *
     * @return the time format used for logging
     */
    public String getTimeFormat() {
        return this.getProperty("tester.log.timeformat");
    }

    /**
     * Return the delimiter used for logging
     *
     * @return the delimiter format used for logging
     */
    public String getDelimiter() {
        return this.getProperty("tester.log.delimiter");
    }

    /**
     * Return the application log file folder
     *
     * @return the application log file folder
     */
    public String getLogFolder() {
        return this.getProperty("tester.logfolder");
    }

    /**
     * Return the number of object to put in the Open chord or FreePastry's DHT
     * for the testing.
     *
     * @return the relaxation index used for fix the tolerance to inconclusive results
     */
    public int getObjects() {
        return Integer.valueOf(this.getProperty("test.objects"));
    }

    /**
     * Return the <i>test actions</i> inactivity time for the synchronization.
     *
     * @return Return the <i>test actions</i> inactivity time for the synchronization
     */
    public int getSleep() {
        return Integer.valueOf(this.getProperty("test.sleep"));
    }

    /**
     * Return the peers bootstrap address, may be different from <i>tester's</i> bootstrap one.
     *
     * @return the peers bootstrap address, may be different from <i>tester's</i> bootstrap one
     */
    public String getBootstrap() {
        return this.getProperty("test.bootstrap");
    }

    /**
     * Return the peers bootstrap port, may be different from <i>tester's</i> bootstrap one.
     *
     * @return the peers bootstrap port, may be different from <i>tester's</i> bootstrap one
     */
    public int getBootstrapPort() {
        return Integer.valueOf(this.getProperty("test.bootstrap.port"));
    }

    /**
     * Return the number of try of a <i>test action</i>.
     *
     * @return the number of try of a <i>test action</i>
     */
    public int getLoopToFail() {
        return Integer.valueOf(this.getProperty("test.loopToFail"));
    }

    /**
     * Return a percentage of peers number that is used by some <i>test cases</i> for instance
     * for choose the number of peers that join the test in first and those who join it in second.
     *
     * @return a percentage of peers number that is used by some <i>test cases</i> for instance
     *         for choose the number of peers that join the test in first and those who join it in
     *         second.
     */
    public int getChurnPercentage() {
        return Integer.valueOf(this.getProperty("test.churnPercentage"));
    }

    /**
     * Return the value of the property that fix the application logging level.
     *
     * @return the property that fix the application logging level
     */
    public Level getLogLevel() {
        return Level.parse(this.getProperty("tester.log.level"));
    }

    /**
     * Return the BTree order, if we are in distributed architecture.
     *
     * @return the BTree order, if we are in distributed architecture
     */
    public int getTreeOrder() {
        return Integer.valueOf(this.getProperty("test.treeOrder"));
    }

    /**
     * Return the testing architecture type, centralized or distributed
     *
     * @return the testing architecture type, centralized or distributed
     */
    public int getCoordinationType() {
        return Integer.valueOf(this.getProperty("test.coordination"));
    }

    /**
     * This method return the Tester's Bootstrap addresses.
     *
     * @return the port for the rmi registry
     */
    public int getRegistryPort() {
        int port;
        try {
            port = Integer.parseInt(this.getProperty("registry.port"));
        } catch (NumberFormatException e) {
            port = 1099;
        }
        return port;
    }

    public Class<?> getCoordinationStrategyClass() {
        Class<?> result = null;
        try {
            String className = this.getProperty("fr.inria.peerunit.coordinator.strategy");
            result = Class.forName(className);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return result;
    }
}