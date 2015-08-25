package org.jnet.core;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.jnet.core.helper.BeanHelper;

public class MetaData {
	private final int id;

	private final List<Field> fields = new LinkedList<>();
	
	private final AtomicInteger objectIdGenerator = new AtomicInteger();

	public MetaData(Class<?> clazz, int id) throws RuntimeException {
		try {
			BeanHelper.forEachRelevantField(clazz, field -> {
				if (BeanHelper.isPrimitive(field.getType())) {
					fields.add(field);
				}
			});
		} catch (RuntimeException re) {
			throw re;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		this.id = id;
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
		return "MetaData [fields=" + fields + "]";
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
}
