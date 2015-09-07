package org.jnet.core.synchronizer;

import org.jnet.core.helper.CompareSameWrapper;
import org.jnet.core.synchronizer.message.ChangedStateMessage;

public class LookAheadObjectMaster<T> extends LookAheadObject<T> implements ObjectReadProvider {
	private final DiffIdentifier<T> diffIdentifier;

	
	public LookAheadObjectMaster(T objectToSynchronize, LookAheadObjectConfiguration<T> config) {
		super(objectToSynchronize, config);
		
		if (config.getDiffIdentifier() != null) {
			this.diffIdentifier = config.getDiffIdentifier();
		} else {
			this.diffIdentifier = new DefaultDiffIdentifier<>(this);
		}
	}

	public LookAheadObjectMaster(T objectToSynchronize) {
		this(objectToSynchronize, new LookAheadObjectConfiguration<>());
	}
	
	public ChangedStateMessage evolveLastTrustedState(int diff) {
		int newTimestamp = lastTrustedState.getTimestamp() + diff;
		T beforeState = diffIdentifier.prepareObject(lastTrustedState.getState());
		lastTrustedState.updateState(sortedEvents, newTimestamp);
		ChangedStateMessage message = new ChangedStateMessage(newTimestamp);
		diffIdentifier.findDiff(beforeState, lastTrustedState.getState(), message);
		
		cleanup();
		return message;
	}

	@Override
	public ObjectId getIdForObject(Object object) {
		return managedObjectsReverse.get(new CompareSameWrapper<>(object));
	}
}
