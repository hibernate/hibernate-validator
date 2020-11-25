/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import jakarta.validation.Configuration;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.TraversableResolver;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.constraints.ParameterScriptAssert;
import org.hibernate.validator.constraints.ScriptAssert;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;
import org.hibernate.validator.spi.messageinterpolation.LocaleResolver;
import org.hibernate.validator.metadata.BeanMetaDataClassNormalizer;
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider;
import org.hibernate.validator.spi.properties.GetterPropertySelectionStrategy;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;
import org.hibernate.validator.spi.scripting.ScriptEvaluator;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorFactory;

/**
 * Base interface for Hibernate Validator specific configurations.
 * <p>
 * Should not be used directly, prefer {@link HibernateValidatorConfiguration} or
 * {@link PredefinedScopeHibernateValidatorConfiguration}.
 *
 * @author Emmanuel Bernard
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Hardy Ferentschik
 * @author Chris Beckey &lt;cbeckey@paypal.com&gt;
 */
public interface BaseHibernateValidatorConfiguration<S extends BaseHibernateValidatorConfiguration<S>> extends Configuration<S> {
	/**
	 * Property corresponding to the {@link #failFast} method.
	 * Accepts {@code true} or {@code false}. Defaults to {@code false}.
	 */
	String FAIL_FAST = "hibernate.validator.fail_fast";

	/**
	 * Property corresponding to the {@link #allowOverridingMethodAlterParameterConstraint} method.
	 * Accepts {@code true} or {@code false}.
	 * Defaults to {@code false}.
	 */
	String ALLOW_PARAMETER_CONSTRAINT_OVERRIDE = "hibernate.validator.allow_parameter_constraint_override";

	/**
	 * Property corresponding to the {@link #allowMultipleCascadedValidationOnReturnValues} method.
	 * Accepts {@code true} or {@code false}.
	 * Defaults to {@code false}.
	 */
	String ALLOW_MULTIPLE_CASCADED_VALIDATION_ON_RESULT = "hibernate.validator.allow_multiple_cascaded_validation_on_result";

	/**
	 * Property corresponding to the {@link #allowParallelMethodsDefineParameterConstraints} method.
	 * Accepts {@code true} or {@code false}.
	 * Defaults to {@code false}.
	 */
	String ALLOW_PARALLEL_METHODS_DEFINE_PARAMETER_CONSTRAINTS = "hibernate.validator.allow_parallel_method_parameter_constraint";

	/**
	 * @deprecated planned for removal. Use hibernate.validator.constraint_mapping_contributors instead.
	 * @since 5.2
	 */
	@Deprecated
	String CONSTRAINT_MAPPING_CONTRIBUTOR = "hibernate.validator.constraint_mapping_contributor";

	/**
	 * Property for configuring constraint mapping contributors, allowing to set up one or more constraint mappings for
	 * the default validator factory. Accepts a String with the comma separated fully-qualified class names of one or more
	 * {@link org.hibernate.validator.spi.cfg.ConstraintMappingContributor} implementations.
	 *
	 * @since 5.3
	 */
	String CONSTRAINT_MAPPING_CONTRIBUTORS = "hibernate.validator.constraint_mapping_contributors";

	/**
	 * Property corresponding to the {@link #enableTraversableResolverResultCache(boolean)}.
	 * Accepts {@code true} or {@code false}.
	 * Defaults to {@code true}.
	 *
	 * @since 6.0.3
	 */
	String ENABLE_TRAVERSABLE_RESOLVER_RESULT_CACHE = "hibernate.validator.enable_traversable_resolver_result_cache";

	/**
	 * Property for configuring the script evaluator factory, allowing to set up which factory will be used to create
	 * {@link ScriptEvaluator}s for evaluation of script expressions in
	 * {@link ScriptAssert} and {@link ParameterScriptAssert}
	 * constraints. A fully qualified name of a class implementing {@link ScriptEvaluatorFactory} is expected as a value.
	 *
	 * @since 6.0.3
	 */
	@Incubating
	String SCRIPT_EVALUATOR_FACTORY_CLASSNAME = "hibernate.validator.script_evaluator_factory";

