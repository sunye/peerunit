package fr.univnantes.alma.rmilite.ioLayer.ioManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import fr.univnantes.alma.rmilite.ioLayer.RemoteProxy;

public class RemoteProxy_IO implements RemoteProxy {

	private Socket socket;

	public RemoteProxy_IO(Socket reference) {
		this.socket = reference;
	}

	public RemoteProxy_IO(InetSocketAddress reference) throws IOException {
		this.socket = new Socket(reference.getAddress(), reference.getPort());
	}

	@Override
	public Object receiveObject() throws IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		return in.readUnshared();
	}

	@Override
	public void sendObject(Object object) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(socket
				.getOutputStream());
		out.writeUnshared(object);
		out.flush();
	}

	@Override
	public void close() throws IOException {
		this.socket.close();
	}

	@Override
	public int getLocalPort() {
		return this.socket.getLocalPort();
	}
}
