package org.jnet.core.synchronizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jnet.core.synchronizer.message.ChangedStateMessage;
import org.jnet.core.synchronizer.message.NewArrayMessage;
import org.jnet.core.synchronizer.message.NewObjectMessage;
import org.jnet.core.synchronizer.message.UpdateArrayMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
		when(testee().createMessage(anyInt())).thenReturn(csm);
		SimpleObject newObject1 = new SimpleObject();
		SimpleObject newObject2 = new SimpleObject();
		SimpleObject newObject3 = new SimpleObject();
		testee.addEvent(new DirectInvokeEvent(10, () -> object.setSimpleObject(newObject1)));
		testee.addEvent(new DirectInvokeEvent(10, () -> object.changeArray(0, newObject2)));
		testee.addEvent(new DirectInvokeEvent(10, () -> object.addSimpleObject(newObject3)));
		ChangedStateMessage createdMessage = testee().evolveLastTrustedState(100);
		verify(createdMessage).addNewObjectMessage(new NewObjectMessage(findObjectIdForObject(newObject1), newObject1.getClass()));
		verify(createdMessage).addNewObjectMessage(new NewObjectMessage(findObjectIdForObject(newObject2), newObject2.getClass()));
		verify(createdMessage).addNewObjectMessage(new NewObjectMessage(findObjectIdForObject(newObject3), newObject3.getClass()));
		verify(createdMessage, times(3)).addNewObjectMessage(any());
	}
	
	@Test
	public void shouldCreateUpdateMessageForSimpleTypes() {
		OuterClass object = new OuterClass();
		testee = createTestee(object);
		when(testee().createMessage(anyInt())).thenReturn(csm);
		testee.addEvent(new DirectInvokeEvent(3, () -> object.simple1.string = "Bulla"));
		testee.addEvent(new DirectInvokeEvent(3, () -> object.simple2 = new SimpleObject()));
		ChangedStateMessage createdMessage = testee().evolveLastTrustedState(100);
		verify(createdMessage).addUpdateObject(testee().getIdForObject(object.simple1), ImmutableMap.of("string", "Bulla"));
		verify(createdMessage).addUpdateObject(testee().getIdForObject(object), ImmutableMap.of("simple2", findObjectIdForObject(object.simple2)));
		verify(createdMessage).addUpdateObject(any(), eq(ImmutableMap.of("string", "string", "aLong", 42l)));
		verify(createdMessage, times(3)).addUpdateObject(any(), any());
	}
	
	@Test
	public void shouldCreateUpdateMessageForChangedArrays() {
		ObjectWithArrays object = new ObjectWithArrays();
		SimpleObject changedObject = new SimpleObject();
		changedObject.aLong = 4711;
		changedObject.string = "changed";
		testee = createTestee(object);
		when(testee().createMessage(anyInt())).thenReturn(csm);
		testee.addEvent(new DirectInvokeEvent(0, () -> {
			object.intArray = new int[] {7, 8};
			object.objectArray = new SimpleObject[] {changedObject};
		}));
		testee().evolveLastTrustedState(100);
		verify(csm).addNewObjectMessage(new NewArrayMessage(findObjectIdForObject(object.intArray), int[].class, 2));
		verify(csm).addUpdateObject(testee().getIdForObject(object), ImmutableMap.of("intArray", findObjectIdForObject(object.intArray), "objectArray", findObjectIdForObject(object.objectArray)));
		verify(csm).addNewObjectMessage(new NewArrayMessage(findObjectIdForObject(object.objectArray), SimpleObject[].class, 1));
		verify(csm).addNewObjectMessage(new NewObjectMessage(findObjectIdForObject(object.objectArray[0]), SimpleObject.class));
		verify(csm).addUpdateObject(new UpdateArrayMessage(testee().getIdForObject(object.intArray), ImmutableMap.of(0, 7, 1, 8)));
		verify(csm).addUpdateObject(new UpdateArrayMessage(testee().getIdForObject(object.objectArray), ImmutableMap.of(0, testee().getIdForObject(object.objectArray[0]))));
		verify(csm).addUpdateObject(testee().getIdForObject(object.objectArray[0]), ImmutableMap.of("string", "changed", "aLong", 4711L));
		verifyNoMoreInteractions(csm);
	}
	
	@Test
	public void shouldCreateUpdateMessageForChangedArrayElements() {
		
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
		return spy(new LookAheadObjectMaster<>(object));
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
	
	private static class ObjectWithArrays {
		private int[] intArray = new int[] {1, 2, 3};
		
		private SimpleObject[] objectArray = new SimpleObject[] {new SimpleObject(), new SimpleObject()};
	}
}