	/**
	 * Property for configuring temporal validation tolerance, allowing to set the acceptable margin of error when
	 * comparing date/time in temporal constraints. In milliseconds.
	 *
	 * @since 6.0.5
	 */
	@Incubating
	String TEMPORAL_VALIDATION_TOLERANCE = "hibernate.validator.temporal_validation_tolerance";

	/**
	 * Property for configuring the getter property selection strategy, allowing to set which rules will be applied
	 * to determine if a method is a valid JavaBean getter.
	 *
	 * @since 6.1.0
	 */
	@Incubating
	String GETTER_PROPERTY_SELECTION_STRATEGY_CLASSNAME = "hibernate.validator.getter_property_selection_strategy";

	/**
	 * Property for configuring the property node name provider, allowing to select an implementation of {@link PropertyNodeNameProvider}
	 * which will be used for property name resolution when creating a property path.
	 *
	 * @since 6.1.0
	 */
	@Incubating
	String PROPERTY_NODE_NAME_PROVIDER_CLASSNAME = "hibernate.validator.property_node_name_provider";

	/**
	 * Property for configuring the locale resolver, allowing to select an implementation of {@link LocaleResolver}
	 * which will be used for locale resolution when interpolating a message.
	 *
	 * @since 6.1.1
	 */
	@Incubating
	String LOCALE_RESOLVER_CLASSNAME = "hibernate.validator.locale_resolver";

	/**
	 * Property for configuring the Expression Language feature level for constraints, allowing to define which
	 * Expression Language features are available for message interpolation.
	 * <p>
	 * This property only affects the EL feature level of "static" constraint violation messages. In particular, it
	 * doesn't affect the default EL feature level for custom violations. Refer to
	 * {@link #CUSTOM_VIOLATION_EXPRESSION_LANGUAGE_FEATURE_LEVEL} to configure that.
	 *
	 * @since 6.2
	 */
	@Incubating
	String CONSTRAINT_EXPRESSION_LANGUAGE_FEATURE_LEVEL = "hibernate.validator.constraint_expression_language_feature_level";

	/**
	 * Property for configuring the Expression Language feature level for custom violations, allowing to define which
	 * Expression Language features are available for message interpolation.
	 *
	 * @since 6.2
	 */
	@Incubating
	String CUSTOM_VIOLATION_EXPRESSION_LANGUAGE_FEATURE_LEVEL = "hibernate.validator.custom_violation_expression_language_feature_level";

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
	 * Returns the default {@link ValueExtractor} implementations as per the
	 * specification.
	 *
	 * @return the default {@code ValueExtractor} implementations compliant
	 * with the specification
	 *
	 * @since 6.0
	 */
	@Incubating
	Set<ValueExtractor<?>> getDefaultValueExtractors();

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
	S addMapping(ConstraintMapping mapping);

	/**
	 * En- or disables the fail fast mode. When fail fast is enabled the validation
	 * will stop on the first constraint violation detected.
	 *
	 * @param failFast {@code true} to enable fail fast, {@code false} otherwise.
	 *
	 * @return {@code this} following the chaining method pattern
	 */
	S failFast(boolean failFast);

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
	 * @param externalClassLoader The class loader for loading user-provided resources.
	 *
	 * @return {@code this} following the chaining method pattern
	 *
	 * @since 5.2
	 */
	S externalClassLoader(ClassLoader externalClassLoader);

	/**
	 * Define whether overriding methods that override constraints should throw a {@code ConstraintDefinitionException}.
	 * The default value is {@code false}, i.e. do not allow.
	 * <p>
	 * See Section 4.5.5 of the JSR 380 specification, specifically
	 * <pre>
	 * "In sub types (be it sub classes/interfaces or interface implementations), no parameter constraints may
	 * be declared on overridden or implemented methods, nor may parameters be marked for cascaded validation.
	 * This would pose a strengthening of preconditions to be fulfilled by the caller."
	 * </pre>
	 *
	 * @param allow flag determining whether validation will allow overriding to alter parameter constraints.
	 *
	 * @return {@code this} following the chaining method pattern
	 *
	 * @since 5.3
	 */
	S allowOverridingMethodAlterParameterConstraint(boolean allow);

