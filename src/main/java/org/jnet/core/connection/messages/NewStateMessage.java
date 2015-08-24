package org.jnet.core.connection.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jnet.core.AbstractGameEngine;
import org.jnet.core.ManagedObject;
import org.jnet.core.MetaData;
import org.jnet.core.MetaDataManager;

public class NewStateMessage extends AbstractMessage {
	private static final Logger logger = LogManager.getLogger(NewStateMessage.class);

	private int id;
	
	private int ts;
	
	private MetaDataManager metaDataManager;
	
	private MetaData metaData;
	
	private Map<Field, Object> state = new HashMap<>();
	
	public NewStateMessage(int id, int ts, ManagedObject<?> state) {
		super();
		this.id = id;
		this.ts = ts;
		this.metaData = state._getMoMetaData_();
		try {
			for (Field field : metaData.getFields()) {
				this.state.put(field, field.get(state));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((metaDataManager == null) ? 0 : metaDataManager.hashCode());
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
		if (id != other.id)
			return false;
		if (metaDataManager == null) {
			if (other.metaDataManager != null)
				return false;
		} else if (!metaDataManager.equals(other.metaDataManager))
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
	public String toString() {
		return "NewStateMessage [id=" + id + ", ts=" + ts + ", metaDataManager=" + metaDataManager + ", state=" + state
				+ "]";
	}

	public NewStateMessage(MetaDataManager metaDataManager) {
		super();
		this.metaDataManager = metaDataManager;
	}
	
	@Override
	public void write(DataOutputStream out) throws Exception {
		out.writeInt(id);
		out.writeInt(ts);
		for (Field field : metaData.getFields()) {
			writePrimitveToStream(field.getType(), state.get(field), out);
		}
	}
	
	private void writePrimitveToStream(Class<?> type, Object primitiveAsObject, DataOutputStream out) throws IOException {
		if (primitiveAsObject == null) {
			logger.trace("writing null to stream");
			out.writeBoolean(true);
			return;
		}
		
		if (!type.isPrimitive()) {
			logger.trace("{} is not a primitive, writing pre-byte {}", primitiveAsObject.getClass(), false);
			out.writeBoolean(false);
		}
		
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
			out.writeUTF((String) primitiveAsObject);
		} else {
			throw new RuntimeException("unknown datatype " + primitiveAsObject.getClass().getName() + " when trying to serialize state");
		}
	}
	
	@Override
	public void read(DataInputStream in) throws Exception {
		id = in.readInt();
		ts = in.readInt();
		MetaData metaData = new MetaData(gameEngine.getObject(Object.class, id));
		for (Field field : metaData.getFields()) {
			logger.trace("reading field {} from stream...", field.getName());
			Object o = readPrimitveFromStream(field.getType(), in);
			logger.trace("value for field {} is {}", field.getName(), o);
			state.put(field, o);
		}
	}

	private Object readPrimitveFromStream(Class<?> type, DataInputStream in) throws IOException {
		if (!type.isPrimitive()) {
			boolean isNull = in.readBoolean();
			logger.trace("{} is not a primitive, pre-byte is {}", type.getName(), isNull);
			if (isNull) {
				return null;
			}
		}
		
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

	public Map<Field, Object> getStateAsMap() {
		return state;
	}
}