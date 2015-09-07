package org.jnet.core.synchronizer;

import org.jnet.core.synchronizer.message.ChangedStateMessage;

public class LookAheadObjectMaster<T> extends LookAheadObject<T> {
	private DiffIdentifier<T> diffIdentifier;

	public LookAheadObjectMaster(T objectToSynchronize) {
		super(objectToSynchronize);
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
}
