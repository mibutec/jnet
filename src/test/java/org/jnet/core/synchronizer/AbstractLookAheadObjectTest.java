package org.jnet.core.synchronizer;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jnet.core.UpdateableObject;
import org.jnet.core.helper.CompareSameWrapper;
import org.jnet.core.helper.Unchecker;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public abstract class AbstractLookAheadObjectTest {
	protected LookAheadObject<?> testee;
	
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
	public void testObjectInventorization() throws Exception {
		ComplexeObject complexeObject = new ComplexeObject();
		testee = createTestee(complexeObject);
		
		// test the mainentity
		assertInInventory(complexeObject);
		
		// test attributes
		assertInInventory(complexeObject.someObject);
		assertInInventory(complexeObject.someUpdateableObject);
		assertNotInInventory(complexeObject.transientState);
		assertNotInInventory(complexeObject.someFloat);
		
		// array of objects
		assertInInventory(complexeObject.figures);
		for (UpdateableTestobject figure : complexeObject.figures) {
			assertInInventory(figure);
		}
		
		// array of primitives
		assertInInventory(complexeObject.intArray);
		for (int i : complexeObject.intArray) {
			assertNotInInventory(i);
		}
		
		// collection of objects
		assertInInventory(complexeObject.moreFigures);
		for (UpdateableTestobject figure : complexeObject.moreFigures) {
			assertInInventory(figure);
		}
		
		// collection of 'primitives'
		assertInInventory(complexeObject.morePrimitives);
		for (Integer i : complexeObject.morePrimitives) {
			assertNotInInventory(i);
		}
		
		// Map of objects
		assertInInventory(complexeObject.complexeMap);
		for (Entry<UpdateableTestobject, SomeObject> entry : complexeObject.complexeMap.entrySet()) {
			assertInInventory(entry.getKey());
			assertInInventory(entry.getValue());
		}
		
		// test object is not inverntorized several times
		assertSameId(complexeObject.someUpdateableObject, complexeObject.someObject.testobject, complexeObject.complexeMap.values().iterator().next().testobject);
		assertThat(testee.managedObjects.size(), is(inInventoryCount));
	}

	@Test()
	public void shouldUpdateObjectWhenTimeFlows() throws Exception {
		LookAheadObject<UpdateableTestobject> testee = createTestee(new UpdateableTestobject());
		assertThat(testee.getStateForTimestamp(0).getX(), is(0));
		assertThat(testee.getStateForTimestamp(1000).getX(), is(500));
	}

	@Test(expected=RuntimeException.class)
	public void shouldThrowExceptionWhenAskingStateInPast() throws Exception {
		LookAheadObject<UpdateableTestobject> testee = createTestee(new UpdateableTestobject());
		testee.lastTrustedState.setTimestamp(1000);
		testee.getStateForTimestamp(0);
	}
	
	@Test
	public void shouldHandleOneEventCorrectly() throws Exception {
		UpdateableTestobject object = new UpdateableTestobject();
		testee = createTestee(object);
		testee.addEvent(new Event(findObjectIdForObject(object), 500, setXMethod, 0));
		assertThat(((UpdateableTestobject) testee.getStateForTimestamp(498)).getX(), is(249));
		assertThat(((UpdateableTestobject) testee.getStateForTimestamp(500)).getX(), is(0));
		assertThat(((UpdateableTestobject) testee.getStateForTimestamp(1000)).getX(), is(250));
	}
	
	@Test
	public void shouldHandleSeveralEventsCorrectly() throws Exception {
		UpdateableTestobject object = new UpdateableTestobject();
		testee = createTestee(object);
		testee.addEvent(new Event(findObjectIdForObject(object), 500, setXMethod, 0));
		testee.addEvent(new Event(findObjectIdForObject(object), 1000, setXMethod, 0));
		assertThat(((UpdateableTestobject) testee.getStateForTimestamp(498)).getX(), is(249));
		assertThat(((UpdateableTestobject) testee.getStateForTimestamp(500)).getX(), is(0));
		assertThat(((UpdateableTestobject) testee.getStateForTimestamp(998)).getX(), is(249));
		assertThat(((UpdateableTestobject) testee.getStateForTimestamp(1000)).getX(), is(0));
		assertThat(((UpdateableTestobject) testee.getStateForTimestamp(2000)).getX(), is(500));
	}
	
	@Test
	public void shouldIgnoreEventsInThePast() {
		UpdateableTestobject object = new UpdateableTestobject();
		testee = createTestee(object);
		testee.addEvent(new Event(findObjectIdForObject(object), 3, setXMethod, Integer.MAX_VALUE));
		testee.lastTrustedState.setTimestamp(10);
		assertThat(((UpdateableTestobject) testee.getStateForTimestamp(1000)).getX(), is(495));
	}
	
	@Test
	public void shouldRemoveLateEventsOnCleanup() {
		UpdateableTestobject object = new UpdateableTestobject();
		testee = createTestee(object);
		testee.addEvent(new Event(findObjectIdForObject(object), 500, setXMethod, 0));
		testee.addEvent(new Event(findObjectIdForObject(object), 1000, setXMethod, 0));
		testee.addEvent(new Event(findObjectIdForObject(object), 1500, setXMethod, 0));
		assertThat(testee.sortedEvents.size(), is(3));
		testee.lastTrustedState.setTimestamp(502);
		testee.cleanup();
		assertThat(testee.sortedEvents.size(), is(2));
		testee.lastTrustedState.setTimestamp(1002);
		testee.cleanup();
		assertThat(testee.sortedEvents.size(), is(1));
		testee.lastTrustedState.setTimestamp(1502);
		testee.cleanup();
		assertThat(testee.sortedEvents.size(), is(0));
	}
	
	private int inInventoryCount = 0;
	private void assertInInventory(Object o) {
		inInventoryCount++;
		assertTrue(testee.managedObjects.values().contains(new CompareSameWrapper<Object>(o)));
	}

	private void assertNotInInventory(Object o) {
		assertFalse(testee.managedObjects.values().contains(new CompareSameWrapper<Object>(o)));
	}
	
	private void assertSameId(Object ... objects) {
		if (objects.length > 0) {
			ObjectId id = findObjectIdForObject(objects[0]);
			for (Object object : objects) {
				assertThat(findObjectIdForObject(object), is(id));
			}
		}
	}
	
	public static class Super implements Serializable {
		String a = "a";
	}
	
	public static class Sub extends Super {
		String b = "b";
	}

	
	protected abstract<T> LookAheadObject<T> createTestee(T object);
	
	protected ObjectId findObjectIdForObject(Object object) {
		return testee.managedObjectsReverse.get(new CompareSameWrapper<>(object));
	}
	
	protected static Method setXMethod = Unchecker.uncheck(() -> UpdateableTestobject.class.getDeclaredMethod("setX", int.class));

	protected static class UpdateableTestobject implements UpdateableObject, Serializable {
		protected float speed = .5f;
		
		protected float x = 0;

		@Override
		public void update(long delta) {
			x += speed * delta;
		}

		public int getX() {
			return (int) x;
		}
		
		public void setX(int x) {
			this.x = x;
		}
	}
	
	protected static class ComplexeObject implements Serializable {
		protected Float someFloat = 23f;
		
		protected UpdateableTestobject nullObject;
		
		protected UpdateableTestobject[] figures;
		
		protected UpdateableTestobject someUpdateableObject;
		
		protected SomeObject someObject;
		
		protected transient UpdateableTestobject transientState;
		
		protected int[] intArray = new int[] {0, 1, 2, 5};
		
		protected List<UpdateableTestobject> moreFigures;
		
		protected List<Integer> morePrimitives;
		
		protected Map<UpdateableTestobject, SomeObject> complexeMap;
		
		public ComplexeObject() {
			super();
			this.nullObject = null;
			this.someUpdateableObject = new UpdateableTestobject();
			this.someObject = new SomeObject(someUpdateableObject);
			this.figures = new UpdateableTestobject[4];
			for (int i = 0; i < 4; i++) {
				figures[i] = new UpdateableTestobject();
			}
			
			moreFigures = new LinkedList<>();
			for (int i = 0; i < 4; i++) {
				moreFigures.add(new UpdateableTestobject());
			}
			
			morePrimitives = new LinkedList<>();
			for (int i = 0; i < 4; i++) {
				morePrimitives.add(i);
			}
			
			complexeMap = new HashMap<>();
			complexeMap.put(new UpdateableTestobject(), new SomeObject(someUpdateableObject));
			
			transientState = new UpdateableTestobject();
		}
	}
	
	protected static class SomeObject implements Serializable {
		protected UpdateableTestobject testobject;

		public SomeObject(UpdateableTestobject testobject) {
			this.testobject = testobject;
		}
	}

}
