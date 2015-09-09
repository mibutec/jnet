package org.jnet.core;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jnet.core.connection.Connection;
import org.jnet.core.connection.messages.EventMessage;
import org.jnet.core.connection.messages.Message;
import org.jnet.core.connection.messages.NewStateMessage;
import org.jnet.core.connection.messages.TimeResponseMessage;
import org.jnet.core.synchronizer.MetaDataManager;
import org.jnet.core.synchronizer.ObjectId;
import org.jnet.core.synchronizer.SerializableEvent;


public class GameClient<MAIN_ENTITY> extends AbstractGameEngine {
	private static final Logger logger = LogManager.getLogger(GameClient.class);
	
	private Connection connection;
	
	private long serverTimeOffset;
	
	private long serverTimeVariation = Long.MAX_VALUE;
	
	private final ExecutorService executorService = Executors.newFixedThreadPool(4);
	
//	private Future<MAIN_ENTITY> mainEntity;
	
	public GameClient() {
		super(new MetaDataManager());
		this.serverTimeOffset = System.currentTimeMillis();
	}
	
	@SuppressWarnings("unchecked")
	public void connect(Connection connection) {
		this.connection = connection;
//		mainEntity = executorService.submit(() -> {
//			Message message = connection.waitForMessage(5000);
//			if (message instanceof MapMessage) {
//				MapMessage mapMessage = (MapMessage) message;
//				return (MAIN_ENTITY) mapMessage.get("mainEntity");
//			} else {
//				throw new RuntimeException("connection failed, didn't receive initial state");
//			}
//		});
	}
	
//	public MAIN_ENTITY getMainEntity() {
//		return Unchecker.uncheck(() -> mainEntity.get(5000, TimeUnit.MILLISECONDS));
//	}
	
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
	protected void distributeEvent(SerializableEvent event) {
		try {
			connection.send(new EventMessage(event));
		} catch (Exception e) {
			logger.error("event {} with id {} could not be distributed", event, e);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void handleNewState(ObjectId id, int ts, Map<Field, Object> state) {
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
		executorService.shutdown();
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