/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.constraintvalidation;

import java.security.AccessController;
import java.security.PrivilegedAction;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;

import org.hibernate.validator.internal.util.privilegedactions.NewInstance;

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
		return run( NewInstance.action( key, "ConstraintValidator" ) );
	}

	@Override
	public void releaseInstance(ConstraintValidator<?, ?> instance) {
		// noop
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
