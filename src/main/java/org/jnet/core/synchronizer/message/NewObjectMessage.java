package org.jnet.core.synchronizer.message;

import java.util.Objects;

import org.jnet.core.helper.PojoHelper;
import org.jnet.core.helper.Unchecker;
import org.jnet.core.synchronizer.ObjectChangeProvider;
import org.jnet.core.synchronizer.ObjectId;

public class NewObjectMessage implements SynchronizationMessage {
	protected final ObjectId objectId;
	
	protected final String className;
	
	public NewObjectMessage(ObjectId objectId, String className) {
		super();
		this.objectId = objectId;
		this.className = className;
	}

	public NewObjectMessage(ObjectId objectId, Class<?> clazz) {
		this(objectId, clazz.getName());
	}

	@Override
	public void apply(ObjectChangeProvider changeProvider) {
		System.out.println("add " + objectId);
		Unchecker.uncheck(() -> changeProvider.addObject(objectId, createInstance()));
	}
	
	protected Object createInstance() throws Exception {
		return PojoHelper.instantiate(Class.forName(className));
	}
	
	@Override
	public String toString() {
		return "NewObjectMessage [objectId=" + objectId + ", className=" + className + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(className, objectId);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != getClass()) {
			return false;
		}
		NewObjectMessage other = (NewObjectMessage) obj;
		return Objects.equals(objectId, other.objectId) && Objects.equals(className, other.className);
	}
}
