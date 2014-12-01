package org.jnet.core;

import junit.framework.Assert;

import org.jala.mixins.Eventually;
import org.jnet.core.GameClient;
import org.jnet.core.GameServer;
import org.jnet.core.connection.DelayedInmemoryServerConnection;
import org.jnet.core.testdata.FigureState;
import org.junit.Test;

public class GameEngineIntTest implements Eventually {
	@Test
	public void testCooperationWithoutDelay() throws Exception {
		testCooperation(0);
	}
	
	@Test
	public void testCooperationWithDelay() throws Exception {
		testCooperation(1500);
	}
	
	private void testCooperation(int delay) throws Exception {
		GameServer server = new GameServer(1500);
		GameClient client = new GameClient(new DelayedInmemoryServerConnection(server, delay));
		FigureState serverState = server.createProxy(new FigureState());
		serverState.setName("server");
		FigureState clientState = client.createProxy(new FigureState());
		clientState.setName("client");
		
		Assert.assertEquals(0.0f, clientState.getX());
		Assert.assertEquals(0.0f, serverState.getX());
		clientState.gotoX(1000);
		sleep(delay + 200);
		
		eventually(() -> {
			Assert.assertTrue(clientState.getX() != 0f);
			Assert.assertEquals((int) clientState.getX(), (int) serverState.getX());
		});
		client.close();
	}
}
