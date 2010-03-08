package nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class ByteSocketServer implements Runnable{

	protected int portToListen;

	/** number of threads used to handle incomming data */
	protected int nbThreads = 5;

	public ByteSocketServer(int port) {
		this.portToListen = port;
	}

	public void setNbThreads(int nbThreads) {
		this.nbThreads = nbThreads;
		if(this.nbThreads<1) { this.nbThreads = 1;}
	}



	protected ServerSocketChannel channel;

	protected Selector sel;

	protected boolean hasToStop = false;

	public void stop() {
		hasToStop = true;
	}

	public void run() {
		hasToStop = false;
		try {
			openSocket();
			startHandling();
		} catch(IOException ioe) {
			ioe.printStackTrace();
			return;
		}
	}

	protected void openSocket() throws IOException {
		channel = ServerSocketChannel.open();
		channel.configureBlocking(false);
		channel.socket().bind(new InetSocketAddress(portToListen));
		sel = Selector.open();
		channel.register(sel, SelectionKey.OP_ACCEPT);
	}

	protected void startHandling() throws IOException{
		while(!hasToStop) {
			sel.select();
			for (SelectionKey key : sel.selectedKeys()) {
				if(key.isAcceptable()) {
					handleIncommingConnectionKey(key);
				}

			}
			sel.selectedKeys().clear();
		}
	}

	protected Map<Channel, ByteArrayHandler> handlers = Collections.synchronizedMap( ( new HashMap<Channel, ByteArrayHandler>() ) ) ;

	protected void handleIncommingConnectionKey(SelectionKey key) {
		ByteArrayHandler datahandler = createHandler();
		handlers.put( key.channel(), datahandler);
	}

	//TODO make this abstract and remove code
	protected ByteArrayHandler createHandler() {
		return null;
	}

	//TODO make this abstract (and the class by the occasion)
	protected void handleObject(Object o, SocketChannel chan) {

	}

}
