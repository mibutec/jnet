package org.jnet.core.synchronizer;

import java.util.List;

import org.jnet.core.UpdateableObject;

public class State<T> implements Cloneable {
	private final T state;
	
	private int timestamp;

	public State(T state, int timestamp) {
		super();
		this.state = state;
		this.timestamp = timestamp;
	}
	
	public State<T> clone(CloneStrategy strategy) {
		return new State<T>(strategy.clone(state), timestamp);
	}
	
	public T updateState(List<Event> events, int now) {
		events.stream().sorted().filter(e -> e.getTs() >= timestamp && e.getTs() <= now).forEach(e -> {
			try {
				if (state instanceof UpdateableObject) {
					long delta = e.getTs() - timestamp;
					if (delta > 0) {
						((UpdateableObject) state).update(delta);
					}
					timestamp = e.getTs();
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
		return "State [state=" + state + ", timestamp=" + timestamp + "]";
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
}
