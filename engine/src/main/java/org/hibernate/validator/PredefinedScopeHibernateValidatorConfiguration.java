package org.hibernate.validator;

import java.util.Set;

/**
 * Extension of {@link HibernateValidatorConfiguration} with additional methods dedicated to defining the predefined
 * scope of bean validation e.g. validated classes, constraint validators...
 *
 * @author Guillaume Smet
 *
 * @since 6.1
 */
@Incubating
public interface PredefinedScopeHibernateValidatorConfiguration extends BaseHibernateValidatorConfiguration<PredefinedScopeHibernateValidatorConfiguration> {

	@Incubating
	PredefinedScopeHibernateValidatorConfiguration builtinConstraints(Set<String> constraints);

	/**
	 * Specify the bean classes whose metadata should be initialized when building the validator factory.
	 * Types configured through programmatic constraint mappings, including mappings created by
	 * {@link org.hibernate.validator.spi.cfg.ConstraintMappingContributor constraint mapping contributors},
	 * are initialized in addition to the specified classes.
	 *
	 * @param beanClassesToInitialize the bean classes to initialize
	 * @return {@code this} for chaining configuration method calls
	 */
	@Incubating
	PredefinedScopeHibernateValidatorConfiguration initializeBeanMetaData(Set<Class<?>> beanClassesToInitialize);

	/**
	 * Specify whether to append the {@link #builtinConstraints(Set) built-in constraints} and {@link #initializeBeanMetaData(Set) beans to initialize}
	 * with constraints and beans provided only through XML mapping.
	 * <p>
	 * This option is enabled by default.
	 *
	 * @param include Whether to include the beans defined only in xml as part of the {@link #initializeBeanMetaData(Set) set of beans to initialize}
	 * and also add built-in constraints used only in xml definitions as part of the {@link #builtinConstraints(Set) set of built-in constraints}.
	 * @return {@code this} for chaining configuration method calls.
	 */
	@Incubating
	PredefinedScopeHibernateValidatorConfiguration includeBeansAndConstraintsDefinedOnlyInXml(boolean include);
}
