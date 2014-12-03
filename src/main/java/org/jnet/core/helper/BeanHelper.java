package org.jnet.core.helper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class BeanHelper {
	private static final List<Class<?>> notProxiedClasses = Arrays.asList(Boolean.class, 
			Byte.class,
			Short.class,
			Character.class,
			Integer.class,
			Long.class,
			Float.class,
			String.class
			);

	private BeanHelper() {
	}
	
	public static boolean isPrimitive(Class<?> clazz) {
		return clazz.isPrimitive() || clazz.isEnum() || notProxiedClasses.contains(clazz);
	}
	
	@SuppressWarnings("unchecked")
	public static<T> T cloneGameObject(T objectToCopy) {
		try {
			T ret = (T) objectToCopy.getClass().newInstance();
			forEachRelevantField(objectToCopy, field -> {
				field.set(ret, field.get(objectToCopy));
			});
			
			return ret;
		} catch (RuntimeException re) {
			throw re;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void forEachField(Object obj, ThrowingConsumer<Field> consumer) throws Exception {
		Class<?> clazz = obj.getClass();
		while (clazz != Object.class) {
			for (Field field: clazz.getDeclaredFields()) {
				consumer.consume(field);
			}
			
			clazz = clazz.getSuperclass();
		}
	}
	
	public static void forEachRelevantField(Object obj, ThrowingConsumer<Field> consumer) throws Exception {
		forEachField(obj, field -> {
			field.setAccessible(true);
			boolean correctModifiers = !Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers());
			if (correctModifiers) {
				consumer.consume(field);
			}
		});
		
	}
	
	public static void merge(Map<Field, Object> src, Object dest) throws Exception {
		for (Entry<Field, Object> entry : src.entrySet()) {
			entry.getKey().setAccessible(true);
			entry.getKey().set(dest, entry.getValue());
		}
	}
	
	public static interface ThrowingConsumer<T> {
		void consume(T s) throws Exception;
	}
}