	/**
	 * Define whether more than one constraint on a return value may be marked for cascading validation are allowed.
	 * The default value is {@code false}, i.e. do not allow.
	 * <p>
	 * See Section 4.5.5 of the JSR 380 specification, specifically
	 * <pre>
	 * "One must not mark a method return value for cascaded validation more than once in a line of a class hierarchy.
	 * In other words, overriding methods on sub types (be it sub classes/interfaces or interface implementations)
	 * cannot mark the return value for cascaded validation if the return value has already been marked on the
	 * overridden method of the super type or interface."
	 * </pre>
	 *
	 * @param allow flag determining whether validation will allow multiple cascaded validation on return values.
	 *
	 * @return {@code this} following the chaining method pattern
	 *
	 * @since 5.3
	 */
	S allowMultipleCascadedValidationOnReturnValues(boolean allow);

	/**
	 * Define whether parallel methods that define constraints should throw a {@code ConstraintDefinitionException}. The
	 * default value is {@code false}, i.e. do not allow.
	 * <p>
	 * See Section 4.5.5 of the JSR 380 specification, specifically
	 * <pre>
	 * "If a sub type overrides/implements a method originally defined in several parallel types of the hierarchy
	 * (e.g. two interfaces not extending each other, or a class and an interface not implemented by said class),
	 * no parameter constraints may be declared for that method at all nor parameters be marked for cascaded validation.
	 * This again is to avoid an unexpected strengthening of preconditions to be fulfilled by the caller."
	 * </pre>
	 *
	 * @param allow flag determining whether validation will allow parameter constraints in parallel hierarchies
	 *
	 * @return {@code this} following the chaining method pattern
	 *
	 * @since 5.3
	 */
	S allowParallelMethodsDefineParameterConstraints(boolean allow);

	/**
	 * Define whether the per validation call caching of {@link TraversableResolver} results is enabled. The default
	 * value is {@code true}, i.e. the caching is enabled.
	 * <p>
	 * This behavior was initially introduced to cache the {@code JPATraversableResolver} results but the map lookups it
	 * introduces can be counterproductive when the {@code TraversableResolver} calls are very fast.
	 *
	 * @param enabled flag determining whether per validation call caching is enabled for {@code TraversableResolver}
	 * results.
	 *
	 * @return {@code this} following the chaining method pattern
	 *
	 * @since 6.0.3
	 */
	S enableTraversableResolverResultCache(boolean enabled);

	/**
	 * Allows to specify a custom {@link ScriptEvaluatorFactory} responsible for creating {@link ScriptEvaluator}s
	 * used to evaluate script expressions for {@link ScriptAssert} and {@link ParameterScriptAssert} constraints.
	 *
	 * @param scriptEvaluatorFactory the {@link ScriptEvaluatorFactory} to be used
	 *
	 * @return {@code this} following the chaining method pattern
	 *
	 * @since 6.0.3
	 */
	@Incubating
	S scriptEvaluatorFactory(ScriptEvaluatorFactory scriptEvaluatorFactory);

	/**
	 * Allows to set the acceptable margin of error when comparing date/time in temporal constraints such as
	 * {@link Past}/{@link PastOrPresent} and {@link Future}/{@link FutureOrPresent}.
	 *
	 * @param temporalValidationTolerance the acceptable tolerance
	 *
	 * @return {@code this} following the chaining method pattern
	 *
	 * @since 6.0.5
	 */
	@Incubating
	S temporalValidationTolerance(Duration temporalValidationTolerance);

	/**
	 * Allows to set a payload which will be passed to the constraint validators. If the method is called multiple
	 * times, only the payload passed last will be propagated.
	 *
	 * @param constraintValidatorPayload the payload passed to constraint validators
	 *
	 * @return {@code this} following the chaining method pattern
	 *
	 * @since 6.0.8
	 */
	@Incubating
	S constraintValidatorPayload(Object constraintValidatorPayload);

