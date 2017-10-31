/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.constraintvalidation;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.spi.scripting.ScriptEvaluator;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorFactory;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorNotFoundException;

/**
 * Provides contextual data and operations when initializing a constraint validator.
 *
 * @author Marko Bekhta
 * @since 6.0.5
 */
@Incubating
public interface HibernateConstraintValidatorInitializationContext {

	/**
	 * Returns a {@link ScriptEvaluator} created by the {@link ScriptEvaluatorFactory}
	 * passed at bootstrap.
	 *
	 * @param languageName the name of the scripting language
	 *
	 * @return a script evaluator for the given language. Never null.
	 *
	 * @throws ScriptEvaluatorNotFoundException in case no {@link ScriptEvaluator} was
	 * found for a given {@code languageName}
	 */
	ScriptEvaluator getScriptEvaluatorForLanguage(String languageName);
}
