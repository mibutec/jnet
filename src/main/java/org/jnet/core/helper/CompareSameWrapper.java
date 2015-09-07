package org.jnet.core.helper;

public final class CompareSameWrapper<T> {
	private final T instance;

	public CompareSameWrapper(T instance) {
		super();
		this.instance = instance;
	}

	@Override
	public int hashCode() {
		return instance.hashCode();
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

