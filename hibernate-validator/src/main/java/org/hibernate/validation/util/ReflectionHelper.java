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
package org.hibernate.validation.util;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.validation.ValidationException;

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


	/**
	 * Checks whether the given annotation is a builtin constraint annotation defined as defined by the specs.
	 *
	 * @param annotation the annotation to check
	 *
	 * @return <code>true</code> if the annotation is a builtin constraint, <code>false</code> otherwise.
	 */
	public static boolean isBuiltInConstraintAnnotation(Annotation annotation) {
		boolean isBuiltInConstraintAnnotation = false;

		String packageName = annotation.annotationType().getPackage().getName();
		if ( "javax.validation.constraints".equals( packageName ) ) {
			isBuiltInConstraintAnnotation = true;
		}
		return isBuiltInConstraintAnnotation;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getAnnotationParameter(Annotation annotation, String parameterName, Class<T> type) {
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
	 * Returns the type of the field of return type of a method.
	 *
	 * @param member the member for which to get the type.
	 *
	 * @return Returns the type of the field of return type of a method.
	 */
	public static Class<?> getAnnotations(Member member) {

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
	 * @return Retrurns the <code>Type</code> of the given <code>Field</code> or <code>Method</code>.
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

	public static void setAccessibility(Member member) {
		if ( !Modifier.isPublic( member.getModifiers() ) ) {
			//Sun's ease of use, sigh...
			( ( AccessibleObject ) member ).setAccessible( true );
		}
	}

	public static Class<?> loadClass(String name, Class caller) throws ClassNotFoundException {
		try {
			//try context classloader, if fails try caller classloader
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			if ( loader != null ) {
				return loader.loadClass( name );
			}
		}
		catch ( ClassNotFoundException e ) {
			//trying caller classloader
			if ( caller == null ) {
				throw e;
			}
		}
		return Class.forName( name, true, caller.getClassLoader() );
	}

	/**
	 * Determines the type of elements of a generic collection or array.
	 *
	 * @param type the collection or array type.
	 *
	 * @return the element type of the generic collection/array or <code>null</code> in case the specified type is not a collection/array or the
	 *         element type cannot be determined.
	 *
	 * @todo Verify algorithm. Does this hold up in most cases?
	 */
	public static Type getIndexedType(Type type) {
		Type indexedType = null;
		if ( isCollection( type ) && type instanceof ParameterizedType ) {
			ParameterizedType paramType = ( ParameterizedType ) type;
			Class collectionClass = getCollectionClass( type );
			if ( collectionClass == Collection.class || collectionClass == java.util.List.class || collectionClass == java.util.Set.class || collectionClass == java.util.SortedSet.class ) {
				indexedType = paramType.getActualTypeArguments()[0];
			}
			else if ( collectionClass == Map.class || collectionClass == java.util.SortedMap.class ) {
				indexedType = paramType.getActualTypeArguments()[1];
			}
		}
		else if ( ReflectionHelper.isArray( type ) && type instanceof GenericArrayType ) {
			GenericArrayType arrayTye = ( GenericArrayType ) type;
			indexedType = arrayTye.getGenericComponentType();
		}
		return indexedType;
	}


	/**
	 * @param type the type to check.
	 *
	 * @return Returns <code>true</code> if <code>type</code> is an array type or <code>false</code> otherwise.
	 */
	public static boolean isArray(Type type) {
		if ( type instanceof Class ) {
			return ( ( Class ) type ).isArray();
		}
		return type instanceof GenericArrayType;
	}


	/**
	 * @param type the type to check.
	 *
	 * @return Returns <code>true</code> if <code>type</code> is a collection type or <code>false</code> otherwise.
	 */
	public static boolean isCollection(Type type) {
		return getCollectionClass( type ) != null;
	}


	/**
	 * Returns the collection class for the specified type provided it is a collection.
	 * <p>
	 * This is a simplified version of commons annotations </code>TypeUtils.getCollectionClass()</code>.
	 * </p>
	 *
	 * @param type the <code>Type</code> to check.
	 *
	 * @return the collection class, or <code>null</code> if type is not a collection class.
	 */
	@SuppressWarnings("unchecked")
	public static Class<? extends Collection> getCollectionClass(Type type) {
		if ( type instanceof Class && isCollectionClass( ( Class ) type ) ) {
			return ( Class<? extends Collection> ) type;
		}
		if ( type instanceof ParameterizedType ) {
			return getCollectionClass( ( ( ParameterizedType ) type ).getRawType() );
		}
		if ( type instanceof WildcardType ) {
			Type[] upperBounds = ( ( WildcardType ) type ).getUpperBounds();
			if ( upperBounds.length == 0 ) {
				return null;
			}
			return getCollectionClass( upperBounds[0] );
		}
		return null;
	}

	/**
	 * Checks whether the specified class parameter is an instance of a collection class.
	 *
	 * @param clazz <code>Class</code> to check.
	 *
	 * @return <code>true</code> is <code>clazz</code> is instance of a collection class, <code>false</code> otherwise.
	 */
	private static boolean isCollectionClass(Class<?> clazz) {
		Class[] interfaces = clazz.getInterfaces();

		for ( Class interfaceClass : interfaces ) {
			if ( interfaceClass == Collection.class
					|| interfaceClass == java.util.List.class
					|| interfaceClass == java.util.Set.class
					|| interfaceClass == java.util.Map.class
					|| interfaceClass == java.util.SortedSet.class // extension to the specs
					|| interfaceClass == java.util.SortedMap.class ) { // extension to the specs)
				return true;
			}
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
	public static Object getIndexedValue(Object value, String index) {
		if ( value == null ) {
			return null;
		}

		// try to create the index
		int numIndex = -1;
		try {
			numIndex = Integer.valueOf( index );
		}
		catch ( NumberFormatException nfe ) {
			// ignore
		}

		if ( numIndex == -1 ) {  // must be a map indexed by string
			Map<?, ?> map = ( Map<?, ?> ) value;
			//noinspection SuspiciousMethodCalls
			return map.get( index );
		}

		Iterator<?> iter = null;
		Type type = value.getClass();
		if ( isCollection( type ) ) {
			boolean isIterable = value instanceof Iterable;
			Map<?, ?> map = !isIterable ? ( Map<?, ?> ) value : null;
			Iterable<?> elements = isIterable ?
					( Iterable<?> ) value :
					map.values();
			iter = elements.iterator();

		}
		else if ( isArray( type ) ) {
			List<?> arrayList = Arrays.asList( value );
			iter = arrayList.iterator();
		}

		int i = 0;
		Object o;
		while ( iter.hasNext() ) {
			o = iter.next();
			if ( i == numIndex ) {
				return o;
			}
			i++;
		}
		return null;
	}

	/**
	 * Checks whether the specified class contains a field or member which matches a given property.
	 *
	 * @param clazz The class to check.
	 * @param property The property name.
	 *
	 * @return Returns <code>true</code> if the cass contains a field or member for the specified property, <code>
	 *         false</code> otherwise.
	 */
	public static boolean containsMember(Class clazz, String property) {
		try {
			clazz.getField( property );
			return true;
		}
		catch ( NoSuchFieldException e ) {
			// ignore
		}

		try {
			clazz.getMethod( "get" + property.substring( 0, 1 ).toUpperCase() + property.substring( 1 ) );
			return true;
		}
		catch ( NoSuchMethodException e ) {
			// ignore
		}
		return false;
	}

	public static Class<?> classForName(String name, Class<?> caller) throws ClassNotFoundException {
		try {
			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			if ( contextClassLoader != null ) {
				return contextClassLoader.loadClass( name );
			}
		}
		catch ( Throwable e ) {
			// ignore
		}
		return Class.forName( name, true, caller.getClassLoader() );
	}


}
