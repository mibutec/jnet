package org.jnet.core.synchronizer;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jnet.core.helper.BestTypeMatcher;
import org.jnet.core.helper.BestTypeMatcher.TypeHandler;
import org.jnet.core.helper.ObjectTraverser;
import org.jnet.core.helper.ObjectTraverser.FieldHandler;
import org.jnet.core.helper.PojoHelper;
import org.jnet.core.helper.Unchecker;
import org.jnet.core.synchronizer.DefaultDiffIdentifier.AbstractTypeHandler;
import org.jnet.core.synchronizer.message.ChangedStateMessage;

import com.google.common.base.Objects;

public class DefaultDiffIdentifier<TYPE> implements DiffIdentifier<TYPE, Map<ObjectId, Object>> {
	private final ObjectTraverser objectTraverser;
	
	private final ObjectReadProvider objectReadProvider;
	
	private final BestTypeMatcher<AbstractTypeHandler<?, ?>> bestTypeMatcher = new BestTypeMatcher<>();

	public DefaultDiffIdentifier(LookAheadObjectConfiguration<?> config, ObjectReadProvider objectReadProvider, ObjectTraverser configuredObjectTraverser) {
		super();
		this.objectReadProvider = objectReadProvider;
		this.objectTraverser = configuredObjectTraverser;
		addTypeHandler(new ObjectDiffer(objectReadProvider));
		
		for (AbstractTypeHandler<?, ?> handler : config.getTypeHandlers()) {
			addTypeHandler(handler);
		}
	}
	
	public void addTypeHandler(AbstractTypeHandler<?, ?> typeDiffer) {
		bestTypeMatcher.addFieldHandler(typeDiffer);
	}

	@Override
	public Map<ObjectId, Object> prepareObject(TYPE beforeState) {
		return createMapRepresentation(beforeState);
	}
	
	@Override
	public void findDiff(Map<ObjectId, Object> before, TYPE afterState, ChangedStateMessage message) {
		addNewObjects(before, afterState, message);
		addChangedObjects(before, afterState, message);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map<ObjectId, Object> createMapRepresentation(TYPE objectToMap) {
		Map<ObjectId, Object> ret = new HashMap<>();
		objectTraverser.traverse(objectToMap, false, false, (object, parent, accessor) -> {
			ObjectId objectId = objectReadProvider.getIdForObject(object);
			if (!ret.containsKey(objectId)) {
				AbstractTypeHandler differ = bestTypeMatcher.getBestMatchingHandler(object.getClass());
				ret.put(objectId, differ.createDiffRepresentation(object));
			}
			
			return true;
		});
		
		return ret;
	}
	
	private void addNewObjects(Map<ObjectId, ?> beforeState, TYPE afterState, ChangedStateMessage message) {
		Set<ObjectId> afterObjects = createMapRepresentation(afterState).keySet();
		Set<ObjectId> beforeObjects = beforeState.keySet();
		
		Set<ObjectId> newObjects = new HashSet<>(afterObjects);
		newObjects.removeAll(beforeObjects);
		
		Set<ObjectId> sameObjects = new HashSet<>(beforeObjects);
		sameObjects.retainAll(afterObjects);
		
		for (ObjectId newObjectId : newObjects) {
			message.addNewObject(newObjectId, objectReadProvider.getObject(newObjectId));
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addChangedObjects(Map<ObjectId, Object> beforeState, TYPE afterState, ChangedStateMessage message) {
		objectTraverser.traverse(afterState, false, false, (object, parent, accessor) -> {
			AbstractTypeHandler differ = bestTypeMatcher.getBestMatchingHandler(object.getClass());
			ObjectId objectId = objectReadProvider.getIdForObject(object);
			differ.addChangeMessages(objectId, (Object) beforeState.get(objectId), object, message);
			return !differ.handlesSubobjectsItself;
		});
	}
	
	public static abstract class AbstractTypeHandler<TYPE, REPRESENTATION_TYPE> extends TypeHandler<TYPE>{
		protected final ObjectReadProvider objectReadProvider;
		
		/**
		 * If this {@link AbstractTypeHandler} finds a Type to handle, this defines if DiffIdentifier should continue working 
		 * for all it subfields, or will this Differ do it itself.
		 * Say you implement a {@link AbstractTypeHandler} for a list, if this field is true, you have to diff all the contained
		 * objects yourself. If it's false you just handle diff recognation for the list itself.
		 */
		protected final boolean handlesSubobjectsItself;

		protected AbstractTypeHandler(Class<TYPE> handledType, ObjectReadProvider objectReadProvider, boolean handlesSubobjectsItself) {
			super(handledType);
			this.objectReadProvider = objectReadProvider;
			this.handlesSubobjectsItself = handlesSubobjectsItself;
		}

		protected AbstractTypeHandler(Class<TYPE> handledType, ObjectReadProvider objectReadProvider) {
			this(handledType, objectReadProvider, false);
		}
		
		public FieldHandler<TYPE> getFieldHandler() {
			return null;
		}
		
		public abstract REPRESENTATION_TYPE createDiffRepresentation(TYPE object);

		public abstract void addChangeMessages(ObjectId objectId, REPRESENTATION_TYPE before, Object after, ChangedStateMessage message);
	}
}

class ObjectDiffer extends AbstractTypeHandler<Object, Map<String, Object>> {

	public ObjectDiffer(ObjectReadProvider objectReadProvider) {
		super(Object.class, objectReadProvider);
	}

	@Override
	public void addChangeMessages(ObjectId objectId, Map<String, Object> before, Object after, ChangedStateMessage message) {
		Unchecker.uncheck(() -> {
			Map<String, Object> changedFields = new HashMap<>();
			PojoHelper.forEachField(after, Modifier.STATIC | Modifier.TRANSIENT, (field, value) -> {
				value = wrapObject(value);
				if (before == null || !Objects.equal(before.get(field.getName()), value)) {
					changedFields.put(field.getName(), value);
				}
			});
			if (changedFields.size() > 0) {
				message.addUpdateObject(objectId, changedFields);
			}
		});
	}

	@Override
	public Map<String, Object> createDiffRepresentation(Object object) {
		return Unchecker.uncheck(() -> {
			Map<String, Object> ret = new HashMap<>();
			PojoHelper.forEachField(object, Modifier.STATIC | Modifier.TRANSIENT, (field, value) -> {
				ret.put(field.getName(), wrapObject(value));
			});
	
			return ret;
		});
	}
	
	private Object wrapObject(Object o) {
		if (o == null) {
			return null;
		}
		if (PojoHelper.isPrimitive(o.getClass())) {
			return o;
		}
		
		return objectReadProvider.getIdForObject(o);
	}
}

class ArrayDiffer extends AbstractTypeHandler<Object, Object[]> {

	public ArrayDiffer(Class<Object> handledType, ObjectReadProvider objectReadProvider) {
		super(handledType, objectReadProvider);
	}

	@Override
	public Object[] createDiffRepresentation(Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addChangeMessages(ObjectId objectId, Object[] before, Object after, ChangedStateMessage message) {
		// TODO Auto-generated method stub
		
	}
	
}