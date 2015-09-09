package org.jnet.core.synchronizer;

import org.jnet.core.helper.CompareSameWrapper;
import org.jnet.core.synchronizer.message.ChangedStateMessage;

public class LookAheadObjectMaster<T> extends LookAheadObject<T> implements ObjectReadProvider {
	private final DiffIdentifier<T, ?> diffIdentifier;

	
	public LookAheadObjectMaster(T objectToSynchronize, LookAheadObjectConfiguration<T> config) {
		super(objectToSynchronize, config);
		
		if (config.getDiffIdentifier() != null) {
			this.diffIdentifier = config.getDiffIdentifier();
		} else {
			this.diffIdentifier = new DefaultDiffIdentifier<>(config, this, objectTraverser);
		}
	}

	public LookAheadObjectMaster(T objectToSynchronize) {
		this(objectToSynchronize, new LookAheadObjectConfiguration<>());
	}
	
	public ChangedStateMessage createMessage(int timestamp) {
		return new ChangedStateMessage(timestamp);
	}
	
	@SuppressWarnings("unchecked")
	public ChangedStateMessage evolveLastTrustedState(int diff) {
		int newTimestamp = lastTrustedState.getTimestamp() + diff;
		Object beforeState = diffIdentifier.prepareObject(lastTrustedState.getState());
		lastTrustedState.updateState(sortedEvents, newTimestamp);
		inventorizeObject(lastTrustedState.getState());
		ChangedStateMessage message = createMessage(newTimestamp);
		((DiffIdentifier<T, Object>) diffIdentifier).findDiff(beforeState, lastTrustedState.getState(), message);
		
		cleanup();
		return message;
	}

	@Override
	public ObjectId getIdForObject(Object object) {
		ObjectId ret = managedObjectsReverse.get(new CompareSameWrapper<>(object));
		
		if (ret == null) {
			throw new RuntimeException(object + " is not a managed object, couldn't retrieve id");
		}
		
		return ret;
	}
}
