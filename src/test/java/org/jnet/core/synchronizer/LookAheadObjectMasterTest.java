package org.jnet.core.synchronizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jnet.core.synchronizer.message.ChangedStateMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class LookAheadObjectMasterTest extends AbstractLookAheadObjectTest {
	@Mock
	private ChangedStateMessage csm;
	
	@Test
	public void shouldIncreaseTimestampOnEvolve() {
		UpdateableTestobject object = new UpdateableTestobject();
		testee = createTestee(object);
		testee().evolveLastTrustedState(1000);
		assertThat(testee.lastTrustedState.getTimestamp(), is(1000));
	}
	
	@Test
	public void shouldCreateNewObjectMessagesOnEvolve() {
		ComplexeTestclass object = new ComplexeTestclass();
		testee = createTestee(object);
		Mockito.when(testee().createMessage(Matchers.anyInt())).thenReturn(csm);
		SimpleObject newObject1 = new SimpleObject();
		SimpleObject newObject2 = new SimpleObject();
		SimpleObject newObject3 = new SimpleObject();
		testee.addEvent(new DirectInvokeEvent(10, () -> object.setSimpleObject(newObject1)));
		testee.addEvent(new DirectInvokeEvent(10, () -> object.changeArray(0, newObject2)));
		testee.addEvent(new DirectInvokeEvent(10, () -> object.addSimpleObject(newObject3)));
		ChangedStateMessage createdMessage = testee().evolveLastTrustedState(100);
		Mockito.verify(createdMessage).addNewObject(testee().getIdForObject(newObject1), newObject1);
		Mockito.verify(createdMessage).addNewObject(testee().getIdForObject(newObject2), newObject2);
		Mockito.verify(createdMessage).addNewObject(testee().getIdForObject(newObject3), newObject3);
		Mockito.verify(createdMessage, times(3)).addNewObject(any(), any());
	}
	
	@Test
	public void shouldCreateUpdateMessageForSimpleTypes() {
		OuterClass object = new OuterClass();
		testee = createTestee(object);
		Mockito.when(testee().createMessage(Matchers.anyInt())).thenReturn(csm);
		testee.addEvent(new DirectInvokeEvent(3, () -> object.simple1.string = "Bulla"));
		testee.addEvent(new DirectInvokeEvent(3, () -> object.simple2 = new SimpleObject()));
		ChangedStateMessage createdMessage = testee().evolveLastTrustedState(100);
		Mockito.verify(createdMessage).addUpdateObject(testee().getIdForObject(object.simple1), ImmutableMap.of("string", "Bulla"));
		Mockito.verify(createdMessage).addUpdateObject(testee().getIdForObject(object), ImmutableMap.of("simple1", new ObjectId(1001)));
		Mockito.verify(createdMessage).addUpdateObject(any(), eq(ImmutableMap.of("string", "michael", "aLong", 42l)));
	}
	
	@Test
	public void shouldCreateUpdateMessageForArrays() {
		
	}
	
	@Test
	public void shouldCreateUpdateMessageForCollections() {
		
	}
	
	@Test
	public void shouldCreateUpdateMessageForMaps() {
		
	}
	
	@Test
	public void shouldCleanManagedObjectsAfterEvolve() {
		
	}
	
	private LookAheadObjectMaster<?> testee() {
		return (LookAheadObjectMaster<?>) testee;
	}

	@Override
	protected <T> LookAheadObject<T> createTestee(T object) {
		return Mockito.spy(new LookAheadObjectMaster<>(object));
	}
	
	public static class ComplexeTestclass {
		private String name = "michael";
		
		private SimpleObject simpleObject = new SimpleObject();
		
		private SimpleObject[] soArray = new SimpleObject[] {new SimpleObject(), new SimpleObject()};
		
		private List<SimpleObject> soList;
		
		@Override
		public String toString() {
			return "ComplexeTestclass [name=" + name + ", simpleObject="
					+ simpleObject + ", soArray=" + Arrays.toString(soArray)
					+ ", soList=" + soList + "]";
		}

		public ComplexeTestclass() {
			soList = new ArrayList<>();
			soList.add(new SimpleObject());
			soList.add(new SimpleObject());
		}
		
		public void setSimpleObject(SimpleObject so) {
			this.simpleObject = so;
		}

		public void changeArray(int i, SimpleObject so) {
			this.soArray[i] = so;
		}

		public void addSimpleObject(SimpleObject so) {
			this.soList.add(so);
		}
	}
	
	private static class SimpleObject {
		private String string = "string";
		
		private long aLong = 42l;
		
		public void changeString() {
			string = "Michael";
		}
	}
	
	private static class OuterClass {
		private SimpleObject simple1 = new SimpleObject();
		
		private Object simple2 = new SimpleObject();
	}
}
