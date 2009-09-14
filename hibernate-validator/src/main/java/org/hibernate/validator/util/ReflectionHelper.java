// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validator.util;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.validation.ValidationException;

import com.googlecode.jtype.TypeUtils;

/**
 * Some reflection utility methods.
 *
 * @author Hardy Ferentschik
 */
public class ReflectionHelper {

	/**
	 * Private constructor in order to avoid instantiation.
	 */
	private ReflectionHelper() {
	}

	//run client in privileged block
	@SuppressWarnings("unchecked")
	static <T> T getAnnotationParameter(Annotation annotation, String parameterName, Class<T> type) {
		try {
			Method m = annotation.getClass().getMethod( parameterName );
			Object o = m.invoke( annotation );
			if ( o.getClass().getName().equals( type.getName() ) ) {
				return ( T ) o;
			}
			else {
				String msg = "Wrong parameter type. Expected: " + type.getName() + " Actual: " + o.getClass().getName();
				throw new ValidationException( msg );
			}
		}
		catch ( NoSuchMethodException e ) {
			String msg = "The specified annotation defines no parameter '" + parameterName + "'.";
			throw new ValidationException( msg, e );
		}
		catch ( IllegalAccessException e ) {
			String msg = "Unable to get '" + parameterName + "' from " + annotation.getClass().getName();
			throw new ValidationException( msg, e );
		}
		catch ( InvocationTargetException e ) {
			String msg = "Unable to get '" + parameterName + "' from " + annotation.getClass().getName();
			throw new ValidationException( msg, e );
		}
	}

	/**
	 * Process bean properties getter by applying the JavaBean naming conventions.
	 *
	 * @param member the member for which to get the property name.
	 *
	 * @return The bean method name with the "is" or "get" prefix stripped off, <code>null</code>
	 *         the method name id not according to the JavaBeans standard.
	 */
	public static String getPropertyName(Member member) {
		String name = null;

		if ( member instanceof Field ) {
			name = member.getName();
		}

		if ( member instanceof Method ) {
			String methodName = member.getName();
			if ( methodName.startsWith( "is" ) ) {
				name = Introspector.decapitalize( methodName.substring( 2 ) );
			}
			else if ( methodName.startsWith( "has" ) ) {
				name = Introspector.decapitalize( methodName.substring( 3 ) );
			}
			else if ( methodName.startsWith( "get" ) ) {
				name = Introspector.decapitalize( methodName.substring( 3 ) );
			}
		}
		return name;
	}

	/**
	 * Returns the type of the field of return type of a method.
	 *
	 * @param member the member for which to get the type.
	 *
	 * @return Returns the type of the field of return type of a method.
	 */
	public static Class<?> getType(Member member) {

		Class<?> type = null;
		if ( member instanceof Field ) {
			type = ( ( Field ) member ).getType();
		}

		if ( member instanceof Method ) {
			type = ( ( Method ) member ).getReturnType();
		}
		return type;
	}

	/**
	 * @param member The <code>Member</code> instance for which to retrieve the type.
	 *
	 * @return Returns the <code>Type</code> of the given <code>Field</code> or <code>Method</code>.
	 *
	 * @throws IllegalArgumentException in case <code>member</code> is not a <code>Field</code> or <code>Method</code>.
	 */
	public static Type typeOf(Member member) {
		if ( member instanceof Field ) {
			return ( ( Field ) member ).getGenericType();
		}
		if ( member instanceof Method ) {
			return ( ( Method ) member ).getGenericReturnType();
		}
		throw new IllegalArgumentException( "Member " + member + " is neither a field nor a method" );
	}


	public static Object getValue(Member member, Object object) {
		Object value = null;

		if ( member instanceof Method ) {
			Method method = ( Method ) member;
			try {
				value = method.invoke( object );
			}
			catch ( IllegalAccessException e ) {
				throw new ValidationException( "Unable to access " + method.getName(), e );
			}
			catch ( InvocationTargetException e ) {
				throw new ValidationException( "Unable to access " + method.getName(), e );
			}
		}
		else if ( member instanceof Field ) {
			Field field = ( Field ) member;
			try {
				value = field.get( object );
			}
			catch ( IllegalAccessException e ) {
				throw new ValidationException( "Unable to access " + field.getName(), e );
			}
		}
		return value;
	}

	//run client in privileged block
	static void setAccessibility(Member member) {
		if ( !Modifier.isPublic( member.getModifiers() ) ) {
			//Sun's ease of use, sigh...
			( ( AccessibleObject ) member ).setAccessible( true );
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
			ParameterizedType paramType = ( ParameterizedType ) type;
			indexedType = paramType.getActualTypeArguments()[0];
		}
		else if ( isMap( type ) && type instanceof ParameterizedType ) {
			ParameterizedType paramType = ( ParameterizedType ) type;
			indexedType = paramType.getActualTypeArguments()[1];
		}
		else if ( TypeUtils.isArray( type ) ) {
			indexedType = TypeUtils.getComponentType( type );
		}
		return indexedType;
	}

