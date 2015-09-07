package org.jnet.core.synchronizer;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.jnet.core.helper.BeanHelper;

public class MetaData {
	private final int id;

	private final List<Field> fields;

	private final Map<String, Field> fieldNameMapping = new ConcurrentHashMap<>();

	private final AtomicInteger objectIdGenerator = new AtomicInteger();

	private final Class<?> clazz;

	public MetaData(Class<?> clazz, int id) throws RuntimeException {
		try {
			List<Field> tmpList = new LinkedList<>();
			BeanHelper.forEachRelevantField(clazz, field -> {
				tmpList.add(field);
				fieldNameMapping.put(field.getName(), field);
			});
			fields = Collections.unmodifiableList(tmpList);
		} catch (RuntimeException re) {
			throw re;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		this.id = id;
		this.clazz = clazz;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, fields);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final MetaData other = (MetaData) obj;
		return Objects.equals(this.id, other.id) && Objects.equals(this.fields, other.fields);
	}

	@Override
	public String toString() {
		return "MetaData [id=" + id + ", fields=" + fields + ", objectIdGenerator=" + objectIdGenerator + ", clazz="
				+ clazz + "]";
	}

	public List<Field> getFields() {
		return fields;
	}

	public int getId() {
		return id;
	}

	public int nextObjectId() {
		return objectIdGenerator.incrementAndGet();
	}

	public Field getField(String name) {
		Field ret = fieldNameMapping.get(name);
		if (ret == null) {
			throw new RuntimeException("Field " + name + " not found for class " + clazz.getName());
		}
		return ret;
	}

	public Class<?> getClazz() {
		return clazz;
	}
}
