package org.jnet.core.synchronizer;


public abstract class Event implements Comparable<Event> {
	protected final int ts;

	public Event(int ts) {
		super();
		this.ts = ts;
	}

	@Override
	public int compareTo(Event other) {
		return Integer.compare(ts, other.ts);
	}

	public abstract void invoke(Object o);

	public int getTs() {
		return ts;
	}
}

