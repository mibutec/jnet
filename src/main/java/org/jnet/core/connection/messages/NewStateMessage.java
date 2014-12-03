package org.jnet.core.connection.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jnet.core.AbstractGameEngine;
import org.jnet.core.helper.BeanHelper;

public class NewStateMessage extends AbstractMessage {
	private int id;
	
	private int ts;
	
	private Map<Field, Object> state;
	
	private AbstractGameEngine gameEngine;
	
	public NewStateMessage(int id, int ts, Object state) {
		super();
		this.id = id;
		this.ts = ts;
		this.state = new HashMap<>();
		try {
			BeanHelper.forEachRelevantField(state, field -> {
				if (BeanHelper.isPrimitive(field.getType())) {
					this.state.put(field, field.get(state));
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public NewStateMessage(AbstractGameEngine gameEngine) {
		super();
		this.gameEngine = gameEngine;
	}
	
	@Override
	public String toString() {
		return "NewStateMessage [id=" + id + ", ts=" + ts + ", state=" + state + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((gameEngine == null) ? 0 : gameEngine.hashCode());
		result = prime * result + id;
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ts;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NewStateMessage other = (NewStateMessage) obj;
		if (gameEngine == null) {
			if (other.gameEngine != null)
				return false;
		} else if (!gameEngine.equals(other.gameEngine))
			return false;
		if (id != other.id)
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		if (ts != other.ts)
			return false;
		return true;
	}

	@Override
	public void write(DataOutputStream out) throws Exception {
		out.writeInt(id);
		out.writeInt(ts);
		for (Entry<Field, Object> entry : state.entrySet()) {
			writePrimitveToStream(entry.getValue(), out);
		}
	}
	
	private void writePrimitveToStream(Object primitiveAsObject, DataOutputStream out) throws IOException {
		if (primitiveAsObject instanceof Byte) {
			out.writeByte((Byte) primitiveAsObject);
		} else if (primitiveAsObject instanceof Boolean) {
			out.writeBoolean((Boolean) primitiveAsObject);
		} else if (primitiveAsObject instanceof Character) {
			out.writeChar((Character) primitiveAsObject);
		} else if (primitiveAsObject instanceof Double) {
			out.writeDouble((Double) primitiveAsObject);
		} else if (primitiveAsObject instanceof Float) {
			out.writeFloat((Float) primitiveAsObject);
		} else if (primitiveAsObject instanceof Integer) {
			out.writeInt((Integer) primitiveAsObject);
		} else if (primitiveAsObject instanceof Long) {
			out.writeLong((Long) primitiveAsObject);
		} else if (primitiveAsObject instanceof Short) {
			out.writeShort((Short) primitiveAsObject);
		} else if (primitiveAsObject instanceof String) {
			out.writeBytes((String) primitiveAsObject);
		} else {
			throw new RuntimeException("unknown datatype " + primitiveAsObject.getClass().getName() + " when trying to serialize state");
		}
	}
		
	@Override
	public void read(DataInputStream in) throws Exception {
		id = in.readInt();
		ts = in.readInt();
		state = new HashMap<>();
		
		BeanHelper.forEachRelevantField(gameEngine.getObject(Object.class, id), field -> {
			if (BeanHelper.isPrimitive(field.getType())) {
				state.put(field, readPrimitveFromStream(field.getType(), in));
			}
		});
	}

	private Object readPrimitveFromStream(Class<?> type, DataInputStream in) throws IOException {
		if (type == Byte.class || type == byte.class) {
			return in.readByte();
		} else if (type == Boolean.class || type == boolean.class) {
			return in.readBoolean();
		} else if (type == Character.class || type == char.class) {
			return in.readChar();
		} else if (type == Double.class || type == double.class) {
			return in.readDouble();
		} else if (type == Float.class || type == float.class) {
			return in.readFloat();
		} else if (type == Integer.class || type == int.class) {
			return in.readInt();
		} else if (type == Long.class || type == long.class) {
			return in.readLong();
		} else if (type == Short.class || type == short.class) {
			return in.readShort();
		} else if (type == String.class) {
			return in.readUTF();
		} else {
			throw new RuntimeException("unknown datatype " + type.getName() + " when trying to deserialize state");
		}
	}
		
	public int getId() {
		return id;
	}

	public int getTs() {
		return ts;
	}

	public Map<Field, Object> getState() {
		return state;
	}
}
