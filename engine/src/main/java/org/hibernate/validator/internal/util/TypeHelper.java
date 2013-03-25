// code originates from TypeUtils.java in jtype (http://code.google.com/p/jtype/) and has been modified to suite
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
package org.hibernate.validator.internal.util;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.validation.ConstraintValidator;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

/**
 * Provides utility methods for working with types.
 *
 * @author Mark Hobson
 * @author Hardy Ferentschik
 */
public final class TypeHelper {
	private static final Map<Class<?>, Set<Class<?>>> SUBTYPES_BY_PRIMITIVE;
	private static final int VALIDATOR_TYPE_INDEX = 1;
	private static final Log log = LoggerFactory.make();

	static {
		Map<Class<?>, Set<Class<?>>> subtypesByPrimitive = newHashMap();

		putPrimitiveSubtypes( subtypesByPrimitive, Void.TYPE );
		putPrimitiveSubtypes( subtypesByPrimitive, Boolean.TYPE );
		putPrimitiveSubtypes( subtypesByPrimitive, Byte.TYPE );
		putPrimitiveSubtypes( subtypesByPrimitive, Character.TYPE );
		putPrimitiveSubtypes( subtypesByPrimitive, Short.TYPE, Byte.TYPE );
		putPrimitiveSubtypes( subtypesByPrimitive, Integer.TYPE, Character.TYPE, Short.TYPE );
		putPrimitiveSubtypes( subtypesByPrimitive, Long.TYPE, Integer.TYPE );
		putPrimitiveSubtypes( subtypesByPrimitive, Float.TYPE, Long.TYPE );
		putPrimitiveSubtypes( subtypesByPrimitive, Double.TYPE, Float.TYPE );

		SUBTYPES_BY_PRIMITIVE = Collections.unmodifiableMap( subtypesByPrimitive );
	}

	private TypeHelper() {
		throw new AssertionError();
	}

	public static boolean isAssignable(Type supertype, Type type) {
		Contracts.assertNotNull( supertype, "supertype" );
		Contracts.assertNotNull( type, "type" );

		if ( supertype.equals( type ) ) {
			return true;
		}

		if ( supertype instanceof Class<?> ) {
			if ( type instanceof Class<?> ) {
				return isClassAssignable( (Class<?>) supertype, (Class<?>) type );
			}

			if ( type instanceof ParameterizedType ) {
				return isAssignable( supertype, ( (ParameterizedType) type ).getRawType() );
			}

			if ( type instanceof TypeVariable<?> ) {
				return isTypeVariableAssignable( supertype, (TypeVariable<?>) type );
			}

			if ( type instanceof GenericArrayType ) {
				if ( ( (Class<?>) supertype ).isArray() ) {
					return isAssignable( getComponentType( supertype ), getComponentType( type ) );
				}

				return isArraySupertype( (Class<?>) supertype );
			}

			if ( type instanceof WildcardType ) {
				return isClassAssignableToWildcardType( (Class<?>) supertype, (WildcardType) type );
			}

			return false;
		}

		if ( supertype instanceof ParameterizedType ) {
			if ( type instanceof Class<?> ) {
				return isSuperAssignable( supertype, type );
			}

			if ( type instanceof ParameterizedType ) {
				return isParameterizedTypeAssignable( (ParameterizedType) supertype, (ParameterizedType) type );
			}

			return false;
		}

		if ( type instanceof TypeVariable<?> ) {
			return isTypeVariableAssignable( supertype, (TypeVariable<?>) type );
		}

		if ( supertype instanceof GenericArrayType ) {
			if ( isArray( type ) ) {
				return isAssignable( getComponentType( supertype ), getComponentType( type ) );
			}

			return false;
		}

		if ( supertype instanceof WildcardType ) {
			return isWildcardTypeAssignable( (WildcardType) supertype, type );
		}

		return false;
	}

