package org.jnet.core.connection;

import org.jnet.core.GameClient;

public class DelayedInMemoryClientConnection implements ConnectionToClient {
	private GameClient client;
	
	private long delay;
	
	public DelayedInMemoryClientConnection(GameClient client, long delay) {
		this.client = client;
		this.delay = delay;
	}
	
	private void sleep() {
		try { Thread.sleep(delay); } catch (Exception e) {}
	}

	
	@Override
	public void sendState(int id, Object state, int ts) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				sleep();
				client.handleNewState(id, ts, state);
				
			}
		}).start();
	}

	@Override
	public void close() throws Exception {
		// nothing to do
	}
}
