package org.jnet.core.connection.messages;

import org.jnet.core.connection.Connection;


public abstract class AbstractMessage implements Message {

	private Connection sender;
	
	public void setSender(Connection sender) {
		this.sender = sender;
	}

	@Override
	public Connection sender() {
		return sender;
	}
}
