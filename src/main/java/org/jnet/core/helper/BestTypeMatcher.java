package org.jnet.core.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jnet.core.helper.BestTypeMatcher.TypeHandler;

public class BestTypeMatcher<T extends TypeHandler<?>> {
	private Map<Class<?>, T> handlers = new HashMap<>();
	
	public void addFieldHandler(T fieldHandler) {
		handlers.put(fieldHandler.getHandledType(), fieldHandler);
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
	public T getBestMatchingHandler(Class<?> clazz) {
		 List<T> sortedHandlers = handlers.values().stream().filter(handler -> handler.canHandle(clazz)).sorted().collect(Collectors.toList());
		 T first = sortedHandlers.get(0);
		 if (sortedHandlers.size() == 1) {
			 return first;
		 }
		 
		 TypeHandler<?> second = sortedHandlers.get(1);
		 if (first.compareTo(second) == 0) {
			 throw new RuntimeException("couldn't determine best matching handler for class " + clazz.getName() + ", both match: " + first + " and " + second);
		 }

		 return first;
	}

	public static abstract class TypeHandler<CLASS> implements Comparable<TypeHandler<?>> {
		private final Class<CLASS> handledType;

		protected TypeHandler(Class<CLASS> handledType) {
			this.handledType = handledType;
		}

		public boolean canHandle(Class<?> clazz) {
			return handledType.isAssignableFrom(clazz);
		}
		
		public Class<CLASS> getHandledType() {
			return handledType;
		}

		@Override
		public int compareTo(TypeHandler<?> other) {
			if (other.handledType == handledType) {
				return 0;
			}

			if (other.handledType.isAssignableFrom(handledType)) {
				return -1;
			}

			if (other.handledType.isAssignableFrom(handledType)) {
				return 1;
			}

			return 0;
		}
	}
}
