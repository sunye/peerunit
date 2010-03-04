package com.alma.rmilite.io;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class RemoteProxy_IO implements RemoteProxy {

	private Socket socket;

	public RemoteProxy_IO(Socket reference) throws IOException {
		this.socket = reference;
	}

	public RemoteProxy_IO(InetSocketAddress reference) throws IOException {
		this.socket = new Socket(reference.getAddress(), reference.getPort());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alma.rmilite.io.RemoteProxy#receiveObject()
	 */
	@Override
	public Object receiveObject() throws IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		return in.readUnshared();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alma.rmilite.io.RemoteProxy#sendObject(java.lang.Object)
	 */
	@Override
	public void sendObject(Object object) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(socket
				.getOutputStream());
		out.writeUnshared(object);
		out.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alma.rmilite.io.RemoteProxy#close()
	 */
	@Override
	public void close() throws IOException {
		this.socket.close();
	}

	/* (non-Javadoc)
	 * @see com.alma.rmilite.io.RemoteProxy#getLocalPort()
	 */
	@Override
	public int getLocalPort() {
		return this.socket.getLocalPort();
	}
}
