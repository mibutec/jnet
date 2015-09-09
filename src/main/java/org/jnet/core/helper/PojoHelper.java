package org.jnet.core.helper;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class PojoHelper {
	public static final List<Class<?>> primitiveTypes = Arrays.asList(Boolean.class, Byte.class, Short.class,
			Character.class, Integer.class, Long.class, Float.class, Double.class, String.class);
	
	public static final List<Class<?>> arrayTypes = Arrays.asList(boolean[].class, byte[].class, short[].class, char[].class, int[].class, 
			long[].class, float[].class, double[].class, Object[].class);
	
	private PojoHelper() {
		
	}

	public static boolean isPrimitive(Class<?> clazz) {
		return clazz.isPrimitive() || primitiveTypes.contains(clazz);
	}
	
	public static void forEachField(Object object, int mofifiersToIgnore, FieldConsumer consumer) throws Exception {
		Class<?> clazz = object.getClass();
		while (clazz != Object.class) {
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				if ((field.getModifiers() & mofifiersToIgnore) != 0) {
					continue;
				}
				boolean isAccessible = field.isAccessible();
				field.setAccessible(true);
				Object fieldValue = field.get(object);
				consumer.consume(field, fieldValue);
				field.setAccessible(isAccessible);
			}
			clazz = clazz.getSuperclass();
		}

	}
	
	public static interface FieldConsumer {
		void consume(Field field, Object value) throws Exception;
	}
}
