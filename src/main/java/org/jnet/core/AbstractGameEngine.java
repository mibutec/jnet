package org.jnet.core;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jnet.core.connection.Connection;
import org.jnet.core.connection.messages.Message;
import org.jnet.core.helper.BeanHelper;
import org.jnet.core.helper.Unchecker;
import org.jnet.core.synchronizer.CloneStrategy;
import org.jnet.core.synchronizer.Event;
import org.jnet.core.synchronizer.MetaData;
import org.jnet.core.synchronizer.MetaDataManager;
import org.jnet.core.synchronizer.ObjectId;
import org.jnet.core.synchronizer.SerializableEvent;
import org.jnet.core.synchronizer.State;

public abstract class AbstractGameEngine implements AutoCloseable {
	private static final Logger logger = LogManager.getLogger(AbstractGameEngine.class);
	
	protected final Map<ObjectId, ManagedObject<?>> handlers = new HashMap<>();
	
	protected final Map<ObjectId, InvokeHandler<?>> handlers2 = new HashMap<>();
	
	protected final MetaDataManager metaDataManager;

	public abstract String name();
	
	abstract protected Set<Connection> getConnections();
	abstract protected void handleMessage(Message message);
	
	protected AbstractGameEngine(MetaDataManager metaDataManager) {
		this.metaDataManager = metaDataManager;
	}

	public ObjectId getIdForProxy(Object proxy) {
		if (proxy instanceof ManagedObject) {
			return ((ManagedObject<?>) proxy)._getMoId_();
		}
		
		return null;
	}
	
