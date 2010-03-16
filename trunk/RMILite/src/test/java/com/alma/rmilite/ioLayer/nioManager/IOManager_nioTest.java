package com.alma.rmilite.ioLayer.nioManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.alma.rmilite.ioLayer.nioManager.IOManager_nio;

public class IOManager_nioTest {

	protected IOManager_nio toTest;

	@BeforeMethod
	public void setUp() {
		toTest = new IOManager_nio();
	}

	@Test( dataProvider = "portsToOpen" )
	public void testDetectAvailablePort( int nbPortsToOpen, int forbiddenPort )
			throws IOException {
		int lastPort = 0;
		ServerSocketChannel forbidden = openListeningPort( forbiddenPort );
		ServerSocketChannel[] opened = new ServerSocketChannel[nbPortsToOpen];
		for( int nbPortOpen = 0 ; nbPortOpen < 10 ; nbPortOpen++ ) {
			int port = toTest.detectAvailablePort();
			assert ( port > lastPort );
			assert ( port != forbiddenPort );
			lastPort = port;
			ServerSocketChannel sc = openListeningPort( port );
			opened[ nbPortOpen ] = sc;
		}

		for( ServerSocketChannel sc : opened ) {
			sc.close();
		}
		forbidden.close();
	}

	@DataProvider( name = "portsToOpen" )
	public Object[][] portsToOpen() {
		return new Object[][] { { 10, 1025 } };
	}
	
	protected ServerSocketChannel openListeningPort(int port) {
		try {
			ServerSocketChannel ret = ServerSocketChannel.open();
			ret.configureBlocking( false );
			ret.socket().bind( new InetSocketAddress( port ) );
			Selector sel = Selector.open();
			ret.register( sel, SelectionKey.OP_ACCEPT );
			return ret;
		} catch( IOException e ) {
			return null;
		}
		
	}

}
