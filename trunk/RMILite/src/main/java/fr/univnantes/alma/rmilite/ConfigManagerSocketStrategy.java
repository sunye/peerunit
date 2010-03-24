package fr.univnantes.alma.rmilite;

import fr.univnantes.alma.rmilite.RemoteMethodFactory;
import fr.univnantes.alma.rmilite.client.StubFactory;
import fr.univnantes.alma.rmilite.io.Manager_IO;
import fr.univnantes.alma.rmilite.registry.NamingServer;
import fr.univnantes.alma.rmilite.registry.NamingServer_Socket;
import fr.univnantes.alma.rmilite.server.RemoteObjectProvider;
import fr.univnantes.alma.rmilite.server.RemoteObjectProvider_Socket;

/**
 * Initializes the system.
 */
public class ConfigManagerSocketStrategy implements ConfigManagerStrategy {

    private static RemoteObjectProvider remoteObjectProvider;
    private static NamingServer namingServer;
    private static boolean constructed = false;

    public ConfigManagerSocketStrategy() {
	if (!constructed) {
	    /* Initialization */
	    Manager_IO io = new Manager_IO();
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
