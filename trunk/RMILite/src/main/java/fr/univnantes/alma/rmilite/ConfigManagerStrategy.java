package fr.univnantes.alma.rmilite;

import fr.univnantes.alma.rmilite.registry.NamingServer;
import fr.univnantes.alma.rmilite.server.RemoteObjectProvider;

public interface ConfigManagerStrategy {

    public RemoteObjectProvider getRemoteObjectProvider();

    public NamingServer getNamingServer();
}
