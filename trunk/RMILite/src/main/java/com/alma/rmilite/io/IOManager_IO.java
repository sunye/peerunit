package com.alma.rmilite.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

import com.alma.rmilite.server.RemoteObjectProvider;
import com.alma.rmilite.server.RemoteObjectManager;

public class IOManager_IO implements IOManager {
	
	private class ServerProxy implements Runnable {

		private ServerSocket serverSocket;
		
		private ServerProxy(ServerSocket ss) {
			this.serverSocket = ss;
		}

		@Override
		public void run() {
			while (!serverSocket.isClosed()) {
				try {
					final RemoteProxy stub = new RemoteProxy_IO(serverSocket.accept());
					new Thread(new Runnable() {					
						@Override
						public void run() {
							((RemoteObjectManager) RemoteObjectProvider.instance).remoteProcedureCall(serverSocket.getLocalPort(), stub);	
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
	
	@Override
	public RemoteProxy getRemoteProxy(InetSocketAddress reference) throws IOException {
		return new RemoteProxy_IO(reference);
	}

	@Override
	public void close(int port) throws IOException {
		this.serverSockets.remove(port).close();
	}

	@Override
	public int open(int port) throws IOException {
		ServerSocket ss = new ServerSocket(port);
		this.serverSockets.put(port, ss);
		new Thread(new ServerProxy(ss)).start();
		return ss.getLocalPort();
	}
}
