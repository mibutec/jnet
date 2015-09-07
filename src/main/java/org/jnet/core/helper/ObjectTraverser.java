package org.jnet.core.helper;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.jnet.core.helper.ObjectTraverser.Consumer;
import org.jnet.core.helper.ObjectTraverser.FieldHandler;
import org.jnet.core.helper.ObjectTraverser.Traverser;


public class ObjectTraverser {
	private static final List<Class<?>> primitives = Arrays.asList(Boolean.class, Byte.class, Short.class,
			Character.class, Integer.class, Long.class, Float.class, Double.class, String.class);
	
	private static final List<Class<?>> arrays = Arrays.asList(boolean[].class, byte[].class, short[].class, char[].class, int[].class, 
			long[].class, float[].class, double[].class, Object[].class);

	public static boolean isPrimitive(Class<?> clazz) {
		return clazz.isPrimitive() || primitives.contains(clazz);
	}
	
	private Set<FieldHandler<?>> handlers = new HashSet<>();

	public ObjectTraverser() {
		addFieldHandler(new ObjectFieldHandler());
		addFieldHandler(new CollectionFieldHandler());
		addFieldHandler(new MapFieldHandler());
		for (Class<?> arrayClass : arrays) {
			addFieldHandler(new ArrayFieldHandler(arrayClass));
		}
	}
	
	public void addFieldHandler(FieldHandler<?> fieldHandler) {
		handlers.add(fieldHandler);
	}
	
	public void traverse(Object object, Consumer consumer) throws Exception {
		traverse(object, null, consumer, new HashSet<>());
	}
	
	/**
	 * Finds the handler, that can handle this class best. This means:
	 * a. The handlers class is assignable from the given class
	 * b. If there are several matches it takes the one "nearst" to the given class in a sense of inheritance
	 *    If you have a handler for Object, List, LinkedList and your Type is "ExtendedLinkedList", it would take LinkedList handler
	 *    
	 * If you find several handlers that can handle this type but are not in a direct inheritance line you get an error
	 * Example: you have a handler for the interface House and one for the interface Boat, you get an error for the Class HouseBoat
	 * until you add a handler for Houseboat. Handling only part could lead to unexpected behavior.
	 * 
	 * TODO: How about using both handlers?

	 * @param clazz
	 * @return best matching handler
	 */
	private FieldHandler<?> getBestMatchingHandler(Class<?> clazz) {
		 List<FieldHandler<?>> sortedHandlers = handlers.stream().filter(handler -> handler.canHandle(clazz)).sorted().collect(Collectors.toList());
		 FieldHandler<?> first = sortedHandlers.get(0);
		 if (sortedHandlers.size() == 1) {
			 return first;
		 }

		 FieldHandler<?> second = sortedHandlers.get(1);
		 if (first.compareTo(second) == 0) {
			 throw new RuntimeException("couldn't determine best matching handler for class " + clazz.getName() + ", both match: " + first + " and " + second);
		 }

		 return first;
	}
	
	@SuppressWarnings("unchecked")
	void traverse(Object object, Object parent, Consumer consumer, Set<CompareSameWrapper<?>> cycleDetector) throws Exception {
		if (object == null) {
			return;
		}

		System.out.println(object + " " + parent);
		if (!consumer.onObjectFound(object, parent)) {
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
		
		FieldHandler<Object> handler = (FieldHandler<Object>) getBestMatchingHandler(clazz);
		handler.handleObject(object, new Traverser(this, cycleDetector), consumer);
	}
	
	public static interface Consumer {
		public boolean onObjectFound(Object object, Object parent);
	}
	
	public static abstract class FieldHandler<T> implements Comparable<FieldHandler<?>> {
		private final Class<T> handledType;
		
		protected FieldHandler(Class<T> handledType) {
			this.handledType = handledType;
		}

		public boolean canHandle(Class<?> clazz) {
			return handledType.isAssignableFrom(clazz);
		}

		public abstract void handleObject(T object, Traverser traverser, Consumer consumer) throws Exception;

		@Override
		public int compareTo(FieldHandler<?> other) {
			if (other.handledType.isAssignableFrom(handledType)) {
				return -1;
			}
			
			if (other.handledType.isAssignableFrom(handledType)) {
				return 1;
			}
			
			return 0;
		}

		@Override
		public int hashCode() {
			return handledType.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			@SuppressWarnings("rawtypes")
			FieldHandler other = (FieldHandler) obj;
			return handledType == other.handledType;
		}
	}
	
	/**
	 * Class used to shortcut the the call to traverse from an FieldHandler. This way you don't
	 * have to handle the Cycle Detector set in Handlers
	 * @author Michael
	 *
	 */
	public class Traverser {
		private final ObjectTraverser objectTraverser;
		
		private final Set<CompareSameWrapper<?>> cycleDetector;

		public Traverser(ObjectTraverser objectTraverser, Set<CompareSameWrapper<?>> cycleDetector) {
			super();
			this.objectTraverser = objectTraverser;
			this.cycleDetector = cycleDetector;
		}
		
		public void goOn(Object object, Object parent, Consumer consumer) throws Exception {
			objectTraverser.traverse(object, parent, consumer, cycleDetector);
		}
	}
}

class ObjectFieldHandler extends FieldHandler<Object> {

	public ObjectFieldHandler() {
		super(Object.class);
	}

	@Override
	public void handleObject(Object object, Traverser traverser, Consumer consumer) throws Exception {
		Class<?> clazz = object.getClass();
		while (clazz != Object.class) {
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
					continue;
				}
				field.setAccessible(true);
				Object fieldValue = field.get(object);
				traverser.goOn(fieldValue, object, consumer);
			}
			clazz = clazz.getSuperclass();
		}
	}
}

@SuppressWarnings({"unchecked", "rawtypes"})
class CollectionFieldHandler extends FieldHandler<Collection> {

	public CollectionFieldHandler() {
		super(Collection.class);
	}

	@Override
	public void handleObject(Collection col, Traverser traverser, Consumer consumer) throws Exception {
		for (Iterator<Object> it = col.iterator(); it.hasNext(); ) {
			traverser.goOn(it.next(), col, consumer);
		}
	}
}

@SuppressWarnings({"rawtypes"})
class MapFieldHandler extends FieldHandler<Map> {
	
	public MapFieldHandler() {
		super(Map.class);
	}
	
	@Override
	public void handleObject(Map map, Traverser traverser, Consumer consumer) throws Exception {
		for (Object o : map.entrySet()) {
			Entry entry = (Entry) o;
			traverser.goOn(entry.getKey(), map, consumer);
			traverser.goOn(entry.getValue(), map, consumer);
		}
	}
}

class ArrayFieldHandler extends FieldHandler<Object> {
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayFieldHandler(Class<?> arrayClass) {
		super((Class) arrayClass);
	}

	@Override
	public void handleObject(Object array, Traverser traverser, Consumer consumer) throws Exception {
	    int length = Array.getLength(array);
	    for (int i = 0; i < length; i ++) {
	        Object arrayElement = Array.get(array, i);
	        traverser.goOn(arrayElement, array, consumer);
	    }
	}
}