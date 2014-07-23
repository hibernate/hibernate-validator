/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.engine.constraintvalidation;

import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;

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
