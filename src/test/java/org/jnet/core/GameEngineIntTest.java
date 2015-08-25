package org.jnet.core;

import junit.framework.Assert;

import org.jnet.core.connection.DelayedInmemoryConnection;
import org.jnet.core.testdata.FigureState;
import org.junit.Test;

public class GameEngineIntTest implements Eventually, Sleep {
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
		DelayedInmemoryConnection c = createInMemoryConnections(delay);
		GameClient client = new GameClient(c);
		server.addConnetion(c.getConterpart());
		FigureState serverState = server.createProxy(new FigureState());
		serverState.setName("server");
		FigureState clientState = client.createProxy(new FigureState());
		clientState.setName("client");
		
		Assert.assertEquals(0.0f, clientState.getX());
		Assert.assertEquals(0.0f, serverState.getX());
		clientState.gotoX(1000);
		sleep(delay + 200);
		
		eventually(() -> {
			client.updateGameState();
			server.updateGameState();
			Assert.assertTrue(clientState.getX() != 0f);
			Assert.assertEquals((int) clientState.getX(), (int) serverState.getX());
		});
		client.close();
		server.close();
	}
	
	public static DelayedInmemoryConnection createInMemoryConnections(int delay) {
		DelayedInmemoryConnection c1 = new DelayedInmemoryConnection(delay);
		DelayedInmemoryConnection c2 = new DelayedInmemoryConnection(delay);
		c1.setConterpart(c2);
		c2.setConterpart(c1);
		
		return c1;
	}
}
