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
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;

import org.hibernate.validator.internal.metadata.raw.ExecutableElement;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.ConstructorInstance;
import org.hibernate.validator.internal.util.privilegedactions.GetAnnotationParameter;
import org.hibernate.validator.internal.util.privilegedactions.GetClassLoader;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredConstructor;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredConstructors;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredField;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredFields;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethod;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethods;
import org.hibernate.validator.internal.util.privilegedactions.GetMethod;
import org.hibernate.validator.internal.util.privilegedactions.GetMethodFromPropertyName;
import org.hibernate.validator.internal.util.privilegedactions.GetMethods;
import org.hibernate.validator.internal.util.privilegedactions.GetProtectionDomainAsArray;
import org.hibernate.validator.internal.util.privilegedactions.LoadClass;
import org.hibernate.validator.internal.util.privilegedactions.NewInstance;
import org.hibernate.validator.internal.util.privilegedactions.SetAccessibility;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

/**
 * Some reflection utility methods.
 * <p>
 * Many methods of this class require special permissions in case a security manager is in place. In that case, the
 * operations are wrapped in {@code PrivilegedAction}s. All the affected methods expect an {@link AccessControlContext}
 * which will be passed to {@link AccessController#doPrivileged(PrivilegedAction, AccessControlContext)} upon execution.
 * <p>
 * Callers can obtain a context representing their own {@link ProtectionDomain} via {@link #getAccessControlContext()}.
 * It is recommended to cache that context but it may <strong>NEVER</strong> be made publicly available as otherwise
 * untrusted code may invoke security-relevant methods under the protection domain of Hibernate Validator.
 * <p>
 * The following shows the recommended usage pattern:
 *
 * <pre>
 * public class MyClass {
 *
 *     private static final AccessControlContext ACCESS_CONTROL_CONTEXT = ReflectionHelper
 *             .getAccessControlContext();
 *
 *     public void myOperation() {
 *         Field field = ReflectionHelper.getDeclaredField( ACCESS_CONTROL_CONTEXT, ArrayList.class, &quot;size&quot; ) );
 *     }
 * }
 * </pre>
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public final class ReflectionHelper {

	private static final String PACKAGE_SEPARATOR = ".";
	private static final String ARRAY_CLASS_NAME_PREFIX = "[L";
	private static final String ARRAY_CLASS_NAME_SUFFIX = ";";

	private static final String PROPERTY_ACCESSOR_PREFIX_GET = "get";
	private static final String PROPERTY_ACCESSOR_PREFIX_IS = "is";
	private static final String PROPERTY_ACCESSOR_PREFIX_HAS = "has";
	private static final String[] PROPERTY_ACCESSOR_PREFIXES = {
			PROPERTY_ACCESSOR_PREFIX_GET,
			PROPERTY_ACCESSOR_PREFIX_IS,
			PROPERTY_ACCESSOR_PREFIX_HAS
	};

	private static final Log log = LoggerFactory.make();

	private static final Map<String, Class<?>> PRIMITIVE_NAME_TO_PRIMITIVE;

	static {
		Map<String, Class<?>> tmpMap = newHashMap( 9 );

		tmpMap.put( boolean.class.getName(), boolean.class );
		tmpMap.put( char.class.getName(), char.class );
		tmpMap.put( double.class.getName(), double.class );
		tmpMap.put( float.class.getName(), float.class );
		tmpMap.put( long.class.getName(), long.class );
		tmpMap.put( int.class.getName(), int.class );
		tmpMap.put( short.class.getName(), short.class );
		tmpMap.put( byte.class.getName(), byte.class );
		tmpMap.put( Void.TYPE.getName(), Void.TYPE );

		PRIMITIVE_NAME_TO_PRIMITIVE = Collections.unmodifiableMap( tmpMap );
	}

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
	 * Provides an {@link AccessControlContext} representing the protection domain of the direct caller of this method.
	 * This context is to be passed to all the methods of this class which need to execute code in a privileged block.
	 * <p>
	 * To avoid repeated context instantiations, callers should invoke this method once and keep the context in a
	 * private constant.
	 * <p>
	 * <b>NOTE</b>: Access control contexts created by Hibernate Validator code must <b>NEVER</b> be exposed to client
	 * code. They must only be used internally. Otherwise client code invoke the methods in this class in the context of
	 * Hibernate Validator.
	 *
	 * @return A {@link AccessControlContext} for the calling class, if a security manager is present, {@code null}
	 *         otherwise.
	 */
	public static AccessControlContext getAccessControlContext() {
		if ( System.getSecurityManager() == null ) {
			return null;
		}

		ProtectionDomain[] protectionDomains = AccessController.doPrivileged(
				GetProtectionDomainAsArray.action( CallerClassProvider.INSTANCE.getCallerClass() )
		);

		return new AccessControlContext( protectionDomains );
	}

	public static ClassLoader getClassLoaderFromContext(AccessControlContext acc) {
		return run( GetClassLoader.fromContext(), acc );
	}

	public static ClassLoader getClassLoaderFromClass(AccessControlContext acc, Class<?> clazz) {
		return run( GetClassLoader.fromClass( clazz ), acc );
	}

	public static Class<?> loadClass(AccessControlContext acc, String className, Class<?> caller) {
		return run( LoadClass.action( className, caller ), acc );
	}

	public static Class<?> loadClass(AccessControlContext acc, String className, String defaultPackage) {
		return loadClass( acc, className, defaultPackage, ReflectionHelper.class );
	}

	public static boolean isClassPresent(AccessControlContext acc, String className, Class<?> caller) {
		try {
			ReflectionHelper.loadClass( acc, className, caller );
			return true;
		}
		catch ( ValidationException e ) {
			return false;
		}
	}

	public static Class<?> loadClass(AccessControlContext acc, String className, String defaultPackage, Class<?> caller) {
		if ( PRIMITIVE_NAME_TO_PRIMITIVE.containsKey( className ) ) {
			return PRIMITIVE_NAME_TO_PRIMITIVE.get( className );
		}

		StringBuilder fullyQualifiedClass = new StringBuilder();
		String tmpClassName = className;
		if ( isArrayClassName( className ) ) {
			fullyQualifiedClass.append( ARRAY_CLASS_NAME_PREFIX );
			tmpClassName = getArrayElementClassName( className );
		}

		if ( isQualifiedClass( tmpClassName ) ) {
			fullyQualifiedClass.append( tmpClassName );
		}
		else {
			fullyQualifiedClass.append( defaultPackage );
			fullyQualifiedClass.append( PACKAGE_SEPARATOR );
			fullyQualifiedClass.append( tmpClassName );
		}

		if ( isArrayClassName( className ) ) {
			fullyQualifiedClass.append( ARRAY_CLASS_NAME_SUFFIX );
		}

		return loadClass( acc, fullyQualifiedClass.toString(), caller );
	}

	private static boolean isArrayClassName(String className) {
		return className.startsWith( ARRAY_CLASS_NAME_PREFIX ) && className.endsWith( ARRAY_CLASS_NAME_SUFFIX );
	}

	private static String getArrayElementClassName(String className) {
		return className.substring( 2, className.length() - 1 );
	}

	private static boolean isQualifiedClass(String clazz) {
		return clazz.contains( PACKAGE_SEPARATOR );
	}

	public static <T> T newInstance(AccessControlContext acc, Class<T> clazz, String message) {
		return run( NewInstance.action( clazz, message ), acc );
	}

	public static <T> T newConstructorInstance(AccessControlContext acc, Constructor<T> constructor, Object... initArgs) {
		return run( ConstructorInstance.action( constructor, initArgs ), acc );
	}

	public static <T> T getAnnotationParameter(AccessControlContext acc, Annotation annotation, String parameterName, Class<T> type) {
		return run( GetAnnotationParameter.action( annotation, parameterName, type ), acc );
	}

	/**
	 * Returns the JavaBeans property name of the given member.
	 * <p>
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
	 * Returns the member with the given name and type.
	 * @param clazz The class from which to retrieve the member. Cannot be {@code null}.
	 * @param property The property name without "is", "get" or "has". Cannot be {@code null} or empty.
	 * @param elementType The element type. Either {@code ElementType.FIELD} or {@code ElementType METHOD}.
	 *
	 * @return the member which matching the name and type or {@code null} if no such member exists.
	 */
	public static Member getMember(AccessControlContext acc, Class<?> clazz, String property, ElementType elementType) {
		Contracts.assertNotNull( clazz, MESSAGES.classCannotBeNull() );

		if ( property == null || property.length() == 0 ) {
			throw log.getPropertyNameCannotBeNullOrEmptyException();
		}

		if ( !( ElementType.FIELD.equals( elementType ) || ElementType.METHOD.equals( elementType ) ) ) {
			throw log.getElementTypeHasToBeFieldOrMethodException();
		}

		Member member = null;
		if ( ElementType.FIELD.equals( elementType ) ) {
			member = run( GetDeclaredField.action( clazz, property ), acc );
		}
		else {
			String methodName = property.substring( 0, 1 ).toUpperCase() + property.substring( 1 );
			for ( String prefix : PROPERTY_ACCESSOR_PREFIXES ) {
				member = run( GetMethod.action( clazz, prefix + methodName ), acc );
				if ( member != null ) {
					break;
				}
			}
		}
		return member;
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

	public static void setAccessibility(AccessControlContext acc, Member member) {
		run( SetAccessibility.action( member ), acc );
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
	 * @param value The object from which to retrieve the indexed value. The object has to be non <code>null</code> and
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
	 * Returns the declared field with the specified name or {@code null} if it does not exist.
	 * @param clazz The class to check.
	 * @param fieldName The field name.
	 *
	 * @return Returns the declared field with the specified name or {@code null} if it does not exist.
	 */
	public static Field getDeclaredField(AccessControlContext acc, Class<?> clazz, String fieldName) {
		return run( GetDeclaredField.action( clazz, fieldName ), acc );
	}

	/**
	 * Returns the fields of the specified class.
	 * @param clazz The class for which to retrieve the fields.
	 *
	 * @return Returns the fields for this class.
	 */
	public static Field[] getDeclaredFields(AccessControlContext acc, Class<?> clazz) {
		return run( GetDeclaredFields.action( clazz ), acc );
	}

	/**
	 * Returns the method with the specified property name or {@code null} if it does not exist. This method will
	 * prepend  'is' and 'get' to the property name and capitalize the first letter.
	 * @param clazz The class to check.
	 * @param methodName The property name.
	 *
	 * @return Returns the method with the specified property or {@code null} if it does not exist.
	 */
	public static Method getMethodFromPropertyName(AccessControlContext acc, Class<?> clazz, String methodName) {
		return run( GetMethodFromPropertyName.action( clazz, methodName ), acc );
	}

	/**
	 * Returns the method with the specified name or {@code null} if it does not exist.
	 *
	 * @param clazz The class to check.
	 * @param methodName The method name.
	 *
	 * @return Returns the method with the specified property or {@code null} if it does not exist.
	 */
	public static Method getMethod(AccessControlContext acc, Class<?> clazz, String methodName) {
		return run( GetMethod.action( clazz, methodName ), acc );
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
	public static Method getDeclaredMethod(AccessControlContext acc, Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		return run( GetDeclaredMethod.action( clazz, methodName, parameterTypes ), acc );
	}

	/**
	 * Returns the declared methods of the specified class.
	 * @param clazz The class for which to retrieve the methods.
	 *
	 * @return Returns the declared methods for this class.
	 */
	public static Method[] getDeclaredMethods(AccessControlContext acc, Class<?> clazz) {
		return run( GetDeclaredMethods.action( clazz ), acc );
	}

	/**
	 * Returns the methods of the specified class (include inherited methods).
	 * @param clazz The class for which to retrieve the methods.
	 *
	 * @return Returns the methods for this class.
	 */
	public static Method[] getMethods(AccessControlContext acc, Class<?> clazz) {
		return run( GetMethods.action( clazz ), acc );
	}

	/**
	 * Returns the declared constructors of the specified class.
	 * @param clazz The class for which to retrieve the constructors.
	 *
	 * @return Returns the declared constructors for this class.
	 */
	public static Constructor<?>[] getDeclaredConstructors(AccessControlContext acc, Class<?> clazz) {
		return run( GetDeclaredConstructors.action( clazz ), acc );
	}

	/**
	 * Returns the declared constructor with the specified parameter types or {@code null} if
	 * it does not exist.
	 *
	 * @param clazz The class to check.
	 * @param params The constructor parameter types.
	 * @param <T> The type of class to get the constructor for.
	 *
	 * @return Returns the declared constructor with the specified name or {@code null} if it does not exist.
	 */
	public static <T> Constructor<T> getDeclaredConstructor(AccessControlContext acc, Class<T> clazz, Class<?>... params) {
		return run( GetDeclaredConstructor.action( clazz, params ), acc );
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
	private static <T> T run(PrivilegedAction<T> action, AccessControlContext acc) {
		if ( System.getSecurityManager() != null ) {
			Contracts.assertNotNull( acc, "access control context" );
			return AccessController.doPrivileged( action, acc );
		}
		else {
			return action.run();
		}
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

	/**
	 * Provides the class calling {@link ReflectionHelper#getAccessControlContext()}.
	 * <p>
	 * <b>Note:</b> This routine depends on the exact depth of the stack, so care must be taken when changing it. There
	 * are several ways for obtaining the caller of a method, refer to http://stackoverflow.com/questions/421280/ for a
	 * discussion of the possible alternatives. We went for the SM-based approach as it doesn't rely on Sun-internal
	 * APIs and is the fastest of the remaining alternatives.
	 *
	 * @author Gunnar Morling
	 */
	private static class CallerClassProvider extends SecurityManager {

		private static final CallerClassProvider INSTANCE = System.getSecurityManager() != null ?
				AccessController.doPrivileged( new InstantiateCallerClassProvider() ) :
				new CallerClassProvider();

		public Class<?> getCallerClass() {
			return getClassContext()[2];
		}
	}

	/**
	 * Instantiates {@link CallerClassProvider}. Required due to {@link RuntimePermission} "createSecurityManager".
	 *
	 * @author Gunnar Morling
	 *
	 */
	private static class InstantiateCallerClassProvider implements PrivilegedAction<CallerClassProvider> {
		@Override
		public CallerClassProvider run() {
			return new CallerClassProvider();
		}
	}
}