	/**
	 * Allows to set a getter property selection strategy defining the rules determining if a method is a getter
	 * or not.
	 *
	 * @param getterPropertySelectionStrategy the {@link GetterPropertySelectionStrategy} to be used
	 *
	 * @return {@code this} following the chaining method pattern
	 *
	 * @since 6.1.0
	 */
	@Incubating
	S getterPropertySelectionStrategy(GetterPropertySelectionStrategy getterPropertySelectionStrategy);

	/**
	 * Allows to set a property node name provider, defining how the name of a property node will be resolved
	 * when constructing a property path as the one returned by {@link ConstraintViolation#getPropertyPath()}.
	 *
	 * @param propertyNodeNameProvider the {@link PropertyNodeNameProvider} to be used
	 *
	 * @return {@code this} following the chaining method pattern
	 *
	 * @since 6.1.0
	 */
	@Incubating
	S propertyNodeNameProvider(PropertyNodeNameProvider propertyNodeNameProvider);

	/**
	 * Allows setting the list of the locales supported by this ValidatorFactory.
	 * <p>
	 * Can be used for advanced locale resolution and/or to force the initialization of the resource bundles at
	 * bootstrap.
	 * <p>
	 * If not set, defaults to a singleton containing {@link Locale#getDefault()}.
	 *
	 * @since 6.1.1
	 */
	@Incubating
	S locales(Set<Locale> locales);

	/**
	 * Allows setting the list of the locales supported by this ValidatorFactory.
	 * <p>
	 * Can be used for advanced locale resolution and/or to force the initialization of the resource bundles at
	 * bootstrap.
	 * <p>
	 * If not set, defaults to a singleton containing {@link Locale#getDefault()}.
	 *
	 * @since 6.1.1
	 */
	@Incubating
	default S locales(Locale... locales) {
		return locales( new HashSet<>( Arrays.asList( locales ) ) );
	}

	/**
	 * Allows setting the default locale used to interpolate the constraint violation messages.
	 * <p>
	 * If not set, defaults to the system locale obtained via {@link Locale#getDefault()}.
	 *
	 * @since 6.1.1
	 */
	@Incubating
	S defaultLocale(Locale defaultLocale);

	/**
	 * Allows setting a locale resolver, defining how the locale will be resolved when interpolating the message of a constraint violation.
	 *
	 * @param localeResolver the {@link LocaleResolver} to be used
	 *
	 * @return {@code this} following the chaining method pattern
	 *
	 * @since 6.1.1
	 */
	@Incubating
	S localeResolver(LocaleResolver localeResolver);

	@Incubating
	S beanMetaDataClassNormalizer(BeanMetaDataClassNormalizer beanMetaDataClassNormalizer);

	/**
	 * Allows setting the Expression Language feature level for message interpolation of constraint messages.
	 * <p>
	 * This is the feature level used for messages hardcoded inside the constraint declaration.
	 * <p>
	 * If you are creating custom constraint violations, Expression Language support needs to be explicitly enabled and
	 * use the safest feature level by default if enabled.
	 *
	 * @param expressionLanguageFeatureLevel the {@link ExpressionLanguageFeatureLevel} to be used
	 * @return {@code this} following the chaining method pattern
	 *
	 * @since 6.2
	 */
	@Incubating
	S constraintExpressionLanguageFeatureLevel(ExpressionLanguageFeatureLevel expressionLanguageFeatureLevel);

	/**
	 * Allows setting the Expression Language feature level for message interpolation of custom violation messages.
	 * <p>
	 * This is the feature level used for messages of custom violations created by the {@link ConstraintValidatorContext}.
	 *
	 * @param expressionLanguageFeatureLevel the {@link ExpressionLanguageFeatureLevel} to be used
	 * @return {@code this} following the chaining method pattern
	 *
	 * @since 6.2
	 */
	@Incubating
	S customViolationExpressionLanguageFeatureLevel(ExpressionLanguageFeatureLevel expressionLanguageFeatureLevel);
}
