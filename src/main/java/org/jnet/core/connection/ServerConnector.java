package org.jnet.core.connection;

import org.jnet.core.GameServer;

public interface ServerConnector extends AutoCloseable {
	void setGameServer(GameServer server);
}
