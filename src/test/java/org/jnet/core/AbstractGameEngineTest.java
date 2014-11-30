package org.jnet.core;

import junit.framework.Assert;

import org.jnet.core.connection.ConnectionToServer;
import org.jnet.core.testdata.FigureState;
import org.jnet.core.testdata.Hochhaus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class AbstractGameEngineTest {
	private int serverTime = 0;
	
	private GameClient client;
	
	@Before
	public void setup() {
		serverTime = 0;
		client = new GameClient(Mockito.mock(ConnectionToServer.class)) {
			@Override
			public int serverTime() {
				return serverTime;
			}
		};
	}
	
	@Test
	public void testCorrectComputing() throws Exception {
		FigureState state = client.createProxy(new FigureState());
		
		Assert.assertEquals(0.0f, state.getX());
		state.gotoX(1000);
		serverTime = 10000;
		Assert.assertEquals(500.0f, state.getX());
		serverTime = 20000;
		Assert.assertEquals(1000.0f, state.getX());
		serverTime = 30000;
		Assert.assertEquals(1000.0f, state.getX());
		
		client.close();
	}
	
	@Test
	public void testProxyReuse() {
		FigureState state = new FigureState();
		
		Assert.assertTrue(client.createProxy(state) == client.createProxy(state));
		Assert.assertTrue(client.createProxy(state) != client.createProxy(new FigureState()));
	}
	
	@Test
	public void testComplexeProxyCreation() {
		Hochhaus hochhaus = client.createProxy(new Hochhaus());
		assertIsProxied(hochhaus);
		assertIsProxied(hochhaus.getFahrstuhl());
		
		for (int i = 0; i < hochhaus.getFigures().length; i++) {
			assertIsProxied(hochhaus.getFigures()[i]);
		}
	}
	
	private void assertIsProxied(Object proxy) {
		Assert.assertTrue(client.getIdForProxy(proxy) != null);
		System.out.println(proxy.getClass());
	}
}