/*
* JBoss, Home of Professional Open Source
* Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

/**
 * Provides helper methods for working with formal parameters and actual type arguments of parameterized types.
 *
 * @author Khalid Alqinyah
 */
public class TypeUseHelper {

	private TypeUseHelper() {

	}

	/**
	 * Given {@code Bar<Integer, String>}, a list of {@code Integer} and {@code String} annotated types is returned.
	 *
	 * @param annotatedType the annotated type that contains the type arguments
	 *
	 * @return the type arguments or empty list if no type arguments are found or the {@code AnnotatedType} is not an
	 * {@code AnnotatedParameterizedType}
	 *
	 * @throws IllegalArgumentException if {@code annotatedType} is null
	 */
	public static List<AnnotatedType> getAnnotatedActualTypeArguments(AnnotatedType annotatedType) {
		Contracts.assertNotNull( annotatedType, MESSAGES.mustNotBeNull( "annotatedType" ) );

		if ( !TypeHelper.isAssignable( AnnotatedParameterizedType.class, annotatedType.getClass() ) ) {
			return Collections.emptyList();
		}

		AnnotatedType[] annotatedArguments = ( (AnnotatedParameterizedType) annotatedType ).getAnnotatedActualTypeArguments();
		return Arrays.asList( annotatedArguments );
	}

	/**
	 * Given {@code class Bar<T, V>} and {@code Bar<Integer, String>}, a map of {@code T} to {@code Integer} and {@code
	 * V} to {@code String} is returned.
	 *
	 * @param annotatedType the annotated type that contains the type arguments
	 * @param cls the class that contains the formal parameters
	 *
	 * @return a map of formal parameter to actual type arguments
	 *
	 * @throws IllegalArgumentException if {@code annotatedType} or {@code cls} is null, or if they're for different types
	 */
	public static Map<TypeVariable<?>, AnnotatedType> getFormalToActualMap(AnnotatedType annotatedType, Class<?> cls) {
		Contracts.assertNotNull( cls, MESSAGES.mustNotBeNull( "cls" ) );

		List<AnnotatedType> annotatedArguments = getAnnotatedActualTypeArguments( annotatedType );
		List<TypeVariable<?>> formalParameters = Arrays.<TypeVariable<?>>asList( cls.getTypeParameters() );

		if ( annotatedArguments.size() == formalParameters.size() && annotatedArguments.size() > 0 ) {

			// They must be of the same type
			if ( ! ( (ParameterizedType) annotatedType.getType() ).getRawType().equals( cls ) ) {
				String error = String.format( "AnnotatedType %s is not of the same type as %s %n", annotatedType.getType(), cls.getName() );
				throw new IllegalArgumentException( error );
			}

			Map<TypeVariable<?>, AnnotatedType> map = newHashMap();

			for ( int i = 0; i < annotatedArguments.size(); i++ ) {
				map.put( formalParameters.get( i ), annotatedArguments.get( i ) );
			}

			return map;
		}
		else {
			return Collections.emptyMap();
		}
	}
}
