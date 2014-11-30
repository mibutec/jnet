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

public class WanConnectionTest implements Eventually {
	private static InetSocketAddress emuAddress = new InetSocketAddress("localhost", 12346);
	private static InetSocketAddress serverAddress = new InetSocketAddress("localhost", 12345);
	private static InetSocketAddress clientAddress = new InetSocketAddress("localhost", 12347);

	private GameServer server;
	private RudpConnectionToServer cts;
	private GameClient client;
	private FigureState clientState;
	private int figureStateId;
	private DatagramWanEmulator emu;
	
	@Before
	public void setup() throws Exception {
		emu = new DatagramWanEmulator(
				emuAddress,
				clientAddress,
				serverAddress);
		emu.startEmulation();
		server = new GameServer(new RudpServerConnector(serverAddress.getPort()));
		server.createProxy(new FigureState());

		cts = new RudpConnectionToServer(emuAddress.getHostName(), emuAddress.getPort(), clientAddress.getHostName(), clientAddress.getPort());
		client = new GameClient(cts);
		clientState = client.createProxy(new FigureState());
		figureStateId = client.getIdForProxy(clientState);
	}
	
	@After
	public void tearDown() throws Exception {
		if (server != null) server.close();
		if (client != null) client.close();
		if (emu != null) emu.stopEmulation();
	}

	@Test
	public void testServerReceivesMessage() throws Exception {
		State<FigureState> oldState = client.getLastTrustedState(FigureState.class, figureStateId);
		clientState.gotoX(100);
		
		eventually(() -> {
			Assert.assertEquals(100, server.getObject(FigureState.class, figureStateId).getTargetX());
			Assert.assertEquals(100, client.getObject(FigureState.class, figureStateId).getTargetX());
			Assert.assertTrue(oldState.getTimestamp() != client.getLastTrustedState(FigureState.class, figureStateId).getTimestamp());
		});
	}
	
	@Test
	public void testPackageLost() throws Exception {
		
		State<FigureState> oldState = client.getLastTrustedState(FigureState.class, figureStateId);
		clientState.gotoX(100);
		
		eventually(() -> {
			Assert.assertEquals(100, server.getObject(FigureState.class, figureStateId).getTargetX());
			Assert.assertEquals(100, client.getObject(FigureState.class, figureStateId).getTargetX());
			Assert.assertTrue(oldState.getTimestamp() != client.getLastTrustedState(FigureState.class, figureStateId).getTimestamp());
		});
	}
}
