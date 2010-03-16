package fr.univnantes.alma.rmilite.ioLayer.ioManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import fr.univnantes.alma.rmilite.ioLayer.Manager;
import fr.univnantes.alma.rmilite.ioLayer.RemoteProxy;
import fr.univnantes.alma.rmilite.server.RemoteObjectManager;

public class Manager_IO implements Manager {

    /**
     * This thread execute the remote call.
     */
    private class RunnableCall implements Runnable {

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
    private class ServerProxy implements Runnable {

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

    private final Map<Integer, ServerSocket> serverSockets;

    private RemoteObjectManager remoteObjectManager;

    public Manager_IO() {
	this.serverSockets = new HashMap<Integer, ServerSocket>();
    }

    /*
     * (non-Javadoc)
     * 
     * @seefr.univnantes.alma.rmilite.io.IOManager#getRemoteProxy(java.net.
     * InetSocketAddress)
     */
    @Override
    public RemoteProxy getRemoteProxy(InetSocketAddress reference)
	    throws IOException {
	return new RemoteProxy_IO(reference);
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.univnantes.alma.rmilite.io.IOManager#close(int)
     */
    @Override
    public void close(int port) throws IOException {
	ServerSocket serverSocket = this.serverSockets.remove(port);
	if (serverSocket == null) {
	    throw new IOException("this port " + port + " is already close !");
	}
	serverSocket.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.univnantes.alma.rmilite.io.IOManager#open(int)
     */
    @Override
    public int open(int port) throws IOException {
	ServerSocket ss = new ServerSocket(port);
	this.serverSockets.put(ss.getLocalPort(), ss);
	new Thread(new ServerProxy(ss)).start();
	return ss.getLocalPort();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.univnantes.alma.rmilite.io.IOManager#setRemoteObjectManager(fr.univnantes
     * .alma.rmilite .server.RemoteObjectManager)
     */
    @Override
    public void setRemoteObjectManager(RemoteObjectManager rom) {
	this.remoteObjectManager = rom;
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.univnantes.alma.rmilite.io.IOManager#getRemoteObjectManager()
     */
    @Override
    public RemoteObjectManager getRemoteObjectManager() {
	return this.remoteObjectManager;
    }
}
