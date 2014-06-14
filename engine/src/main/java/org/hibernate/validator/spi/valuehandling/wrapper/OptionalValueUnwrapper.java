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
package org.hibernate.validator.spi.valuehandling.wrapper;

import java.lang.reflect.Type;
import java.util.Optional;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;

import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

/**
 * Unwraps an {@code Optional} and returns the wrapped value and type. Empty {@code Optional} value is returned as
 * {@code null}.
 *
 * @author Khalid Alqinyah
 */
public class OptionalValueUnwrapper extends ValidatedValueUnwrapper<Optional<?>> {

	private final TypeResolver typeResolver = new TypeResolver();

	@Override
	public Object handleValidatedValue(Optional<?> value) {
		if ( value.isPresent() ) {
			return value.get();
		}

		return null;
	}

	@Override
	public Type getValidatedValueType(Type valueType) {
		ResolvedType resolvedType = typeResolver.resolve( valueType );
		return resolvedType.typeParametersFor( Optional.class ).get( 0 ).getErasedType();
	}
}
