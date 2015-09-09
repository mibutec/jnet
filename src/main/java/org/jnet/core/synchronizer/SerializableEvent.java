package org.jnet.core.synchronizer;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public class SerializableEvent extends Event {
	private final ObjectId objectId;
	
	private final Method method;

	private final Object[] args;

	public SerializableEvent(ObjectId objectId, int ts, Method method, Object... args) {
		super(ts);
		this.method = method;
		this.args = args;
		this.objectId = objectId;
	}

	@Override
	public String toString() {
		return "Event [ts=" + ts + ", method=" + method + ", args=" + Arrays.toString(args)
				+ "]";
	}

	@Override
	public int compareTo(Event other) {
		return Integer.compare(ts, other.ts);
	}

	@Override
	public int hashCode() {
		return Objects.hash(objectId, ts, method, Arrays.hashCode(args));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != getClass()) {
			return false;
		}

		SerializableEvent other = (SerializableEvent) obj;
		return Objects.equals(objectId, other.objectId) && Objects.equals(ts, other.ts)
				&& Objects.equals(method, other.method) && Arrays.equals(args, other.args);
	}

	public void invoke(Object object) {
		try {
			method.invoke(object, args);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public int getTs() {
		return ts;
	}

	public Method getEvent() {
		return method;
	}

	public Object[] getArgs() {
		return args;
	}

	public ObjectId getObjectId() {
		return objectId;
	}
}