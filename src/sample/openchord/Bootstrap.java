package openchord;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.util.TesterUtil;

public class Bootstrap {
	
	private static final Logger log = Logger.getLogger(Bootstrap.class.getName());
			
	public static void main(String[] args) {	
		de.uniba.wiai.lspi.chord.service.PropertiesLoader.loadPropertyFile(); 
		String protocol = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
		URL localURL = null; 
		
		try {
			// Log creation
			FileHandler handler = new FileHandler(TesterUtil.getLogfolder()+"/bootstrap.log",true);
			handler.setFormatter(new LogFormat());
			log.addHandler(handler);				
			
			/*String address = InetAddress.getLocalHost().toString();
			address = address.substring(address.indexOf("/")+1,address.length());
			localURL = new URL(protocol + "://"+address+":"+TesterUtil.getBootstrapPort()+"/");
			log.info("[Bootstrap] Starting at: "+address+" "+TesterUtil.getBootstrapPort());*/
			localURL = new URL(protocol + "://"+TesterUtil.getBootstrap()+":"+TesterUtil.getBootstrapPort()+"/");			
			log.info("[Dbpartout] Bootstrap : "+localURL);
		} catch (MalformedURLException e){
			throw new RuntimeException(e); 
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Chord chord = new de.uniba.wiai.lspi.chord.service.impl.ChordImpl(); 
		try {			
			chord.create(localURL);
			log.info("[Dbpartout] Creating DHT : "+chord.toString());
		} catch (ServiceException e) {
			throw new RuntimeException("Could not create DHT!", e); 
		}
	}
}	
	
		
	
	
	
	