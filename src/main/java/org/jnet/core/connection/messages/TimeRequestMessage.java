package org.jnet.core.connection.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TimeRequestMessage extends AbstractMessage {
	private long clientTimestamp;
	
	public TimeRequestMessage() {
		super();
	}

	public TimeRequestMessage(long clientTimestamp) {
		super();
		this.clientTimestamp = clientTimestamp;
	}
	
	@Override
	public String toString() {
		return "TimeRequestMessage [clientTimestamp=" + clientTimestamp + "]";
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeLong(clientTimestamp);
	}
		
	@Override
	public void read(DataInputStream in) throws Exception {
		clientTimestamp = in.readLong();
	}

	public long getClientTimestamp() {
		return clientTimestamp;
	}
}
