/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator;

import javax.validation.Configuration;

import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.spi.constraintdefinition.ConstraintDefinitionContributor;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;
import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

/**
 * Uniquely identifies Hibernate Validator in the Bean Validation bootstrap
 * strategy. Also contains Hibernate Validator specific configurations.
 *
 * @author Emmanuel Bernard
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
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
	 * Property corresponding to the {@link #addConstraintDefinitionContributor(org.hibernate.validator.spi.constraintdefinition.ConstraintDefinitionContributor)}
	 * method. Accepts a String with the comma-separated fully-qualified names of one or more {@link org.hibernate.validator.spi.constraintdefinition.ConstraintDefinitionContributor} implementations.
	 */
	String CONSTRAINT_DEFINITION_CONTRIBUTORS = "hibernate.validator.constraint_definition_contributors";

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

	/**
	 * @return the default {@link org.hibernate.validator.spi.constraintdefinition.ConstraintDefinitionContributor}. Never {@code null}.
	 */
	ConstraintDefinitionContributor getDefaultConstraintDefinitionContributor();

	/**
	 * Registers the given {@code ConstraintDefinitionContributor} with the bootstrapped validator factory.
	 *
	 * @param contributor the {@code ConstraintDefinitionContributor} to register. Cannot be {@code null}.
	 *
	 * @return {@code this} following the chaining method pattern
	 *
	 * @hv.experimental This API is considered experimental and may change in future revisions
	 */
	HibernateValidatorConfiguration addConstraintDefinitionContributor(ConstraintDefinitionContributor contributor);
}