	public void updateGameState() {
		for (Connection connection : getConnections()) {
			Message message = null;
			while ((message = connection.nextMessage()) != null) {
				handleMessage(message);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T getObject(Class<T> clazz, ObjectId id) {
		ManagedObject<T> ret = (ManagedObject<T>) handlers.get(id);
		if (ret == null) {
			throw new RuntimeException("no entity found for id " + id);
		}

		if (!clazz.isAssignableFrom(ret.getClass())) {
			throw new RuntimeException("entity with id " + id + " is not assignable to class " + clazz.getName() + " => " + ret.getClass().getName());
		}

		return (T) ret;
	}

	public <T> State<T> getLastTrustedState(Class<T> clazz, ObjectId id) {
		InvokeHandler<T> ret = getHandler(id);
		if (ret == null) {
			throw new RuntimeException("no entity found for id " + id);
		}

		if (!clazz.isAssignableFrom(ret.getLastTrustedState().getState().getClass())) {
			throw new RuntimeException("entity with id " + id + " is not assignable to class " + clazz.getName());
		}

		return ret.getLastTrustedState();
	}
	
	@SuppressWarnings("unchecked")
	protected<T> InvokeHandler<T> getHandler(ObjectId objectId) {
		return (InvokeHandler<T>) handlers2.get(objectId);
	}

	public abstract int serverTime();

	protected abstract void distributeEvent(SerializableEvent event);

	@SuppressWarnings("unchecked")
	public <T> T createProxy(T impl) {
		logger.info("creating proxy for " + impl);
		// reuse existing proxies
		ManagedObject<T> existingHandler = (ManagedObject<T>) handlers.values().stream()
				.filter(v -> v._getMoLastTrustedState_() == impl).findFirst().orElseGet(() -> null);

		if (existingHandler != null) {
			System.out.println(existingHandler);
			return (T) existingHandler;
		}

		// create proxies for all the attributes of that object
		BeanHelper.forEachRelevantField(impl.getClass(), field -> {
			Object fieldValue = field.get(impl);
			Object mayBeAProxy = createProxyOrNot(fieldValue);
			field.set(impl, mayBeAProxy);
		});

		// create proxy for the given object
		ObjectId objectId = metaDataManager.createObjectId(impl.getClass());
		InvokeHandler<T> handler = new InvokeHandler<>(objectId, impl, this);

		ProxyFactory factory = new ProxyFactory();
		factory.setSuperclass(impl.getClass());
		factory.setInterfaces(new Class<?>[]{ManagedObject.class});
		T proxy;
		try {
			proxy = (T) factory.create(new Class[0], new Object[0], handler);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		handlers.put(objectId, (ManagedObject<T>) proxy);
		handlers2.put(objectId, handler);
		
		return proxy;
	}
	
	@SuppressWarnings("unchecked")
	private Object createProxyOrNot(Object o) throws Exception {
		if (o == null) {
			return null;
		} else if (BeanHelper.isPrimitive(o.getClass())) {
			return o;
		} else if (o.getClass().isArray()) {
		    int length = Array.getLength(o);
		    for (int i = 0; i < length; i ++) {
		        Object arrayElement = Array.get(o, i);
		        Array.set(o, i, createProxyOrNot(arrayElement));
		    }
		    return o;
		} else if (o instanceof Map) {
			Map<Object, Object> newMap = (Map<Object, Object>) o.getClass().newInstance();
			for (Entry<Object, Object> entry : ((Map<Object, Object>) o).entrySet()) {
				newMap.put(createProxyOrNot(entry.getKey()), createProxyOrNot(entry.getValue()));
			}
			
			return newMap;
		} else if (o instanceof Collection) {
			Collection<Object> newCollection = (Collection<Object>) o.getClass().newInstance();
			for (Iterator<Object> it = ((Collection<Object>)o).iterator(); it.hasNext(); ) {
				newCollection.add(createProxyOrNot(it.next()));
			}
			
			return newCollection;
		} else {
			return createProxy(o);
		}
	}
	
	public MetaDataManager getMetaDataManager() {
		return metaDataManager;
	}
}

class InvokeHandler<T> implements MethodHandler {
	private static final Method getIdMethod = Unchecker.uncheck(() -> {
		return ManagedObject.class.getMethod("_getMoId_", new Class[0]);
	});
	
	private static final Method getMetaDataMethod = Unchecker.uncheck(() -> {
		return ManagedObject.class.getMethod("_getMoMetaData_", new Class[0]);
	});
	
	private static final Method getMoLatestStateMethod = Unchecker.uncheck(() -> {
		return ManagedObject.class.getMethod("_getMoLatestState_", new Class[0]);
	});
	
	private static final Method getMoLastTrustedStateMethod = Unchecker.uncheck(() -> {
		return ManagedObject.class.getMethod("_getMoLastTrustedState_", new Class[0]);
	});
	
	private final ObjectId id;

	private final MetaData metaData;

	private State<T> lastTrustedState;

	private State<T> latestState;

	private final AbstractGameEngine gameEngine;

	private List<Event> sortedEvents = new LinkedList<>();
	
	public InvokeHandler(ObjectId id, T impl, AbstractGameEngine gameEngine) {
		super();
		this.id = id;
		this.lastTrustedState = new State<T>(impl, 0);
		this.metaData = gameEngine.getMetaDataManager().get(impl.getClass());
		resetLatestState();
		this.gameEngine = gameEngine;
	}

	public State<T> getLastTrustedState() {
		return lastTrustedState;
	}

	public T getLatestState(int now) {
		if (now < latestState.getTimestamp()) {
			resetLatestState();
		}

		return latestState.updateState(sortedEvents, now);
	}

	private void addEvent(Event event) {
		sortedEvents.add(event);
		Collections.sort(sortedEvents);
		
		// TODO: muss der reset immer erfolgen, oder nur wenn event.ts < lateststate.ts?
		resetLatestState();
	}

	@Override
	public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
		// handle generic ManagedObject-interface methods
		if (thisMethod.equals(getIdMethod)) {
			return id;
		} else if (thisMethod.equals(getMetaDataMethod)) {
			return metaData;
		} else if (thisMethod.equals(getMoLatestStateMethod)) {
			return getLatestState(gameEngine.serverTime());
		} else if (thisMethod.equals(getMoLastTrustedStateMethod)) {
			return getLastTrustedState().getState();
		}
		
		// handle events
		return handleEvent(gameEngine.serverTime(),
				metaData.getClazz().getMethod(thisMethod.getName(), thisMethod.getParameterTypes()), args, true);
	}

	public Object handleEvent(int eventTime, Method implMethod, Object[] args, boolean doDistribute) throws Throwable {
		if (implMethod.getAnnotation(Action.class) != null) {
			SerializableEvent event = new SerializableEvent(id, eventTime, implMethod, args);
			addEvent(event);
			if (doDistribute) {
				gameEngine.distributeEvent(event);
			}
		}

		return implMethod.invoke(getLatestState(gameEngine.serverTime()), args);
	}

	public void newState(int ts, Map<Field, Object> state) throws Exception {
		BeanHelper.merge(state, lastTrustedState.getState());
		lastTrustedState.setTimestamp(ts);
		resetLatestState();
		sortedEvents = sortedEvents.stream().filter(e -> e.getTs() > ts).collect(Collectors.toList());
	}

	private void resetLatestState() {
		latestState = lastTrustedState.clone(new CloneStrategy() {
			
			@Override
			public <C> C clone(C object) {
				return BeanHelper.cloneGameObject(object);
			}
		});
	}

	public ObjectId getId() {
		return id;
	}
}
