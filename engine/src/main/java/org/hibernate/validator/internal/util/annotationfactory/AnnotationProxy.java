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
package org.hibernate.validator.internal.util.annotationfactory;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethod;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethods;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

/**
 * A concrete implementation of <code>Annotation</code> that pretends it is a
 * "real" source code annotation. It's also an <code>InvocationHandler</code>.
 * <p/>
 * When you create an <code>AnnotationProxy</code>, you must initialize it
 * with an <code>AnnotationDescriptor</code>.
 * The adapter checks that the provided elements are the same elements defined
 * in the annotation interface. However, it does <i>not</i> check that their
 * values are the right type. If you omit an element, the adapter will use the
 * default value for that element from the annotation interface, if it exists.
 * If no default exists, it will throw an exception.
 * <p/>
 *
 * @author Paolo Perrotta
 * @author Davide Marchignoli
 * @author Gunnar Morling
 * @see java.lang.annotation.Annotation
 */
class AnnotationProxy implements Annotation, InvocationHandler, Serializable {

	private static final long serialVersionUID = 6907601010599429454L;
	private static final Log log = LoggerFactory.make();

	private final Class<? extends Annotation> annotationType;
	private final Map<String, Object> values;
	private final int hashCode;

	AnnotationProxy(AnnotationDescriptor<?> descriptor) {
		this.annotationType = descriptor.type();
		values = Collections.unmodifiableMap( getAnnotationValues( descriptor ) );
		this.hashCode = calculateHashCode();
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if ( values.containsKey( method.getName() ) ) {
			return values.get( method.getName() );
		}
		return method.invoke( this, args );
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return annotationType;
	}

	/**
	 * Performs an equality check as described in {@link Annotation#equals(Object)}.
	 *
	 * @param obj The object to compare
	 *
	 * @return Whether the given object is equal to this annotation proxy or not
	 *
	 * @see Annotation#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( !annotationType.isInstance( obj ) ) {
			return false;
		}

		Annotation other = annotationType.cast( obj );

		//compare annotation member values
		for ( Entry<String, Object> member : values.entrySet() ) {

			Object value = member.getValue();
			Object otherValue = getAnnotationMemberValue( other, member.getKey() );

			if ( !areEqual( value, otherValue ) ) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Calculates the hash code of this annotation proxy as described in
	 * {@link Annotation#hashCode()}.
	 *
	 * @return The hash code of this proxy.
	 *
	 * @see Annotation#hashCode()
	 */
	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append( '@' ).append( annotationType.getName() ).append( '(' );
		for ( String s : getRegisteredMethodsInAlphabeticalOrder() ) {
			result.append( s ).append( '=' ).append( values.get( s ) ).append( ", " );
		}
		// remove last separator:
		if ( values.size() > 0 ) {
			result.delete( result.length() - 2, result.length() );
			result.append( ")" );
		}
		else {
			result.delete( result.length() - 1, result.length() );
		}

		return result.toString();
	}


	private Map<String, Object> getAnnotationValues(AnnotationDescriptor<?> descriptor) {
		Map<String, Object> result = newHashMap();
		int processedValuesFromDescriptor = 0;
		final Method[] declaredMethods = run( GetDeclaredMethods.action( annotationType ) );
		for ( Method m : declaredMethods ) {
			if ( descriptor.containsElement( m.getName() ) ) {
				result.put( m.getName(), descriptor.valueOf( m.getName() ) );
				processedValuesFromDescriptor++;
			}
			else if ( m.getDefaultValue() != null ) {
				result.put( m.getName(), m.getDefaultValue() );
			}
			else {
				throw log.getNoValueProvidedForAnnotationParameterException(
						m.getName(),
						annotationType.getSimpleName()
				);
			}
		}
		if ( processedValuesFromDescriptor != descriptor.numberOfElements() ) {

			Set<String> unknownParameters = descriptor.getElements().keySet();
			unknownParameters.removeAll( result.keySet() );

			throw log.getTryingToInstantiateAnnotationWithUnknownParametersException(
					annotationType,
					unknownParameters
			);
		}
		return result;
	}

	private int calculateHashCode() {
		int hashCode = 0;

		for ( Entry<String, Object> member : values.entrySet() ) {
			Object value = member.getValue();

			int nameHashCode = member.getKey().hashCode();

			int valueHashCode =
					!value.getClass().isArray() ? value.hashCode() :
							value.getClass() == boolean[].class ? Arrays.hashCode( (boolean[]) value ) :
									value.getClass() == byte[].class ? Arrays.hashCode( (byte[]) value ) :
											value.getClass() == char[].class ? Arrays.hashCode( (char[]) value ) :
													value.getClass() == double[].class ? Arrays.hashCode( (double[]) value ) :
															value.getClass() == float[].class ? Arrays.hashCode( (float[]) value ) :
																	value.getClass() == int[].class ? Arrays.hashCode( (int[]) value ) :
																			value.getClass() == long[].class ? Arrays.hashCode(
																					(long[]) value
																			) :
																					value.getClass() == short[].class ? Arrays
																							.hashCode( (short[]) value ) :
																							Arrays.hashCode( (Object[]) value );

			hashCode += 127 * nameHashCode ^ valueHashCode;
		}

		return hashCode;
	}

	private SortedSet<String> getRegisteredMethodsInAlphabeticalOrder() {
		SortedSet<String> result = new TreeSet<String>();
		result.addAll( values.keySet() );
		return result;
	}

	private boolean areEqual(Object o1, Object o2) {
		return
				!o1.getClass().isArray() ? o1.equals( o2 ) :
						o1.getClass() == boolean[].class ? Arrays.equals( (boolean[]) o1, (boolean[]) o2 ) :
								o1.getClass() == byte[].class ? Arrays.equals( (byte[]) o1, (byte[]) o2 ) :
										o1.getClass() == char[].class ? Arrays.equals( (char[]) o1, (char[]) o2 ) :
												o1.getClass() == double[].class ? Arrays.equals(
														(double[]) o1,
														(double[]) o2
												) :
														o1.getClass() == float[].class ? Arrays.equals(
																(float[]) o1,
																(float[]) o2
														) :
																o1.getClass() == int[].class ? Arrays.equals(
																		(int[]) o1,
																		(int[]) o2
																) :
																		o1.getClass() == long[].class ? Arrays.equals(
																				(long[]) o1,
																				(long[]) o2
																		) :
																				o1.getClass() == short[].class ? Arrays.equals(
																						(short[]) o1,
																						(short[]) o2
																				) :
																						Arrays.equals(
																								(Object[]) o1,
																								(Object[]) o2
																						);
	}

	private Object getAnnotationMemberValue(Annotation annotation, String name) {
		try {
			return run( GetDeclaredMethod.action( annotation.annotationType(), name ) ).invoke( annotation );
		}
		catch ( IllegalAccessException e ) {
			throw log.getUnableToRetrieveAnnotationParameterValueException( e );
		}
		catch ( IllegalArgumentException e ) {
			throw log.getUnableToRetrieveAnnotationParameterValueException( e );
		}
		catch ( InvocationTargetException e ) {
			throw log.getUnableToRetrieveAnnotationParameterValueException( e );
		}
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
