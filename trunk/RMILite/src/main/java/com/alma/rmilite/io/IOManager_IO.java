package com.alma.rmilite.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

import com.alma.rmilite.server.RemoteObjectProvider;
import com.alma.rmilite.server.RemoteObjectManager;

public class IOManager_IO implements IOManager {

	/**
	 * Each {@link ServerSocket} is associated with a thread.
	 * 
	 * @see Runnable
	 */
	private class ServerProxy implements Runnable {

		private ServerSocket serverSocket;

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
				try {
					/*
					 * Listens for a connection to be made to this socket and
					 * accepts it.
					 * 
					 * A new connection is encapsulated in a new RemoteProxy.
					 */
					final RemoteProxy stub = new RemoteProxy_IO(serverSocket
							.accept());

					new Thread(new Runnable() {
						@Override
						public void run() {
							/*
							 * We link the remote stub with the remote object
							 * (identified the port of the serverSockect).
							 */
							((RemoteObjectManager) RemoteObjectProvider.instance)
									.remoteProcedureCall(serverSocket
											.getLocalPort(), stub);
						}
					}).start();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	public Map<Integer, ServerSocket> serverSockets;

	public IOManager_IO() {
		this.serverSockets = new HashMap<Integer, ServerSocket>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alma.rmilite.io.IOManager#getRemoteProxy(java.net.InetSocketAddress)
	 */
	@Override
	public RemoteProxy getRemoteProxy(InetSocketAddress reference)
			throws IOException {
		return new RemoteProxy_IO(reference);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alma.rmilite.io.IOManager#close(int)
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
	 * @see com.alma.rmilite.io.IOManager#open(int)
	 */
	@Override
	public int open(int port) throws IOException {
		ServerSocket ss = new ServerSocket(port);
		this.serverSockets.put(ss.getLocalPort(), ss);
		new Thread(new ServerProxy(ss)).start();
		return ss.getLocalPort();
	}
}
