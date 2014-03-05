// code originates from TypeUtilsTest.java in jtype (http://code.google.com/p/jtype/) and has been modified to suite
// the HV requirements and code style
/*
 * Copyright 2009 IIZUKA Software Technologies Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.validator.test.internal.util;

import java.io.Serializable;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintValidator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.util.TypeHelper;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests {@code TypeUtils}.
 *
 * @author Mark Hobson
 * @see org.hibernate.validator.internal.util.TypeHelper
 */
public class TypeHelperTest {
	private static final Type[] DEFAULT_UPPER_BOUNDS = new Type[] { Object.class };
	private static final Type[] DEFAULT_LOWER_BOUNDS = new Type[0];
	private GenericDeclaration declaration;

	private static class IntegerArrayList extends ArrayList<Integer> {
		// simple subclass to fix generics
	}

	private static class IntegerKeyHashMap<V> extends HashMap<Integer, V> {
		// simple subclass to fix generics
	}

	private static class StringsByIntegerHashMap extends IntegerKeyHashMap<String> {
		// simple subclass to fix generics
	}

	@BeforeClass
	public void beforeClass() throws Exception {
		declaration = getClass().getConstructor();
	}

	@Test
	public void isAssignableWithPrimitiveDouble() {
		assertAssignable( Double.TYPE, Double.TYPE );
		assertAsymmetricallyAssignable( Double.TYPE, Float.TYPE );
		assertAsymmetricallyAssignable( Double.TYPE, Long.TYPE );
		assertAsymmetricallyAssignable( Double.TYPE, Integer.TYPE );
		assertAsymmetricallyAssignable( Double.TYPE, Character.TYPE );
		assertAsymmetricallyAssignable( Double.TYPE, Short.TYPE );
		assertAsymmetricallyAssignable( Double.TYPE, Byte.TYPE );
	}

	@Test
	public void isAssignableWithPrimitiveFloat() {
		assertAssignable( Float.TYPE, Float.TYPE );
		assertAsymmetricallyAssignable( Float.TYPE, Long.TYPE );
		assertAsymmetricallyAssignable( Float.TYPE, Integer.TYPE );
		assertAsymmetricallyAssignable( Float.TYPE, Character.TYPE );
		assertAsymmetricallyAssignable( Float.TYPE, Short.TYPE );
		assertAsymmetricallyAssignable( Float.TYPE, Byte.TYPE );
	}

	@Test
	public void isAssignableWithPrimitiveLong() {
		assertAssignable( Long.TYPE, Long.TYPE );
		assertAsymmetricallyAssignable( Long.TYPE, Integer.TYPE );
		assertAsymmetricallyAssignable( Long.TYPE, Character.TYPE );
		assertAsymmetricallyAssignable( Long.TYPE, Short.TYPE );
		assertAsymmetricallyAssignable( Long.TYPE, Byte.TYPE );
	}

	@Test
	public void isAssignableWithPrimitiveInt() {
		assertAssignable( Integer.TYPE, Integer.TYPE );
		assertAsymmetricallyAssignable( Integer.TYPE, Character.TYPE );
		assertAsymmetricallyAssignable( Integer.TYPE, Short.TYPE );
		assertAsymmetricallyAssignable( Integer.TYPE, Byte.TYPE );
	}

	@Test
	public void isAssignableWithPrimitiveShort() {
		assertAssignable( Short.TYPE, Short.TYPE );
		assertAsymmetricallyAssignable( Short.TYPE, Byte.TYPE );
	}

	// JLS 4.10.2 Subtyping among Class and Interface Types

	/**
	 * The direct superclasses of C.
	 */
	@Test
	public void isAssignableWithDirectSuperclassFromParameterizedType() {
		assertAssignable( AbstractList.class, TypeHelper.parameterizedType( ArrayList.class, Integer.class ) );
	}

