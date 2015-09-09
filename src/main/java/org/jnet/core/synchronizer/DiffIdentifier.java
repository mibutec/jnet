package org.jnet.core.synchronizer;

import org.jnet.core.synchronizer.message.ChangedStateMessage;

public interface DiffIdentifier<TYPE, REPRESENTATION_TYPE> {
	public REPRESENTATION_TYPE prepareObject(TYPE beforeState);
	public void findDiff(REPRESENTATION_TYPE beforeState, TYPE afterState, ChangedStateMessage message);
}