package org.jnet.core.connection.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jnet.core.AbstractGameEngine;
import org.jnet.core.helper.BeanHelper;

public class NewStateMessage extends AbstractMessage {
	private static final Logger logger = LogManager.getLogger(NewStateMessage.class);

	private int id;
	
	private int ts;
	
	private MetaData metaData;
	
	private AbstractGameEngine gameEngine;
	
	private Map<Field, Object> state = new HashMap<>();
	
	public NewStateMessage(int id, int ts, Object state) {
		super();
		this.id = id;
		this.ts = ts;
		try {
			metaData = new MetaData(state);
			for (Field field : metaData.getFields()) {
				this.state.put(field, field.get(state));
			}
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
		return "NewStateMessage [id=" + id + ", ts=" + ts + ", metaData=" + metaData + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((gameEngine == null) ? 0 : gameEngine.hashCode());
		result = prime * result + id;
		result = prime * result + ((metaData == null) ? 0 : metaData.hashCode());
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
		if (metaData == null) {
			if (other.metaData != null)
				return false;
		} else if (!metaData.equals(other.metaData))
			return false;
		if (ts != other.ts)
			return false;
		return true;
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
	
	public void setGameEngine(AbstractGameEngine gameEngine) {
		this.gameEngine = gameEngine;
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

class MetaData {
	private final List<Field> fields = new LinkedList<>();

	private int nullableCount;
	
	public MetaData(Object o) throws Exception {
		BeanHelper.forEachRelevantField(o, field -> {
			if (BeanHelper.isPrimitive(field.getType())) {
				fields.add(field);
				if (field.getType().isPrimitive()) {
					nullableCount++;
				}
			}
		});
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fields == null) ? 0 : fields.hashCode());
		result = prime * result + nullableCount;
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
		MetaData other = (MetaData) obj;
		if (fields == null) {
			if (other.fields != null)
				return false;
		} else if (!fields.equals(other.fields))
			return false;
		if (nullableCount != other.nullableCount)
			return false;
		return true;
	}



	@Override
	public String toString() {
		return "MetaData [fields=" + fields + ", nullableCount=" + nullableCount + "]";
	}



	public List<Field> getFields() {
		return fields;
	}
}
