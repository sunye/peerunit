package fr.univnantes.alma.rmilite.ioLayer.ioManager;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

import fr.univnantes.alma.rmilite.ioLayer.Manager;
import fr.univnantes.alma.rmilite.ioLayer.RemoteProxy;
import fr.univnantes.alma.rmilite.server.RemoteObjectManager;

public class Manager_IO implements Manager {

    /**
     * This thread execute the remote call.
     */
    protected class RunnableCall implements Runnable {

	private final RemoteProxy stub;

	private RunnableCall(Socket call) {
	    this.stub = new RemoteProxy_IO(call);
	}

	@Override
	public void run() {
	    /*
	     * We link the remote stub with the remote object (identified the
	     * port of the serverSockect).
	     */
	    remoteObjectManager.remoteProcedureCall(stub);
	}
    }

    /**
     * Each {@link ServerSocket} is associated with a thread.
     * 
     * @see Runnable
     */
    protected class ServerProxy implements Runnable {

	private final ServerSocket serverSocket;

	private ServerProxy(ServerSocket ss) {
	    this.serverSocket = ss;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
	    while (!serverSocket.isClosed()) {
		/*
		 * Listens for a connection to be made to this socket and
		 * accepts it.
		 * 
		 * A new connection is encapsulated in a new RemoteProxy.
		 */
		try {
		    new Thread(new RunnableCall(serverSocket.accept())).start();
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	}
    }

    protected Map<Integer, ServerSocket> serverSockets;

    protected RemoteObjectManager remoteObjectManager;

    public Manager_IO() {
	this.serverSockets = new HashMap<Integer, ServerSocket>();
    }

    @Override
    public RemoteProxy getRemoteProxy(InetSocketAddress reference)
	    throws IOException {
	return new RemoteProxy_IO(reference);
    }

    @Override
    public void close(int port) throws IOException {
	ServerSocket serverSocket = this.serverSockets.remove(port);
	if (serverSocket == null) {
	    throw new IOException("this port " + port + " is already close !");
	}
	serverSocket.close();
    }

    @Override
    public int open(int port) throws IOException {
	ServerSocket ss = new ServerSocket(port);
	this.serverSockets.put(ss.getLocalPort(), ss);
	new Thread(new ServerProxy(ss)).start();
	return ss.getLocalPort();
    }

    @Override
    public void setRemoteObjectManager(RemoteObjectManager rom) {
	this.remoteObjectManager = rom;
    }

    @Override
    public RemoteObjectManager getRemoteObjectManager() {
	return this.remoteObjectManager;
    }
}
