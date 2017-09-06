/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator;

import javax.validation.BootstrapConfiguration;

/**
 * Contains Hibernate Validator specific bootstrap configurations.
 *
 * @author Marko Bekhta
 * @since 6.1
 */
public interface HibernateValidatorBootstrapConfiguration extends BootstrapConfiguration {
	/**
	 * Class name of the {@link org.hibernate.validator.cfg.scriptengine.ScriptEvaluatorFactory} implementation or
	 * {@code null} if none is specified.
	 *
	 * @return script evaluator factory class name or {@code null}
	 *
	 * @since 6.1
	 */
	default String getScriptEvaluatorFactoryClassName() {
		return "org.hibernate.validator.internal.util.scriptengine.DefaultLookupScriptEvaluatorFactory";
	}
}
