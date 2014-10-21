/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valuehandling.model;

import java.lang.reflect.Type;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;

import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

/**
 * @author Gunnar Morling
 */
public class PropertyValueUnwrapper extends ValidatedValueUnwrapper<Property<?>> {

	private final TypeResolver typeResolver = new TypeResolver();

	@Override
	public Object handleValidatedValue(Property<?> source) {
		return source.getValue();
	}

	@Override
	public Type getValidatedValueType(Type sourceType) {
		ResolvedType resolvedType = typeResolver.resolve( sourceType );
		return resolvedType.typeParametersFor( Property.class ).get( 0 ).getErasedType();
	}
}
