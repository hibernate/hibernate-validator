/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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

package org.hibernate.validator;

import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.ValidatorContext;

import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

/**
 * Represents a Hibernate Validator specific context that is used to create
 * {@link javax.validation.Validator} instances. Adds additional configuration options to those
 * provided by {@link ValidatorContext}.
 *
 * @author Emmanuel Bernard
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public interface HibernateValidatorContext extends ValidatorContext {

	@Override
	HibernateValidatorContext messageInterpolator(MessageInterpolator messageInterpolator);

	@Override
	HibernateValidatorContext traversableResolver(TraversableResolver traversableResolver);

	@Override
	HibernateValidatorContext constraintValidatorFactory(ConstraintValidatorFactory factory);

	/**
	 * En- or disables the fail fast mode. When fail fast is enabled the validation
	 * will stop on the first constraint violation detected.
	 *
	 * @param failFast {@code true} to enable fail fast, {@code false} otherwise.
	 *
	 * @return {@code this} following the chaining method pattern
	 */
	HibernateValidatorContext failFast(boolean failFast);

	/**
	 * Registers the given validated value unwrapper with the bootstrapped validator. When validating an element which
	 * is of a type supported by the unwrapper and which is annotated with
	 * {@link org.hibernate.validator.valuehandling.UnwrapValidatedValue}, the unwrapper will be applied to retrieve the
	 * value to validate.
	 *
	 * @param handler the unwrapper to register
	 *
	 * @return {@code this} following the chaining method pattern
	 *
	 * @hv.experimental This API is considered experimental and may change in future revisions
	 */
	HibernateValidatorContext addValidationValueHandler(ValidatedValueUnwrapper<?> handler);
}
