package org.jnet.core;

import java.lang.reflect.Method;

import junit.framework.Assert;

import org.jnet.core.connection.Connection;
import org.jnet.core.connection.messages.NewStateMessage;
import org.jnet.core.synchronizer.Event;
import org.jnet.core.synchronizer.ObjectId;
import org.jnet.core.testdata.FigureState;
import org.jnet.core.tools.Sleep;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;

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
	public void testDelayedEventsAreEnqueued() throws Exception {
		FigureState state = server.createProxy(new FigureState());
		ObjectId id = server.getIdForProxy(state);

		serverTime = 10000;
		state.gotoX(1000);
		Assert.assertEquals(0f, state.getX());
		server.receiveEvent(new Event(id, 9000, gotoMethod, new Object[] {1000}));
		Assert.assertEquals(50f, state.getX());
	}
	
	/**
	 * If an event is delayed more than the acceptedDelay, it is dated to the actual timestamp
	 * of acceptedDelay
	 */
	@Test
	public void testLateEventsAreEnqueued() throws Exception {
		FigureState state = server.createProxy(new FigureState());
		ObjectId id = server.getIdForProxy(state);

		serverTime = 10000;
		state.gotoX(1000);
		Assert.assertEquals(0f, state.getX());
		server.receiveEvent(new Event(id, 5000, gotoMethod, new Object[] {1000}));
		Assert.assertEquals(75f, state.getX());
	}
	
	@Test
	public void testEventsAreDistributed() throws Exception {
		Connection ctc = mock(Connection.class);
		server.addConnetion(ctc);
		FigureState proxy = server.createProxy(new FigureState());
		ObjectId id = server.getIdForProxy(proxy);

		proxy.gotoX(1000);
		
		FigureState should = new FigureState();
		should.setTargetX(1000);
		Mockito.verify(ctc, Mockito.times(1)).send(new NewStateMessage(id, 0, server.getMetaDataManager().get(FigureState.class), should));
	}
}
