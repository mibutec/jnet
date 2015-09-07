package org.jnet.core.synchronizer.message;

import org.jnet.core.synchronizer.ObjectChangeProvider;

public interface SynchronizationMessage {
	void apply(ObjectChangeProvider changeProvider);
}
