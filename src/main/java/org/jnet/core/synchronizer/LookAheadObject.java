package org.jnet.core.synchronizer;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jnet.core.helper.CompareSameWrapper;
import org.jnet.core.helper.ObjectTraverser;
import org.jnet.core.helper.Unchecker;
import org.jnet.core.synchronizer.DefaultDiffIdentifier.AbstractTypeHandler;

public abstract class LookAheadObject<T> implements ObjectChangeProvider {
	protected final Map<ObjectId, CompareSameWrapper<?>> managedObjects = new HashMap<>();
	protected final Map<CompareSameWrapper<?>, ObjectId> managedObjectsReverse = new HashMap<>();
	
	protected final MetaDataManager metaDataManager = new MetaDataManager();
	
	protected final ObjectTraverser objectTraverser;
	
	protected List<Event> sortedEvents = new LinkedList<>();

	protected final State<T> lastTrustedState;
	
	protected final CloneStrategy cloneStrategy;

	protected final LateEventStrategy lateEventStrategy;
	
	protected LookAheadObject(T managedObject, LookAheadObjectConfiguration<T> config) {
		this.objectTraverser = new ObjectTraverser();
		this.objectTraverser.setModifierToIgnore(Modifier.STATIC | Modifier.TRANSIENT);
		this.lastTrustedState = new State<T>(managedObject, 0);
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
		
		for (AbstractTypeHandler<?, ?> handler : config.getTypeHandlers()) {
			if (handler.getFieldHandler() != null) {
				objectTraverser.addFieldHandler(handler.getFieldHandler());
			}
		}
		
		inventorizeObject(managedObject);
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
		return managedObjects.get(objectId).getInstance();
	}
	
	@Override
	public MetaData getMetaData(Object object) {
		return metaDataManager.get(object.getClass());
	}
	
	@Override
	public MetaDataManager getMetaDataManager() {
		return metaDataManager;
	}
	
	protected void inventorizeObject(Object objectToInventorize) {
		Unchecker.uncheck(() -> 
			objectTraverser.traverse(objectToInventorize, false, false, (object, parent, accessor) -> {
				CompareSameWrapper<?> wrapper = new CompareSameWrapper<Object>(object);
				if (!managedObjects.values().contains(wrapper)) {
					ObjectId id = metaDataManager.createObjectId(object.getClass());
					addObject(id, object);
				}
				return true;
			})
		);
	}
}
