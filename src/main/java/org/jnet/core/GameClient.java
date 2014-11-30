package org.jnet.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jnet.core.connection.ConnectionToServer;


public class GameClient extends AbstractGameEngine {
	private static final Logger logger = LogManager.getLogger(GameClient.class);
	
	private final ConnectionToServer connection;
	
	private long serverTimeOffset;
	
	private long serverTimeVariation = Long.MAX_VALUE;
	
	public GameClient(ConnectionToServer connection) {
		super();
		this.connection = connection;
		this.serverTimeOffset = System.currentTimeMillis();
		connection.setClient(this);
	}

	@Override
	public int serverTime() {
		return (int) (System.currentTimeMillis() - serverTimeOffset);
	}
	
	public void calibrateServerTime(long requestTs, int serverTime) {
		logger.debug("calibrating servertime on client, requestTs: {}, serverTime: {}", requestTs, serverTime);
		long diff = System.currentTimeMillis() - requestTs;
		long diff2 = diff / 2;
		
		if (diff2 < serverTimeVariation) {
			serverTimeVariation = diff2;
			long oldOffset = serverTimeOffset; 
			serverTimeOffset = System.currentTimeMillis() - (serverTime + diff2);
			logger.info("changed timeoffset from {} to {}", oldOffset, serverTimeOffset);
		}
	}
	
	@Override
	protected void distributeEvent(int id, Event<?> event) {
		try {
			connection.sendEvent(id, event);
		} catch (Exception e) {
			logger.error("event {} with id {} could not be distributed", event, id, e);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void handleNewState(int id, int ts, Object state) {
		try {
			InvokeHandler<?> handler = handlers.get(id);
			((InvokeHandler) handler).newState(ts, state);
		} catch (Exception e) {
			logger.error("state {} with id {} could not be updated", state, id, e);
		}
	}

	@Override
	public void close() throws Exception {
		connection.close();
	}
}