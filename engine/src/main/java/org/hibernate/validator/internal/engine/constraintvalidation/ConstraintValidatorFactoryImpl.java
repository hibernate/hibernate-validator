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

import java.security.AccessControlContext;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;

import org.hibernate.validator.internal.util.ReflectionHelper;

/**
 * Default {@code ConstraintValidatorFactory} using a no-arg constructor.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class ConstraintValidatorFactoryImpl implements ConstraintValidatorFactory {

	private static final AccessControlContext ACCESS_CONTROL_CONTEXT = ReflectionHelper.getAccessControlContext();

	@Override
	public final <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
		return ReflectionHelper.newInstance( ACCESS_CONTROL_CONTEXT, key, "ConstraintValidator" );
	}

	@Override
	public void releaseInstance(ConstraintValidator<?, ?> instance) {
		// noop
	}
}
