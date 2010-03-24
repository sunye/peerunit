package fr.univnantes.alma.rmilite;

import fr.univnantes.alma.rmilite.registry.NamingServer;
import fr.univnantes.alma.rmilite.registry.NamingServer_RMI;
import fr.univnantes.alma.rmilite.server.RemoteObjectProvider;
import fr.univnantes.alma.rmilite.server.RemoteObjectProvider_RMI;

public class ConfigManagerRMI implements ConfigManager {

    private static RemoteObjectProvider remoteObjectProvider = new RemoteObjectProvider_RMI();
    private static NamingServer namingServer = new NamingServer_RMI();

    public RemoteObjectProvider getRemoteObjectProvider() {
    	return remoteObjectProvider;
    }

    public NamingServer getNamingServer() {
    	return namingServer;
    }
}