	/**
	 * The direct superinterfaces of C.
	 */
	@Test
	public void isAssignableWithDirectSuperinterfaceFromParameterizedType() {
		assertAssignable( Collection.class, TypeHelper.parameterizedType( List.class, Integer.class ) );
	}

	/**
	 * The type Object, if C is an interface type with no direct superinterfaces.
	 */
	@Test
	public void isAssignableWithObjectFromInterface() {
		assertAssignable( Object.class, Iterable.class );
	}

	/**
	 * The raw type C.
	 */
	@Test
	public void isAssignableWithRawTypeFromParameterizedType() {
		assertAssignable( List.class, TypeHelper.parameterizedType( List.class, Integer.class ) );
	}

	// TODO: finish 4.10.2

	// JLS 4.10.3 Subtyping among Array Types

	/**
	 * If S and T are both reference types, then S[] >1 T[] iff S >1 T.
	 */
	@Test
	public void isAssignableWithArrayClassFromDirectSubtypeArrayClass() {
		assertAsymmetricallyAssignable( Number[].class, Integer[].class );
	}

	@Test
	public void isAssignableWithArrayClassFromIndirectSubtypeArrayClass() {
		assertAsymmetricallyAssignable( Object[].class, Integer[].class );
	}

	@Test
	public void isAssignableWithArrayClassFromGenericArrayType() {
		assertAssignable( Integer[].class, TypeHelper.genericArrayType( Integer.class ) );
	}

	@Test
	public void isAssignableWithArrayClassFromDirectSubtypeGenericArrayType() {
		assertAsymmetricallyAssignable( Number[].class, TypeHelper.genericArrayType( Integer.class ) );
	}

	@Test
	public void isAssignableWithArrayClassFromIndirectSubtypeGenericArrayType() {
		assertAsymmetricallyAssignable( Object[].class, TypeHelper.genericArrayType( Integer.class ) );
	}

	@Test
	public void isAssignableWithGenericArrayTypeFromDirectSubtypeGenericArrayType() {
		assertAsymmetricallyAssignable(
				TypeHelper.genericArrayType( Number.class ),
				TypeHelper.genericArrayType( Integer.class )
		);
	}

	@Test
	public void isAssignableWithGenericArrayTypeFromIndirectSubtypeGenericArrayType() {
		assertAsymmetricallyAssignable(
				TypeHelper.genericArrayType( Object.class ),
				TypeHelper.genericArrayType( Integer.class )
		);
	}

	@Test
	public void isAssignableWithGenericArrayTypeFromArrayClass() {
		assertAssignable( TypeHelper.genericArrayType( Integer.class ), Integer[].class );
	}

	@Test
	public void isAssignableWithGenericArrayTypeFromDirectSubtypeArrayClass() {
		assertAsymmetricallyAssignable( TypeHelper.genericArrayType( Number.class ), Integer[].class );
	}

	@Test
	public void isAssignableWithGenericArrayTypeFromIndirectSubtypeArrayClass() {
		assertAsymmetricallyAssignable( TypeHelper.genericArrayType( Object.class ), Integer[].class );
	}

	/**
	 * Object >1 Object[].
	 */
	@Test
	public void isAssignableWithObjectFromObjectArrayClass() {
		assertAsymmetricallyAssignable( Object.class, Object[].class );
	}

	@Test
	public void isAssignableWithObjectFromArrayClass() {
		assertAsymmetricallyAssignable( Object.class, Integer[].class );
	}

	@Test
	public void isAssignableWithObjectFromObjectGenericArrayType() {
		assertAsymmetricallyAssignable( Object.class, TypeHelper.genericArrayType( Object.class ) );
	}

	@Test
	public void isAssignableWithObjectFromGenericArrayType() {
		assertAsymmetricallyAssignable( Object.class, TypeHelper.genericArrayType( Integer.class ) );
	}

	/**
	 * Cloneable >1 Object[].
	 */
	@Test
	public void isAssignableWithCloneableFromObjectArrayClass() {
		assertAsymmetricallyAssignable( Cloneable.class, Object[].class );
	}

