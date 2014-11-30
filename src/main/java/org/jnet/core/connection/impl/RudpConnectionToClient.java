package org.jnet.core.connection.impl;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jnet.core.Event;
import org.jnet.core.GameServer;
import org.jnet.core.connection.ConnectionToClient;

public class RudpConnectionToClient extends AbstractRudpConnection implements ConnectionToClient {
	private static final Logger logger = LogManager.getLogger(RudpConnectionToClient.class);

	private final GameServer server;

	public RudpConnectionToClient(Socket clientSocket, GameServer server) {
		super(clientSocket);
		logger.info("new RudpConnectionToClient instantiated");
		this.server = server;
		new Thread(this).start();
	}

	@Override
	public void sendState(int id, Object state, int ts) throws IOException {
		logger.debug("sending state {} with id {} to client", state, id);
		Map<String, Object> map = new HashMap<>();
		map.put("id", id);
		map.put("state", state);
		map.put("ts", ts);
		send(map);
	}

	@Override
	protected void handleMap(Map<String, Object> map) {
		Integer id = (Integer) map.get("id");
		if (id != null) {
			logger.debug("new event for id {} arrived at server", id);
			Event<?> event = (Event<?>) map.get("event");
			server.receiveEvent(id, event);
		}
		
		Long clientTime = (Long) map.get("clientTime");
		if (clientTime != null) {
			try {
				logger.debug("new request for client time arrived at server");
				Map<String, Object> timeReponse = new HashMap<>();
				timeReponse.put("serverTime", server.serverTime());
				timeReponse.put("clientTime", clientTime);
				send(timeReponse);
			} catch (Exception e) {
				logger.error("error answering time request", e);
			}
		}
	}

	@Override
	protected String myName() {
		return "server";
	}
}
