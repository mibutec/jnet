package org.jnet.core.synchronizer;

import org.jnet.core.synchronizer.message.ChangedStateMessage;

public class LookAheadObjectSlave<T> extends LookAheadObject<T> {

	protected LookAheadObjectSlave(T objectToSynchronize) {
		super(objectToSynchronize);
	}
	
	public void evolveLastTrustedState(ChangedStateMessage changedStateMessage) {
		int timestamp = changedStateMessage.getTimestamp();
		lastTrustedState.setTimestamp(timestamp);
		changedStateMessage.apply(this);
		cleanup();
	}
}
