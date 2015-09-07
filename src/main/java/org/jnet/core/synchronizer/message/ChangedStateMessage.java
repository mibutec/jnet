package org.jnet.core.synchronizer.message;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jnet.core.synchronizer.ObjectChangeProvider;
import org.jnet.core.synchronizer.ObjectId;

public class ChangedStateMessage implements SynchronizationMessage {
	private final int timestamp;
	
	private final List<AddObjectMessage> addObjectMessages = new LinkedList<>();

	private final List<SynchronizationMessage> updateObjectMessages = new LinkedList<>();

	public ChangedStateMessage(int timestamp) {
		super();
		this.timestamp = timestamp;
	}

	@Override
	public void apply(ObjectChangeProvider changeProvider) {
		for (SynchronizationMessage message : addObjectMessages) {
			message.apply(changeProvider);
		}
		for (SynchronizationMessage message : updateObjectMessages) {
			message.apply(changeProvider);
		}
	}
	
	public void addNewObject(ObjectId objectId, Object instantiatedObject) {
		addObjectMessages.add(new AddObjectMessage(objectId, instantiatedObject));
	}
	
	public void addUpdateObject(ObjectId objectId, Map<String, Object> fieldsToUpdate) {
		updateObjectMessages.add(new UpdateObjectMessage(objectId, fieldsToUpdate));
	}
	
	public void addUpdateObject(SynchronizationMessage message) {
		updateObjectMessages.add(message);
	}
	
	public int getTimestamp() {
		return timestamp;
	}
}
