package fr.univnantes.alma.rmilite.ioLayer.nioManager;

import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.SocketChannel;

import fr.univnantes.alma.nio.Server;
import fr.univnantes.alma.nio.objectToBytes.Decoder;
import fr.univnantes.alma.nio.server.*;
import fr.univnantes.alma.rmilite.server.RemoteObjectManager;

/**
 * {@link Server} that listen to incoming data, decode them with a
 * {@link Decoder} and then ask a {@link RemoteObjectManager} to handle them
 * 
 * @author Guillaume Le Louët
 * 
 */
public class NioByteArrayDispatcherServer extends ANioServer {

    @Override
    protected ByteArrayHandler createHandler(SocketChannel socket) {
	return new ByteArrayDecoder(socket) {

	    @Override
	    public void handleObject(Serializable o, SocketChannel sc) {
		try {
		    remoteobjectmanager
			    .remoteProcedureCall(new RemoteProxy_nio_Received(
				    sc, o));
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }

	};
    }

    protected RemoteObjectManager remoteobjectmanager;

    public RemoteObjectManager getRemoteObjectManager() {
	return remoteobjectmanager;
    }

    public void setRemoteObjectManager(RemoteObjectManager rom) {
	this.remoteobjectmanager = rom;
    }

}