	/**
	 * @param type the type to check.
	 *
	 * @return Returns <code>true</code> if <code>type</code> is a iterable type, <code>false</code> otherwise.
	 */
	public static boolean isIterable(Type type) {
		if ( type instanceof Class && extendsOrImplements( ( Class ) type, Iterable.class ) ) {
			return true;
		}
		if ( type instanceof ParameterizedType ) {
			return isIterable( ( ( ParameterizedType ) type ).getRawType() );
		}
		if ( type instanceof WildcardType ) {
			Type[] upperBounds = ( ( WildcardType ) type ).getUpperBounds();
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
		if ( type instanceof Class && extendsOrImplements( ( Class ) type, Map.class ) ) {
			return true;
		}
		if ( type instanceof ParameterizedType ) {
			return isMap( ( ( ParameterizedType ) type ).getRawType() );
		}
		if ( type instanceof WildcardType ) {
			Type[] upperBounds = ( ( WildcardType ) type ).getUpperBounds();
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
		if ( type instanceof Class && extendsOrImplements( ( Class ) type, List.class ) ) {
			return true;
		}
		if ( type instanceof ParameterizedType ) {
			return isList( ( ( ParameterizedType ) type ).getRawType() );
		}
		if ( type instanceof WildcardType ) {
			Type[] upperBounds = ( ( WildcardType ) type ).getUpperBounds();
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
			iter = ( ( Iterable<?> ) value ).iterator();
		}
		else if ( TypeUtils.isArray( type ) ) {
			List<?> arrayList = Arrays.asList( ( Object ) value );
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
		if ( value == null || !( value instanceof Map ) ) {
			return null;
		}

		Map<?, ?> map = ( Map<?, ?> ) value;
		//noinspection SuspiciousMethodCalls
		return map.get( key );
	}

	/**
	 * Returns the method with the specified name or <code>null</code> if it does not exist.
	 *
	 * @param clazz The class to check.
	 * @param methodName The method name.
	 *
	 * @return Returns the method with the specified name or <code>null</code> if it does not exist.
	 */
	//run client in privileged block
	static Method getMethod(Class<?> clazz, String methodName) {
		try {
			char string[] = methodName.toCharArray();
			string[0] = Character.toUpperCase( string[0] );
			methodName = new String( string );
			try {
				return clazz.getMethod( "get" + methodName );
			}
			catch ( NoSuchMethodException e ) {
				return clazz.getMethod( "is" + methodName );
			}
		}
		catch ( NoSuchMethodException e ) {
			return null;
		}
	}

	/**
	 * Returns the autoboxed type of a primitive type.
	 *
	 * @param primitiveType the primitive type
	 *
	 * @return the autoboxed type of a primitive type.
	 *
	 * @throws IllegalArgumentException in case the parameter {@code primitiveType} does not represent a primitive type.
	 */
	public static Class<?> boxedType(Type primitiveType) {
		if ( !( primitiveType instanceof Class ) && !( ( Class ) primitiveType ).isPrimitive() ) {
			throw new IllegalArgumentException( primitiveType.getClass() + "has to be a primitive type" );
		}

		if ( primitiveType == boolean.class ) {
			return Boolean.class;
		}
		else if ( primitiveType == char.class ) {
			return Character.class;
		}
		else if ( primitiveType == double.class ) {
			return Double.class;
		}
		else if ( primitiveType == float.class ) {
			return Float.class;
		}
		else if ( primitiveType == long.class ) {
			return Long.class;
		}
		else if ( primitiveType == int.class ) {
			return Integer.class;
		}
		else if ( primitiveType == short.class ) {
			return Short.class;
		}
		else if ( primitiveType == byte.class ) {
			return Byte.class;
		}
		else {
			throw new RuntimeException( "Unhandled primitive type." );
		}
	}

	/**
	 * Get all superclasses and interfaces recursively.
	 *
	 * @param clazz The class to start the search with.
	 * @param classes List of classes to which to add all found super classes and interfaces.
	 */
	public static void computeClassHierarchy(Class<?> clazz, List<Class<?>> classes) {
		for ( Class current = clazz; current != null; current = current.getSuperclass() ) {
			if ( classes.contains( current ) ) {
				return;
			}
			classes.add( current );
			for ( Class currentInterface : current.getInterfaces() ) {
				computeClassHierarchy( currentInterface, classes );
			}
		}
	}

	/**
	 * Checks whether the specified {@code clazz} extends or inherits the specified super class or interface.
	 *
	 * @param clazz @{code Class} to check.
	 * @param superClassOrInterface The super class/interface {@code clazz}.
	 *
	 * @return {@code true} if {@code clazz} extends or implements {@code superClassOrInterface}, {@code false} otherwise.
	 */
	private static boolean extendsOrImplements(Class<?> clazz, Class<?> superClassOrInterface) {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		computeClassHierarchy( clazz, classes );
		return classes.contains( superClassOrInterface );
	}
}
