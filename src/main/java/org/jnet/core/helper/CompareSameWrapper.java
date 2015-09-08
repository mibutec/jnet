package org.jnet.core.helper;

public final class CompareSameWrapper<T> {
	private final T instance;

	public CompareSameWrapper(T instance) {
		super();
		this.instance = instance;
	}

	@Override
	public String toString() {
		return "c{" + instance.toString() + "}";
	}
	
	@Override
	public int hashCode() {
		return System.identityHashCode(instance);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		
		if (!(obj instanceof CompareSameWrapper)) {
			return false;
		}
		
			return instance == ((CompareSameWrapper<T>) obj).instance;
	}

	public T getInstance() {
		return instance;
	}
}

