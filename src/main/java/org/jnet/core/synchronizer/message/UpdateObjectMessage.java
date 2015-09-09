package org.jnet.core.synchronizer.message;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;

import org.jnet.core.helper.Unchecker;
import org.jnet.core.synchronizer.MetaData;
import org.jnet.core.synchronizer.ObjectChangeProvider;
import org.jnet.core.synchronizer.ObjectId;



public class UpdateObjectMessage extends AbstractUpdateMessage {
	private Map<String, Object> fieldsToUpdate;
	
	public UpdateObjectMessage(ObjectId objectId, Map<String, Object> fieldsToUpdate) {
		super(objectId);
		this.fieldsToUpdate = fieldsToUpdate;
	}

	@Override
	public void apply(ObjectChangeProvider changeProvider) {
		Unchecker.uncheck(() -> {
			Object objectToUpdate = changeProvider.getObject(objectId);
			MetaData metaData = changeProvider.getMetaData(objectToUpdate);
			
			for (Entry<String, Object> entry : fieldsToUpdate.entrySet()) {
				Field field = metaData.getField(entry.getKey());
				Object newValue = entry.getValue();
				field.setAccessible(true);
				field.set(objectToUpdate, unwrap(changeProvider, newValue));
			}
		});
	}
}
