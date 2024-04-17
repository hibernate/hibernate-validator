/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.constraintvalidation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;

import org.hibernate.validator.internal.util.actions.NewInstance;

/**
 * Default {@code ConstraintValidatorFactory} using a no-arg constructor.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
//TODO Can we make the constructor non-public?
public class ConstraintValidatorFactoryImpl implements ConstraintValidatorFactory {

	@Override
	public final <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
		return NewInstance.action( key, "ConstraintValidator" );
	}

	@Override
	public void releaseInstance(ConstraintValidator<?, ?> instance) {
		// noop
	}
}
