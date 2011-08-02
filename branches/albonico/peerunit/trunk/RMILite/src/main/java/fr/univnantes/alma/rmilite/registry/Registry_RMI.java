package fr.univnantes.alma.rmilite.registry;

import java.rmi.Remote;
import java.rmi.RemoteException;

public class Registry_RMI implements Registry {
	
	private java.rmi.registry.Registry reg;
	
	public Registry_RMI(java.rmi.registry.Registry reg) throws RemoteException {
		this.reg = reg;
	}

	/* (non-Javadoc)
	 * @see fr.univnantes.alma.rmilite.registry.Registry#bind(java.lang.String, java.rmi.Remote)
	 */
	@Override
	public void bind(String name, Remote object) throws Exception {
		this.reg.rebind(name, object);		
	}

	/* (non-Javadoc)
	 * @see fr.univnantes.alma.rmilite.registry.Registry#lookup(java.lang.String)
	 */
	@Override
	public Remote lookup(String name) throws Exception {
		return this.reg.lookup(name);
	}

	/* (non-Javadoc)
	 * @see fr.univnantes.alma.rmilite.registry.Registry#unbind(java.lang.String)
	 */
	@Override
	public void unbind(String name) throws Exception {
		this.reg.unbind(name);
	}
}
