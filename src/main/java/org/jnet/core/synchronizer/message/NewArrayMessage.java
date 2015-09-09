package org.jnet.core.synchronizer.message;

import java.lang.reflect.Array;
import java.util.Objects;

import org.jnet.core.synchronizer.ObjectId;

public class NewArrayMessage extends NewObjectMessage {

	private final int length;
	
	public NewArrayMessage(ObjectId objectId, Class<?> clazz, int length) {
		super(objectId, clazz);
		this.length = length;
	}
	
	@Override
	protected Object createInstance() throws Exception {
		Object ret = Array.newInstance(Class.forName(className).getComponentType(), length);
		System.out.println(ret);
		System.out.println(new int[2]);
		return ret;
	}
	
	@Override
	public String toString() {
		return "NewArrayMessage [length=" + length + ", objectId=" + objectId + ", className=" + className + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(className, objectId, length);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != getClass()) {
			return false;
		}
		NewArrayMessage other = (NewArrayMessage) obj;
		return Objects.equals(objectId, other.objectId) && Objects.equals(className, other.className) && Objects.equals(length, other.length);
	}
}
