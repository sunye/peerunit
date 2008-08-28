package fr.inria.peerunit.btree;


public class Inbox {
	boolean result;
	
	public boolean execute(MessageType message){
		if(message.equals(MessageType.EXECUTE)){
		 // invoke			
		}else if (message.equals(MessageType.FAIL)||
				message.equals(MessageType.ERROR)) {			
			return false;
		}
		return true; 
	}
}
