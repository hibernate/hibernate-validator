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
package org.hibernate.validator.util.annotationfactory;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.hibernate.validator.util.ReflectionHelper;


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
 * Warning: this class does not implement <code>hashCode()</code> and
 * <code>equals()</code> - it just uses the ones it inherits from <code>Object</code>.
 * This means that an <code>AnnotationProxy</code> does <i>not</i> follow the
 * recommendations of the <code>Annotation</code> javadoc about these two
 * methods. That's why you should never mix <code>AnnotationProxies</code>
 * with "real" annotations. For example, don't put them into the same
 * <code>Collection</code>.
 *
 * @author Paolo Perrotta
 * @author Davide Marchignoli
 * @see java.lang.annotation.Annotation
 */
public class AnnotationProxy implements Annotation, InvocationHandler, Serializable {

	private static final long serialVersionUID = 6907601010599429454L;
	private final Class<? extends Annotation> annotationType;
	private final Map<String, Object> values;


	public AnnotationProxy(AnnotationDescriptor<?> descriptor) {
		this.annotationType = descriptor.type();
		values = getAnnotationValues( descriptor );
	}

	private Map<String, Object> getAnnotationValues(AnnotationDescriptor<?> descriptor) {
		Map<String, Object> result = new HashMap<String, Object>();
		int processedValuesFromDescriptor = 0;
		final Method[] declaredMethods = ReflectionHelper.getDeclaredMethods( annotationType );
		for ( Method m : declaredMethods ) {
			if ( descriptor.containsElement( m.getName() ) ) {
				result.put( m.getName(), descriptor.valueOf( m.getName() ) );
				processedValuesFromDescriptor++;
			}
			else if ( m.getDefaultValue() != null ) {
				result.put( m.getName(), m.getDefaultValue() );
			}
			else {
				throw new IllegalArgumentException( "No value provided for " + m.getName() );
			}
		}
		if ( processedValuesFromDescriptor != descriptor.numberOfElements() ) {

			Set<String> unknownParameters = descriptor.getElements().keySet();
			unknownParameters.removeAll( result.keySet() );

			throw new RuntimeException( "Trying to instantiate " + annotationType + " with unknown parameter(s): " + unknownParameters );
		}
		return result;
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if ( values.containsKey( method.getName() ) ) {
			return values.get( method.getName() );
		}
		return method.invoke( this, args );
	}

	public Class<? extends Annotation> annotationType() {
		return annotationType;
	}

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

	private SortedSet<String> getRegisteredMethodsInAlphabeticalOrder() {
		SortedSet<String> result = new TreeSet<String>();
		result.addAll( values.keySet() );
		return result;
	}
}
