package org.jnet.core;

import static org.mockito.Matchers.any;
import junit.framework.Assert;

import org.jnet.core.connection.Connection;
import org.jnet.core.connection.messages.NewStateMessage;
import org.jnet.core.synchronizer.ObjectId;
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
		client = new GameClient() {
			@Override
			public int serverTime() {
				return serverTime;
			}
		};
		client.connect(Mockito.mock(Connection.class));
	}

	@Test
	public void testNewState() throws Exception {
		// the entity on that we want to test
		FigureState state = client.createProxy(new FigureState());
		ObjectId id = client.getIdForProxy(state);
		
		// server sends a new state
		FigureState newState = new FigureState();
		newState.setTargetX(500);
		client.handleNewState(id, 0, new NewStateMessage(id, 0, client.getMetaDataManager().get(FigureState.class), newState).getStateAsMap());

		// client reacts on that new state
		serverTime = 1000;
		Assert.assertEquals(50f, state.getX());
		
	}

	@Test
	public void testNewStateCanBeOverwritten() throws Exception {
		// the entity on that we want to test
		FigureState state = client.createProxy(new FigureState());
		ObjectId id = client.getIdForProxy(state);
		
		// create a new event in eventqueue
		serverTime = 1000;
		state.gotoX(1000);
		serverTime = 2000;
		Assert.assertEquals(50f, state.getX());
		
		// let the "server" send a state in timeline before the last client event
		// that will be overwritten by the clients request in the future 
		FigureState newState = new FigureState();
		newState.setTargetX(10);
		
		client.handleNewState(id, 0, new NewStateMessage(id, 0, client.getMetaDataManager().get(FigureState.class), newState).getStateAsMap());
		Assert.assertEquals(60f, state.getX());
	}
	
	@Test
	public void testCallsToServer() throws Exception {
		Connection serverConnection = Mockito.mock(Connection.class);
		GameClient client = new GameClient();
		client.connect(serverConnection);
		FigureState state = client.createProxy(new FigureState());
		
		Assert.assertEquals(0.0f, state.getX());
		Mockito.verify(serverConnection, Mockito.never()).send(any());

		state.update(42);
		Mockito.verify(serverConnection, Mockito.never()).send(any());

		state.gotoX(42);
		Mockito.verify(serverConnection, Mockito.times(1)).send(any());
		
		client.close();
	}
}
