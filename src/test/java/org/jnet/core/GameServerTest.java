package org.jnet.core;

import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;

import junit.framework.Assert;

import org.jala.mixins.Sleep;
import org.jnet.core.Event;
import org.jnet.core.GameServer;
import org.jnet.core.connection.ConnectionToClient;
import org.jnet.core.testdata.FigureState;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class GameServerTest implements Sleep {
	private int serverTime = 0;
	
	private GameServer server;
	
	private static Method gotoMethod;
	
	@BeforeClass
	public static void init() throws Exception {
		gotoMethod = FigureState.class.getMethod("gotoX", new Class[] {int.class});
	}
	
	@Before
	public void setup() throws Exception {
		serverTime = 0;
		server = new GameServer(1500) {
			@Override
			public int serverTime() {
				return serverTime;
			}
		};
	}
	
	/**
	 * Start an event in present leeds to the expected behavior in present
	 * Adding an event in the past changes the present
	 */
	@Test
	public void testDelayedEventsAreEnqueued() {
		FigureState state = server.createProxy(new FigureState());
		int id = server.getIdForProxy(state);

		serverTime = 10000;
		state.gotoX(1000);
		Assert.assertEquals(0f, state.getX());
		server.receiveEvent(id, new Event<FigureState>(9000, (byte) 0, gotoMethod, new Object[] {1000}));
		Assert.assertEquals(50f, state.getX());
	}
	
	/**
	 * If an event is delayed more than the acceptedDelay, it is dated to the actual timestamp
	 * of acceptedDelay
	 */
	@Test
	public void testLateEventsAreEnqueued() {
		FigureState state = server.createProxy(new FigureState());
		int id = server.getIdForProxy(state);

		serverTime = 10000;
		state.gotoX(1000);
		Assert.assertEquals(0f, state.getX());
		server.receiveEvent(id, new Event<FigureState>(5000, (byte) 0, gotoMethod, new Object[] {1000}));
		Assert.assertEquals(75f, state.getX());
	}
	
	@Test
	public void testEventsAreDistributed() throws Exception {
		ConnectionToClient ctc = mock(ConnectionToClient.class);
		server.addConnetion(ctc);
		FigureState state = server.createProxy(new FigureState());
		int id = server.getIdForProxy(state);

		state.gotoX(1000);
		Mockito.verify(ctc, Mockito.times(1)).sendState(id, state, 0);
	}
}