	/**
	 * Gets the erased type of the specified type.
	 *
	 * @param type the type to perform erasure on
	 *
	 * @return the erased type, never a parameterized type nor a type variable
	 *
	 * @see <a href="http://java.sun.com/docs/books/jls/third_edition/html/typesValues.html#4.6">4.6 Type Erasure</a>
	 */
	public static Type getErasedType(Type type) {
		// the erasure of a parameterized type G<T1, ... ,Tn> is |G|
		if ( type instanceof ParameterizedType ) {
			Type rawType = ( (ParameterizedType) type ).getRawType();

			return getErasedType( rawType );
		}

		// TODO: the erasure of a nested type T.C is |T|.C

		// the erasure of an array type T[] is |T|[]
		if ( isArray( type ) ) {
			Type componentType = getComponentType( type );
			Type erasedComponentType = getErasedType( componentType );

			return getArrayType( erasedComponentType );
		}

		// the erasure of a type variable is the erasure of its leftmost bound
		if ( type instanceof TypeVariable<?> ) {
			Type[] bounds = ( (TypeVariable<?>) type ).getBounds();

			return getErasedType( bounds[0] );
		}

		// the erasure of every other type is the type itself
		return type;
	}

	private static Class<?> getErasedReferenceType(Type type) {
		Contracts.assertTrue( isReferenceType( type ), "type is not a reference type: " + type );
		return (Class<?>) getErasedType( type );
	}

	public static boolean isArray(Type type) {
		return ( type instanceof Class<?> && ( (Class<?>) type ).isArray() )
				|| ( type instanceof GenericArrayType );
	}

	public static Type getComponentType(Type type) {
		if ( type instanceof Class<?> ) {
			Class<?> klass = (Class<?>) type;

			return klass.isArray() ? klass.getComponentType() : null;
		}

		if ( type instanceof GenericArrayType ) {
			return ( (GenericArrayType) type ).getGenericComponentType();
		}

		return null;
	}

	private static Type getArrayType(Type componentType) {
		Contracts.assertNotNull( componentType, "componentType" );

		if ( componentType instanceof Class<?> ) {
			return Array.newInstance( (Class<?>) componentType, 0 ).getClass();
		}

		return genericArrayType( componentType );
	}

	/**
	 * Creates a generic array type for the specified component type.
	 *
	 * @param componentType the component type
	 *
	 * @return the generic array type
	 */
	public static GenericArrayType genericArrayType(final Type componentType) {
		return new GenericArrayType() {

			@Override
			public Type getGenericComponentType() {
				return componentType;
			}
		};
	}

	public static boolean isInstance(Type type, Object object) {
		return getErasedReferenceType( type ).isInstance( object );
	}

	/**
	 * Creates a parameterized type for the specified raw type and actual type arguments.
	 *
	 * @param rawType the raw type
	 * @param actualTypeArguments the actual type arguments
	 *
	 * @return the parameterized type
	 *
	 * @throws MalformedParameterizedTypeException if the number of actual type arguments differs from those defined on the raw type
	 */
	public static ParameterizedType parameterizedType(final Class<?> rawType, final Type... actualTypeArguments) {
		return new ParameterizedType() {
			@Override
			public Type[] getActualTypeArguments() {
				return actualTypeArguments;
			}

			@Override
			public Type getRawType() {
				return rawType;
			}

			@Override
			public Type getOwnerType() {
				return null;
			}
		};
	}

	private static Type getResolvedSuperclass(Type type) {
		Contracts.assertNotNull( type, "type" );

		Class<?> rawType = getErasedReferenceType( type );
		Type supertype = rawType.getGenericSuperclass();

		if ( supertype == null ) {
			return null;
		}

		return resolveTypeVariables( supertype, type );
	}

	private static Type[] getResolvedInterfaces(Type type) {
		Contracts.assertNotNull( type, "type" );

		Class<?> rawType = getErasedReferenceType( type );
		Type[] interfaces = rawType.getGenericInterfaces();
		Type[] resolvedInterfaces = new Type[interfaces.length];

		for ( int i = 0; i < interfaces.length; i++ ) {
			resolvedInterfaces[i] = resolveTypeVariables( interfaces[i], type );
		}

		return resolvedInterfaces;
	}

