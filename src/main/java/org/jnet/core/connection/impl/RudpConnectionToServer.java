package org.jnet.core.connection.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import net.rudp.ReliableSocket;
import net.rudp.ReliableSocketProfile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jnet.core.Event;
import org.jnet.core.GameClient;
import org.jnet.core.connection.ConnectionToServer;

public class RudpConnectionToServer extends AbstractRudpConnection implements ConnectionToServer {
	private static final Logger logger = LogManager.getLogger(RudpConnectionToServer.class);

	private GameClient client;
	
	public RudpConnectionToServer(String host, int port) throws IOException {
		super(new ReliableSocket(createProfile()));
		socket.connect(new InetSocketAddress(host, port));
	}

	public RudpConnectionToServer(String host, int port, String localAddr, int localPort) throws IOException {
		super(new ReliableSocket(createProfile(),
				new InetSocketAddress(host, port),
				new InetSocketAddress(localAddr, localPort)));
	}
	
	private static ReliableSocketProfile createProfile() {
		ReliableSocketProfile profile = new ReliableSocketProfile();
		profile.setMaxSegmentSize(1400);
		return profile;
	}
	
	@Override
	public void sendEvent(int id, Event<?> event) throws IOException {
		logger.debug("sending event {} with id {} to server", event, id);
		Map<String, Object> map = new HashMap<>();
		map.put("id", id);
		map.put("event", event);
		send(map);
	}
	
	@Override
	public void requestServerTime() throws IOException {
		Map<String, Object> map = new HashMap<>();
		map.put("clientTime", System.currentTimeMillis());
		send(map);
	}

	@Override
	public void setClient(GameClient client) {
		this.client = client;
		new Thread(this).start();
	}

	@Override
	protected void handleMap(Map<String, Object> map) {
		Integer id = (Integer) map.get("id");
		if (id != null) {
			Object state = map.get("state");
			int ts = (int) map.get("ts");
			client.handleNewState(id, ts, state);
		}
		
		Integer serverTime = (Integer) map.get("serverTime");
		Long clientTime = (Long) map.get("clientTime");
		
		if (serverTime != null && clientTime != null) {
			client.calibrateServerTime(clientTime, serverTime);
		}
	}

	@Override
	protected String myName() {
		return "client";
	}
}
