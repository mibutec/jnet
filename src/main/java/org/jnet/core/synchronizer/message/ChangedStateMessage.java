package org.jnet.core.synchronizer.message;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jnet.core.helper.Unchecker;
import org.jnet.core.synchronizer.ObjectChangeProvider;
import org.jnet.core.synchronizer.ObjectId;

public class ChangedStateMessage implements SynchronizationMessage {
	private final int timestamp;

	private final List<SynchronizationMessage> newObjects = new ArrayList<>();

	private final List<SynchronizationMessage> updateObjectMessages = new LinkedList<>();

	public ChangedStateMessage(int timestamp) {
		super();
		this.timestamp = timestamp;
	}

	@Override
	public void apply(ObjectChangeProvider changeProvider) {
		Unchecker.uncheck(() -> {
			for (SynchronizationMessage newObjectMessage : newObjects) {
				newObjectMessage.apply(changeProvider);
			}
			for (SynchronizationMessage message : updateObjectMessages) {
				message.apply(changeProvider);
			}
		});
	}

	public void addNewObjectMessage(SynchronizationMessage newObjectMessage) {
		newObjects.add(newObjectMessage);
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