	/**
	 * @param validators List of constraint validator classes (for a given constraint).
	 *
	 * @return Return a Map&lt;Class, Class&lt;? extends ConstraintValidator&gt;&gt; where the map
	 *         key is the type the validator accepts and value the validator class itself.
	 */
	public static <A extends Annotation> Map<Type, Class<? extends ConstraintValidator<A, ?>>> getValidatorsTypes(
			Class<A> annotationType,
			List<Class<? extends ConstraintValidator<A, ?>>> validators) {
		Map<Type, Class<? extends ConstraintValidator<A, ?>>> validatorsTypes =
				newHashMap();
		for ( Class<? extends ConstraintValidator<A, ?>> validator : validators ) {
			Type type = extractType( validator );

			if ( validatorsTypes.containsKey( type ) ) {
				throw log.getMultipleValidatorsForSameTypeException( annotationType.getName(), type.toString() );
			}

			validatorsTypes.put( type, validator );
		}
		return validatorsTypes;

	}

	private static Type extractType(Class<? extends ConstraintValidator<?, ?>> validator) {
		Map<Type, Type> resolvedTypes = newHashMap();
		Type constraintValidatorType = resolveTypes( resolvedTypes, validator );

		//we now have all bind from a type to its resolution at one level
		Type validatorType = ( (ParameterizedType) constraintValidatorType ).getActualTypeArguments()[VALIDATOR_TYPE_INDEX];
		if ( validatorType == null ) {
			throw log.getNullIsAnInvalidTypeForAConstraintValidatorException();
		}
		else if ( validatorType instanceof GenericArrayType ) {
			validatorType = TypeHelper.getArrayType( TypeHelper.getComponentType( validatorType ) );
		}

		while ( resolvedTypes.containsKey( validatorType ) ) {
			validatorType = resolvedTypes.get( validatorType );
		}
		//FIXME raise an exception if validatorType is not a class
		return validatorType;
	}

	private static Type resolveTypes(Map<Type, Type> resolvedTypes, Type type) {
		if ( type == null ) {
			return null;
		}
		else if ( type instanceof Class ) {
			Class<?> clazz = (Class<?>) type;
			final Type returnedType = resolveTypeForClassAndHierarchy( resolvedTypes, clazz );
			if ( returnedType != null ) {
				return returnedType;
			}
		}
		else if ( type instanceof ParameterizedType ) {
			ParameterizedType paramType = (ParameterizedType) type;
			if ( !( paramType.getRawType() instanceof Class ) ) {
				return null; //don't know what to do here
			}
			Class<?> rawType = (Class<?>) paramType.getRawType();

			TypeVariable<?>[] originalTypes = rawType.getTypeParameters();
			Type[] partiallyResolvedTypes = paramType.getActualTypeArguments();
			int nbrOfParams = originalTypes.length;
			for ( int i = 0; i < nbrOfParams; i++ ) {
				resolvedTypes.put( originalTypes[i], partiallyResolvedTypes[i] );
			}

			if ( rawType.equals( ConstraintValidator.class ) ) {
				//we found our baby
				return type;
			}
			else {
				Type returnedType = resolveTypeForClassAndHierarchy( resolvedTypes, rawType );
				if ( returnedType != null ) {
					return returnedType;
				}
			}
		}
		//else we don't care I think
		return null;
	}

	private static Type resolveTypeForClassAndHierarchy(Map<Type, Type> resolvedTypes, Class<?> clazz) {
		Type returnedType = resolveTypes( resolvedTypes, clazz.getGenericSuperclass() );
		if ( returnedType != null ) {
			return returnedType;
		}
		for ( Type genericInterface : clazz.getGenericInterfaces() ) {
			returnedType = resolveTypes( resolvedTypes, genericInterface );
			if ( returnedType != null ) {
				return returnedType;
			}
		}
		return null;
	}

