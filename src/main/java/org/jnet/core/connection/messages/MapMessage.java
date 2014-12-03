package org.jnet.core.connection.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

public class MapMessage extends AbstractMessage {
	private Map<String, Object> map;
	
	public MapMessage() {
		super();
	}

	public MapMessage(Map<String, Object> map) {
		super();
		this.map = map;
	}
	
	@Override
	public String toString() {
		return "MapMessage [map=" + map + "]";
	}

	@Override
	public void write(DataOutputStream outStream) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(outStream);
		out.writeObject(map);
	}
		
	@SuppressWarnings("unchecked")
	@Override
	public void read(DataInputStream inStream) throws Exception {
		ObjectInputStream in = new ObjectInputStream(inStream);
		map = (Map<String, Object>) in.readObject();
	}

	public Map<String, Object> getMap() {
		return map;
	}
}
