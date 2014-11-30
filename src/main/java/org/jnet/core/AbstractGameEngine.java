package org.jnet.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

public abstract class AbstractGameEngine implements AutoCloseable {
	protected final Map<Integer, InvokeHandler<?>> handlers = new HashMap<>();

	private AtomicInteger idGenerator = new AtomicInteger();

	public Integer getIdForProxy(Object proxy) {
		InvokeHandler<?> existingHandler = handlers.values().stream()
				.filter(v -> v.getProxy() == proxy).findFirst().orElseGet(() -> null);

		if (existingHandler != null) {
			return existingHandler.getId();
		}
		
		return null;
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
	public <T> T createProxy(T impl) {
		InvokeHandler<T> existingHandler = (InvokeHandler<T>) handlers.values().stream()
				.filter(v -> v.getLastTrustedState().getState() == impl).findFirst().orElseGet(() -> null);

		if (existingHandler != null) {
			return existingHandler.getProxy();
		}

		Integer id = idGenerator.addAndGet(1);
		InvokeHandler<T> handler = new InvokeHandler<>(id, impl, this);
		handlers.put(id, handler);

		ProxyFactory factory = new ProxyFactory();
		factory.setSuperclass(impl.getClass());
		T proxy;
		try {
			proxy = (T) factory.create(new Class[0], new Object[0], handler);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		handler.setProxy(proxy);

		return proxy;
	}
	
	static void merge(Object src, Object dest) throws Exception {
		Class<?> srcClass = src.getClass();
		while (srcClass != Object.class) {
			for (Field field: srcClass.getDeclaredFields()) {
				System.out.println("merging " + field.getName());
				field.setAccessible(true);
				if (Modifier.isStatic(field.getModifiers()) ||
						Modifier.isTransient(field.getModifiers()) ||
						field.get(dest) instanceof Proxy) {
					System.out.println("fished out " + field.getName());
					continue;
				}
				
				System.out.println("setting " + field.getName() + " to " + field.get(src));
				field.set(dest, field.get(src));
			}
			
			srcClass = srcClass.getSuperclass();
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

	@SuppressWarnings("unchecked")
	public InvokeHandler(int id, T impl, AbstractGameEngine gameEngine) {
		super();
		this.id = id;
		this.lastTrustedState = new State<T>(impl, gameEngine.serverTime(), (byte) 0);
		this.latestState = deepCopy(lastTrustedState);
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

	public void newState(int ts, T state) throws Exception {
//		lastTrustedState.setTimestamp(ts);
//		lastTrustedState.setSequence((byte) 0);
//		AbstractGameEngine.merge(state, lastTrustedState.getState());
		lastTrustedState = new State<T>(state, ts, (byte) 0);
		System.out.println(lastTrustedState.getState());
		resetLatestState();
		sortedEvents = sortedEvents.stream().filter(e -> e.getTs() > ts).collect(Collectors.toList());
	}

	private void resetLatestState() {
		latestState = deepCopy(lastTrustedState);
	}

	@SuppressWarnings("unchecked")
	protected static <T> T deepCopy(T objectToCopy) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(objectToCopy);
			return (T) new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).readObject();
		} catch (RuntimeException re) {
			throw re;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
