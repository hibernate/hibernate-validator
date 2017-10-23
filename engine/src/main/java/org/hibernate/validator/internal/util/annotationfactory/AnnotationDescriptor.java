/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.annotationfactory;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetAnnotationParameters;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethods;

/**
 * Encapsulates the data you need to create an annotation. In
 * particular, it stores the type of an {@code Annotation} instance
 * and the values of its elements.
 * The "elements" we're talking about are the annotation attributes,
 * not its targets (the term "element" is used ambiguously
 * in Java's annotations documentation).
 *
 * @author Paolo Perrotta
 * @author Davide Marchignoli
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
public class AnnotationDescriptor<T extends Annotation> implements Serializable {

	private static final Log LOG = LoggerFactory.make();

	private final Class<T> type;

	private final AnnotationParameters attributes;

	private final int hashCode;

	private final T annotation;

	@SuppressWarnings("unchecked")
	public AnnotationDescriptor(T annotation) {
		this.type = (Class<T>) annotation.annotationType();
		this.attributes = run( GetAnnotationParameters.action( annotation ) );
		this.annotation = annotation;
		this.hashCode = buildHashCode();
	}

	private AnnotationDescriptor(Class<T> annotationType, AnnotationParameters parameters) {
		this.type = annotationType;
		this.attributes = parameters;
		this.hashCode = buildHashCode();
		this.annotation = AnnotationFactory.create( this );
	}

	public Class<T> type() {
		return type;
	}

	public AnnotationParameters getAttributes() {
		return attributes;
	}

	public T annotation() {
		return annotation;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null || obj.getClass() != AnnotationDescriptor.class ) {
			return false;
		}

		AnnotationDescriptor<?> other = (AnnotationDescriptor<?>) obj;

		if ( !type.equals( other.type ) ) {
			return false;
		}

		if ( attributes.size() != other.attributes.size() ) {
			return false;
		}

		for ( Entry<String, Object> member : attributes.entrySet() ) {
			Object value = member.getValue();
			Object otherValue = other.attributes.getParameter( member.getKey() );

			if ( !areEqual( value, otherValue ) ) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Calculates the hash code of this annotation descriptor as described in
	 * {@link Annotation#hashCode()}.
	 *
	 * @return The hash code of this descriptor.
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
		result.append( '@' ).append( type.getName() ).append( '(' );
		for ( String s : getRegisteredAttributesInAlphabeticalOrder() ) {
			result.append( s ).append( '=' ).append( attributes.getParameter( s ) ).append( ", " );
		}
		// remove last separator:
		if ( attributes.size() > 0 ) {
			result.delete( result.length() - 2, result.length() );
			result.append( ")" );
		}
		else {
			result.delete( result.length() - 1, result.length() );
		}

		return result.toString();
	}

	private SortedSet<String> getRegisteredAttributesInAlphabeticalOrder() {
		return new TreeSet<String>( attributes.keySet() );
	}

	private int buildHashCode() {
		int hashCode = 0;

		for ( Entry<String, Object> member : attributes.entrySet() ) {
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

	public static class Builder<S extends Annotation> {

		private final Class<S> type;

		private final Map<String, Object> elements;

		public Builder(Class<S> type) {
			this.type = type;
			this.elements = new HashMap<String, Object>();
		}

		public Builder(Class<S> type, Map<String, Object> elements) {
			this.type = type;
			this.elements = new HashMap<String, Object>( elements );
		}

		@SuppressWarnings("unchecked")
		public Builder(S annotation) {
			this.type = (Class<S>) annotation.annotationType();
			this.elements = new HashMap<String, Object>( run( GetAnnotationParameters.action( annotation ) ).toMap() );
		}

		public void setValue(String elementName, Object value) {
			elements.put( elementName, value );
		}

		public boolean containsKey(String key) {
			return elements.containsKey( key );
		}

		public AnnotationDescriptor<S> build() {
			return new AnnotationDescriptor<S>( type, getAnnotationParameters() );
		}

		private AnnotationParameters getAnnotationParameters() {
			Map<String, Object> result = newHashMap( elements.size() );
			int processedValuesFromDescriptor = 0;
			final Method[] declaredMethods = run( GetDeclaredMethods.action( type ) );
			for ( Method m : declaredMethods ) {
				Object elementValue = elements.get( m.getName() );
				if ( elementValue != null ) {
					result.put( m.getName(), elementValue );
					processedValuesFromDescriptor++;
				}
				else if ( m.getDefaultValue() != null ) {
					result.put( m.getName(), m.getDefaultValue() );
				}
				else {
					throw LOG.getNoValueProvidedForAnnotationParameterException(
							m.getName(),
							type
					);
				}
			}
			if ( processedValuesFromDescriptor != elements.size() ) {
				Set<String> unknownParameters = elements.keySet();
				unknownParameters.removeAll( result.keySet() );

				throw LOG.getTryingToInstantiateAnnotationWithUnknownParametersException(
						type,
						unknownParameters
				);
			}
			return new AnnotationParameters( result );
		}
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private static <V> V run(PrivilegedAction<V> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
