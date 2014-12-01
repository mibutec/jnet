package org.jnet.core;

import java.io.Serializable;
import java.util.List;

import org.jnet.core.helper.BeanHelper;

public class State<T> implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;

	private final T state;
	
	private int timestamp;
	
	private byte sequence;

	public State(T state, int timestamp, byte sequence) {
		super();
		this.state = state;
		this.timestamp = timestamp;
		this.sequence = sequence;
	}
	
	public State<T> clone() {
		return new State<T>(BeanHelper.cloneGameObject(state), timestamp, sequence);
	}
	
	public T updateState(List<Event<T>> events, int now) {
		events.stream().filter(e -> e.compareTo(timestamp, sequence) == 1 && e.getTs() < now).forEach(e -> {
			try {
				if (state instanceof UpdateableObject) {
					long delta = e.getTs() - timestamp;
					if (delta > 0) {
						((UpdateableObject) state).update(delta);
					}
					timestamp = e.getTs();
					sequence = e.getSequence();
				}
				
				e.getEvent().invoke(state, e.getArgs());
			} catch (Exception e1) {
				throw new RuntimeException(e1);
			}
		});
		
		if (state instanceof UpdateableObject) {
			long delta = now - timestamp;
			if (delta > 0) {
				((UpdateableObject) state).update(delta);
				timestamp = now;
			}
		}
		
		return state;
	}
	
	@Override
	public String toString() {
		return "State [state=" + state + ", timestamp=" + timestamp + ", sequence=" + sequence + "]";
	}

	public T getState() {
		return state;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	public byte getSequence() {
		return sequence;
	}

	public void setSequence(byte sequence) {
		this.sequence = sequence;
	}
}
