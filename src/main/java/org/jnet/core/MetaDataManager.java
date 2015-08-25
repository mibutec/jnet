package org.jnet.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MetaDataManager {
	public final int MAX_CLASS_COUNT = 1000;
	
	private final Map<Class<?>, MetaData> metaData;
	
	private final Map<Integer, MetaData> metaDataById;
	
	private AtomicInteger idGenerator = new AtomicInteger(0);
	
	public MetaDataManager() {
		metaData = new ConcurrentHashMap<>();
		metaDataById = new ConcurrentHashMap<>();
	}

	public MetaData get(Class<?> clazz) {
		MetaData existing = metaData.get(clazz);
		if (existing == null) {
			existing = new MetaData(clazz, idGenerator.incrementAndGet());
			if (existing.getId() > MAX_CLASS_COUNT) {
				throw new RuntimeException("overflow in metaDataId creation for class " + clazz.getName());
			}
			metaData.put(clazz, existing);
			metaDataById.put(existing.getId(), existing);
		}
		return existing;
	}
	
	public MetaData getByMetaDataId(int id) {
		MetaData metaData = metaDataById.get(id);
		if (metaData == null) {
			throw new RuntimeException("no meta data exist for id " + id);
		}
		return metaData;
	}

	public MetaData getByObjectId(int id) {
		return getByMetaDataId(id % MAX_CLASS_COUNT);
	}

	public Integer createObjectId(Class<?> clazz) {
		MetaData md = get(clazz);
		int noi = md.nextObjectId();
		if (noi > Integer.MAX_VALUE / MAX_CLASS_COUNT) {
			throw new RuntimeException("overflow in objectId creation for " + clazz.getName());
		}
		return noi * MAX_CLASS_COUNT + md.getId();
	}
}
