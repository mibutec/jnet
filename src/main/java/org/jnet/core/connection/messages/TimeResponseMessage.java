package org.jnet.core.connection.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TimeResponseMessage extends AbstractMessage {
	private long clientTimestamp;
	
	private int serverTime;
	
	public TimeResponseMessage() {
		super();
	}

	public TimeResponseMessage(long clientTimestamp, int serverTime) {
		super();
		this.clientTimestamp = clientTimestamp;
		this.serverTime = serverTime;
	}
	
	@Override
	public String toString() {
		return "TimeResponseMessage [clientTimestamp=" + clientTimestamp + ", serverTime=" + serverTime + "]";
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeLong(clientTimestamp);
		out.writeInt(serverTime);
	}
		
	@Override
	public void read(DataInputStream in) throws Exception {
		clientTimestamp = in.readLong();
		serverTime = in.readInt();
	}

	public long getClientTimestamp() {
		return clientTimestamp;
	}

	public int getServerTime() {
		return serverTime;
	}
}
