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
package org.hibernate.validator;

import javax.validation.Configuration;

import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;
import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

/**
 * Uniquely identifies Hibernate Validator in the Bean Validation bootstrap
 * strategy. Also contains Hibernate Validator specific configurations.
 *
 * @author Emmanuel Bernard
 * @author Gunnar Morling
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 * @author Hardy Ferentschik
 */
public interface HibernateValidatorConfiguration extends Configuration<HibernateValidatorConfiguration> {
	/**
	 * Property corresponding to the {@link #failFast} method.
	 * Accepts {@code true} or {@code false}. Defaults to {@code false}.
	 */
	String FAIL_FAST = "hibernate.validator.fail_fast";

	/**
	 * Property corresponding to the {@link #addValidatedValueHandler(ValidatedValueUnwrapper)} method. Accepts a String
	 * with the comma-separated fully-qualified names of one or more {@link ValidatedValueUnwrapper} implementations.
	 */
	String VALIDATED_VALUE_HANDLERS = "hibernate.validator.validated_value_handlers";

	/**
	 * <p>
	 * Returns the {@link ResourceBundleLocator} used by the
	 * {@link Configuration#getDefaultMessageInterpolator() default message
	 * interpolator} to load user-provided resource bundles. In conformance with
	 * the specification this default locator retrieves the bundle
	 * "ValidationMessages".
	 * </p>
	 * <p>
	 * This locator can be used as delegate for custom locators when setting a
	 * customized {@link org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator}:
	 * </p>
	 * <p/>
	 * <pre>
	 * {@code
	 * 	HibernateValidatorConfiguration configure =
	 *    Validation.byProvider(HibernateValidator.class).configure();
	 *
	 *  ResourceBundleLocator defaultResourceBundleLocator =
	 *    configure.getDefaultBundleLocator();
	 *  ResourceBundleLocator myResourceBundleLocator =
	 *    new MyResourceBundleLocator(defaultResourceBundleLocator);
	 *
	 *  configure.messageInterpolator(
	 *    new ResourceBundleMessageInterpolator(myResourceBundleLocator));
	 * }
	 * </pre>
	 *
	 * @return The default {@link ResourceBundleLocator}. Never null.
	 */
	ResourceBundleLocator getDefaultResourceBundleLocator();

	/**
	 * Creates a new constraint mapping which can be used to programmatically configure the constraints for given types. After
	 * the mapping has been set up, it must be added to this configuration via {@link #addMapping(ConstraintMapping)}.
	 *
	 * @return A new constraint mapping.
	 */
	ConstraintMapping createConstraintMapping();

	/**
	 * Adds the specified {@link ConstraintMapping} instance to the configuration. Constraints configured in {@code mapping}
	 * will be added to the constraints configured via annotations and/or xml.
	 *
	 * @param mapping {@code ConstraintMapping} instance containing programmatic configured constraints
	 *
	 * @return {@code this} following the chaining method pattern
	 *
	 * @throws IllegalArgumentException if {@code mapping} is {@code null}
	 */
	HibernateValidatorConfiguration addMapping(ConstraintMapping mapping);

	/**
	 * En- or disables the fail fast mode. When fail fast is enabled the validation
	 * will stop on the first constraint violation detected.
	 *
	 * @param failFast {@code true} to enable fail fast, {@code false} otherwise.
	 *
	 * @return {@code this} following the chaining method pattern
	 */
	HibernateValidatorConfiguration failFast(boolean failFast);

	/**
	 * Registers the given validated value unwrapper with the bootstrapped validator factory. When validating an element
	 * which is of a type supported by the unwrapper and which is annotated with
	 * {@link org.hibernate.validator.valuehandling.UnwrapValidatedValue}, the unwrapper will be applied to retrieve the
	 * value to validate.
	 *
	 * @param handler the unwrapper to register
	 *
	 * @return {@code this} following the chaining method pattern
	 *
	 * @hv.experimental This API is considered experimental and may change in future revisions
	 */
	HibernateValidatorConfiguration addValidatedValueHandler(ValidatedValueUnwrapper<?> handler);
}
