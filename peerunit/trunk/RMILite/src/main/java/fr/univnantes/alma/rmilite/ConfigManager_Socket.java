package fr.univnantes.alma.rmilite;

import fr.univnantes.alma.rmilite.RemoteMethodFactory;
import fr.univnantes.alma.rmilite.client.StubFactory;
import fr.univnantes.alma.rmilite.io.IOManager_IO;
import fr.univnantes.alma.rmilite.registry.NamingServer;
import fr.univnantes.alma.rmilite.registry.NamingServer_Socket;
import fr.univnantes.alma.rmilite.server.RemoteObjectProvider;
import fr.univnantes.alma.rmilite.server.RemoteObjectProvider_Socket;

/**
 * Initializes the system.
 */
public class ConfigManager_Socket implements ConfigManager {

	private static RemoteObjectProvider remoteObjectProvider;
	private static NamingServer namingServer;
	private static boolean constructed = false;

	public ConfigManager_Socket() {
		if (!constructed) {
			/* Initialization */
			IOManager_IO io = new IOManager_IO();
			NamingServer_Socket ns = new NamingServer_Socket();
			RemoteObjectProvider_Socket rop = new RemoteObjectProvider_Socket();

			rop.setIOManager(io);
			io.setRemoteObjectManager(rop);
			ns.setRemoteObjectProvider(rop);
			RemoteMethodFactory.remoteObjectManager = rop;
			StubFactory.ioManager = io;

			remoteObjectProvider = rop;
			namingServer = ns;
		}
	}

	public RemoteObjectProvider getRemoteObjectProvider() {
		return remoteObjectProvider;
	}

	public NamingServer getNamingServer() {
		return namingServer;
	}
}
