/**
 * 
 */
package fr.univnantes.alma.rmilite.ioLayer.nioManager;

import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.SocketChannel;

import fr.univnantes.alma.nio.Client;
import fr.univnantes.alma.nio.client.NioByteArrayClient;
import fr.univnantes.alma.rmilite.ioLayer.RemoteProxy;

/**
 * implementation of {@link RemoteProxy} using the
 * {@link java.nio.channels.SocketChannel} network layer
 * 
 * @author E06A193P
 * 
 */
public class RemoteProxy_nio implements RemoteProxy {

    protected SocketChannel socket;
    protected Client client;

    public RemoteProxy_nio(SocketChannel socket) throws IOException {
	this.socket = socket;
	client = new NioByteArrayClient(socket);
    }

    @Override
    public void close() throws IOException {
	socket.close();
    }

    @Override
    public int getLocalPort() {
	return socket.socket().getLocalPort();
    }

    @Override
    public Object receiveObject() throws IOException, ClassNotFoundException {
	return client.receive();
    }

    @Override
    public void sendObject(Object object) throws IOException {
	client.send((Serializable) object);
    }

}
