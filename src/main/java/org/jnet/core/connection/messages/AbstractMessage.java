package org.jnet.core.connection.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

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

	@Override
	public void write(OutputStream stream) throws Exception {
		write(new DataOutputStream(stream));
	}

	@Override
	public void read(InputStream stream) throws Exception {
		read(new DataInputStream(stream));
	}
}
