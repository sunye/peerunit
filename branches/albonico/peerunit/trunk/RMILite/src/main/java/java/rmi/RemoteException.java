package java.rmi;

@SuppressWarnings("serial")
public class RemoteException extends Exception {

	public RemoteException() {
		super();
	}
	
	public RemoteException(String s) {
		super(s);
	}
}
