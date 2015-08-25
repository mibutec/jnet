package org.jnet.core.connection.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.Map;

import junit.framework.Assert;

import org.jnet.core.GameClient;
import org.jnet.core.ManagedObject;
import org.jnet.core.connection.Connection;
import org.jnet.core.testdata.FigureState;
import org.jnet.core.testdata.Hochhaus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class NewStateMessageTest {
	private GameClient client;
	
	@Before
	public void setup() {
		client = new GameClient(Mockito.mock(Connection.class));
	}
	
	@Test
	public void testSimpleSerialization() throws Exception {
		FigureState state = new FigureState();
		state.setTargetX(100);
		state.setX(50f);

		Map<Field, Object> map = serialize(state);

		Assert.assertEquals(4, map.size());
		Assert.assertEquals(100, map.get(FigureState.class.getDeclaredField("targetX")));
		Assert.assertEquals(50f, map.get(FigureState.class.getDeclaredField("x")));
		Assert.assertEquals(0.05f, map.get(FigureState.class.getDeclaredField("speed")));
		Assert.assertNull(map.get(FigureState.class.getDeclaredField("aString")));
	}
	
	@Test
	public void testSerializationWithNull() throws Exception {
		FigureState state = new FigureState();
		state.setTargetX(100);
		state.setX(50f);
		state.setSpeed(null);

		Map<Field, Object> map = serialize(state);

		Assert.assertEquals(4, map.size());
		Assert.assertEquals(100, map.get(FigureState.class.getDeclaredField("targetX")));
		Assert.assertEquals(50f, map.get(FigureState.class.getDeclaredField("x")));
		Assert.assertNull(map.get(FigureState.class.getDeclaredField("speed")));
		Assert.assertNull(map.get(FigureState.class.getDeclaredField("aString")));
	}

	@Test
	public void testSerializationOfComplexeObjects() throws Exception {
		Hochhaus state = new Hochhaus();
		state.setSomething(100f);

		Map<Field, Object> map = serialize(state);

		Assert.assertEquals(1, map.size());
		Assert.assertEquals(100f, map.get(Hochhaus.class.getDeclaredField("something")));
	}
	
	@Test
	public void testSerializationOfString() throws Exception {
		FigureState state = new FigureState();
		state.setaString("Michael���");

		Map<Field, Object> map = serialize(state);

		Assert.assertEquals("Michael���", map.get(FigureState.class.getDeclaredField("aString")));
	}

	
	private Map<Field, Object> serialize(Object state) throws Exception {
		int id = client.getIdForProxy(client.createProxy(state));
		NewStateMessage message = new NewStateMessage(id, 0, client.getMetaDataManager().get(state.getClass()), state);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		message.write(baos);
		message = new NewStateMessage(client.getMetaDataManager());
		message.read(new ByteArrayInputStream(baos.toByteArray()));
		return message.getStateAsMap();
		
	}
}
