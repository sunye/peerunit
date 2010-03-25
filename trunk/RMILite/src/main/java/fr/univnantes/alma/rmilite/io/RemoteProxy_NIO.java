/**
 * 
 */
package fr.univnantes.alma.rmilite.io;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import fr.univnantes.alma.nio.Client;
import fr.univnantes.alma.nio.client.NioByteArrayClient;
import fr.univnantes.alma.nio.client.NioByteArrayWriter;
import fr.univnantes.alma.nio.objectToBytes.Encoder;

/**
 * implementation of {@link RemoteProxy} using the
 * {@link java.nio.channels.SocketChannel} network layer and the {@link Encoder}
 * protocol for transmitting Bytes
 * 
 * @author Guillaume Le Louët
 * 
 */
public class RemoteProxy_NIO implements RemoteProxy {

    protected SocketChannel socket;
    protected Client client;

    public RemoteProxy_NIO(SocketChannel socket) throws IOException {
	this.socket = socket;
	client = new NioByteArrayClient(socket);
    }

    public RemoteProxy_NIO(InetSocketAddress reference) throws IOException {
	this(NioByteArrayWriter.openChannel(reference));
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
