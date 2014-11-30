package org.jnet.core.connection;

import java.io.IOException;

import org.jnet.core.GameServer;

public interface ServerConnector extends AutoCloseable {
	void setGameServer(GameServer server) throws IOException;
}
