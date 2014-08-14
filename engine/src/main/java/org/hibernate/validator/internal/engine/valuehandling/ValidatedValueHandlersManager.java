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
package org.hibernate.validator.internal.engine.valuehandling;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;

import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

/**
 * Keeps track of registered {@link org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper} and allows for
 * the retrieval of unwrappers based on the wrapper type. The class also holds flags related to validated value
 * unwrapping options.
 *
 * @author Khalid Alqinyah
 */
public class ValidatedValueHandlersManager {

	/**
	 * Contains handlers to be applied to the validated value when validating elements.
	 */
	private final List<ValidatedValueUnwrapper<?>> validatedValueHandlers;

	/**
	 * Used for resolving type parameters. Thread-safe.
	 */
	private final TypeResolutionHelper typeResolutionHelper;

	/**
	 * Hibernate Validator specific flag to auto unwrap wrapper types by default.
	 */
	private final boolean autoUnwrapValidatedValue;

	public ValidatedValueHandlersManager(List<ValidatedValueUnwrapper<?>> validatedValueHandlers,
										 TypeResolutionHelper typeResolutionHelper,
										 boolean autoUnwrapValidatedValue) {
		this.validatedValueHandlers = Collections.unmodifiableList( validatedValueHandlers );
		this.typeResolutionHelper = typeResolutionHelper;
		this.autoUnwrapValidatedValue = autoUnwrapValidatedValue;
	}

	/**
	 * Returns a list of registered  {@code ValidatedValueUnwrapper}.
	 *
	 * @return a list of registered  {@code ValidatedValueUnwrapper}
	 */
	public List<ValidatedValueUnwrapper<?>> getValidatedValueHandlers() {
		return this.validatedValueHandlers;
	}

	/**
	 * Return the flag for whether wrapper types should be unwrapped by default.
	 *
	 * @return {@code true} if wrapper types should be unwrapped by default, {@code false} otherwise
	 */
	public boolean isAutoUnwrapValidatedValue() {
		return this.autoUnwrapValidatedValue;
	}

	/**
	 * Returns the first validated value handler found which supports the given type.
	 * <p>
	 * If required this could be enhanced to search for the most-specific handler and raise an exception in case more
	 * than one matching handler is found (or a scheme of prioritizing handlers to process several handlers in order.
	 *
	 * @param type the type to be handled
	 *
	 * @return the handler for the given type or {@code null} if no matching handler was found
	 */
	public ValidatedValueUnwrapper<?> getValidatedValueHandler(Type type) {
		TypeResolver typeResolver = typeResolutionHelper.getTypeResolver();

		for ( ValidatedValueUnwrapper<?> handler : validatedValueHandlers ) {
			ResolvedType handlerType = typeResolver.resolve( handler.getClass() );
			List<ResolvedType> typeParameters = handlerType.typeParametersFor( ValidatedValueUnwrapper.class );

			if ( TypeHelper.isAssignable( typeParameters.get( 0 ).getErasedType(), type ) ) {
				return handler;
			}
		}

		return null;
	}
}
