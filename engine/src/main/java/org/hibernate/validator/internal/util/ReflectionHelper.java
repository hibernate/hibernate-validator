/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.internal.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.validator.internal.metadata.raw.ExecutableElement;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

/**
 * Some reflection utility methods. Where necessary calls will be performed as {@code PrivilegedAction} which is necessary
 * for situations where a security manager is in place.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public final class ReflectionHelper {

	private static final String PROPERTY_ACCESSOR_PREFIX_GET = "get";
	private static final String PROPERTY_ACCESSOR_PREFIX_IS = "is";
	private static final String PROPERTY_ACCESSOR_PREFIX_HAS = "has";
	public static final String[] PROPERTY_ACCESSOR_PREFIXES = {
			PROPERTY_ACCESSOR_PREFIX_GET,
			PROPERTY_ACCESSOR_PREFIX_IS,
			PROPERTY_ACCESSOR_PREFIX_HAS
	};

	private static final Log log = LoggerFactory.make();

	private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER_TYPES;

	static {
		Map<Class<?>, Class<?>> tmpMap = newHashMap( 9 );

		tmpMap.put( boolean.class, Boolean.class );
		tmpMap.put( char.class, Character.class );
		tmpMap.put( double.class, Double.class );
		tmpMap.put( float.class, Float.class );
		tmpMap.put( long.class, Long.class );
		tmpMap.put( int.class, Integer.class );
		tmpMap.put( short.class, Short.class );
		tmpMap.put( byte.class, Byte.class );
		tmpMap.put( Void.TYPE, Void.TYPE );

		PRIMITIVE_TO_WRAPPER_TYPES = Collections.unmodifiableMap( tmpMap );
	}

	private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE_TYPES;

	static {
		Map<Class<?>, Class<?>> tmpMap = newHashMap( 9 );

		tmpMap.put( Boolean.class, boolean.class );
		tmpMap.put( Character.class, char.class );
		tmpMap.put( Double.class, double.class );
		tmpMap.put( Float.class, float.class );
		tmpMap.put( Long.class, long.class );
		tmpMap.put( Integer.class, int.class );
		tmpMap.put( Short.class, short.class );
		tmpMap.put( Byte.class, byte.class );
		tmpMap.put( Void.TYPE, Void.TYPE );

		WRAPPER_TO_PRIMITIVE_TYPES = Collections.unmodifiableMap( tmpMap );
	}

	/**
	 * Private constructor in order to avoid instantiation.
	 */
	private ReflectionHelper() {
	}

	/**
	 * Returns the JavaBeans property name of the given member.
	 * </p>
	 * For fields, the field name will be returned. For getter methods, the
	 * decapitalized property name will be returned, with the "get", "is" or "has"
	 * prefix stripped off. Getter methods are methods
	 * </p>
	 * <ul>
	 * <li>whose name start with "get" and who have a return type but no parameter
	 * or</li>
	 * <li>whose name starts with "is" and who have no parameter and return
	 * {@code boolean} or</li>
	 * <li>whose name starts with "has" and who have no parameter and return
	 * {@code boolean} (HV-specific, not mandated by JavaBeans spec).</li>
	 * </ul>
	 *
	 * @param member The member for which to get the property name.
	 *
	 * @return The property name for the given member or {@code null} if the
	 *         member is neither a field nor a getter method according to the
	 *         JavaBeans standard.
	 */
	public static String getPropertyName(Member member) {
		String name = null;

		if ( member instanceof Field ) {
			name = member.getName();
		}

		if ( member instanceof Method ) {
			String methodName = member.getName();
			for ( String prefix : PROPERTY_ACCESSOR_PREFIXES ) {
				if ( methodName.startsWith( prefix ) ) {
					name = StringHelper.decapitalize( methodName.substring( prefix.length() ) );
				}
			}
		}
		return name;
	}

	/**
	 * Checks whether the given method is a valid JavaBeans getter method, which
	 * is the case if
	 * <ul>
	 * <li>its name starts with "get" and it has a return type but no parameter or</li>
	 * <li>its name starts with "is", it has no parameter and is returning
	 * {@code boolean} or</li>
	 * <li>its name starts with "has", it has no parameter and is returning
	 * {@code boolean} (HV-specific, not mandated by JavaBeans spec).</li>
	 * </ul>
	 *
	 * @param method The method of interest.
	 *
	 * @return {@code true}, if the given method is a JavaBeans getter method,
	 *         {@code false} otherwise.
	 */
	public static boolean isGetterMethod(Method method) {
		if ( method.getParameterTypes().length != 0 ) {
			return false;
		}

		String methodName = method.getName();

		//<PropertyType> get<PropertyName>()
		if ( methodName.startsWith( PROPERTY_ACCESSOR_PREFIX_GET ) && method.getReturnType() != void.class ) {
			return true;
		}
		//boolean is<PropertyName>()
		else if ( methodName.startsWith( PROPERTY_ACCESSOR_PREFIX_IS ) && method.getReturnType() == boolean.class ) {
			return true;
		}
		//boolean has<PropertyName>()
		else if ( methodName.startsWith( PROPERTY_ACCESSOR_PREFIX_HAS ) && method.getReturnType() == boolean.class ) {
			return true;
		}

		return false;
	}

	/**
	 * @param member The <code>Member</code> instance for which to retrieve the type.
	 *
	 * @return Returns the <code>Type</code> of the given <code>Field</code> or <code>Method</code>.
	 *
	 * @throws IllegalArgumentException in case <code>member</code> is not a <code>Field</code> or <code>Method</code>.
	 */
	public static Type typeOf(Member member) {
		Type type;
		if ( member instanceof Field ) {
			type = ( (Field) member ).getGenericType();
		}
		else if ( member instanceof Method ) {
			type = ( (Method) member ).getGenericReturnType();
		}
		else if ( member instanceof Constructor<?> ) {
			type = member.getDeclaringClass();
		}
		//TODO HV-571 change log method name
		else {
			throw log.getMemberIsNeitherAFieldNorAMethodException( member );
		}
		if ( type instanceof TypeVariable ) {
			type = TypeHelper.getErasedType( type );
		}
		return type;
	}

	/**
	 * Returns the type of the parameter of the given method with the given parameter index.
	 *
	 * @param executable The executable of interest.
	 * @param parameterIndex The index of the parameter for which the type should be returned.
	 *
	 * @return The erased type.
	 */
	public static Type typeOf(ExecutableElement executable, int parameterIndex) {
		Type[] genericParameterTypes = executable.getGenericParameterTypes();

		// getGenericParameterTypes() doesn't return synthetic parameters; in this case fall back to getParameterTypes()
		if ( parameterIndex >= genericParameterTypes.length ) {
			genericParameterTypes = executable.getParameterTypes();
		}

		Type type = genericParameterTypes[parameterIndex];

		if ( type instanceof TypeVariable ) {
			type = TypeHelper.getErasedType( type );
		}
		return type;
	}

	public static Object getValue(Member member, Object object) {
		if ( member instanceof Method ) {
			return getValue( (Method) member, object );
		}
		else if ( member instanceof Field ) {
			return getValue( (Field) member, object );
		}
		return null;
	}

	public static Object getValue(Field field, Object object) {
		try {
			return field.get( object );
		}
		catch ( IllegalAccessException e ) {
			throw log.getUnableToAccessMemberException( field.getName(), e );
		}
	}

	public static Object getValue(Method method, Object object) {
		try {
			return method.invoke( object );
		}
		catch ( IllegalAccessException e ) {
			throw log.getUnableToAccessMemberException( method.getName(), e );
		}
		catch ( InvocationTargetException e ) {
			throw log.getUnableToAccessMemberException( method.getName(), e );
		}
	}

	/**
	 * Determines the type of elements of an <code>Iterable</code>, array or the value of a <code>Map</code>.
	 *
	 * @param type the type to inspect
	 *
	 * @return Returns the type of elements of an <code>Iterable</code>, array or the value of a <code>Map</code>. <code>
	 *         null</code> is returned in case the type is not indexable (in the context of JSR 303).
	 */
	public static Type getIndexedType(Type type) {
		Type indexedType = null;
		if ( isIterable( type ) && type instanceof ParameterizedType ) {
			ParameterizedType paramType = (ParameterizedType) type;
			indexedType = paramType.getActualTypeArguments()[0];
		}
		else if ( isMap( type ) && type instanceof ParameterizedType ) {
			ParameterizedType paramType = (ParameterizedType) type;
			indexedType = paramType.getActualTypeArguments()[1];
		}
		else if ( TypeHelper.isArray( type ) ) {
			indexedType = TypeHelper.getComponentType( type );
		}
		return indexedType;
	}

	/**
	 * @param type the type to check.
	 *
	 * @return Returns <code>true</code> if <code>type</code> is a iterable type, <code>false</code> otherwise.
	 */
	public static boolean isIterable(Type type) {
		if ( type instanceof Class && Iterable.class.isAssignableFrom( (Class<?>) type ) ) {
			return true;
		}
		if ( type instanceof ParameterizedType ) {
			return isIterable( ( (ParameterizedType) type ).getRawType() );
		}
		if ( type instanceof WildcardType ) {
			Type[] upperBounds = ( (WildcardType) type ).getUpperBounds();
			return upperBounds.length != 0 && isIterable( upperBounds[0] );
		}
		return false;
	}

	/**
	 * @param type the type to check.
	 *
	 * @return Returns <code>true</code> if <code>type</code> is implementing <code>Map</code>, <code>false</code> otherwise.
	 */
	public static boolean isMap(Type type) {
		if ( type instanceof Class && Map.class.isAssignableFrom( (Class<?>) type ) ) {
			return true;
		}
		if ( type instanceof ParameterizedType ) {
			return isMap( ( (ParameterizedType) type ).getRawType() );
		}
		if ( type instanceof WildcardType ) {
			Type[] upperBounds = ( (WildcardType) type ).getUpperBounds();
			return upperBounds.length != 0 && isMap( upperBounds[0] );
		}
		return false;
	}

	/**
	 * @param type the type to check.
	 *
	 * @return Returns <code>true</code> if <code>type</code> is implementing <code>List</code>, <code>false</code> otherwise.
	 */
	public static boolean isList(Type type) {
		if ( type instanceof Class && List.class.isAssignableFrom( (Class<?>) type ) ) {
			return true;
		}
		if ( type instanceof ParameterizedType ) {
			return isList( ( (ParameterizedType) type ).getRawType() );
		}
		if ( type instanceof WildcardType ) {
			Type[] upperBounds = ( (WildcardType) type ).getUpperBounds();
			return upperBounds.length != 0 && isList( upperBounds[0] );
		}
		return false;
	}

	/**
	 * Tries to retrieve the indexed value from the specified object.
	 *
	 * @param value The object from which to retrieve the indexed value. The object has to be non <code>null</null> and
	 * either a collection or array.
	 * @param index The index. The index does not have to be numerical. <code>value</code> could also be a map in which
	 * case the index could also be a string key.
	 *
	 * @return The indexed value or <code>null</code> if <code>value</code> is <code>null</code> or not a collection or array.
	 *         <code>null</code> is also returned in case the index does not exist.
	 */
	public static Object getIndexedValue(Object value, Integer index) {
		if ( value == null ) {
			return null;
		}

		Iterator<?> iter;
		Type type = value.getClass();
		if ( isIterable( type ) ) {
			iter = ( (Iterable<?>) value ).iterator();
		}
		else if ( TypeHelper.isArray( type ) ) {
			List<?> arrayList = Arrays.asList( value );
			iter = arrayList.iterator();
		}
		else {
			return null;
		}

		int i = 0;
		Object o;
		while ( iter.hasNext() ) {
			o = iter.next();
			if ( i == index ) {
				return o;
			}
			i++;
		}
		return null;
	}

	/**
	 * Tries to retrieve the mapped value from the specified object.
	 *
	 * @param value The object from which to retrieve the mapped value. The object has to be non {@code null} and
	 * must implement the  @{code Map} interface.
	 * @param key The map key. index.
	 *
	 * @return The mapped value or {@code null} if {@code value} is {@code null} or not implementing @{code Map}.
	 */
	public static Object getMappedValue(Object value, Object key) {
		if ( !( value instanceof Map ) ) {
			return null;
		}

		Map<?, ?> map = (Map<?, ?>) value;
		//noinspection SuspiciousMethodCalls
		return map.get( key );
	}

	/**
	 * Returns the auto-boxed type of a primitive type.
	 *
	 * @param primitiveType the primitive type
	 *
	 * @return the auto-boxed type of a primitive type. In case {@link Void} is
	 *         passed (which is considered as primitive type by
	 *         {@link Class#isPrimitive()}), {@link Void} will be returned.
	 *
	 * @throws IllegalArgumentException in case the parameter {@code primitiveType} does not
	 * represent a primitive type.
	 */
	public static Class<?> boxedType(Class<?> primitiveType) {
		Class<?> wrapperType = PRIMITIVE_TO_WRAPPER_TYPES.get( primitiveType );

		if ( wrapperType == null ) {
			throw log.getHasToBeAPrimitiveTypeException( primitiveType.getClass() );
		}

		return wrapperType;
	}

	/**
	 * Returns the primitive type for a boxed type.
	 *
	 * @param type the boxed type
	 *
	 * @return the primitive type for a auto-boxed type. In case {@link Void} is
	 *         passed (which is considered as primitive type by
	 *         {@link Class#isPrimitive()}), {@link Void} will be returned.
	 *
	 * @throws IllegalArgumentException in case the parameter {@code primitiveType} does not
	 * represent a primitive type.
	 */
	public static Class<?> unBoxedType(Class<?> type) {
		Class<?> wrapperType = WRAPPER_TO_PRIMITIVE_TYPES.get( type );

		if ( wrapperType == null ) {
			throw log.getHasToBeABoxedTypeException( type.getClass() );
		}

		return wrapperType;
	}
}
