package org.jnet.core.synchronizer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jnet.core.helper.Unchecker;
import org.jnet.core.synchronizer.message.ChangedStateMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

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
		System.out.println(testee().createMessage(Matchers.anyInt()));
		Mockito.when(testee().createMessage(Matchers.anyInt())).thenReturn(csm);
		SimpleObject newObject1 = new SimpleObject();
		SimpleObject newObject2 = new SimpleObject();
		SimpleObject newObject3 = new SimpleObject();
		ObjectId mainId = testee().getIdForObject(object);
		testee.addEvent(new Event(mainId, 10, ComplexeTestclass.setSimpleObjectMethod, newObject1));
		testee.addEvent(new Event(mainId, 10, ComplexeTestclass.changeArrayMethod, 0, newObject2));
		testee.addEvent(new Event(mainId, 10, ComplexeTestclass.addSimpleObjectMethod, newObject3));
		ChangedStateMessage createdMessage = testee().evolveLastTrustedState(100);
		Mockito.verify(createdMessage).addNewObject(testee().getIdForObject(newObject1), newObject1);
		Mockito.verify(createdMessage).addNewObject(testee().getIdForObject(newObject2), newObject2);
		Mockito.verify(createdMessage).addNewObject(testee().getIdForObject(newObject3), newObject3);
		Mockito.verifyNoMoreInteractions(createdMessage);
	}
	
	private LookAheadObjectMaster<?> testee() {
		return (LookAheadObjectMaster<?>) testee;
	}

	@Override
	protected <T> LookAheadObject<T> createTestee(T object) {
		return Mockito.spy(new LookAheadObjectMaster<T>(object));
	}
	
	public static class ComplexeTestclass {
		private String name = "michael";
		
		private SimpleObject simpleObject = new SimpleObject();
		
		private SimpleObject[] soArray = new SimpleObject[] {new SimpleObject(), new SimpleObject()};
		
		private List<SimpleObject> soList;
		
		public static final Method setSimpleObjectMethod = Unchecker.uncheck(() -> ComplexeTestclass.class.getDeclaredMethod("setSimpleObject", SimpleObject.class));
		
		public static final Method changeArrayMethod = Unchecker.uncheck(() -> ComplexeTestclass.class.getDeclaredMethod("changeArray", int.class, SimpleObject.class));
		
		public static final Method addSimpleObjectMethod = Unchecker.uncheck(() -> ComplexeTestclass.class.getDeclaredMethod("addSimpleObject", SimpleObject.class));
		
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
			System.out.println("setSimpleObject");
			this.simpleObject = so;
		}

		public void changeArray(int i, SimpleObject so) {
			System.out.println("changeArray");
			this.soArray[i] = so;
		}

		public void addSimpleObject(SimpleObject so) {
			System.out.println("addSimpleObject");
			this.soList.add(so);
		}
	}
	
	public static class SimpleObject {
		private String string = "string";
		
		private long aLong = 42l;
		
		public transient int i = cnt++;
		
		private static int cnt = 0;

		@Override
		public String toString() {
			return "SimpleObject [i=" + i + "]";
		}
	}
}
