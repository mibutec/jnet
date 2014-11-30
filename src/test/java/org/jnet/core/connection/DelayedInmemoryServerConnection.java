package org.jnet.core.connection;

import org.jala.mixins.Sleep;
import org.jnet.core.Event;
import org.jnet.core.GameClient;
import org.jnet.core.GameServer;

public class DelayedInmemoryServerConnection implements ConnectionToServer, Sleep {
	private GameServer server;
	
	private GameClient client;
	
	private long delay;
	
	public DelayedInmemoryServerConnection(GameServer server, long delay) {
		this.server = server;
		this.delay = delay;
	}
	
	@Override
	public void sendEvent(int id, Event<?> event) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				sleep(delay);
				server.receiveEvent(id, event);
				
			}
		}).start();
	}

	@Override
	public void requestServerTime() {
		final long currentTs = System.currentTimeMillis();
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				sleep(delay);
				client.calibrateServerTime(currentTs, server.serverTime());
				
			}
		}).start();
	}

	@Override
	public void setClient(GameClient client) {
		this.client = client;
		server.addConnetion(new DelayedInMemoryClientConnection(client, delay));
	}
	
	@Override
	public void close() throws Exception {
		// nothing to do
	}
}
