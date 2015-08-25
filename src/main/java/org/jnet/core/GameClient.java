package org.jnet.core;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jnet.core.connection.Connection;
import org.jnet.core.connection.messages.EventMessage;
import org.jnet.core.connection.messages.Message;
import org.jnet.core.connection.messages.NewStateMessage;
import org.jnet.core.connection.messages.TimeResponseMessage;


public class GameClient extends AbstractGameEngine {
	private static final Logger logger = LogManager.getLogger(GameClient.class);
	
	private final Connection connection;
	
	private long serverTimeOffset;
	
	private long serverTimeVariation = Long.MAX_VALUE;
	
	public GameClient(Connection connection) {
		super(new MetaDataManager());
		this.connection = connection;
		this.serverTimeOffset = System.currentTimeMillis();
		connection.setGameEngine(this);
	}
	
	@Override
	public String name() {
		return "client";
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
			connection.send(new EventMessage(id, event));
		} catch (Exception e) {
			logger.error("event {} with id {} could not be distributed", event, id, e);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void handleNewState(int id, int ts, Map<Field, Object> state) {
		try {
			InvokeHandler<?> handler = getHandler(id);
			((InvokeHandler) handler).newState(ts, state);
		} catch (Exception e) {
			logger.error("state {} with id {} could not be updated", state, id, e);
		}
	}

	@Override
	public void close() throws Exception {
		connection.close();
	}

	@Override
	protected Set<Connection> getConnections() {
		Set<Connection> ret = new HashSet<Connection>();
		ret.add(connection);
		
		return ret;
	}

	@Override
	protected void handleMessage(Message message) {
		if (message instanceof TimeResponseMessage) {
			TimeResponseMessage trMessage = (TimeResponseMessage) message;
			calibrateServerTime(trMessage.getClientTimestamp(), trMessage.getServerTime());
		} else if (message instanceof NewStateMessage) {
			NewStateMessage nsMessage = (NewStateMessage) message;
			handleNewState(nsMessage.getObjectId(), nsMessage.getTs(), nsMessage.getStateAsMap());
		} else {
			logger.error("unknown messageType arrived on {}, type = {}", name(), message);
		}
	}
}