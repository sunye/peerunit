package freepastry;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import rice.environment.Environment;
import util.FreeLocalPort;
import fr.inria.peerunit.util.TesterUtil;

public class Network {
	InetSocketAddress bootadd;
	public boolean joinNetwork(Peer peer, InetSocketAddress bootaddress, boolean createNetwork, Logger log){
		bootadd=bootaddress;
		Environment env = new Environment();

		// the port to use locally
		FreeLocalPort port= new FreeLocalPort();				
		int bindport = port.getPort(); 

		// build the bootaddress from the command line args			
		InetAddress bootIP=null;
		try {
			bootIP = InetAddress.getByName(TesterUtil.getBootstrap());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(bootadd==null){
			bootadd = new InetSocketAddress(bootIP,bindport);
		}

		boolean joined=false;
		try {
			if(!peer.join(bindport, bootadd, env, log,createNetwork))						
				joined=false;						
			else 
				joined=true;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return joined;
	}
	public InetSocketAddress getInetSocketAddress(){
		return bootadd;
	}
}
