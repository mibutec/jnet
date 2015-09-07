package org.jnet.core.synchronizer.message;

import org.jnet.core.synchronizer.ObjectChangeProvider;
import org.jnet.core.synchronizer.ObjectId;

public class AddObjectMessage implements SynchronizationMessage {
	private ObjectId objectId;
	
	private Object instantiatedObject;
	
	public AddObjectMessage(ObjectId objectId, Object instantiatedObject) {
		super();
		this.objectId = objectId;
		this.instantiatedObject = instantiatedObject;
	}

	@Override
	public void apply(ObjectChangeProvider changeProvider) {
		changeProvider.addObject(objectId, instantiatedObject);
	}
}
