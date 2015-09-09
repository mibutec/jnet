package org.jnet.core.helper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import sun.reflect.ReflectionFactory;

import com.google.common.collect.ImmutableMap;

public class PojoHelper {
	public static final List<Class<?>> primitiveTypes = Arrays.asList(Boolean.class, Byte.class, Short.class,
			Character.class, Integer.class, Long.class, Float.class, Double.class, String.class);

	public static final List<Class<?>> arrayTypes = Arrays.asList(boolean[].class, byte[].class, short[].class,
			char[].class, int[].class, long[].class, float[].class, double[].class, Object[].class);

	private static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS = new ImmutableMap.Builder<Class<?>, Class<?>>()
			.put(boolean.class, Boolean.class).put(byte.class, Byte.class).put(char.class, Character.class)
			.put(double.class, Double.class).put(float.class, Float.class).put(int.class, Integer.class)
			.put(long.class, Long.class).put(short.class, Short.class).put(void.class, Void.class).build();

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

	@SuppressWarnings("unchecked")
	public static <T> Class<T> primitiveToWrapper(Class<T> c) {
		return c.isPrimitive() ? (Class<T>) PRIMITIVES_TO_WRAPPERS.get(c) : c;
	}

	@SuppressWarnings("unchecked")
	public static <T> T instantiate(Class<T> cls) {
		return Unchecker.uncheck(() -> {
			try {
				Constructor<T> constructor = cls.getDeclaredConstructor();
				constructor.setAccessible(true);
				return constructor.newInstance();
			} catch (Exception e) {
				// Create instance of the given class
				final Constructor<T> constructor = (Constructor<T>) cls.getDeclaredConstructors()[0];
				constructor.setAccessible(true);
				final List<Object> params = new ArrayList<Object>();
				for (Class<?> pType : constructor.getParameterTypes()) {
					params.add((pType.isPrimitive()) ? PojoHelper.primitiveToWrapper(pType).newInstance() : null);
				}

				return cls.cast(constructor.newInstance(params.toArray()));
			}
		});
	}
}
