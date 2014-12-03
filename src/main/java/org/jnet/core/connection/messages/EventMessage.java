package org.jnet.core.connection.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;

import org.jnet.core.Event;

public class EventMessage extends AbstractMessage {
	private Event<?> event;
	
	private int id;
	
	public EventMessage() {
		super();
	}

	public EventMessage(int id, Event<?> event) {
		super();
		this.event = event;
		this.id = id;
	}
	
	@Override
	public String toString() {
		return "EventMessage [event=" + event + ", id=" + id + "]";
	}

	@Override
	public void write(DataOutputStream outStream) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(outStream);
		out.writeInt(id);
		out.writeInt(event.getTs());
		out.writeByte(event.getTs());
		out.writeObject(event.getEvent().getDeclaringClass().getName());
		out.writeObject(event.getEvent().getName());
		out.writeObject(event.getEvent().getParameterTypes());
		out.writeObject(event.getArgs());
	}
		
	@Override
	public void read(DataInputStream inStream) throws Exception {
		ObjectInputStream in = new ObjectInputStream(inStream);
		id = in.readInt();
		int ts = in.readInt();
		byte sequence = in.readByte();
		String classname = (String) in.readObject();
		String methodName = (String) in.readObject();
		Class<?>[] parameterTypes = (Class<?>[]) in.readObject();
		Method method = Class.forName(classname).getDeclaredMethod(methodName, parameterTypes);
		Object[] args = (Object[]) in.readObject();
		
		event = new Event<Object>(ts, sequence, method, args);
	}

	public Event<?> getEvent() {
		return event;
	}

	public int getId() {
		return id;
	}
}
