package org.jnet.core.synchronizer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;


public class ObjectId {
	public final int id;

	public ObjectId(int id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return "ObjectId [id=" + id + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != getClass()) {
			return false;
		}
		
		ObjectId other = (ObjectId) obj;
		return id == other.id;
	}
	
	public void toStream(DataOutputStream stream) throws IOException {
		stream.writeInt(id);
	}
	
	public static ObjectId fromStream(DataInputStream stream) throws IOException {
		return new ObjectId(stream.readInt());
	}
}
