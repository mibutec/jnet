package org.jnet.core;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import junit.framework.Assert;

import org.jnet.core.connection.ConnectionToServer;
import org.jnet.core.testdata.FigureState;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class GameClientTest {
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
	public void testNewState() {
		// the entity on that we want to test
		FigureState state = client.createProxy(new FigureState());
		int id = client.getIdForProxy(state);
		
		// server sends a new state
		FigureState newState = new FigureState();
		newState.setTargetX(500);
		client.handleNewState(id, 0, newState);

		// client reacts on that new state
		serverTime = 1000;
		Assert.assertEquals(50f, state.getX());
		
	}

	@Test
	public void testNewStateCanBeOverwritten() {
		// the entity on that we want to test
		FigureState state = client.createProxy(new FigureState());
		int id = client.getIdForProxy(state);
		
		// create a new event in eventqueue
		serverTime = 1000;
		state.gotoX(1000);
		serverTime = 2000;
		Assert.assertEquals(50f, state.getX());
		
		// let the "server" send a state in timeline before the last client event
		// that will be overwritten by the clients request in the future 
		FigureState newState = new FigureState();
		newState.setTargetX(10);
		client.handleNewState(id, 0, newState);
		Assert.assertEquals(60f, state.getX());
	}
	
	@Test
	public void testCallsToServer() throws Exception {
		ConnectionToServer serverConnection = Mockito.mock(ConnectionToServer.class);
		GameClient client = new GameClient(serverConnection);
		FigureState state = client.createProxy(new FigureState());
		
		Assert.assertEquals(0.0f, state.getX());
		Mockito.verify(serverConnection, Mockito.never()).sendEvent(anyInt(), any());

		state.update(42);
		Mockito.verify(serverConnection, Mockito.never()).sendEvent(anyInt(), any());

		state.gotoX(42);
		Mockito.verify(serverConnection, Mockito.times(1)).sendEvent(anyInt(), any());
		
		client.close();
	}
}