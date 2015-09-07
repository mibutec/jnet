package org.jnet.core;

import org.jnet.core.synchronizer.MetaData;
import org.jnet.core.synchronizer.ObjectId;

public interface ManagedObject<T> {
	ObjectId _getMoId_();
	
	MetaData _getMoMetaData_();
	
	T _getMoLatestState_();
	
	T _getMoLastTrustedState_();
}
