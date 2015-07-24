/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator;

import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.spi.constraintdefinition.ConstraintDefinitionContributor;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;
import org.hibernate.validator.spi.time.TimeProvider;
import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

import javax.validation.Configuration;

/**
 * Uniquely identifies Hibernate Validator in the Bean Validation bootstrap
 * strategy. Also contains Hibernate Validator specific configurations.
 *
 * @author Emmanuel Bernard
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Hardy Ferentschik
 * @author Chris Beckey cbeckey@paypal.com
 */
public interface HibernateValidatorConfiguration extends Configuration<HibernateValidatorConfiguration> {
	/**
	 * Property corresponding to the {@link #failFast} method.
	 * Accepts {@code true} or {@code false}. Defaults to {@code false}.
	 */
	String FAIL_FAST = "hibernate.validator.fail_fast";

	/**
	 * Property corresponding to the {@link MethodValidationConfiguration#allowOverridingMethodAlterParameterConstraint} method.
	 * Accepts {@code true} or {@code false}. 
	 * Defaults to {@code false}.
	 */
	String ALLOW_PARAMETER_CONSTRAINT_OVERRIDE = "hibernate.validator.allow_parameter_constraint_override";
	
	/**
	 * Property corresponding to the {@link MethodValidationConfiguration#allowMultipleCascadedValidationOnReturnValues} method.
	 * Accepts {@code true} or {@code false}. 
	 * Defaults to {@code false}.
	 */
	String ALLOW_MULTIPLE_CASCADED_VALIDATION_ON_RESULT = "hibernate.validator.allow_multiple_cascaded_validation_on_result";

	/**
	 * Property corresponding to the {@link MethodValidationConfiguration#allowParallelMethodsDefineParameterConstraints} method.
	 * Accepts {@code true} or {@code false}. 
	 * Defaults to {@code false}.
	 */
	String ALLOW_PARALLEL_METHODS_DEFINE_PARAMETER_CONSTRAINTS = "hibernate.validator.allow_parallel_method_parameter_constraint";

	/**
	 * Property corresponding to the {@link #addValidatedValueHandler(ValidatedValueUnwrapper)} method. Accepts a String
	 * with the comma-separated fully-qualified names of one or more {@link ValidatedValueUnwrapper} implementations.
	 */
	String VALIDATED_VALUE_HANDLERS = "hibernate.validator.validated_value_handlers";

	/**
	 * Property corresponding to the {@link #addConstraintDefinitionContributor(org.hibernate.validator.spi.constraintdefinition.ConstraintDefinitionContributor)}
	 * method. Accepts a String with the comma-separated fully-qualified names of one or more {@link org.hibernate.validator.spi.constraintdefinition.ConstraintDefinitionContributor} implementations.
	 *
	 * @since 5.2
	 */
	String CONSTRAINT_DEFINITION_CONTRIBUTORS = "hibernate.validator.constraint_definition_contributors";

	/**
	 * Property for configuring a constraint mapping contributor, allowing to set up one or more constraint mappings for
	 * the default validator factory. Accepts a String with the fully-qualified class name of a
	 * {@link org.hibernate.validator.spi.cfg.ConstraintMappingContributor} implementation.
	 *
	 * @since 5.2
	 */
	String CONSTRAINT_MAPPING_CONTRIBUTOR = "hibernate.validator.constraint_mapping_contributor";

	/*
	 * Property corresponding to the {@link #timeProvider(TimeProvider)} method. Accepts a String with the
	 * fully-qualified class name of a {@link TimeProvider} implementation.
	 *
	 * @since 5.2
	 */
	String TIME_PROVIDER = "hibernate.validator.time_provider";


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
	boolean getFailFast();
	
	/**
	 * The properties contained in this property modify the behavior of the Validator with respect to 
	 * Specification section 4.5.5.
	 * In particular: "Out of the box, a conforming Bean Validation provider must throw a 
	 * ConstraintDeclarationException when discovering that any of these rules are violated. 
	 * In addition providers may implement alternative, potentially more liberal, approaches 
	 * for handling constrained methods in inheritance hierarchies. Possible means for activating 
	 * such alternative behavior include provider-specific configuration properties or annotations. 
	 * Note that client code relying on such alternative behavior is not portable between Bean 
	 * Validation providers."
	 * The getter method is not required for operation but it is available for convenience in
	 * testing.
	 * @param mvc the MethodValidationConfiguration instance
	 * @return {@code this} following the chaining method pattern
	 * @since 5.2
	 */
	HibernateValidatorConfiguration setMethodValidationConfiguration(MethodValidationConfiguration mvc);
	MethodValidationConfiguration getMethodValidationConfiguration();

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
	 * @since 5.2
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
	 * @since 5.2
	 */
	HibernateValidatorConfiguration addConstraintDefinitionContributor(ConstraintDefinitionContributor contributor);

	/**
	 * Sets the class loader to be used for loading user-provided resources:
	 * <ul>
	 * <li>XML descriptors ({@code META-INF/validation.xml} as well as XML constraint mappings)</li>
	 * <li>classes specified by name in XML descriptors (e.g. custom message interpolators etc.)</li>
	 * <li>the {@code ValidationMessages} resource bundle</li>
	 * </ul>
	 * If no class loader is given, these resources will be obtained through the thread context class loader and as a
	 * last fallback through Hibernate Validator's own class loader.
	 *
	 * @param externalClassLoader
	 *            The class loader for loading user-provided resources.
	 *
	 * @return {@code this} following the chaining method pattern
	 * @since 5.2
	 */
	HibernateValidatorConfiguration externalClassLoader(ClassLoader externalClassLoader);

	/**
	 * Registers the given time provider with the bootstrapped validator factory. This provider will be used to obtain
	 * the current time when validating {@code @Future} and {@code @Past} constraints. By default the current system
	 * time and the current default time zone will be used when validating these constraints.
	 *
	 * @param timeProvider the time provider to register. Must not be {@code null}
	 *
	 * @return {@code this} following the chaining method pattern
	 *
	 * @hv.experimental This API is considered experimental and may change in future revisions
	 * @since 5.2
	 */
	HibernateValidatorConfiguration timeProvider(TimeProvider timeProvider);
}
