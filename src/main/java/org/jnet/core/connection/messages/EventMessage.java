package org.jnet.core.connection.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;

import org.jnet.core.synchronizer.Event;
import org.jnet.core.synchronizer.ObjectId;

public class EventMessage extends AbstractMessage {
	private Event event;
	
	public EventMessage() {
		super();
	}

	public EventMessage(Event event) {
		super();
		this.event = event;
	}
	
	@Override
	public String toString() {
		return "EventMessage [event=" + event + "]";
	}

	@Override
	public void write(DataOutputStream outStream) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(outStream);
		event.getObjectId().toStream(outStream);
		out.writeInt(event.getTs());
		out.writeObject(event.getEvent().getDeclaringClass().getName());
		out.writeObject(event.getEvent().getName());
		out.writeObject(event.getEvent().getParameterTypes());
		out.writeObject(event.getArgs());
	}
		
	@Override
	public void read(DataInputStream inStream) throws Exception {
		ObjectInputStream in = new ObjectInputStream(inStream);
		ObjectId id = ObjectId.fromStream(inStream);
		int ts = in.readInt();
		String classname = (String) in.readObject();
		String methodName = (String) in.readObject();
		Class<?>[] parameterTypes = (Class<?>[]) in.readObject();
		Method method = Class.forName(classname).getDeclaredMethod(methodName, parameterTypes);
		Object[] args = (Object[]) in.readObject();
		
		event = new Event(id, ts, method, args);
	}

	public Event getEvent() {
		return event;
	}
}
