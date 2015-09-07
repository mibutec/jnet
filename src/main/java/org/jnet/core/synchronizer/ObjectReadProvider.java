package org.jnet.core.synchronizer;

public interface ObjectReadProvider {
	ObjectId getIdForObject(Object object);
	Object getObject(ObjectId objectId);
}
