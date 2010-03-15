package com.alma.rmilite.io.nioManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import nio.server.ANioServer;

import com.alma.rmilite.io.IOManager;
import com.alma.rmilite.io.RemoteProxy;
import com.alma.rmilite.server.RemoteObjectManager;

public class IOManager_nio implements IOManager {

	protected Map<Integer, ANioServer> listeningservers = new HashMap<Integer, ANioServer>();

	@Override
	public void close( int port ) throws IOException {
		ANioServer bss = listeningservers.get( port );
		if( bss != null ) bss.stop();
	}

	protected RemoteObjectManager remoteobjectmanager;

	@Override
	public RemoteObjectManager getRemoteObjectManager() {
		return remoteobjectmanager;
	}

	@Override
	public void setRemoteObjectManager( RemoteObjectManager rom ) {
		this.remoteobjectmanager = rom;
	}

	@Override
	public RemoteProxy getRemoteProxy( InetSocketAddress reference )
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int open( int port ) throws IOException {
		if(port==0) {
			port = detectAvailablePort();
		}
		if(port==0) {
			return 0;
		}
		// TODO Auto-generated method stub
		return 0;
	}

	int lastAvailablePort = 1024;
	
	/**
	 * guess what port is available to listen at on local machine
	 * @return 0 if no port can be opened, or an available port if exists
	 */
	protected int detectAvailablePort() {
		lastAvailablePort++;
		for( ; lastAvailablePort < 10000 ; lastAvailablePort++ ) {
			try {
				SocketChannel sc = SocketChannel.open( new InetSocketAddress(
						"localhost", lastAvailablePort ) );
				sc.close();
			} catch( IOException ioe ) {//nothing listens at this port
				ioe.printStackTrace();
				System.out.println("port : "+lastAvailablePort);
				return lastAvailablePort;
			}
		}
		return 0;
	}

}
