package org.jnet.core;

public interface ManagedObject<T> {
	int _getMoId_();
	
	MetaData _getMoMetaData_();
	
	T _getMoWrappedObject_();
}
