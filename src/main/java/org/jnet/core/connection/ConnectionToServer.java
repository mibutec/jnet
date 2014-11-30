package org.jnet.core.connection;

import java.io.IOException;

import org.jnet.core.Event;
import org.jnet.core.GameClient;

public interface ConnectionToServer extends AutoCloseable {
	void sendEvent(int id, Event<?> event) throws IOException;
	
	void requestServerTime() throws IOException;
	
	public void setClient(GameClient client);
}
