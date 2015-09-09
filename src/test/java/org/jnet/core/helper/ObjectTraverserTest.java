package org.jnet.core.helper;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jnet.core.helper.ObjectTraverser.Consumer;
import org.jnet.core.helper.ObjectTraverser.FieldHandler;
import org.jnet.core.helper.ObjectTraverser.ConfiguredTraverser;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@SuppressWarnings("all")
public class ObjectTraverserTest {
	@Mock
	private Consumer consumer;
	
	@Spy
	private FieldHandler independentInterface1Handler = new TestFieldHandler<>(IndependentInterface1.class);
	
	@Spy
	private FieldHandler independentInterface2Handler = new TestFieldHandler<>(IndependentInterface2.class);
	
	@Spy
	private FieldHandler masterInterfaceHandler = new TestFieldHandler<>(MasterInterface.class);
	
	@Spy
	private FieldHandler collectionHandler = new TestFieldHandler<>(Collection.class);
	
	@Spy
	private FieldHandler goArrayHandler = new TestFieldHandler<>(GenericClass[].class);
	
	private ObjectTraverser testee;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		testee = new ObjectTraverser();
		when(consumer.onObjectFound(any(), any(), any())).thenReturn(true);
	}
	
	@Test
	public void shouldTraverseCompleteAnObjectTree() throws Exception {
		runTest(new ClassWithInnerClass(), (object) -> {
			verify(consumer).onObjectFound(object.primitives, object, "primitives");
			verifyPrimitives(object.primitives);
		});
	}
	
	@Test
	public void shouldTraverseEnums() throws Exception {
		GenericClass gc = new GenericClass();
		gc.object1 = SimpleEnum.TRUE;
		gc.object2 = ComplexeEnum.FALSE;
		runTest(gc, (object) -> {
			verify(consumer).onObjectFound(object.object1, object, "object1");
			verify(consumer).onObjectFound(SimpleEnum.TRUE.name(), object.object1, "name");
			verify(consumer).onObjectFound(SimpleEnum.TRUE.ordinal(), object.object1, "ordinal");
			verify(consumer).onObjectFound(object.object2, object, "object2");
			verify(consumer).onObjectFound(ComplexeEnum.FALSE.someInner, ComplexeEnum.FALSE, "someInner");
			verify(consumer).onObjectFound(ComplexeEnum.FALSE.someInner.object1, ComplexeEnum.FALSE.someInner, "object1");
			verify(consumer).onObjectFound(ComplexeEnum.FALSE.someInner.object2, ComplexeEnum.FALSE.someInner, "object2");
			verify(consumer).onObjectFound(ComplexeEnum.FALSE.name(), object.object2, "name");
			verify(consumer).onObjectFound(ComplexeEnum.FALSE.ordinal(), object.object2, "ordinal");
		});
	}
	
	@Test
	public void shouldHandleNullFields() throws Exception {
		runTest(new NullClass(), (object) -> {
			verify(consumer).onObjectFound(object.classWithInnerClass, object, "classWithInnerClass");
			verify(consumer).onObjectFound(object.innerClass, object, "innerClass");
			verify(consumer).onObjectFound(object.string, object, "string");
		});
	}
	
	@Test
	public void shouldIgnoreStaticFields() throws Exception {
		runTest(new ClassWithStaticFields(), (object) -> {});
	}
	
	@Test
	public void shouldAllowToConfigureModifiers() throws Exception {
		testee.setModifierToIgnore(Modifier.TRANSIENT);
		runTest(new ClassWithStaticFields(), (object) -> {
			verify(consumer).onObjectFound("static", ClassWithStaticFields.class, "staticField");
		});
		runTest(new ComplexeClass(), (object) -> {
			verify(consumer).onObjectFound(object.string, object, "string");
			verify(consumer).onObjectFound(object.classWithInnerClass, object, "classWithInnerClass");
			verify(consumer).onObjectFound(object.innerClass, object, "innerClass");
			verify(consumer).onObjectFound(object.classWithInnerClass.primitives, object.classWithInnerClass, "primitives");
			verifyPrimitives(object.classWithInnerClass.primitives);
			verifyPrimitives(object.innerClass);
		});
	}
	
	@Test
	public void shouldIterateThroughAllKindsOfArrays() throws Exception {
		runTest(new ArrayClass(), (object) -> {
			verify(consumer).onObjectFound(object.boolArray, object, "boolArray");
			verify(consumer).onObjectFound(object.boolArray[0], object.boolArray, "0");
			verify(consumer).onObjectFound(object.boolArray[1], object.boolArray, "1");
			verify(consumer).onObjectFound(object.byteArray, object, "byteArray");
			verify(consumer).onObjectFound(object.byteArray[0], object.byteArray, "0");
			verify(consumer).onObjectFound(object.byteArray[1], object.byteArray, "1");
			verify(consumer).onObjectFound(object.shortArray, object, "shortArray");
			verify(consumer).onObjectFound(object.shortArray[0], object.shortArray, "0");
			verify(consumer).onObjectFound(object.shortArray[1], object.shortArray, "1");
			verify(consumer).onObjectFound(object.charArray, object, "charArray");
			verify(consumer).onObjectFound(object.charArray[0], object.charArray, "0");
			verify(consumer).onObjectFound(object.charArray[1], object.charArray, "1");
			verify(consumer).onObjectFound(object.intArray, object, "intArray");
			verify(consumer).onObjectFound(object.intArray[0], object.intArray, "0");
			verify(consumer).onObjectFound(object.intArray[1], object.intArray, "1");
			verify(consumer).onObjectFound(object.longArray, object, "longArray");
			verify(consumer).onObjectFound(object.longArray[0], object.longArray, "0");
			verify(consumer).onObjectFound(object.longArray[1], object.longArray, "1");
			verify(consumer).onObjectFound(object.floatArray, object, "floatArray");
			verify(consumer).onObjectFound(object.floatArray[0], object.floatArray, "0");
			verify(consumer).onObjectFound(object.floatArray[1], object.floatArray, "1");
			verify(consumer).onObjectFound(object.doubleArray, object, "doubleArray");
			verify(consumer).onObjectFound(object.doubleArray[0], object.doubleArray, "0");
			verify(consumer).onObjectFound(object.doubleArray[1], object.doubleArray, "1");
			verify(consumer).onObjectFound(object.objectArray, object, "objectArray");
			verify(consumer).onObjectFound(object.objectArray[0], object.objectArray, "0");
			verify(consumer).onObjectFound(object.objectArray[1], object.objectArray, "1");
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
			verify(consumer).onObjectFound(object.set, object, "set");
			verify(consumer).onObjectFound(eq(o1), eq(object.set), matches("0|1"));
			verify(consumer).onObjectFound(eq(o2), eq(object.set), matches("0|1"));
			verifyPrimitives(o1);
			verify(consumer).onObjectFound(o2.primitives, o2, "primitives");
			verifyPrimitives(o2.primitives);
		});
	}
	
	@Test
	public void shouldIterateThroughLists() throws Exception {
		runTest(new ClassWithList(), (object) -> {
			verify(consumer).onObjectFound(object.list, object, "list");
			verify(consumer).onObjectFound(object.list.get(0), object.list, "0");
			verify(consumer).onObjectFound(object.list.get(1), object.list, "1");
			verifyPrimitives(object.list.get(0));
			verify(consumer).onObjectFound(((ClassWithInnerClass) (object.list.get(1))).primitives, object.list.get(1), "primitives");
			verifyPrimitives(((ClassWithInnerClass) (object.list.get(1))).primitives);
		});
	}
	
	@Test
	public void shouldIterateThroughMapsAndTraverseKeysAndValues() throws Exception {
		runTest(new ClassWithMap(), (object) -> {
			verify(consumer).onObjectFound(object.map, object, "map");
			Entry<PrimitivesClass, ClassWithInnerClass> entry = object.map.entrySet().iterator().next();
			verify(consumer).onObjectFound(entry.getKey(), object.map, "key0");
			verifyPrimitives(entry.getKey());
			verify(consumer).onObjectFound(entry.getValue(), object.map, "value0");
			verify(consumer).onObjectFound(entry.getValue().primitives, entry.getValue(), "primitives");
			verifyPrimitives(entry.getValue().primitives);
		});
	}
	
	@Test
	public void shouldDetectCycles() throws Exception {
		GenericClass gc = new GenericClass();
		gc.object1 = gc;
		gc.object2 = new GenericClass();
		runTest(gc, (object) -> {
			verify(consumer).onObjectFound(gc.object1, gc, "object1");
			verify(consumer).onObjectFound(gc.object2, gc, "object2");
			verify(consumer).onObjectFound(null, gc.object2, "object1");
			verify(consumer).onObjectFound(null, gc.object2, "object2");
		});
	}

	@Test
	public void shouldDetectCyclesInvolvingArrays() throws Exception {
		GenericClass[] gcArray = new GenericClass[2];
		GenericClass gc = new GenericClass();
		gc.object1 = gcArray;
		gcArray[0] = gc;
		gcArray[1] = new GenericClass();
		runTest(gc, (object) -> {
			verify(consumer).onObjectFound(gcArray, object, "object1");
			verify(consumer).onObjectFound(gcArray[0], object.object1, "0");
			verify(consumer).onObjectFound(gcArray[1], object.object1, "1");
			verify(consumer).onObjectFound(null, object, "object2");
			verify(consumer).onObjectFound(null, gcArray[1], "object1");
			verify(consumer).onObjectFound(null, gcArray[1], "object2");
		});
	}

	@Test
	public void shouldDetectCyclesInvolvingCollections() throws Exception {
		List<GenericClass> gcList = new ArrayList<>();
		GenericClass gc = new GenericClass();
		gc.object1 = gcList;
		gcList.add(gc);
		gcList.add(new GenericClass());
		runTest(gc, (object) -> {
			verify(consumer).onObjectFound(gcList, object, "object1");
			verify(consumer).onObjectFound(null, object, "object2");
			verify(consumer).onObjectFound(gcList.get(1), object.object1, "1");
			verify(consumer).onObjectFound(gcList.get(0), object.object1, "0");
			verify(consumer).onObjectFound(null, gcList.get(1), "object1");
			verify(consumer).onObjectFound(null, gcList.get(1), "object2");
		});
	}
	
	@Test
	public void shouldNotDetectCycleOnEqualObjects() throws Exception {
		GenericClass gc = new GenericClass();
		gc.object1 = new EqualClass();
		gc.object2 = new EqualClass();
		runTest(gc, (object) -> {
			verify(consumer).onObjectFound(gc.object1, object, "object1");
			verify(consumer).onObjectFound(gc.object2, object, "object2");
		});
	}
	
	@Test
	public void shouldExcludePrimitivesFromCycleDetection() throws Exception {
		GenericClass gc = new GenericClass();
		gc.object1 = new PrimitivesClass();
		gc.object2 = new PrimitivesClass();
		
		runTest(gc, (object) -> {
			verify(consumer).onObjectFound(gc.object1, object, "object1");
			verify(consumer).onObjectFound(gc.object2, object, "object2");
			verifyPrimitives(gc.object1);
			verifyPrimitives(gc.object2);
		});
	}
	
	@Test
	public void shouldAllowAddingFieldHandlers() throws Exception {
		GenericClass gc = new GenericClass();
		gc.object1 = new IndependentInterface1() {};
		testee.addFieldHandler(independentInterface1Handler);
		testee.traverse(gc, consumer);
		verify(independentInterface1Handler).handleObject(eq(gc.object1), any(), eq(consumer));
		verify(independentInterface1Handler, times(1)).handleObject(any(), any(), any());
	}
	
	@Test
	public void shouldAllowOverridingFieldHandlers() throws Exception {
		GenericClass gc = new GenericClass();
		gc.object1 = new LinkedList<>();
		gc.object2 = new HashSet<>();
		testee.addFieldHandler(collectionHandler);
		testee.traverse(gc, consumer);
		verify(collectionHandler).handleObject(eq(gc.object1), any(), eq(consumer));
		verify(collectionHandler).handleObject(eq(gc.object2), any(), eq(consumer));
		verify(collectionHandler, times(2)).handleObject(any(), any(), any());
	}
	
	@Test
	public void shouldDetermineBestMatchingFieldHandler() throws Exception {
		GenericClass gc = new GenericClass();
		gc.object1 = new MasterInterface() {};
		gc.object2 = new IndependentInterface2() {};
		testee.addFieldHandler(masterInterfaceHandler);
		testee.addFieldHandler(independentInterface2Handler);
		testee.traverse(gc, consumer);
		verify(masterInterfaceHandler).handleObject(eq(gc.object1), any(), eq(consumer));
		verify(independentInterface2Handler).handleObject(eq(gc.object2), any(), eq(consumer));
		verify(masterInterfaceHandler, times(1)).handleObject(any(), any(), any());
		verify(independentInterface2Handler, times(1)).handleObject(any(), any(), any());
	}

	@Test(expected=RuntimeException.class)
	public void shouldThrowAnExceptionIfTwoIndependentInterfacesFieldHandlersAreBestMatch() throws Exception {
		GenericClass gc = new GenericClass();
		gc.object1 = new MasterInterface() {};
		testee.addFieldHandler(independentInterface1Handler);
		testee.addFieldHandler(independentInterface2Handler);
		testee.traverse(gc, consumer);
	}

	@Test
	public void shouldAllowCoveringTwoIndependentInterfaceFieldHandlerssInOneMasterInterfaceFieldHandlers() throws Exception {
		GenericClass gc = new GenericClass();
		gc.object1 = new MasterInterface() {};
		testee.addFieldHandler(independentInterface1Handler);
		testee.addFieldHandler(independentInterface2Handler);
		testee.addFieldHandler(masterInterfaceHandler);
		testee.traverse(gc, consumer);
		verify(masterInterfaceHandler).handleObject(eq(gc.object1), any(), eq(consumer));
		verify(masterInterfaceHandler, times(1)).handleObject(any(), any(), any());
		verify(independentInterface1Handler, times(0)).handleObject(any(), any(), any());
		verify(independentInterface2Handler, times(0)).handleObject(any(), any(), any());
	}
	
	@Test
	public void shouldPrefferMoreSpecificArrayFieldHandlersOverGeneral() throws Exception {
		GenericClass gc = new GenericClass();
		gc.object1 = new GenericClass[0];
		testee.addFieldHandler(goArrayHandler);
		testee.traverse(gc, consumer);
		verify(goArrayHandler).handleObject(eq(gc.object1), any(), eq(consumer));
		verify(goArrayHandler, times(1)).handleObject(any(), any(), any());
	}
	
	private<T> void runTest(T object, Verifier<T> verifications) throws Exception {
		testee.traverse(object, consumer);
		verify(consumer).onObjectFound(object, null, null);
		verifications.verify(object);
		verifyNoMoreInteractions(consumer);
	}
	
	private static interface Verifier<T> {
		public void verify(T object);
	}
	
	private void verifyPrimitives(Object parent) {
		PrimitivesClass compareValues = new PrimitivesClass();
		
		verify(consumer).onObjectFound(compareValues.bigBool, parent, "bigBool");
		verify(consumer).onObjectFound(compareValues.bigByte, parent, "bigByte");
		verify(consumer).onObjectFound(compareValues.bigChar, parent, "bigChar");
		verify(consumer).onObjectFound(compareValues.bigDouble, parent, "bigDouble");
		verify(consumer).onObjectFound(compareValues.bigFloat, parent, "bigFloat");
		verify(consumer).onObjectFound(compareValues.bigInt, parent, "bigInt");
		verify(consumer).onObjectFound(compareValues.bigLong, parent, "bigLong");
		verify(consumer).onObjectFound(compareValues.bigShort, parent, "bigShort");
		verify(consumer).onObjectFound(compareValues.smallBool, parent, "smallBool");
		verify(consumer).onObjectFound(compareValues.smallByte, parent, "smallByte");
		verify(consumer).onObjectFound(compareValues.smallChar, parent, "smallChar");
		verify(consumer).onObjectFound(compareValues.smallDouble, parent, "smallDouble");
		verify(consumer).onObjectFound(compareValues.smallFloat, parent, "smallFloat");
		verify(consumer).onObjectFound(compareValues.smallInt, parent, "smallInt");
		verify(consumer).onObjectFound(compareValues.smallLong, parent, "smallLong");
		verify(consumer).onObjectFound(compareValues.smallShort, parent, "smallShort");
		verify(consumer).onObjectFound(compareValues.string, parent, "string");
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

@SuppressWarnings("all")
class ClassWithList {
	List list = new LinkedList();
	
	ClassWithList() {
		list.add(new PrimitivesClass());
		list.add(new ClassWithInnerClass());
	}
}

@SuppressWarnings("all")
class ClassWithSet {
	Set set = new HashSet();
	
	ClassWithSet() {
		set.add(new PrimitivesClass());
		set.add(new ClassWithInnerClass());
	}
}

class ClassWithMap {
	Map<PrimitivesClass, ClassWithInnerClass> map = new HashMap<>();
	
	ClassWithMap() {
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
	transient int anInt = 42;
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
	public void handleObject(T object, ConfiguredTraverser traverser, Consumer consumer) throws Exception {
	}
}

interface IndependentInterface1 {
	
}

interface IndependentInterface2 {
	
}

interface MasterInterface extends IndependentInterface1, IndependentInterface2 {
	
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