/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter11.valuehandling;

//end::include[]

import java.lang.reflect.Type;

import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

/**
 * @author Gunnar Morling
 */
//tag::include[]
public class PropertyValueUnwrapper extends ValidatedValueUnwrapper<Property<?>> {

	@Override
	public Object handleValidatedValue(Property<?> value) {
		//...
		return null;
	}

	@Override
	public Type getValidatedValueType(Type valueType) {
		//...
		return null;
	}
}
//end::include[]