	@Test
	public void isAssignableWithCloneableFromArrayClass() {
		assertAsymmetricallyAssignable( Cloneable.class, Integer[].class );
	}

	@Test
	public void isAssignableWithCloneableFromObjectGenericArrayType() {
		assertAsymmetricallyAssignable( Cloneable.class, TypeHelper.genericArrayType( Object.class ) );
	}

	@Test
	public void isAssignableWithCloneableFromGenericArrayType() {
		assertAsymmetricallyAssignable( Cloneable.class, TypeHelper.genericArrayType( Integer.class ) );
	}

	/**
	 * java.io.Serializable >1 Object[].
	 */
	@Test
	public void isAssignableWithSerializableFromObjectArrayClass() {
		assertAsymmetricallyAssignable( Serializable.class, Object[].class );
	}

	@Test
	public void isAssignableWithSerializableFromArrayClass() {
		assertAsymmetricallyAssignable( Serializable.class, Integer[].class );
	}

	@Test
	public void isAssignableWithSerializableFromObjectGenericArrayType() {
		assertAsymmetricallyAssignable( Serializable.class, TypeHelper.genericArrayType( Object.class ) );
	}

	@Test
	public void isAssignableWithSerializableFromGenericArrayType() {
		assertAsymmetricallyAssignable( Serializable.class, TypeHelper.genericArrayType( Integer.class ) );
	}

	/**
	 * If p is a primitive type, then Object >1 p[].
	 */
	@Test
	public void isAssignableWithObjectFromPrimitiveArray() {
		assertAsymmetricallyAssignable( Object.class, int[].class );
	}

	/**
	 * If p is a primitive type, then Cloneable >1 p[].
	 */
	@Test
	public void isAssignableWithCloneableFromPrimitiveArray() {
		assertAsymmetricallyAssignable( Cloneable.class, int[].class );
	}

