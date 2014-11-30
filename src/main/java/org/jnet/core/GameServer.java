package org.jnet.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jnet.core.connection.ConnectionToClient;
import org.jnet.core.connection.ServerConnector;


public class GameServer extends AbstractGameEngine {
	private static final Logger logger = LogManager.getLogger(GameServer.class);
	
	private final List<ConnectionToClient> clientConnections;
	
	private final int acceptedDelay;
	
	private final ServerConnector[] connectors;
	
	private final long serverTimeOffset;
	
	public GameServer(int acceptedDelay, ServerConnector... connectors) throws IOException {
		this.acceptedDelay = acceptedDelay;
		this.clientConnections = new LinkedList<>();
		this.connectors = connectors;
		for (ServerConnector connector : connectors) {
			connector.setGameServer(this);
		}
		serverTimeOffset = System.currentTimeMillis();
	}
	
	public GameServer(ServerConnector... connectors) throws IOException {
		this(1500, connectors);
	}
	
	@Override
	public int serverTime() {
		return (int) (System.currentTimeMillis() - serverTimeOffset);
	}
	
	public void receiveEvent(int id, Event<?> event) {
		try {
			int ts = event.getTs();
			if (event.getTs() < serverTime() - acceptedDelay) {
				ts = serverTime() - acceptedDelay;
			}
			handlers.get(id).handleEvent(ts, event.getEvent(), event.getArgs(), true);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void distributeEvent(int id, Event<?> event) {
		logger.debug("distributing event {} with id {} to {} clients", event, id, clientConnections.size());
		clientConnections.stream().forEach(cc -> {
			try {
				cc.sendState(id, handlers.get(id).getLatestState(serverTime()), serverTime());
			} catch (Exception e) {
				logger.error("couldn't send new state to client", e);
			}
		});
	}
	
	public void addConnetion(ConnectionToClient connection) {
		clientConnections.add(connection);
	}

	public List<ConnectionToClient> getClientConnections() {
		return clientConnections;
	}

	@Override
	public void close() throws Exception {
		clientConnections.forEach(c -> {
			try {
				c.close();
			} catch (Exception e) {
				logger.error("error closing resource", e);
			}
		});
		
		Arrays.asList(connectors).forEach(c -> {
			try {
				c.close();
			} catch (Exception e) {
				logger.error("error closing resource", e);
			}
		});;

	}
}