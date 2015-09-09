package org.jnet.core.helper;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jnet.core.helper.BestTypeMatcher.TypeHandler;
import org.jnet.core.helper.ObjectTraverser.ConfiguredTraverser;
import org.jnet.core.helper.ObjectTraverser.Consumer;
import org.jnet.core.helper.ObjectTraverser.FieldHandler;

import static org.jnet.core.helper.PojoHelper.arrayTypes;
import static org.jnet.core.helper.PojoHelper.isPrimitive;


public class ObjectTraverser {
	private BestTypeMatcher<FieldHandler<?>> bestTypeMatcher = new BestTypeMatcher<>();

	int modifiersToIgnore = Modifier.STATIC;
	
	public ObjectTraverser() {
		addFieldHandler(new ObjectFieldHandler());
		addFieldHandler(new CollectionFieldHandler());
		addFieldHandler(new MapFieldHandler());
		for (Class<?> arrayClass : arrayTypes) {
			addFieldHandler(new ArrayFieldHandler(arrayClass));
		}
	}
	
	public void setModifierToIgnore(int newValue) {
		this.modifiersToIgnore = newValue;
	}
	
	public int getModifiersToIgnore() {
		return modifiersToIgnore;
	}

	public void setModifiersToIgnore(int modifiersToIgnore) {
		this.modifiersToIgnore = modifiersToIgnore;
	}

	public void addFieldHandler(FieldHandler<?> fieldHandler) {
		bestTypeMatcher.addFieldHandler(fieldHandler);
	}
	
	public void traverse(Object object, Consumer consumer) {
		traverse(object, true, true, consumer);
	}
	
	public void traverse(Object object, boolean consumeNull, boolean consumePrimitives, Consumer consumer) {
		Unchecker.uncheck(() ->	traverse(object, null, null, consumer, new HashSet<>(), consumeNull, consumePrimitives));
	}
	
	@SuppressWarnings("unchecked")
	void traverse(Object object, Object parent,  String accessor, Consumer consumer, Set<CompareSameWrapper<?>> cycleDetector, boolean consumeNull, boolean consumePrimitives) throws Exception {
		
		if ((consumeNull || object != null) && (consumePrimitives || !isPrimitive(object.getClass()))) {
			if (!consumer.onObjectFound(object, parent, accessor)) {
				return;
			}
		}
		
		if (object == null) {
			return;
		}

		Class<?> clazz = object.getClass();
		if (isPrimitive(clazz)) {
			return;
		} else {
			CompareSameWrapper<?> wrapper = new CompareSameWrapper<Object>(object);
			if (cycleDetector.contains(wrapper)) {
				return;
			} else {
				cycleDetector.add(wrapper);
			}
		}

		FieldHandler<Object> handler = (FieldHandler<Object>) bestTypeMatcher.getBestMatchingHandler(clazz);
		handler.handleObject(object, new ConfiguredTraverser(this, cycleDetector, consumeNull, consumePrimitives), consumer);
	}
	
	public static interface Consumer {
		public boolean onObjectFound(Object object, Object parent, String accessor);
	}
	
	public static abstract class FieldHandler<T> extends TypeHandler<T> {
		protected FieldHandler(Class<T> handledType) {
			super(handledType);
		}
		
		public abstract void handleObject(T object, ConfiguredTraverser traverser, Consumer consumer) throws Exception;
	}
	
	/**
	 * Class used to shortcut the the call to traverse from an FieldHandler. This way you don't
	 * have to handle the Cycle Detector and others set in Handlers
	 * @author Michael
	 *
	 */
	public class ConfiguredTraverser {
		final ObjectTraverser objectTraverser;
		
		final Set<CompareSameWrapper<?>> cycleDetector;
		
		final boolean consumeNull;
		
		final boolean consumePrimitives;

		private ConfiguredTraverser(ObjectTraverser objectTraverser, Set<CompareSameWrapper<?>> cycleDetector,
				boolean consumeNull, boolean consumePrimitives) {
			super();
			this.objectTraverser = objectTraverser;
			this.cycleDetector = cycleDetector;
			this.consumeNull = consumeNull;
			this.consumePrimitives = consumePrimitives;
		}


		public void goOn(Object object, Object parent,  String accessor, Consumer consumer) throws Exception {
			objectTraverser.traverse(object, parent, accessor, consumer, cycleDetector, consumeNull, consumePrimitives);
		}
	}
}

class ObjectFieldHandler extends FieldHandler<Object> {

	public ObjectFieldHandler() {
		super(Object.class);
	}

	@Override
	public void handleObject(Object object, ConfiguredTraverser traverser, Consumer consumer) throws Exception {
		PojoHelper.forEachField(object, traverser.objectTraverser.modifiersToIgnore, (field, value) -> {
			if ((field.getModifiers() & Modifier.STATIC) != 0) {
				traverser.goOn(value, object.getClass(), field.getName(), consumer);
			} else {
				traverser.goOn(value, object, field.getName(), consumer);
			}
		});
	}
}

@SuppressWarnings({"unchecked", "rawtypes"})
class CollectionFieldHandler extends FieldHandler<Collection> {

	public CollectionFieldHandler() {
		super(Collection.class);
	}

	@Override
	public void handleObject(Collection col, ConfiguredTraverser traverser, Consumer consumer) throws Exception {
		int i = 0;
		for (Iterator<Object> it = col.iterator(); it.hasNext(); ) {
			traverser.goOn(it.next(), col, Integer.toString(i++), consumer);
		}
	}
}

@SuppressWarnings({"rawtypes"})
class MapFieldHandler extends FieldHandler<Map> {
	
	public MapFieldHandler() {
		super(Map.class);
	}
	
	@Override
	public void handleObject(Map map, ConfiguredTraverser traverser, Consumer consumer) throws Exception {
		int i = 0;
		for (Object o : map.entrySet()) {
			Entry entry = (Entry) o;
			traverser.goOn(entry.getKey(), map, "key" + i, consumer);
			traverser.goOn(entry.getValue(), map, "value" + i, consumer);
			i++;
		}
	}
}

class ArrayFieldHandler extends FieldHandler<Object> {
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayFieldHandler(Class<?> arrayClass) {
		super((Class) arrayClass);
	}

	@Override
	public void handleObject(Object array, ConfiguredTraverser traverser, Consumer consumer) throws Exception {
	    int length = Array.getLength(array);
	    for (int i = 0; i < length; i ++) {
	        Object arrayElement = Array.get(array, i);
	        traverser.goOn(arrayElement, array, Integer.toString(i), consumer);
	    }
	}
}