package org.jnet.core.synchronizer.message;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

import org.jnet.core.synchronizer.ObjectChangeProvider;
import org.jnet.core.synchronizer.ObjectId;

public class UpdateArrayMessage extends AbstractUpdateMessage {
	private final Map<Integer, Object> changedIndexes;
	
	public UpdateArrayMessage(ObjectId objectId, Map<Integer, Object> changedIndexes) {
		super(objectId);
		this.changedIndexes = changedIndexes;
	}
	
	public void addChangedIndecValue(int index, Object value) {
		changedIndexes.put(index, value);
	}

	@Override
	public void apply(ObjectChangeProvider changeProvider) {
		Object objectToChange = changeProvider.getObject(objectId);
		
		for (Entry<Integer, Object> entry : changedIndexes.entrySet()) {
			Array.set(objectToChange, entry.getKey(), entry.getValue());
		}
	}
	
	
	
	@Override
	public String toString() {
		return "UpdateArrayMessage [objectId=" + objectId + ", changedIndexes=" + changedIndexes + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(changedIndexes, objectId);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != getClass()) {
			return false;
		}
		UpdateArrayMessage other = (UpdateArrayMessage) obj;
		return Objects.equals(changedIndexes, other.changedIndexes) && Objects.equals(objectId, other.objectId);
	}
}
