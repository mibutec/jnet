package org.jnet.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

public class Event<T> implements Comparable<Event<T>>, Serializable {
	private static final long serialVersionUID = 1L;

	private int ts;
	
	private byte sequence;
	
	private Method method;
	
	private Object[] args;

	Event() {
		
	}

	public Event(int ts, byte sequence, Method method, Object[] args) {
		super();
		this.ts = ts;
		this.method = method;
		this.args = args;
		this.sequence = sequence;
	}
	
	@Override
	public String toString() {
		return "Event [ts=" + ts + ", sequence=" + sequence + ", method=" + method + ", args=" + Arrays.toString(args)
				+ "]";
	}

	public int compareTo(long otherTs, byte otherSequence) {
		if (getTs() != otherTs) {
			return Long.valueOf(getTs()).compareTo(otherTs);
		}
		
		return Byte.valueOf(sequence).compareTo(otherSequence);
	}

	@Override
	public int compareTo(Event<T> other) {
		return compareTo(other.ts, other.sequence);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(args);
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + sequence;
		result = prime * result + (int) (ts ^ (ts >>> 32));
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Event<T> other = (Event<T>) obj;
		if (!Arrays.equals(args, other.args))
			return false;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		if (sequence != other.sequence)
			return false;
		if (ts != other.ts)
			return false;
		return true;
	}

	public void invoke(T object) {
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

	public byte getSequence() {
		return sequence;
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(ts);
		out.writeByte(sequence);
		out.writeObject(method.getDeclaringClass().getName());
		out.writeObject(method.getName());
		out.writeObject(method.getParameterTypes());
		out.writeObject(args);
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException, NoSuchMethodException {
		ts = in.readInt();
		sequence = in.readByte();
		String classname = (String) in.readObject();
		String methodName = (String) in.readObject();
		Class<?>[] parameterTypes = (Class<?>[]) in.readObject();
		method = Class.forName(classname).getDeclaredMethod(methodName, parameterTypes);
		args = (Object[]) in.readObject();
	}
}