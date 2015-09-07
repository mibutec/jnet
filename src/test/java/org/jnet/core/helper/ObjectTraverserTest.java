package org.jnet.core.helper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jnet.core.helper.ObjectTraverser.Consumer;
import org.jnet.core.helper.ObjectTraverser.FieldHandler;
import org.jnet.core.helper.ObjectTraverser.Traverser;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


public class ObjectTraverserTest {
	@Mock
	private Consumer consumer;
	
	private ObjectTraverser testee;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		testee = new ObjectTraverser();
		when(consumer.onObjectFound(any(), any())).thenReturn(true);
	}
	
	@Test
	public void shouldTraverseCompleteAnObjectTree() throws Exception {
		runTest(new ClassWithInnerClass(), (object) -> {
			verify(consumer).onObjectFound(object.primitives, object);
			verifyPrimitives(object.primitives);
		});
	}
	
	@Test
	public void shouldTraverseEnums() throws Exception {
		GenericClass gc = new GenericClass();
		gc.object1 = SimpleEnum.TRUE;
		gc.object2 = ComplexeEnum.FALSE;
		runTest(gc, (object) -> {
			verify(consumer).onObjectFound(object, null);
			verify(consumer).onObjectFound(object.object1, object);
			verify(consumer).onObjectFound(SimpleEnum.TRUE.name(), object.object1);
			verify(consumer).onObjectFound(SimpleEnum.TRUE.ordinal(), object.object1);
			verify(consumer).onObjectFound(object.object2, object);
			verify(consumer).onObjectFound(ComplexeEnum.FALSE.someInner, ComplexeEnum.FALSE);
			verify(consumer).onObjectFound(ComplexeEnum.FALSE.name(), object.object2);
			verify(consumer).onObjectFound(ComplexeEnum.FALSE.ordinal(), object.object2);
		});
	}
	
	@Test
	public void shouldHandleNullFields() throws Exception {
		runTest(new NullClass(), (object) -> {});
	}
	
	@Test
	public void shouldIgnoreStaticFields() throws Exception {
		runTest(new ClassWithStaticFields(), (object) -> {});
	}
	
	@Test
	public void shouldAllowToConfigureModifiers() throws Exception {
//		testee.changeModifier
//		runTest(new ClassWithStaticFields(), (object) -> {
//			verify(consumer).onObjectFound("static", ClassWithStaticFields.class);
//		});
	}
	
	@Test
	public void shouldIterateThroughAllKindsOfArrays() throws Exception {
		runTest(new ArrayClass(), (object) -> {
			verify(consumer).onObjectFound(object.boolArray, object);
			verify(consumer).onObjectFound(object.boolArray[0], object.boolArray);
			verify(consumer).onObjectFound(object.boolArray[1], object.boolArray);
			verify(consumer).onObjectFound(object.byteArray, object);
			verify(consumer).onObjectFound(object.byteArray[0], object.byteArray);
			verify(consumer).onObjectFound(object.byteArray[1], object.byteArray);
			verify(consumer).onObjectFound(object.shortArray, object);
			verify(consumer).onObjectFound(object.shortArray[0], object.shortArray);
			verify(consumer).onObjectFound(object.shortArray[1], object.shortArray);
			verify(consumer).onObjectFound(object.charArray, object);
			verify(consumer).onObjectFound(object.charArray[0], object.charArray);
			verify(consumer).onObjectFound(object.charArray[1], object.charArray);
			verify(consumer).onObjectFound(object.intArray, object);
			verify(consumer).onObjectFound(object.intArray[0], object.intArray);
			verify(consumer).onObjectFound(object.intArray[1], object.intArray);
			verify(consumer).onObjectFound(object.longArray, object);
			verify(consumer).onObjectFound(object.longArray[0], object.longArray);
			verify(consumer).onObjectFound(object.longArray[1], object.longArray);
			verify(consumer).onObjectFound(object.floatArray, object);
			verify(consumer).onObjectFound(object.floatArray[0], object.floatArray);
			verify(consumer).onObjectFound(object.floatArray[1], object.floatArray);
			verify(consumer).onObjectFound(object.doubleArray, object);
			verify(consumer).onObjectFound(object.doubleArray[0], object.doubleArray);
			verify(consumer).onObjectFound(object.doubleArray[1], object.doubleArray);
			verify(consumer).onObjectFound(object.objectArray, object);
			verify(consumer).onObjectFound(object.objectArray[0], object.objectArray);
			verify(consumer).onObjectFound(object.objectArray[1], object.objectArray);
			verifyPrimitives(object.objectArray[0]);
			verifyPrimitives(object.objectArray[1]);
		});
	}
	
	@Test
	public void shouldIterateThroughSets() throws Exception {
		ClassWithSet cws = new ClassWithSet();
		PrimitivesClass o1 = (PrimitivesClass) cws.set.stream().filter((o) -> o instanceof PrimitivesClass).findAny().get();
		ClassWithInnerClass o2 = (ClassWithInnerClass) cws.set.stream().filter((o) -> o instanceof ClassWithInnerClass).findAny().get();
		runTest(cws, (object) -> {
			verify(consumer).onObjectFound(object.set, object);
			verify(consumer).onObjectFound(o1, object.set);
			verify(consumer).onObjectFound(o2, object.set);
			verifyPrimitives(o1);
			verify(consumer).onObjectFound(o2.primitives, o2);
			verifyPrimitives(o2.primitives);
		});
	}
	
	@Test
	public void shouldIterateThroughLists() throws Exception {
		runTest(new ClassWithList(), (object) -> {
			verify(consumer).onObjectFound(object.list, object);
			verify(consumer).onObjectFound(object.list.get(0), object.list);
			verify(consumer).onObjectFound(object.list.get(1), object.list);
			verifyPrimitives(object.list.get(0));
			verify(consumer).onObjectFound(((ClassWithInnerClass) (object.list.get(1))).primitives, object.list.get(1));
			verifyPrimitives(((ClassWithInnerClass) (object.list.get(1))).primitives);
		});
	}
	
	@Test
	public void shouldIterateThroughMapsAndTraverseKeysAndValues() throws Exception {
		runTest(new Object(), (object) -> {
			
		});
	}
	
	@Test
	public void shouldDetectCycles() throws Exception {
		runTest(new Object(), (object) -> {
			
		});
	}

	@Test
	public void shouldDetectCyclesInvolvingArrays() throws Exception {
		runTest(new Object(), (object) -> {
			
		});
	}

	@Test
	public void shouldDetectCyclesInvolvingCollections() throws Exception {
		runTest(new Object(), (object) -> {
			
		});
	}
	
	@Test
	public void shouldNotDetectCycleOnEqualObjects() throws Exception {
		runTest(new Object(), (object) -> {
			
		});
	}
	
	@Test
	public void shouldExcludePrimitivesFromCycleDetection() throws Exception {
		runTest(new Object(), (object) -> {
			
		});
	}
	
	@Test
	public void shouldAllowAddingFieldHandlers() throws Exception {
		
	}
	
	@Test
	public void shouldAllowOverridingFieldHandlers() throws Exception {
		
	}
	
	@Test
	public void shouldDetermineBestMatchingFieldHandler() throws Exception {
		
	}

	@Test
	public void shouldThrowAnExceptionIfTwoIndependentInterfacesFieldHandlersAreBestMatch() throws Exception {
		
	}

	@Test
	public void shouldAllowCoveringTwoIndependentInterfaceFieldHandlerssInOneMasterInterfaceFieldHandlers() throws Exception {
		
	}
	
	@Test
	public void shouldPrefferMoreSpecificArrayFieldHandlersOverGeneral() throws Exception {
		
	}
	
	private<T> void runTest(T object, Verifier<T> verifications) throws Exception {
		testee.traverse(object, consumer);
		verify(consumer).onObjectFound(object, null);
		verifications.verify(object);
		verifyNoMoreInteractions(consumer);
	}
	
	private static interface Verifier<T> {
		public void verify(T object);
	}
	
	private void verifyPrimitives(Object parent) {
		PrimitivesClass compareValues = new PrimitivesClass();
		
		// one for Boolean and one for boolean
		verify(consumer, times(2)).onObjectFound(compareValues.bigBool, parent);
		verify(consumer).onObjectFound(compareValues.bigByte, parent);
		verify(consumer).onObjectFound(compareValues.bigChar, parent);
		verify(consumer).onObjectFound(compareValues.bigDouble, parent);
		verify(consumer).onObjectFound(compareValues.bigFloat, parent);
		verify(consumer).onObjectFound(compareValues.bigInt, parent);
		verify(consumer).onObjectFound(compareValues.bigLong, parent);
		verify(consumer).onObjectFound(compareValues.bigShort, parent);
		verify(consumer).onObjectFound(compareValues.smallByte, parent);
		verify(consumer).onObjectFound(compareValues.smallChar, parent);
		verify(consumer).onObjectFound(compareValues.smallDouble, parent);
		verify(consumer).onObjectFound(compareValues.smallFloat, parent);
		verify(consumer).onObjectFound(compareValues.smallInt, parent);
		verify(consumer).onObjectFound(compareValues.smallLong, parent);
		verify(consumer).onObjectFound(compareValues.smallShort, parent);
		verify(consumer).onObjectFound(compareValues.string, parent);
	}
}

