package fr.univnantes.alma.nio;

import java.util.Collection;

/**
 * A Server listens to a set of port, accepts incoming connections, and
 * delegates the handling of incoming bytes in those sockets
 * 
 * @author Guillaume Le Lou�t
 */
public interface Server extends Runnable {

    /**
     * Ask the server to listen on a given port
     * 
     * @param port
     *            the port to listen
     * @return true if the server is now listening to the given port. It may
     *         have been listening to this port before we calle that method
     */
    public boolean openPort(int port);

    /**
     * Close the port if it was listened to by this server<br />
     * Any active communication on that port is crashed
     * 
     * @param port
     *            the port to close
     */
    public void closePort(int port);

    /**
     * check if a specific port is being listened to by the server
     * 
     * @param port
     *            the number of the port we want to know if we are listening to
     * @return is the server handling incoming connections to the port?
     */
    public boolean isPortOpened(int port);

    /**
     * @return the set of ports' number which are listened to by the server
     */
    public Collection<Integer> getOpenedPort();

    /**
     * Set the number of threads this server should use to process incoming data
     */
    public void setNbThreads(int nbThreads);

    /** @return the number of threads this server uses to handle incoming data */
    public int getNbThreads();

    /**
     * ask the server to stop as soon as possible. When this method return, the
     * server is stopped
     */
    public void stop();

    /** is the server listening to sockets or manipulating data ? */
    public boolean isRunning();

    /**
     * start a thread making the server listen to incoming connections.
     * 
     * @return true if the server has been started, false if it was already
     *         started or may not be started
     */
    public boolean start();
}
