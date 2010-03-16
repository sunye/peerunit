package fr.univnantes.alma.rmilite.ioLayer.nioManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import fr.univnantes.alma.nio.Server;
import fr.univnantes.alma.rmilite.ioLayer.Manager;
import fr.univnantes.alma.rmilite.ioLayer.RemoteProxy;
import fr.univnantes.alma.rmilite.server.RemoteObjectManager;

public class Manager_nio implements Manager {

    Server server;// TODO construct this

    @Override
    public void close(int port) throws IOException {
	server.closePort(port);
    }

    protected RemoteObjectManager remoteobjectmanager;

    @Override
    public RemoteObjectManager getRemoteObjectManager() {
	return remoteobjectmanager;
    }

    @Override
    public void setRemoteObjectManager(RemoteObjectManager rom) {
	this.remoteobjectmanager = rom;
    }

    @Override
    public RemoteProxy getRemoteProxy(InetSocketAddress reference)
	    throws IOException {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public int open(int port) throws IOException {
	if (port == 0) {
	    port = detectAvailablePort();
	}
	if (port == 0) {
	    return 0;
	}
	server.openPort(port);
	return port;
    }

    int lastAvailablePort = 1024;

    /**
     * guess what port is available to listen at on local machine
     * 
     * @return 0 if no port can be opened, or an available port if exists
     */
    protected int detectAvailablePort() {
	lastAvailablePort++;
	for (; lastAvailablePort < 10000; lastAvailablePort++) {
	    try {
		SocketChannel sc = SocketChannel.open(new InetSocketAddress(
			"localhost", lastAvailablePort));
		sc.close();
	    } catch (IOException ioe) {// nothing listens at this port
		return lastAvailablePort;
	    }
	}
	return 0;
    }

}
