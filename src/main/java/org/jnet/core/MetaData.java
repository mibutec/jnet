package org.jnet.core;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import org.jnet.core.helper.BeanHelper;

public class MetaData {
	private final List<Field> fields = new LinkedList<>();

	private int nullableCount;
	
	public MetaData(Class<?> clazz) throws Exception {
		BeanHelper.forEachRelevantField(clazz, field -> {
			if (BeanHelper.isPrimitive(field.getType())) {
				fields.add(field);
				if (field.getType().isPrimitive()) {
					nullableCount++;
				}
			}
		});
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fields == null) ? 0 : fields.hashCode());
		result = prime * result + nullableCount;
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MetaData other = (MetaData) obj;
		if (fields == null) {
			if (other.fields != null)
				return false;
		} else if (!fields.equals(other.fields))
			return false;
		if (nullableCount != other.nullableCount)
			return false;
		return true;
	}



	@Override
	public String toString() {
		return "MetaData [fields=" + fields + ", nullableCount=" + nullableCount + "]";
	}



	public List<Field> getFields() {
		return fields;
	}
}
