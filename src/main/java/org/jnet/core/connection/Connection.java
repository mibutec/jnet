package org.jnet.core.connection;

import org.jnet.core.connection.messages.Message;


public interface Connection extends AutoCloseable {
	void send(Message nessage) throws Exception;
	Message nextMessage();
	Message waitForMessage(int timeout);
}