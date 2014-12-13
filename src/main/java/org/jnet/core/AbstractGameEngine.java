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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jala.mixins.Unchecker;
import org.jnet.core.connection.Connection;
import org.jnet.core.connection.messages.Message;
import org.jnet.core.helper.BeanHelper;

public abstract class AbstractGameEngine implements AutoCloseable {
	private static final Logger logger = LogManager.getLogger(AbstractGameEngine.class);
	
	protected final Map<Integer, InvokeHandler<?>> handlers = new HashMap<>();

	private AtomicInteger idGenerator = new AtomicInteger(0);
	
	public abstract String name();
	
	abstract protected Set<Connection> getConnections();
	abstract protected void handleMessage(Message message);

	public Integer getIdForProxy(Object proxy) {
		if (proxy instanceof ManagedObject) {
			return ((ManagedObject) proxy)._getMoId_();
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
	public <T> T getObject(Class<T> clazz, int id) {
		Object ret = handlers.get(id).getProxy();
		if (ret == null) {
			throw new RuntimeException("no entity found for id " + id);
		}

		if (!clazz.isAssignableFrom(ret.getClass())) {
			throw new RuntimeException("entity with id " + id + " is not assignable to class " + clazz.getName() + " => " + ret.getClass().getName());
		}

		return (T) ret;
	}

	@SuppressWarnings("unchecked")
	public <T> State<T> getLastTrustedState(Class<T> clazz, int id) {
		InvokeHandler<T> ret = (InvokeHandler<T>) handlers.get(id);
		if (ret == null) {
			throw new RuntimeException("no entity found for id " + id);
		}

		if (!clazz.isAssignableFrom(ret.getLastTrustedState().getState().getClass())) {
			throw new RuntimeException("entity with id " + id + " is not assignable to class " + clazz.getName());
		}

		return ret.getLastTrustedState();
	}

	public abstract int serverTime();

	protected abstract void distributeEvent(int id, Event<?> event);

	@SuppressWarnings("unchecked")
	public <T> T createProxy(T impl) throws Exception {
		logger.info("creating proxy for " + impl);
		// reuse existing proxies
		InvokeHandler<T> existingHandler = (InvokeHandler<T>) handlers.values().stream()
				.filter(v -> v.getLastTrustedState().getState() == impl).findFirst().orElseGet(() -> null);

		if (existingHandler != null) {
			return existingHandler.getProxy();
		}

		// create proxies for all the attributes of that object
		BeanHelper.forEachRelevantField(impl, field -> {
			Object fieldValue = field.get(impl);
			Object mayBeAProxy = createProxyOrNot(fieldValue);
			field.set(impl, mayBeAProxy);
		});

		// create proxy for the given object
		Integer id = idGenerator.addAndGet(1);
		InvokeHandler<T> handler = new InvokeHandler<>(id, impl, this);
		handlers.put(id, handler);

		ProxyFactory factory = new ProxyFactory();
		factory.setSuperclass(impl.getClass());
		factory.setInterfaces(new Class<?>[]{ManagedObject.class});
		T proxy;
		try {
			proxy = (T) factory.create(new Class[0], new Object[0], handler);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		handler.setProxy(proxy);
		
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
}

class InvokeHandler<T> implements MethodHandler {
	private final int id;

	private final Class<T> clazz;

	private State<T> lastTrustedState;

	private State<T> latestState;

	private final AbstractGameEngine gameEngine;

	private List<Event<T>> sortedEvents = new LinkedList<>();

	private T proxy;

	private static final Method getIdMethod = Unchecker.uncheck(() -> {
		return ManagedObject.class.getMethod("_getMoId_", new Class[0]);
	});
	
	@SuppressWarnings("unchecked")
	public InvokeHandler(int id, T impl, AbstractGameEngine gameEngine) {
		super();
		this.id = id;
		this.lastTrustedState = new State<T>(impl, 0, (byte) -1);
		resetLatestState();
		this.clazz = (Class<T>) impl.getClass();
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

	private void addEvent(Event<T> event) {
		sortedEvents.add(event);
		Collections.sort(sortedEvents);
		resetLatestState();
	}

	@Override
	public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
		// handle generic ManagedObject-interface methods
		if (thisMethod.equals(getIdMethod)) {
			return id;
		}
		
		// handle events
		return handleEvent(gameEngine.serverTime(),
				clazz.getMethod(thisMethod.getName(), thisMethod.getParameterTypes()), args, true);
	}

	public Object handleEvent(int eventTime, Method implMethod, Object[] args, boolean doDistribute) throws Throwable {
		if (implMethod.getAnnotation(Action.class) != null) {
			byte sequence = 0;
			if (sortedEvents.size() > 0 && sortedEvents.get(sortedEvents.size() - 1).getTs() == eventTime) {
				sequence = (byte) (sortedEvents.get(sortedEvents.size() - 1).getSequence() + 1);
			}
			Event<T> event = new Event<T>(eventTime, sequence, implMethod, args);
			addEvent(event);
			if (doDistribute) {
				gameEngine.distributeEvent(id, event);
			}
		}

		return implMethod.invoke(getLatestState(gameEngine.serverTime()), args);
	}

	public void newState(int ts, Map<Field, Object> state) throws Exception {
		BeanHelper.merge(state, lastTrustedState.getState());
		lastTrustedState.setTimestamp(ts);
		lastTrustedState.setSequence((byte) 0);
		resetLatestState();
		sortedEvents = sortedEvents.stream().filter(e -> e.getTs() > ts).collect(Collectors.toList());
	}

	private void resetLatestState() {
		latestState = lastTrustedState.clone();
	}

	public T getProxy() {
		return proxy;
	}

	public void setProxy(T proxy) {
		this.proxy = proxy;
	}

	public int getId() {
		return id;
	}
}