	private static void putPrimitiveSubtypes(Map<Class<?>, Set<Class<?>>> subtypesByPrimitive, Class<?> primitiveType,
											 Class<?>... directSubtypes) {
		Set<Class<?>> subtypes = newHashSet();

		for ( Class<?> directSubtype : directSubtypes ) {
			subtypes.add( directSubtype );
			subtypes.addAll( subtypesByPrimitive.get( directSubtype ) );
		}

		subtypesByPrimitive.put( primitiveType, Collections.unmodifiableSet( subtypes ) );
	}

	private static boolean isClassAssignable(Class<?> supertype, Class<?> type) {
		// Class.isAssignableFrom does not perform primitive widening
		if ( supertype.isPrimitive() && type.isPrimitive() ) {
			return SUBTYPES_BY_PRIMITIVE.get( supertype ).contains( type );
		}

		return supertype.isAssignableFrom( type );
	}

	private static boolean isClassAssignableToWildcardType(Class<?> supertype, WildcardType type) {
		for ( Type upperBound : type.getUpperBounds() ) {
			if ( !isAssignable( supertype, upperBound ) ) {
				return false;
			}
		}

		return true;
	}

	private static boolean isParameterizedTypeAssignable(ParameterizedType supertype, ParameterizedType type) {
		Type rawSupertype = supertype.getRawType();
		Type rawType = type.getRawType();

		if ( !rawSupertype.equals( rawType ) ) {
			// short circuit when class raw types are unassignable
			if ( rawSupertype instanceof Class<?> && rawType instanceof Class<?>
					&& !( ( (Class<?>) rawSupertype ).isAssignableFrom( (Class<?>) rawType ) ) ) {
				return false;
			}

			return isSuperAssignable( supertype, type );
		}

		Type[] supertypeArgs = supertype.getActualTypeArguments();
		Type[] typeArgs = type.getActualTypeArguments();

		if ( supertypeArgs.length != typeArgs.length ) {
			return false;
		}

		for ( int i = 0; i < supertypeArgs.length; i++ ) {
			Type supertypeArg = supertypeArgs[i];
			Type typeArg = typeArgs[i];

			if ( supertypeArg instanceof WildcardType ) {
				if ( !isWildcardTypeAssignable( (WildcardType) supertypeArg, typeArg ) ) {
					return false;
				}
			}
			else if ( !supertypeArg.equals( typeArg ) ) {
				return false;
			}
		}

		return true;
	}

	private static boolean isTypeVariableAssignable(Type supertype, TypeVariable<?> type) {
		for ( Type bound : type.getBounds() ) {
			if ( isAssignable( supertype, bound ) ) {
				return true;
			}
		}

		return false;
	}

	private static boolean isWildcardTypeAssignable(WildcardType supertype, Type type) {
		for ( Type upperBound : supertype.getUpperBounds() ) {
			if ( !isAssignable( upperBound, type ) ) {
				return false;
			}
		}

		for ( Type lowerBound : supertype.getLowerBounds() ) {
			if ( !isAssignable( type, lowerBound ) ) {
				return false;
			}
		}

		return true;
	}

