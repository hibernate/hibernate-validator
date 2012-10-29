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

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.metadata.raw.ExecutableElement;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.ConstructorInstance;
import org.hibernate.validator.internal.util.privilegedactions.GetAnnotationParameter;
import org.hibernate.validator.internal.util.privilegedactions.GetClassLoader;
import org.hibernate.validator.internal.util.privilegedactions.GetConstructor;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredConstructors;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredField;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredFields;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethod;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethods;
import org.hibernate.validator.internal.util.privilegedactions.GetMethod;
import org.hibernate.validator.internal.util.privilegedactions.GetMethodFromPropertyName;
import org.hibernate.validator.internal.util.privilegedactions.GetMethods;
import org.hibernate.validator.internal.util.privilegedactions.LoadClass;
import org.hibernate.validator.internal.util.privilegedactions.NewInstance;
import org.hibernate.validator.internal.util.privilegedactions.SetAccessibility;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

/**
 * Some reflection utility methods. Where necessary calls will be performed as {@code PrivilegedAction} which is necessary
 * for situations where a security manager is in place.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public final class ReflectionHelper {

	private static final Log log = LoggerFactory.make();

	private static String[] PROPERTY_ACCESSOR_PREFIXES = { "is", "get", "has" };

	private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER_TYPES;

	static {
		Map<Class<?>, Class<?>> temp = newHashMap( 9 );

		temp.put( boolean.class, Boolean.class );
		temp.put( char.class, Character.class );
		temp.put( double.class, Double.class );
		temp.put( float.class, Float.class );
		temp.put( long.class, Long.class );
		temp.put( int.class, Integer.class );
		temp.put( short.class, Short.class );
		temp.put( byte.class, Byte.class );
		temp.put( Void.TYPE, Void.TYPE );

		PRIMITIVE_TO_WRAPPER_TYPES = Collections.unmodifiableMap( temp );
	}

	/**
	 * Private constructor in order to avoid instantiation.
	 */
	private ReflectionHelper() {
	}

	public static ClassLoader getClassLoaderFromContext() {
		return run( GetClassLoader.fromContext() );
	}

	public static ClassLoader getClassLoaderFromClass(Class<?> clazz) {
		return run( GetClassLoader.fromClass( clazz ) );
	}

	public static Class<?> loadClass(String className, Class<?> caller) {
		return run( LoadClass.action( className, caller ) );
	}

	public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... params) {
		return run( GetConstructor.action( clazz, params ) );
	}

	public static <T> T newInstance(Class<T> clazz, String message) {
		return run( NewInstance.action( clazz, message ) );
	}

	public static <T> T newConstructorInstance(Constructor<T> constructor, Object... initArgs) {
		return run( ConstructorInstance.action( constructor, initArgs ) );
	}

	public static <T> T getAnnotationParameter(Annotation annotation, String parameterName, Class<T> type) {
		return run( GetAnnotationParameter.action( annotation, parameterName, type ) );
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
			for ( String prefix : PROPERTY_ACCESSOR_PREFIXES ) {
				if ( methodName.startsWith( prefix ) ) {
					name = Introspector.decapitalize( methodName.substring( prefix.length() ) );
				}
			}
		}
		return name;
	}

	/**
	 * Checks whether the given method is a valid JavaBeans getter method,
	 * meaning its name starts with "is" or "has" and it has no parameters.
	 *
	 * @param method The method of interest.
	 *
	 * @return {@code true}, if the given method is a JavaBeans getter method,
	 *         {@code false} otherwise.
	 */
	public static boolean isGetterMethod(Method method) {
		return getPropertyName( method ) != null && method.getParameterTypes().length == 0;
	}

	/**
	 * Checks whether the property with the specified name and type exists on the given class.
	 *
	 * @param clazz The class to check for the property. Cannot be {@code null}.
	 * @param property The property name without 'is', 'get' or 'has'. Cannot be {@code null} or empty.
	 * @param elementType The element type. Either {@code ElementType.FIELD} or {@code ElementType METHOD}.
	 *
	 * @return {@code true} is the property and can be access via the specified type, {@code false} otherwise.
	 */
	public static boolean propertyExists(Class<?> clazz, String property, ElementType elementType) {
		return getMember( clazz, property, elementType ) != null;
	}

	/**
	 * Returns the member with the given name and type.
	 *
	 * @param clazz The class from which to retrieve the member. Cannot be {@code null}.
	 * @param property The property name without 'is', 'get' or 'has'. Cannot be {@code null} or empty.
	 * @param elementType The element type. Either {@code ElementType.FIELD} or {@code ElementType METHOD}.
	 *
	 * @return the member which matching the name and type or {@code null} if no such member exists.
	 */
	public static Member getMember(Class<?> clazz, String property, ElementType elementType) {

		Contracts.assertNotNull( clazz, MESSAGES.classCannotBeNull() );

		if ( property == null || property.length() == 0 ) {
			throw log.getPropertyNameCannotBeNullOrEmptyException();
		}

		if ( !( ElementType.FIELD.equals( elementType ) || ElementType.METHOD.equals( elementType ) ) ) {
			throw log.getElementTypeHasToBeFieldOrMethodException();
		}

		Member member = null;
		if ( ElementType.FIELD.equals( elementType ) ) {
			member = run( GetDeclaredField.action( clazz, property ) );
		}
		else {
			String methodName = property.substring( 0, 1 ).toUpperCase() + property.substring( 1 );
			for ( String prefix : PROPERTY_ACCESSOR_PREFIXES ) {
				member = run( GetMethod.action( clazz, prefix + methodName ) );
				if ( member != null ) {
					break;
				}
			}
		}
		return member;
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
			type = ( (Field) member ).getType();
		}

		if ( member instanceof Method ) {
			type = ( (Method) member ).getReturnType();
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

		Type type = executable.getGenericParameterTypes()[parameterIndex];

		if ( type instanceof TypeVariable ) {
			type = TypeHelper.getErasedType( type );
		}
		return type;
	}

	public static Object getValue(Member member, Object object) {
		Object value = null;

		if ( member instanceof Method ) {
			Method method = (Method) member;
			try {
				value = method.invoke( object );
			}
			catch ( IllegalAccessException e ) {
				throw log.getUnableToAccessMemberException( method.getName(), e );
			}
			catch ( InvocationTargetException e ) {
				throw log.getUnableToAccessMemberException( method.getName(), e );
			}
		}
		else if ( member instanceof Field ) {
			Field field = (Field) member;
			try {
				value = field.get( object );
			}
			catch ( IllegalAccessException e ) {
				throw log.getUnableToAccessMemberException( field.getName(), e );
			}
		}
		return value;
	}

	public static void setAccessibility(Member member) {
		run( SetAccessibility.action( member ) );
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
			List<?> arrayList = Arrays.asList( (Object) value );
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
	 * Returns the declared field with the specified name or {@code null} if it does not exist.
	 *
	 * @param clazz The class to check.
	 * @param fieldName The field name.
	 *
	 * @return Returns the declared field with the specified name or {@code null} if it does not exist.
	 */
	public static Field getDeclaredField(Class<?> clazz, String fieldName) {
		return run( GetDeclaredField.action( clazz, fieldName ) );
	}

	/**
	 * Checks whether the specified class contains a declared field with the given name.
	 *
	 * @param clazz The class to check.
	 * @param fieldName The field name.
	 *
	 * @return Returns {@code true} if the field exists, {@code false} otherwise.
	 */
	public static boolean containsDeclaredField(Class<?> clazz, String fieldName) {
		return getDeclaredField( clazz, fieldName ) != null;
	}

	/**
	 * Returns the fields of the specified class.
	 *
	 * @param clazz The class for which to retrieve the fields.
	 *
	 * @return Returns the fields for this class.
	 */
	public static Field[] getDeclaredFields(Class<?> clazz) {
		return run( GetDeclaredFields.action( clazz ) );
	}

	/**
	 * Returns the method with the specified property name or {@code null} if it does not exist. This method will
	 * prepend  'is' and 'get' to the property name and capitalize the first letter.
	 *
	 * @param clazz The class to check.
	 * @param methodName The property name.
	 *
	 * @return Returns the method with the specified property or {@code null} if it does not exist.
	 */
	public static Method getMethodFromPropertyName(Class<?> clazz, String methodName) {
		return run( GetMethodFromPropertyName.action( clazz, methodName ) );
	}

	/**
	 * Checks whether the specified class contains a method for the specified property.
	 *
	 * @param clazz The class to check.
	 * @param property The property name.
	 *
	 * @return Returns {@code true} if the method exists, {@code false} otherwise.
	 */
	public static boolean containsMethodWithPropertyName(Class<?> clazz, String property) {
		return getMethodFromPropertyName( clazz, property ) != null;
	}

	/**
	 * Returns the method with the specified name or {@code null} if it does not exist.
	 *
	 * @param clazz The class to check.
	 * @param methodName The method name.
	 *
	 * @return Returns the method with the specified property or {@code null} if it does not exist.
	 */
	public static Method getMethod(Class<?> clazz, String methodName) {
		return run( GetMethod.action( clazz, methodName ) );
	}

	/**
	 * Returns the declared method with the specified name and parameter types or {@code null} if
	 * it does not exist.
	 *
	 * @param clazz The class to check.
	 * @param methodName The method name.
	 * @param parameterTypes The method parameter types.
	 *
	 * @return Returns the declared method with the specified name or {@code null} if it does not exist.
	 */
	public static Method getDeclaredMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		return run( GetDeclaredMethod.action( clazz, methodName, parameterTypes ) );
	}

	/**
	 * Returns the declared methods of the specified class.
	 *
	 * @param clazz The class for which to retrieve the methods.
	 *
	 * @return Returns the declared methods for this class.
	 */
	public static Method[] getDeclaredMethods(Class<?> clazz) {
		return run( GetDeclaredMethods.action( clazz ) );
	}

	/**
	 * Returns the methods of the specified class (include inherited methods).
	 *
	 * @param clazz The class for which to retrieve the methods.
	 *
	 * @return Returns the methods for this class.
	 */
	public static Method[] getMethods(Class<?> clazz) {
		return run( GetMethods.action( clazz ) );
	}

	/**
	 * Returns the declared constructors of the specified class.
	 *
	 * @param clazz The class for which to retrieve the constructors.
	 *
	 * @return Returns the declared constructors for this class.
	 */
	public static Constructor<?>[] getDeclaredConstructors(Class<?> clazz) {
		return run( GetDeclaredConstructors.action( clazz ) );
	}

	/**
	 * Executes the given privileged action either directly or with privileges
	 * enabled, depending on whether a security manager is around or not.
	 *
	 * @param <T> The return type of the privileged action to run.
	 * @param action The action to run.
	 *
	 * @return The result of the privileged action's execution.
	 */
	private static <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}

	/**
	 * Checks, whether the given methods have the same signature, which is the
	 * case if they have the same name, parameter count and types.
	 *
	 * @param method1 A first method.
	 * @param method2 A second method.
	 *
	 * @return True, if the methods have the same signature, false otherwise.
	 */
	public static boolean haveSameSignature(ExecutableElement method1, ExecutableElement method2) {

		Contracts.assertValueNotNull( method1, "method1" );
		Contracts.assertValueNotNull( method2, "method2" );

		return
				method1.getMember().getName().equals( method2.getMember().getName() ) &&
						Arrays.equals( method1.getGenericParameterTypes(), method2.getGenericParameterTypes() );
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
	 * Get all superclasses and optionally interfaces recursively.
	 *
	 * @param clazz The class to start the search with.
	 * @param includeInterfaces whether or not to include interfaces
	 *
	 * @return List of all super classes and interfaces of {@code clazz}. The list contains the class itself! The empty
	 *         list is returned if {@code clazz} is {@code null}.
	 */
	public static List<Class<?>> computeClassHierarchy(Class<?> clazz, boolean includeInterfaces) {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		computeClassHierarchy( clazz, classes, includeInterfaces );
		return classes;
	}

	/**
	 * Get all superclasses and interfaces recursively.
	 *
	 * @param clazz The class to start the search with
	 * @param classes List of classes to which to add all found super classes and interfaces
	 * @param includeInterfaces whether or not to include interfaces
	 */
	private static void computeClassHierarchy(Class<?> clazz, List<Class<?>> classes, boolean includeInterfaces) {
		for ( Class<?> current = clazz; current != null; current = current.getSuperclass() ) {
			if ( classes.contains( current ) ) {
				return;
			}
			classes.add( current );
			if ( includeInterfaces ) {
				for ( Class<?> currentInterface : current.getInterfaces() ) {
					computeClassHierarchy( currentInterface, classes, includeInterfaces );
				}
			}
		}
	}

	/**
	 * Get all interfaces a class directly implements.
	 *
	 * @param clazz The class for which to find the interfaces
	 *
	 * @return Set of all interfaces {@code clazz} implements. The empty list is returned if {@code clazz} does not
	 *         implement any interfaces or {@code clazz} is {@code null}
	 */
	public static Set<Class<?>> computeAllImplementedInterfaces(Class<?> clazz) {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		computeAllImplementedInterfaces( clazz, classes );
		return classes;
	}

	private static void computeAllImplementedInterfaces(Class<?> clazz, Set<Class<?>> classes) {
		if ( clazz == null ) {
			return;
		}
		for ( Class<?> currentInterface : clazz.getInterfaces() ) {
			classes.add( currentInterface );
			computeAllImplementedInterfaces( currentInterface, classes );
		}
	}

	/**
	 * This methods inspects the type of the cascaded constraints and in case
	 * of a list or array creates an iterator in order to validate each element. For maps an iterator over the values
	 * is returned.
	 *
	 * @param type the type of the cascaded field or property.
	 * @param value the actual value.
	 *
	 * @return An iterator over the value of a cascaded property.
	 */
	//TODO HV-571: Check whether we can unify this with ValidatorImpl#createIteratorForCascadedValue()
	public static Iterator<?> createIteratorForCascadedValue(Type type, Object value) {
		Iterator<?> iter;
		if ( isIterable( type ) ) {
			iter = ( (Iterable<?>) value ).iterator();
		}
		else if ( isMap( type ) ) {
			Map<?, ?> map = (Map<?, ?>) value;
			iter = map.values().iterator();
		}
		else if ( TypeHelper.isArray( type ) ) {
			List<?> arrayList = Arrays.asList( (Object[]) value );
			iter = arrayList.iterator();
		}
		else {
			List<Object> list = newArrayList();
			list.add( value );
			iter = list.iterator();
		}
		return iter;
	}
}
