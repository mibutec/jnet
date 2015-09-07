package org.jnet.core;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jnet.core.connection.Connection;
import org.jnet.core.connection.ServerConnector;
import org.jnet.core.connection.messages.EventMessage;
import org.jnet.core.connection.messages.Message;
import org.jnet.core.connection.messages.NewStateMessage;
import org.jnet.core.connection.messages.TimeRequestMessage;
import org.jnet.core.connection.messages.TimeResponseMessage;
import org.jnet.core.synchronizer.Event;
import org.jnet.core.synchronizer.MetaDataManager;


public class GameServer extends AbstractGameEngine {
	private static final Logger logger = LogManager.getLogger(GameServer.class);
	
	private final Set<Connection> connections;
	
	private final int acceptedDelay;
	
	private final Collection<ServerConnector> connectors = new HashSet<>();
	
	private final long serverTimeOffset;
	
	public GameServer(int acceptedDelay) throws IOException {
		super(new MetaDataManager());
		this.acceptedDelay = acceptedDelay;
		this.connections = new HashSet<>();
		serverTimeOffset = System.currentTimeMillis();
	}
	
	public GameServer() throws IOException {
		this(1500);
	}
	
	@Override
	public String name() {
		return "server";
	}
	
	@Override
	public int serverTime() {
		return (int) (System.currentTimeMillis() - serverTimeOffset);
	}
	
	@Override
	protected void handleMessage(Message message) {
		if (message instanceof EventMessage) {
			EventMessage eventMessage = (EventMessage) message;
			logger.debug("new event for id {} arrived at server", eventMessage.getEvent().getObjectId());
			receiveEvent(eventMessage.getEvent());
		} else if (message instanceof TimeRequestMessage) {
			TimeRequestMessage trMessage = (TimeRequestMessage) message;
			logger.debug("new request for client time arrived at server");
			try {
				message.sender().send(new TimeResponseMessage(trMessage.getClientTimestamp(), serverTime()));
			} catch (Exception e) {
				logger.error("error answering time request", e);
			}
		} else {
			logger.error("unknown messageType arrived on {}, type = {}", name(), message);
		}
	}
	
	public void receiveEvent(Event event) {
		try {
			int ts = event.getTs();
			if (event.getTs() < serverTime() - acceptedDelay) {
				ts = serverTime() - acceptedDelay;
			}
			getHandler(event.getObjectId()).handleEvent(ts, event.getEvent(), event.getArgs(), true);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void distributeEvent(Event event) {
		logger.debug("distributing result of event {} with id {} to {} clients", event, event.getObjectId(), connections.size());
		connections.stream().forEach(cc -> {
			try {
				ManagedObject<?> mo = handlers.get(event.getObjectId());
				cc.send(new NewStateMessage(event.getObjectId(), serverTime(), mo._getMoMetaData_(), mo._getMoLatestState_()));
			} catch (Exception e) {
				logger.error("couldn't send new state to client", e);
			}
		});
	}
	
	public void addConnector(ServerConnector connector) {
		connectors.add(connector);
		connector.setGameServer(this);
	}
	
	public void addConnetion(Connection connection) {
		connections.add(connection);
	}

	public Set<Connection> getConnections() {
		return connections;
	}

	@Override
	public void close() throws Exception {
		connections.forEach(c -> {
			try {
				c.close();
			} catch (Exception e) {
				logger.error("error closing resource", e);
			}
		});
		
		connectors.forEach(c -> {
			try {
				c.close();
			} catch (Exception e) {
				logger.error("error closing resource", e);
			}
		});
	}
}