	private static boolean isSuperAssignable(Type supertype, Type type) {
		Type superclass = getResolvedSuperclass( type );

		if ( superclass != null && isAssignable( supertype, superclass ) ) {
			return true;
		}

		for ( Type interphace : getResolvedInterfaces( type ) ) {
			if ( isAssignable( supertype, interphace ) ) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Gets whether the specified type is a <em>reference type</em>.
	 * <p>
	 * More specifically, this method returns {@code true} if the specified type is one of the following:
	 * <ul>
	 * <li>a class type</li>
	 * <li>an interface type</li>
	 * <li>an array type</li>
	 * <li>a parameterized type</li>
	 * <li>a type variable</li>
	 * <li>the null type</li>
	 * </ul>
	 *
	 * @param type the type to check
	 *
	 * @return {@code true} if the specified type is a reference type
	 *
	 * @see <a href="http://java.sun.com/docs/books/jls/third_edition/html/typesValues.html#4.3">4.3 Reference Types and Values</a>
	 */
	private static boolean isReferenceType(Type type) {
		return type == null
				|| type instanceof Class<?>
				|| type instanceof ParameterizedType
				|| type instanceof TypeVariable<?>
				|| type instanceof GenericArrayType;
	}

	private static boolean isArraySupertype(Class<?> type) {
		return Object.class.equals( type )
				|| Cloneable.class.equals( type )
				|| Serializable.class.equals( type );
	}

	private static Type resolveTypeVariables(Type type, Type subtype) {
		// TODO: need to support other types in future, e.g. T[], etc.
		if ( !( type instanceof ParameterizedType ) ) {
			return type;
		}

		Map<Type, Type> actualTypeArgumentsByParameter = getActualTypeArgumentsByParameter( type, subtype );
		Class<?> rawType = getErasedReferenceType( type );

		return parameterizeClass( rawType, actualTypeArgumentsByParameter );
	}

	private static Map<Type, Type> getActualTypeArgumentsByParameter(Type... types) {
		// TODO: return Map<TypeVariable<Class<?>>, Type> somehow

		Map<Type, Type> actualTypeArgumentsByParameter = new LinkedHashMap<Type, Type>();

		for ( Type type : types ) {
			actualTypeArgumentsByParameter.putAll( getActualTypeArgumentsByParameterInternal( type ) );
		}

		return normalize( actualTypeArgumentsByParameter );
	}

	private static Map<Type, Type> getActualTypeArgumentsByParameterInternal(Type type) {
		// TODO: look deeply within non-parameterized types when visitors implemented
		if ( !( type instanceof ParameterizedType ) ) {
			return Collections.emptyMap();
		}

		TypeVariable<?>[] typeParameters = getErasedReferenceType( type ).getTypeParameters();
		Type[] typeArguments = ( (ParameterizedType) type ).getActualTypeArguments();

		if ( typeParameters.length != typeArguments.length ) {
			throw new MalformedParameterizedTypeException();
		}

		Map<Type, Type> actualTypeArgumentsByParameter = new LinkedHashMap<Type, Type>();

		for ( int i = 0; i < typeParameters.length; i++ ) {
			actualTypeArgumentsByParameter.put( typeParameters[i], typeArguments[i] );
		}

		return actualTypeArgumentsByParameter;
	}

	private static ParameterizedType parameterizeClass(Class<?> type, Map<Type, Type> actualTypeArgumentsByParameter) {
		return parameterizeClassCapture( type, actualTypeArgumentsByParameter );
	}

	private static <T> ParameterizedType parameterizeClassCapture(Class<T> type, Map<Type, Type> actualTypeArgumentsByParameter) {
		// TODO: actualTypeArgumentsByParameter should be Map<TypeVariable<Class<T>>, Type>

		TypeVariable<Class<T>>[] typeParameters = type.getTypeParameters();
		Type[] actualTypeArguments = new Type[typeParameters.length];

		for ( int i = 0; i < typeParameters.length; i++ ) {
			TypeVariable<Class<T>> typeParameter = typeParameters[i];
			Type actualTypeArgument = actualTypeArgumentsByParameter.get( typeParameter );

			if ( actualTypeArgument == null ) {
				throw log.getMissingActualTypeArgumentForTypeParameterException( typeParameter );
			}

			actualTypeArguments[i] = actualTypeArgument;
		}

		return parameterizedType( getErasedReferenceType( type ), actualTypeArguments );
	}

	private static <K, V> Map<K, V> normalize(Map<K, V> map) {
		// TODO: will this cause an infinite loop with recursive bounds?

		for ( Entry<K, V> entry : map.entrySet() ) {
			K key = entry.getKey();
			V value = entry.getValue();

			while ( map.containsKey( value ) ) {
				value = map.get( value );
			}

			map.put( key, value );
		}

		return map;
	}
}
