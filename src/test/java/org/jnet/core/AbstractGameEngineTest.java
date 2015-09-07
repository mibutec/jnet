package org.jnet.core;

import java.util.Map.Entry;

import junit.framework.Assert;

import org.jnet.core.connection.Connection;
import org.jnet.core.testdata.FahrstuhlState;
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
		client = new GameClient() {
			@Override
			public int serverTime() {
				return serverTime;
			}
		};
		client.connect(Mockito.mock(Connection.class));
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
	public void testProxyReuse() throws Exception {
		FigureState state = new FigureState();
		
		Assert.assertTrue(client.createProxy(state) == client.createProxy(state));
		Assert.assertTrue(client.createProxy(state) != client.createProxy(new FigureState()));
	}
	
	/**
	 * Test to create a proxy for an object containing
	 * - other complexe objects
	 * - arrays of complexe objects
	 * - arrays of primitives
	 * - collections of complexe objects
	 * - collections of primitives
	 * - transient objects
	 * - maps
	 * @throws Exception
	 */
	@Test
	public void testComplexeProxyCreation() throws Exception {
		// create Proxy
		Hochhaus hochhaus = client.createProxy(new Hochhaus());
		
		// test the proxied class itself
		assertIsProxied(hochhaus);
		
		// test attributes
		assertIsProxied(hochhaus.getFahrstuhl());
		assertIsNotProxied(hochhaus.getTransientState());
		
		// array of objects
		assertIsNotProxied(hochhaus.getFigures());
		for (FigureState figure : hochhaus.getFigures()) {
			assertIsProxied(figure);
			Assert.assertTrue(hochhaus.getFahrstuhl() == figure.getFahrstuhl());
		}
		
		// array of primitives
		assertIsNotProxied(hochhaus.getIntArray());
		for (int i : hochhaus.getIntArray()) {
			assertIsNotProxied(i);
		}
		
		// collection of objects
		assertIsNotProxied(hochhaus.getMoreFigures());
		for (FigureState figure : hochhaus.getMoreFigures()) {
			assertIsProxied(figure);
		}
		
		// collection of 'primitives'
		assertIsNotProxied(hochhaus.getMorePrimitives());
		for (Integer i : hochhaus.getMorePrimitives()) {
			assertIsNotProxied(i);
		}
		
		// Map of objects
		assertIsNotProxied(hochhaus.getComplexeMap());
		for (Entry<FigureState, FahrstuhlState> entry : hochhaus.getComplexeMap().entrySet()) {
			assertIsProxied(entry.getKey());
			assertIsProxied(entry.getValue());
		}
		
	}
	
	private void assertIsProxied(Object proxy) {
		Assert.assertTrue(proxy instanceof ManagedObject);
		Assert.assertTrue(client.getIdForProxy(proxy) != null);
	}
	private void assertIsNotProxied(Object proxy) {
		Assert.assertFalse(proxy instanceof ManagedObject);
		Assert.assertFalse(client.getIdForProxy(proxy) != null);
	}
}
