package org.jnet.core.synchronizer;

import org.jnet.core.helper.Unchecker;

public class DirectInvokeEvent extends Event {

	private EventAction action;
	
	public DirectInvokeEvent(int ts, EventAction action) {
		super(ts);
		this.action = action;
	}

	@Override
	public void invoke(Object o) {
		Unchecker.uncheck(() ->action.invoke());
	}
	
	public static interface EventAction {
		void invoke() throws Exception;
	}

}
