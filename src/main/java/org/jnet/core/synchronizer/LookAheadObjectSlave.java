package org.jnet.core.synchronizer;

import org.jnet.core.synchronizer.message.ChangedStateMessage;

public class LookAheadObjectSlave<T> extends LookAheadObject<T> {

	
	public LookAheadObjectSlave(T objectToSynchronize, LookAheadObjectConfiguration<T> config) {
		super(objectToSynchronize, config);
	}

	public LookAheadObjectSlave(T objectToSynchronize) {
		this(objectToSynchronize, null);
	}
	
	public void evolveLastTrustedState(ChangedStateMessage changedStateMessage) {
		int timestamp = changedStateMessage.getTimestamp();
		lastTrustedState.setTimestamp(timestamp);
		changedStateMessage.apply(this);
		cleanup();
	}
}
