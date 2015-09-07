package org.jnet.core.synchronizer;

public interface ObjectChangeProvider {
	void addObject(ObjectId objectId, Object object);
	Object getObject(ObjectId objectId);
	MetaData getMetaData(Object object);
}
