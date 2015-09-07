package org.jnet.core.synchronizer;

import org.jnet.core.synchronizer.message.ChangedStateMessage;

public interface DiffIdentifier<T> {
	public T prepareObject(T beforeState);
	public void findDiff(T beforeState, T afterState, ChangedStateMessage message);
}