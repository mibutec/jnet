package org.jnet.core.synchronizer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jnet.core.helper.ObjectTraverser;
import org.jnet.core.synchronizer.message.ChangedStateMessage;

public class DefaultDiffIdentifier<T> implements DiffIdentifier<T> {
	private final ObjectTraverser objectTraverser = new ObjectTraverser();

	private final ObjectReadProvider objectReadProvider;
	
	private Map<ObjectId, Map<String, Object>> before = new HashMap<>();

	public DefaultDiffIdentifier(ObjectReadProvider objectReadProvider) {
		super();
		this.objectReadProvider = objectReadProvider;
	}

	@Override
	public T prepareObject(T beforeState) {
		before = createMapRepresentation(beforeState);
		return beforeState;
	}
	
	@Override
	public void findDiff(T beforeState, T afterState, ChangedStateMessage message) {
		Map<ObjectId, Map<String, Object>> after = createMapRepresentation(afterState);
		addNewObjects(before, after, message);
	}
	
	private Map<ObjectId, Map<String, Object>> createMapRepresentation(T objectToMap) {
		Map<ObjectId, Map<String, Object>> ret = new HashMap<>();
		objectTraverser.traverse(objectToMap, (object, parent, accessor) -> {
			if (!ObjectTraverser.isPrimitive(object.getClass())) {
				handleMap(ret, object);
			}
			
			if (parent != null) {
				Map<String, Object> parentMap = handleMap(ret, parent);
				if (!ObjectTraverser.isPrimitive(object.getClass())) {
					object = objectReadProvider.getIdForObject(object);
				}
				parentMap.put(accessor, object);
			}
			
			return true;
		});
		
		return ret;
	}
	
	private Map<String, Object> handleMap(Map<ObjectId, Map<String, Object>> map, Object object) {
		ObjectId objectId = objectReadProvider.getIdForObject(object);
		Map<String, Object> objectMap = map.get(objectId);
		if (objectMap == null) {
			objectMap = new HashMap<>();
			map.put(objectId, objectMap);
		}
		
		return objectMap;
	}

	private void addNewObjects(Map<ObjectId, Map<String, Object>> beforeState, Map<ObjectId, Map<String, Object>> afterState, ChangedStateMessage message) {
		Set<ObjectId> afterObjects = afterState.keySet();
		Set<ObjectId> beforeObjects = beforeState.keySet();
		
		Set<ObjectId> newObjects = new HashSet<>(afterObjects);
		newObjects.removeAll(beforeObjects);
		
		Set<ObjectId> sameObjects = new HashSet<>(beforeObjects);
		sameObjects.retainAll(afterObjects);
		
		for (ObjectId newObjectId : newObjects) {
			message.addNewObject(newObjectId, objectReadProvider.getObject(newObjectId));
		}

//		for (ObjectId newObjectId : newObjects) {
//			message.addNewObject(newObjectId, objectReadProvider.getObject(newObjectId));
//		}
	}
}

abstract class AbstractTypeDiffer<T> {
	private final Class<T> handledType;
	
	protected final ObjectReadProvider objectReadProvider;

	protected AbstractTypeDiffer(Class<T> handledType, ObjectReadProvider objectReadProvider) {
		this.handledType = handledType;
		this.objectReadProvider = objectReadProvider;
	}

	public boolean canHandle(Class<?> clazz) {
		return handledType.isAssignableFrom(clazz);
	}
	
	public abstract void addChangeMessages(T object1, T object2, ChangedStateMessage message);
}

class ObjectDiffer extends AbstractTypeDiffer<Object> {

	public ObjectDiffer(ObjectReadProvider objectReadProvider) {
		super(Object.class, objectReadProvider);
	}

	@Override
	public void addChangeMessages(Object object1, Object object2, ChangedStateMessage message) {
		assert(objectReadProvider.getIdForObject(object1).equals(objectReadProvider.getIdForObject(object2)));
		
	}
}