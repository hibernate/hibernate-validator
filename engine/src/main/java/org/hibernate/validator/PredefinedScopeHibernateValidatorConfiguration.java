/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator;

import java.util.Locale;
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

	@Incubating
	PredefinedScopeHibernateValidatorConfiguration initializeBeanMetaData(Set<Class<?>> beanClassesToInitialize);

	/**
	 * @deprecated Planned for removal, use {@link BaseHibernateValidatorConfiguration#locales(Set)} instead.
	 */
	@Incubating
	@Deprecated
	PredefinedScopeHibernateValidatorConfiguration initializeLocales(Set<Locale> locales);
}
