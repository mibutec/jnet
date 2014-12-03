package org.jnet.core.connection;

import org.jnet.core.AbstractGameEngine;
import org.jnet.core.connection.messages.Message;


public interface Connection extends AutoCloseable {
	void send(Message nessage) throws Exception;
	Message nextMessage();
	void setGameEngine(AbstractGameEngine engine);
}