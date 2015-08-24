package org.jnet.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MetaDataManager {
	private final Map<Class<?>, MetaData> metaData;
	
	public MetaDataManager() {
		metaData = new ConcurrentHashMap<>();
	}

	public MetaData get(Class<?> clazz) {
		return metaData.get(clazz);
	}
	
	public void add(Class<?> clazz) throws Exception {
		metaData.put(clazz, new MetaData(clazz));

	}
}
