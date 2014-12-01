package org.jnet.core.connection;

import java.net.InetSocketAddress;

import junit.framework.Assert;

import org.jala.mixins.Eventually;
import org.jnet.core.GameClient;
import org.jnet.core.GameServer;
import org.jnet.core.State;
import org.jnet.core.connection.impl.RudpConnectionToServer;
import org.jnet.core.connection.impl.RudpServerConnector;
import org.jnet.core.testdata.FigureState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RudpConnectionTest implements Eventually {
	private static InetSocketAddress serverAddress = new InetSocketAddress("localhost", 12345);

	private GameServer server;
	private RudpConnectionToServer cts;
	private GameClient client;
	private FigureState clientState;
	private int figureStateId;
	
	@Before
	public void setup() throws Exception {
		server = new GameServer(new RudpServerConnector(serverAddress.getPort()));
		server.createProxy(new FigureState());
		cts = new RudpConnectionToServer(serverAddress.getHostName(), serverAddress.getPort());
		client = new GameClient(cts);
		clientState = client.createProxy(new FigureState());
		figureStateId = client.getIdForProxy(clientState);
	}
	
	@After
	public void tearDown() throws Exception {
		if (server != null) server.close();
		if (client != null) client.close();
	}

	@Test
	public void testEstablishingConnection() throws Exception {
		eventually(() -> {
			Assert.assertEquals(1, server.getClientConnections().size());
		});
	}

	@Test
	public void testServerReceivesMessage() throws Exception {
		int oldTs = client.getLastTrustedState(FigureState.class, figureStateId).getTimestamp();
		clientState.gotoX(100);
		
		eventually(() -> {
			Assert.assertEquals(100, server.getObject(FigureState.class, figureStateId).getTargetX());
			Assert.assertEquals(100, client.getObject(FigureState.class, figureStateId).getTargetX());
			Assert.assertTrue(oldTs != client.getLastTrustedState(FigureState.class, figureStateId).getTimestamp());
		});
	}
	
	@Test
	public void testTimeSync() throws Exception {
		// Fake clientTime, so at the end reset can be testet
		client.calibrateServerTime(System.currentTimeMillis() - 10000, 100000);
		Assert.assertTrue(client.serverTime() >= 100000);
		cts.requestServerTime();
		
		// Servertime is a little bit higher than 0
		eventually(() -> {
			Assert.assertTrue(client.serverTime() < 50000);
		});
	}
}
