package fr.univnantes.alma.rmilite.ioLayer.nioManager;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * A {@link RemoteProxy_nio} which is constructed on a received object, and then
 * will only return it once and then throw an exception
 * <p>
 * Usefull in case of sockets already listened to, and thus they should not be
 * listened any more
 * </p>
 * 
 * @author Guillaume Le Louët
 * 
 */
public class RemoteProxy_nio_Received extends RemoteProxy_nio {

    protected Object received;

    public RemoteProxy_nio_Received(SocketChannel socket, Object received)
	    throws IOException {
	super(socket);
	this.received = received;
    }

    @Override
    public Object receiveObject() {
	if (received == null) {
	    throw new UnsupportedOperationException();
	} else {
	    Object toReturn = received;
	    received = null;
	    return toReturn;
	}
    }

}
