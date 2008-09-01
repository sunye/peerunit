package fr.inria.peerunit.btree;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.logging.Level;

import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.test.oracle.Oracle;
import fr.inria.peerunit.test.oracle.Verdicts;
import fr.inria.peerunit.util.PeerUnitLogger;


public class Inbox {
	boolean result;
	
	private PeerUnitLogger log ;
	
	public Inbox(PeerUnitLogger log){
		this.log=log;
	}
	
	public boolean execute(MessageType message){
		if(message.equals(MessageType.EXECUTE)){
			log.log(Level.INFO, "[Inbox] EXECUTE");	
		}else if (message.equals(MessageType.FAIL)||
				message.equals(MessageType.ERROR)) {
			log.log(Level.INFO, "[Inbox] FAIL or ERROR");
			return false;
		}
		return true; 
	}
	
	
}