class PrimitivesClass {
	Boolean bigBool = true;
	Byte bigByte = (byte) 1;
	Short bigShort = (short) 2;
	Character bigChar = 'A';
	Integer bigInt = 3;
	Long bigLong = 4l;
	Float bigFloat = .5f;
	Double bigDouble = .6;
	String string = "string";
	boolean smallBool = true;
	byte smallByte = (byte) 7;
	short smallShort = (short) 8;
	char smallChar = 'B';
	int smallInt = 9;
	long smallLong = 10l;
	float smallFloat = .11f;
	double smallDouble = .12;
}

class ArrayClass {
	boolean[] boolArray = new boolean[]{true, false};
	byte[] byteArray = new byte[] {(byte) 1, (byte) 2};
	short[] shortArray = new short[] {(short) 1, (short) 2};
	char[] charArray = new char[] {'a', 'b'};
	int[] intArray = new int[]{1, 2};
	long[] longArray = new long[]{1l, 2l};
	float[] floatArray = new float[] {1f, 2f};
	double[] doubleArray = new double[] {1, 2};
	Object[] objectArray = new 	PrimitivesClass[]{new PrimitivesClass(), new PrimitivesClass()};
}

class ClassWithList {
	List list = new LinkedList();
	
	ClassWithList() {
		list.add(new PrimitivesClass());
		list.add(new ClassWithInnerClass());
	}
}

