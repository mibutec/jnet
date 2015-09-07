package org.jnet.core.synchronizer;

public interface LateEventStrategy {
	public Event handleEvent(Event event, int trustedStateTs);
	
	public static final LateEventStrategy dismiss = new LateEventStrategy() {
		@Override
		public Event handleEvent(Event event, int trustedStateTs) {
			return null;
		}
	};
}
