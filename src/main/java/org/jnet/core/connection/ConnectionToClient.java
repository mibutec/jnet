package org.jnet.core.connection;

import java.io.IOException;


public interface ConnectionToClient extends AutoCloseable {
	void sendState(int id, Object state, int ts) throws IOException;
}