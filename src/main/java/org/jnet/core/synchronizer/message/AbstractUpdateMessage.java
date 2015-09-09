package org.jnet.core.synchronizer.message;

import org.jnet.core.synchronizer.ObjectChangeProvider;
import org.jnet.core.synchronizer.ObjectId;

public abstract class AbstractUpdateMessage implements SynchronizationMessage {
	protected final ObjectId objectId;
	
	protected AbstractUpdateMessage(ObjectId objectId) {
		this.objectId = objectId;
	}
	
	protected Object unwrap(ObjectChangeProvider changeProvider, Object value) {
		if (value instanceof ObjectId) {
			value = changeProvider.getObject((ObjectId) value);
		}
		
		return value;
	}
}