class ClassWithSet {
	Set set = new HashSet();
	
	ClassWithSet() {
		set.add(new PrimitivesClass());
		set.add(new ClassWithInnerClass());
	}
}

class ClassWithMap {
	Map map = new HashMap();
	
	ClassWithMap() {
		map.put(new PrimitivesClass(), new ClassWithInnerClass());
		map.put(new PrimitivesClass(), new ClassWithInnerClass());
	}
}

class EqualClass {
	@Override
	public boolean equals(Object obj) {
		return true;
	}
	
	@Override
	public int hashCode() {
		return 1;
	}
}

class ClassWithInnerClass {
	PrimitivesClass primitives = new PrimitivesClass();
}

class ComplexeClass {
	int anInt = 42;
	String string = "string";
	PrimitivesClass innerClass = new PrimitivesClass();
	ClassWithInnerClass classWithInnerClass = new ClassWithInnerClass();
}

class NullClass {
	String string;
	PrimitivesClass innerClass;
	ClassWithInnerClass classWithInnerClass;
}

class TestFieldHandler<T> extends FieldHandler<T> {

	protected TestFieldHandler(Class<T> handledType) {
		super(handledType);
	}

	@Override
	public void handleObject(T object, Traverser traverser, Consumer consumer) throws Exception {
	}
}

interface IndependentInterface1 {
	
}

interface IndependetInterface2 {
	
}

interface MasterInterface extends IndependentInterface1, IndependetInterface2 {
	
}

class GenericClass {
	Object object1;
	Object object2;
}

class ClassWithStaticFields {
	static String staticField = "static";
}

enum SimpleEnum {
	TRUE, FALSE
}

enum ComplexeEnum {
	TRUE, FALSE;
	
	GenericClass someInner = new GenericClass();
}