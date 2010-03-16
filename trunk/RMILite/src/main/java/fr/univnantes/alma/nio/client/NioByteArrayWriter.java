package fr.univnantes.alma.nio.client;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import fr.univnantes.alma.nio.Writer;
import fr.univnantes.alma.nio.objectToBytes.Encoder;

/**
 * {@link Writer} that send objects through a {@link SocketChannel}, with a
 * specific protocol for transmission This Client should not be shared amongst
 * threads.
 * 
 * @author Guillaume Le Louët
 */
public class NioByteArrayWriter implements Writer {

    /** the channel with which we send data to */
    SocketChannel sc;

    public NioByteArrayWriter(SocketChannel socketToSend) throws IOException {
	this.sc = socketToSend;
    }

    public NioByteArrayWriter(String remoteAdress, int remotePort)
	    throws IOException {
	this(openChannel(remoteAdress, remotePort));
    }

    public static SocketChannel openChannel(String remoteAdress, int remotePort)
	    throws IOException {
	return openChannel(new InetSocketAddress(remoteAdress, remotePort));
    }

    public static SocketChannel openChannel(InetSocketAddress address)
	    throws IOException {
	SocketChannel sc = SocketChannel.open();
	sc.connect(address);
	sc.configureBlocking(false);
	return sc;
    }

    protected Encoder encoder = new Encoder();

    @Override
    public boolean send(Serializable s) {
	try {
	    sc.write(ByteBuffer.wrap(encoder.encode(s)));
	    return true;
	} catch (IOException ioe) {
	    ioe.printStackTrace();
	    return false;
	}
    }

    @Override
    public void close() {
	try {
	    sc.close();
	} catch (IOException ioe) {

	}
    }

}
