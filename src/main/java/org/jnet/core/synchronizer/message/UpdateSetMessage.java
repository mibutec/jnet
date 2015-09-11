package org.jnet.core.synchronizer.message;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import org.jnet.core.synchronizer.ObjectChangeProvider;
import org.jnet.core.synchronizer.ObjectId;

public class UpdateSetMessage extends AbstractUpdateMessage {
	private final Set<Object> removedElements;
	private final Set<Object> addedElements;
	
	public UpdateSetMessage(ObjectId objectId, Set<Object> addedElements, Set<Object> removedElements) {
		super(objectId);
		this.addedElements = addedElements;
		this.removedElements = removedElements;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void apply(ObjectChangeProvider changeProvider) {
		Collection collection = (Collection) changeProvider.getObject(objectId);
		for (Object element : addedElements) {
			collection.add(element);
		}
		for (Object element : removedElements) {
			collection.remove(element);
		}
	}

	@Override
	public String toString() {
		return "UpdateSetMessage [removedElements=" + removedElements
				+ ", addedElements=" + addedElements + ", objectId=" + objectId
				+ "]";
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(removedElements, addedElements, objectId);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != getClass()) {
			return false;
		}
		UpdateSetMessage other = (UpdateSetMessage) obj;
		return Objects.equals(removedElements, other.removedElements) && Objects.equals(addedElements, other.addedElements) && Objects.equals(objectId, other.objectId);
	}

}