	/**
	 * If p is a primitive type, then java.io.Serializable >1 p[].
	 */
	@Test
	public void isAssignableWithSerializableFromPrimitiveArray() {
		assertAsymmetricallyAssignable( Serializable.class, int[].class );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void isAssignableWithNullSupertype() {
		assertAssignable( null, Integer.class );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void isAssignableWithNullType() {
		assertAssignable( Integer.class, null );
	}

	/**
	 * Tests that classes are assignable to their direct superclasses.
	 *
	 * {@literal Number <: Integer}
	 */
	@Test
	public void isAssignableWithClassFromDirectSubclass() {
		assertAsymmetricallyAssignable( Number.class, Integer.class );
	}

	/**
	 * Tests that classes are assignable to their indirect superclasses.
	 *
	 * {@literal Object <: Integer}
	 */
	@Test
	public void isAssignableWithClassFromIndirectSubclass() {
		assertAsymmetricallyAssignable( Object.class, Integer.class );
	}

	/**
	 * Tests that parameterized types are assignable to their raw types.
	 *
	 * {@literal List <: List<Integer>}
	 */
	@Test
	public void isAssignableWithClassFromParameterizedType() {
		assertAsymmetricallyAssignable( List.class, TypeHelper.parameterizedType( List.class, Integer.class ) );
	}

	/**
	 * Tests that parameterized types are assignable if their raw types are directly assignable.
	 *
	 * {@literal Collection<Integer> <: List<Integer>}
	 */
	@Test
	public void isAssignableWithDirectlyAssignableParameterizedTypeRawTypes() {
		assertAsymmetricallyAssignable(
				TypeHelper.parameterizedType( Collection.class, Integer.class ),
				TypeHelper.parameterizedType( List.class, Integer.class )
		);
	}

	/**
	 * Tests that parameterized types are assignable if their raw types are indirectly assignable.
	 *
	 * {@literal Collection<Integer> <: ArrayList<Integer>}
	 */
	@Test
	public void isAssignableWithIndirectlyAssignableParameterizedTypeRawTypes() {
		assertAsymmetricallyAssignable(
				TypeHelper.parameterizedType( Collection.class, Integer.class ),
				TypeHelper.parameterizedType( ArrayList.class, Integer.class )
		);
	}

	/**
	 * Tests that parameterized types are not assignable if their raw types are not assignable.
	 *
	 * {@literal List<Integer> !<: Set<Integer>}
	 */
	@Test
	public void isAssignableWithUnassignableParameterizedTypeRawTypes() {
		assertUnassignable(
				TypeHelper.parameterizedType( List.class, Integer.class ),
				TypeHelper.parameterizedType( Set.class, Integer.class )
		);

		assertUnassignable(
				TypeHelper.parameterizedType( Set.class, Integer.class ),
				TypeHelper.parameterizedType( List.class, Integer.class )
		);
	}

	/**
	 * Tests that parameterized types are not assignable even if their type arguments are assignable.
	 *
	 * {@literal List<Number> !<: List<Integer>}
	 */
	@Test
	public void isAssignableWithAssignableParameterizedTypeArguments() {
		assertUnassignable(
				TypeHelper.parameterizedType( List.class, Number.class ),
				TypeHelper.parameterizedType( List.class, Integer.class )
		);

		assertUnassignable(
				TypeHelper.parameterizedType( List.class, Integer.class ),
				TypeHelper.parameterizedType( List.class, Number.class )
		);
	}

	/**
	 * Tests that parameterized type arguments are assignable to wildcard types.
	 *
	 * {@literal List<?> <: List<Integer>}
	 */
	@Test
	public void isAssignableWithWildcardParameterizedTypeFromParameterizedType() {
		assertAsymmetricallyAssignable(
				TypeHelper.parameterizedType( List.class, wildcardType() ),
				TypeHelper.parameterizedType( List.class, Integer.class )
		);
	}

	/**
	 * Tests that parameterized type upper bounded wildcard type arguments are assignable to wildcard types.
	 *
	 * {@literal List<?> <: List<? extends Number>}
	 */
	@Test
	public void isAssignableWithWildcardParameterizedTypeFromUpperBoundedWildcardParameterizedType() {
		assertAsymmetricallyAssignable(
				TypeHelper.parameterizedType( List.class, wildcardType() ),
				TypeHelper.parameterizedType(
						List.class,
						wildcardTypeUpperBounded( new Type[] { Number.class } )
				)
		);
	}

	/**
	 * Tests that parameterized type arguments are assignable to wildcard types on their upper bound.
	 *
	 * {@literal List<? extends Number> <: List<Number>}
	 */
	@Test
	public void isAssignableWithUpperBoundedWildcardParameterizedTypeFromParameterizedType() {
		assertAsymmetricallyAssignable(
				TypeHelper.parameterizedType(
						List.class,
						wildcardTypeLowerBounded( new Type[] { Number.class } )
				),
				TypeHelper.parameterizedType( List.class, Number.class )

		);
	}

	/**
	 * Tests that parameterized type arguments are assignable to wildcard types within their upper bound.
	 *
	 * {@literal List<? extends Number> <: List<Integer>}
	 */
	@Test
	public void isAssignableWithUpperBoundedWildcardParameterizedTypeFromInBoundsParameterizedType() {
		assertAsymmetricallyAssignable(
				TypeHelper.parameterizedType(
						List.class,
						wildcardTypeUpperBounded( new Type[] { Number.class } )
				),
				TypeHelper.parameterizedType( List.class, Integer.class )

		);
	}

	/**
	 * Tests that parameterized type arguments are not assignable to wildcard types outside of their upper bound.
	 *
	 * {@literal List<? extends Number> !<: List<Object>}
	 */
	@Test
	public void isAssignableWithUpperBoundedWildcardParameterizedTypeFromOutOfBoundsParameterizedType() {
		assertUnassignable(
				TypeHelper.parameterizedType(
						List.class,
						wildcardTypeUpperBounded( new Type[] { Number.class } )
				),
				TypeHelper.parameterizedType( List.class, Object.class )

		);

		assertUnassignable(
				TypeHelper.parameterizedType( List.class, Object.class ),
				TypeHelper.parameterizedType(
						List.class,
						wildcardTypeLowerBounded( new Type[] { Number.class } )
				)
		);
	}

	/**
	 * {@literal List<? extends Number> <: List<? extends Integer>}
	 */
	@Test
	public void isAssignableWithUpperBoundedWildcardParameterizedTypeFromInBoundsUpperBoundedWildcardParameterizedType() {
		assertAsymmetricallyAssignable(
				TypeHelper.parameterizedType(
						List.class,
						wildcardTypeLowerBounded( new Type[] { Number.class } )
				),
				TypeHelper.parameterizedType(
						List.class,
						wildcardTypeLowerBounded( new Type[] { Integer.class } )
				)

		);
	}

	/**
	 * Tests that parameterized type arguments are assignable to wildcard types on their lower bound.
	 *
	 * {@literal List<? super Number> <: List<Number>}
	 */
	@Test
	public void isAssignableWithLowerBoundedWildcardParameterizedTypeFromParameterizedType() {
		assertAsymmetricallyAssignable(
				TypeHelper.parameterizedType(
						List.class,
						wildcardTypeLowerBounded( new Type[] { Number.class } )
				),
				TypeHelper.parameterizedType(
						List.class,
						Number.class
				)

		);
	}

	/**
	 * Tests that parameterized type arguments are assignable to wildcard types within their lower bound.
	 *
	 * {@literal List<? super Number> <: List<Object>}
	 */
	@Test
	public void isAssignableWithLowerBoundedWildcardParameterizedTypeFromInBoundsParameterizedType() {
		assertAsymmetricallyAssignable(
				TypeHelper.parameterizedType(
						List.class,
						wildcardTypeLowerBounded( new Type[] { Number.class } )
				),
				TypeHelper.parameterizedType(
						List.class,
						Object.class
				)

		);
	}

	/**
	 * Tests that parameterized type arguments are assignable to wildcard types outside of their lower bound.
	 *
	 * {@literal List<? super Number> !<: List<Integer>}
	 */
	@Test
	public void isAssignableWithLowerBoundedWildcardParameterizedTypeFromOutOfBoundsParameterizedType() {
		assertUnassignable(
				TypeHelper.parameterizedType(
						List.class,
						wildcardTypeLowerBounded( new Type[] { Number.class } )
				),
				TypeHelper.parameterizedType(
						List.class,
						Integer.class
				)

		);

		assertUnassignable(
				TypeHelper.parameterizedType(
						List.class,
						Integer.class
				),
				TypeHelper.parameterizedType(
						List.class,
						wildcardTypeLowerBounded( new Type[] { Number.class } )
				)
		);
	}

	/**
	 * Tests that classes are assignable to parameterized supertypes.
	 *
	 * {@literal List<Integer> <: IntegerArrayList}
	 */
	@Test
	public void isAssignableWithParameterizedTypeFromClassWithActualTypeArguments() {
		assertAsymmetricallyAssignable(
				TypeHelper.parameterizedType( List.class, Integer.class ),
				IntegerArrayList.class
		);
	}

	@Test
	public void isAssignableWithUnboundedWildcardParameterizedTypeFromClass() {

		assertAsymmetricallyAssignable(
				TypeHelper.parameterizedType( List.class, wildcardType() ),
				ArrayList.class
		);
	}

	@Test
	public void isAssignableWithUnboundedWildcardParameterizedTypeFromClassWithActualTypeArguments() {
		assertAsymmetricallyAssignable(
				TypeHelper.parameterizedType(
						Map.class,
						wildcardType(),
						wildcardType()
				),
				StringsByIntegerHashMap.class
		);
	}

	/**
	 * Tests that unbounded type variables are assignable to Object.
	 *
	 * {@literal Object <: T}
	 */
	@Test
	public void isAssignableWithObjectFromTypeVariableWithNoBounds() {
		assertAssignable( Object.class, typeVariable( declaration, "T" ) );
	}

	/**
	 * Tests that type variables with a single bound are assignable to their bound.
	 *
	 * {@literal Number <: T extends Number}
	 */
	@Test
	public void isAssignableWithBoundFromTypeVariableWithBound() {
		assertAssignable( Object.class, typeVariable( declaration, "T", Number.class ) );
	}

	/**
	 * Tests that type variables with a single bound are not assignable to subtypes of their bound.
	 *
	 * {@literal Integer !<: T extends Number}
	 */
	@Test
	public void isAssignableWithTypeOutsideOfBoundFromTypeVariableWithBound() {
		assertUnassignable( Integer.class, typeVariable( declaration, "T", Number.class ) );
	}

	/**
	 * Tests that type variables with a single bound are assignable to supertypes of their bound.
	 *
	 * {@literal Number <: T extends Integer}
	 */
	@Test
	public void isAssignableWithTypeInsideOfBoundFromTypeVariableWithBound() {
		assertAssignable( Number.class, typeVariable( declaration, "T", Integer.class ) );
	}

	/**
	 * Tests that type variables with multiple bounds are assignable to their bounds.
	 *
	 * {@literal Number, Collection <: T extends Number & Collection}
	 */
	@Test
	public void isAssignableWithBoundsFromTypeVariableWithBounds() {
		TypeVariable<?> type = typeVariable( declaration, "T", Number.class, Collection.class );

		assertAssignable( Number.class, type );
		assertAssignable( Collection.class, type );
	}

	/**
	 * Tests that type variables with multiple bounds are not assignable to supertypes of their bounds.
	 *
	 * {@literal Integer, Thread !<: T extends Number & Runnable}
	 */
	@Test
	public void isAssignableWithTypeOutsideOfBoundsFromTypeVariableWithBounds() {
		TypeVariable<?> type = typeVariable( declaration, "T", Number.class, Collection.class );

		assertUnassignable( Integer.class, type );
		assertUnassignable( List.class, type );
	}

	/**
	 * Tests that type variables with multiple bounds are assignable to subtypes of their bounds.
	 *
	 * {@literal Number, Collection <: T extends Integer & List}
	 */
	@Test
	public void isAssignableWithTypeInsideOfBoundsFromTypeVariableWithBounds() {
		TypeVariable<?> type = typeVariable( declaration, "T", Integer.class, List.class );

		assertAssignable( Number.class, type );
		assertAssignable( Collection.class, type );
	}

	/**
	 * Tests that unbounded wildcards are assignable to Object.
	 *
	 * {@literal Object <: ?}
	 */
	@Test
	public void isAssignableWithObjectFromUnboundedWildcardType() {
		assertAssignable( Object.class, wildcardType() );
	}

	/**
	 * Tests that upper bounded wildcards are assignable to their upper bound.
	 *
	 * {@literal Number <: ? extends Number}
	 */
	@Test
	public void isAssignableWithBoundFromUpperBoundedWildcardType() {
		assertAssignable( Number.class, wildcardTypeUpperBounded( new Type[] { Number.class } ) );
	}

	/**
	 * Tests that upper bounded wildcards are assignable to supertypes of their upper bound.
	 *
	 * {@literal Number <: ? extends Integer}
	 */
	@Test
	public void isAssignableWithBoundSupertypeFromUpperBoundedWildcardType() {
		assertAssignable( Number.class, wildcardTypeUpperBounded( new Type[] { Integer.class } ) );
	}

	/**
	 * Tests that upper bounded wildcards are not assignable to subtypes of their upper bound.
	 *
	 * {@literal Integer !<: ? extends Number}
	 */
	@Test
	public void isAssignableWithBoundSubtypeFromUpperBoundedWildcardType() {
		assertUnassignable( Integer.class, wildcardTypeUpperBounded( new Type[] { Number.class } ) );
	}

	/**
	 * Tests that lower bounded wildcards are assignable to Object.
	 *
	 * {@literal Object <: ? super Number}
	 */
	@Test
	public void isAssignableWithObjectFromLowerBoundedWildcardType() {
		assertAssignable( Object.class, wildcardTypeLowerBounded( new Type[] { Number.class } ) );
	}

	/**
	 * Tests that lower bounded wildcards are not assignable to their lower bound.
	 *
	 * {@literal Number !<: ? super Number}
	 */
	@Test
	public void isAssignableWithBoundFromLowerBoundedWildcardType() {
		assertUnassignable( Number.class, wildcardTypeLowerBounded( new Type[] { Number.class } ) );
	}

	/**
	 * Tests that lower bounded wildcards are not assignable to supertypes of their lower bound.
	 *
	 * {@literal Number !<: ? super Integer}
	 */
	@Test
	public void isAssignableWithBoundSupertypeFromLowerBoundedWildcardType() {
		assertUnassignable( Number.class, wildcardTypeLowerBounded( new Type[] { Integer.class } ) );
	}

	/**
	 * Tests that upper bounded wildcards are not assignable to subtypes of their upper bound.
	 *
	 * {@literal Integer <: ? super Number}
	 */
	@Test
	public void isAssignableWithBoundSubtypeFromLowerBoundedWildcardType() {
		assertUnassignable( Integer.class, wildcardTypeLowerBounded( new Type[] { Number.class } ) );
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void isInstanceWithNullType() {
		TypeHelper.isInstance( null, 123 );
	}

	@Test
	public void isInstanceWithClass() {
		assertTrue( TypeHelper.isInstance( Integer.class, 123 ) );
	}

	@Test
	public void isInstanceWithClassAndSubclass() {
		assertTrue( TypeHelper.isInstance( Number.class, 123 ) );
	}

	@Test
	public void isInstanceWithClassAndSuperclass() {
		assertFalse( TypeHelper.isInstance( Number.class, new Object() ) );
	}

	@Test
	public void isInstanceWithClassAndDisjointClass() {
		assertFalse( TypeHelper.isInstance( Integer.class, 123L ) );
	}

	@Test
	public void isInstanceWithClassAndNull() {
		assertFalse( TypeHelper.isInstance( Integer.class, null ) );
	}

	@Test
	public void isInstanceWithClassArray() {
		assertTrue( TypeHelper.isInstance( Integer[].class, new Integer[0] ) );
	}

	@Test
	public void isInstanceWithClassArrayAndSubclassArray() {
		assertTrue( TypeHelper.isInstance( Number[].class, new Integer[0] ) );
	}

	@Test
	public void isInstanceWithClassArrayAndSuperclassArray() {
		assertFalse( TypeHelper.isInstance( Number[].class, new Object[0] ) );
	}

	@Test
	public void isInstanceWithClassArrayAndDisjointClass() {
		assertFalse( TypeHelper.isInstance( Integer[].class, new Long[0] ) );
	}

	@Test
	public void isArrayWithNull() {
		assertFalse( TypeHelper.isArray( null ) );
	}

	@Test
	public void isArrayWithClass() {
		assertFalse( TypeHelper.isArray( Integer.class ) );
	}

	@Test
	public void isArrayWithClassArray() {
		assertTrue( TypeHelper.isArray( Integer[].class ) );
	}

	@Test
	public void isArrayWithGenericArrayType() {
		assertTrue( TypeHelper.isArray( TypeHelper.genericArrayType( Integer.class ) ) );
	}

	@Test
	public void isArrayWithParameterizedType() {
		assertFalse( TypeHelper.isArray( TypeHelper.parameterizedType( List.class, Integer.class ) ) );
	}

	@Test
	public void testTypeDiscovery() {
		List<Class<? extends ConstraintValidator<Positive, ?>>> validators = newArrayList();
		validators.add( PositiveConstraintValidator.class );
		Map<Type, Class<? extends ConstraintValidator<Positive, ?>>> validatorsTypes = TypeHelper
				.getValidatorsTypes( Positive.class, validators );

		assertEquals( validatorsTypes.get( Integer.class ), PositiveConstraintValidator.class );
		assertNull( validatorsTypes.get( String.class ) );
	}

	private static void assertAsymmetricallyAssignable(Type supertype, Type type) {
		assertAssignable( supertype, type );
		assertUnassignable( type, supertype );
	}

	private static void assertAssignable(Type supertype, Type type) {
		assertTrue( TypeHelper.isAssignable( supertype, type ), "Expected " + type + " assignable to " + supertype );
	}

	private static void assertUnassignable(Type supertype, Type type) {
		assertFalse(
				TypeHelper.isAssignable( supertype, type ), "Expected " + type + " not assignable to " + supertype
		);
	}

	private static WildcardType wildcardTypeUpperBounded(final Type[] upperBounds) {
		return wildcardType( upperBounds, DEFAULT_LOWER_BOUNDS );
	}

	private static WildcardType wildcardTypeLowerBounded(final Type[] lowerBounds) {
		return wildcardType( DEFAULT_UPPER_BOUNDS, lowerBounds );
	}

	private static WildcardType wildcardType() {
		return wildcardType( DEFAULT_UPPER_BOUNDS, DEFAULT_LOWER_BOUNDS );
	}

	private static WildcardType wildcardType(final Type[] upperBounds, final Type[] lowerBounds) {
		return new WildcardType() {

			@Override
			public Type[] getUpperBounds() {
				return upperBounds;
			}

			@Override
			public Type[] getLowerBounds() {
				return lowerBounds;
			}
		};
	}

	private static TypeVariable<GenericDeclaration> typeVariable(final GenericDeclaration declaration,
			final String name,
			final Type... bounds) {

		Class<?>[] interfaces = { TypeVariable.class };

		// HV-871 Implementing TypeVariable via a dynamic proxy to ensure this code can be compiled with Java 7 and 8;
		// New methods have been added to the TypeVariable interface in Java 8; To ensure compatibility with Java 7, we
		// don't directly implement these as they use types which themselves have been added in Java 8
		@SuppressWarnings("unchecked")
		TypeVariable<GenericDeclaration> typeVariable = (TypeVariable<GenericDeclaration>) Proxy.newProxyInstance(
				TypeHelperTest.class.getClassLoader(),
				interfaces,
				new TypeVariableImpl( bounds, declaration, name )
		);

		return typeVariable;
	}

	private static class TypeVariableImpl implements InvocationHandler {

		private final Type[] bounds;
		GenericDeclaration declaration;
		String name;

		public TypeVariableImpl(Type[] bounds, GenericDeclaration declaration, String name) {
			this.bounds = bounds;
			this.declaration = declaration;
			this.name = name;
		}

		public Type[] getBounds() {
			if ( bounds == null || bounds.length == 0 ) {
				return new Type[] { Object.class };
			}
			return bounds;
		}

		public GenericDeclaration getGenericDeclaration() {
			return declaration;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return "TypeVariableImpl [bounds=" + Arrays.toString( bounds )
					+ ", declaration=" + declaration + ", name=" + name + "]";
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if ( method.getName().equals( "getBounds" ) ) {
				return getBounds();
			}
			else if ( method.getName().equals( "getGenericDeclaration" ) ) {
				return getGenericDeclaration();
			}
			else if ( method.getName().equals( "getName" ) ) {
				return getName();
			}
			else if ( method.getName().equals( "toString" ) ) {
				return toString();
			}
			else {
				throw new UnsupportedOperationException( "Method " + method + " is not implemented." );
			}
		}
	}
}
