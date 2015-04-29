/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.valuehandling;

import java.util.Optional;

import org.hibernate.validator.internal.util.IgnoreJava6Requirement;
import org.hibernate.validator.internal.util.TypeResolutionHelper;

/**
 * Unwraps an {@code Optional} and returns the wrapped value and type. Empty {@code Optional} value is returned as
 * {@code null}.
 *
 * @author Khalid Alqinyah
 */
@IgnoreJava6Requirement
public class OptionalValueUnwrapper extends TypeResolverBasedValueUnwrapper<Optional<?>> {

	public OptionalValueUnwrapper(TypeResolutionHelper typeResolutionHelper) {
		super( typeResolutionHelper );
	}

	@Override
	public Object handleValidatedValue(Optional<?> value) {
		if ( value == null ) {
			return null;
		}

		if ( value.isPresent() ) {
			return value.get();
		}

		return null;
	}
}
