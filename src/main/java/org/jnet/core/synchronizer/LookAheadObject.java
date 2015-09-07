package org.jnet.core.synchronizer;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jnet.core.helper.CompareSameWrapper;
import org.jnet.core.helper.ObjectTraverser;
import org.jnet.core.helper.ObjectTraverser.Consumer;
import org.jnet.core.helper.Unchecker;

public abstract class LookAheadObject<T> implements ObjectChangeProvider {
	protected final Map<ObjectId, CompareSameWrapper<?>> managedObjects = new HashMap<>();
	protected final Map<CompareSameWrapper<?>, ObjectId> managedObjectsReverse = new HashMap<>();
	
	protected final MetaDataManager metaDataManager = new MetaDataManager();
	
	protected final ObjectTraverser objectTraverser;
	
	private final SynchronizeConsumer consumer = new SynchronizeConsumer();
	
	protected List<Event> sortedEvents = new LinkedList<>();

	protected final State<T> lastTrustedState;
	
	protected final CloneStrategy cloneStrategy;

	protected final LateEventStrategy lateEventStrategy;

	protected LookAheadObject(T objectToSynchronize, LookAheadObjectConfiguration<T> config) {
		this.objectTraverser = new ObjectTraverser();
		this.objectTraverser.setModifierToIgnore(Modifier.STATIC | Modifier.TRANSIENT);
		this.lastTrustedState = new State<T>(objectToSynchronize, 0);
		if (config == null) {
			config = new LookAheadObjectConfiguration<>();
		}
		
		if (config.getCloneStrategy() != null) {
			this.cloneStrategy = config.getCloneStrategy();
		} else {
			this.cloneStrategy = CloneStrategy.serializer;
		}
		
		if (config.getLateEventStrategy() != null) {
			this.lateEventStrategy = config.getLateEventStrategy();
		} else {
			this.lateEventStrategy = LateEventStrategy.dismiss;
		}
		
		inventorizeObject(objectToSynchronize);
	}
	
	public T getStateForTimestamp(int ts) {
		if (ts < lastTrustedState.getTimestamp()) {
			throw new RuntimeException("cant compute state for ts " + ts + ". Actual trusted ts is " + lastTrustedState.getTimestamp());
		}

		State<T> ret = lastTrustedState.clone(cloneStrategy);
		return ret.updateState(sortedEvents, ts);
	}

	public void addEvent(Event event) {
		if (event.getTs() < lastTrustedState.getTimestamp()) {
			event = lateEventStrategy.handleEvent(event, lastTrustedState.getTimestamp());
		}
		
		if (event != null && event.getTs() >= lastTrustedState.getTimestamp()) {
			sortedEvents.add(event);
		}
	}
	
	public void cleanup() {
		sortedEvents = sortedEvents.stream().filter((e) -> e.getTs() > lastTrustedState.getTimestamp()).collect(Collectors.toList());
	}
	
	@Override
	public void addObject(ObjectId objectId, Object object) {
		managedObjects.put(objectId, new CompareSameWrapper<>(object));
		managedObjectsReverse.put(new CompareSameWrapper<>(object), objectId);
		
	}

	@Override
	public Object getObject(ObjectId objectId) {
		Object ret = managedObjects.get(objectId).getInstance();
		if (ret == null) {
			throw new RuntimeException("object with id " + objectId + " doesnt exist");
		}
		
		return ret;
	}
	
	@Override
	public MetaData getMetaData(Object object) {
		return metaDataManager.get(object.getClass());
	}
	
	private void inventorizeObject(Object object) {
		Unchecker.uncheck(() -> 
			objectTraverser.traverse(object, consumer)
		);
	}
	
	private class SynchronizeConsumer implements Consumer {

		@Override
		public boolean onObjectFound(Object object, Object parent, String accessor) {
			if (object == null) {
				return false;
			}
			Class<?> clazz = object.getClass();
			if (!ObjectTraverser.isPrimitive(clazz)) {
				CompareSameWrapper<?> wrapper = new CompareSameWrapper<Object>(object);
				if (!managedObjects.values().contains(wrapper)) {
					ObjectId id = metaDataManager.createObjectId(clazz);
					addObject(id, object);
				}
			}
			return true;
		}
	}
}
