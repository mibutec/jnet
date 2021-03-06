package org.jnet.core.synchronizer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.jnet.core.synchronizer.message.ChangedStateMessage;
import org.jnet.core.synchronizer.message.NewArrayMessage;
import org.jnet.core.synchronizer.message.NewObjectMessage;
import org.jnet.core.synchronizer.message.UpdateArrayMessage;
import org.jnet.core.synchronizer.message.UpdateSetMessage;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;



public class LookAheadObjectSlaveTest extends AbstractLookAheadObjectTest {
	
	@Test
	public void shouldChangeWrappedObjectsStateOnEvolve() {
		UpdateableTestobject object = new UpdateableTestobject();
		testee = createTestee(object);
		ChangedStateMessage message = new ChangedStateMessage(1000);
		message.addUpdateObject(findObjectIdForObject(object), ImmutableMap.of("x", 42f));
		testee().evolveLastTrustedState(message);
		assertThat(object.getX(), is(42));
	}
	
	@Test
	public void shouldChangeWrappedObjectsStateForInheritedClasses() {
		Sub sub = new Sub();
		testee = createTestee(sub);
		ChangedStateMessage message = new ChangedStateMessage(0);
		message.addUpdateObject(findObjectIdForObject(sub), ImmutableMap.of("a", "a2", "b", "b2"));
		testee().evolveLastTrustedState(message);
		assertThat(sub.a, is("a2"));
		assertThat(sub.b, is("b2"));
	}
	
	@Test
	public void shouldChangeNonPrimitiveFieldOnEvolve() {
		ComplexeObject complexeObject = new ComplexeObject();
		testee = createTestee(complexeObject);
		ChangedStateMessage message = new ChangedStateMessage(0);
		ObjectId objectIdForExistingObject = findObjectIdForObject(complexeObject.someUpdateableObject);
		message.addUpdateObject(findObjectIdForObject(complexeObject), ImmutableMap.of("nullObject", objectIdForExistingObject));
		testee().evolveLastTrustedState(message);
		assertThat(complexeObject.nullObject, sameInstance(complexeObject.someUpdateableObject));
	}
	
	@Test
	public void shouldHandleArraysOnEvolve() {
		ComplexeObject complexeObject = new ComplexeObject();
		testee = createTestee(complexeObject);
		ChangedStateMessage message = new ChangedStateMessage(0);
		ObjectId newObjectIdForIntArrays = new ObjectId(Integer.MAX_VALUE);
		message.addNewObjectMessage(new NewArrayMessage(newObjectIdForIntArrays, int[].class, 2));
		message.addUpdateObject(findObjectIdForObject(complexeObject), ImmutableMap.of("intArray", newObjectIdForIntArrays));
		message.addUpdateObject(new UpdateArrayMessage(newObjectIdForIntArrays, ImmutableMap.of(0,7,1,8)));
		testee().evolveLastTrustedState(message);
		assertThat(complexeObject.intArray, is(new int[] {7, 8}));
	}

	@Test
	public void shouldHandleChangedSetsOnEvolve() {
		ObjectWithSet object = new ObjectWithSet();
		testee = createTestee(object);
		ChangedStateMessage message = new ChangedStateMessage(0);
		message.addUpdateObject(new UpdateSetMessage(findObjectIdForObject(object.intSet), ImmutableSet.of(42), ImmutableSet.of(1)));
		testee().evolveLastTrustedState(message);
		assertThat(object.intSet, is(ImmutableSet.of(2, 42)));
	}

	@Test
	public void shouldSetNewObjectsOnEvolve() {
		ComplexeObject complexeObject = new ComplexeObject();
		testee = createTestee(complexeObject);
		ChangedStateMessage message = new ChangedStateMessage(0);
		message.addNewObjectMessage(new NewObjectMessage(new ObjectId(Integer.MAX_VALUE), UpdateableTestobject.class));
		message.addUpdateObject(new ObjectId(Integer.MAX_VALUE), ImmutableMap.of("x", 100f, "speed", 30f));
		message.addUpdateObject(findObjectIdForObject(complexeObject), ImmutableMap.of("nullObject", new ObjectId(Integer.MAX_VALUE)));
		testee().evolveLastTrustedState(message);
		assertThat(complexeObject.nullObject.speed, is(30f));
		assertThat(complexeObject.nullObject.x, is(100f));
	}
	
	@Test
	public void shouldHandleEventsCorrectlyAfterEvolve() {
		UpdateableTestobject object = new UpdateableTestobject();
		testee = createTestee(object);
		testee.addEvent(new SerializableEvent(findObjectIdForObject(object), 500, setXMethod, 0));
		testee.addEvent(new SerializableEvent(findObjectIdForObject(object), 1000, setXMethod, 0));
		assertThat(((UpdateableTestobject) testee.getStateForTimestamp(498)).getX(), is(249));
		assertThat(((UpdateableTestobject) testee.getStateForTimestamp(502)).getX(), is(1));
		
		ChangedStateMessage message = new ChangedStateMessage(502);
		message.addUpdateObject(findObjectIdForObject(object), ImmutableMap.of("x", -17f));
		testee().evolveLastTrustedState(message);
		assertThat(((UpdateableTestobject) testee.getStateForTimestamp(502)).getX(), is(-17));
		// 231 = (998 - 502) / 2 - 17
		assertThat(((UpdateableTestobject) testee.getStateForTimestamp(998)).getX(), is(231));
		message = new ChangedStateMessage(998);
		message.addUpdateObject(findObjectIdForObject(object), ImmutableMap.of("x", -177f));
		testee().evolveLastTrustedState(message);
		assertThat(((UpdateableTestobject) testee.getStateForTimestamp(998)).getX(), is(-177));
		assertThat(((UpdateableTestobject) testee.getStateForTimestamp(1002)).getX(), is(1));
	}
	
	private LookAheadObjectSlave<?> testee() {
		return (LookAheadObjectSlave<?>) testee;
	}

	@Override
	protected <T> LookAheadObject<T> createTestee(T object) {
		return new LookAheadObjectSlave<T>(object);
	}
}
