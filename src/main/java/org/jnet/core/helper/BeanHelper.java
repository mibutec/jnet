package org.jnet.core.helper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class BeanHelper {
	private BeanHelper() {
	}
	
	@SuppressWarnings("unchecked")
	public static<T> T cloneGameObject(T objectToCopy) {
		try {
			T ret = (T) Class.forName(objectToCopy.getClass().getName()).newInstance();
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
	
	public static void merge(Object src, Object dest) throws Exception {
		forEachRelevantField(src, field -> {
			field.set(dest, field.get(src));
		});
	}
	
	public static interface ThrowingConsumer<T> {
		void consume(T s) throws Exception;
	}
}
