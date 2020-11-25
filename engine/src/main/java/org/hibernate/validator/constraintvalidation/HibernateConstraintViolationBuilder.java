/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator.constraintvalidation;

import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;

public interface HibernateConstraintViolationBuilder extends ConstraintViolationBuilder {

	/**
	 * Enable Expression Language with the default Expression Language feature level for the constraint violation
	 * created by this builder if the chosen {@code MessageInterpolator} supports it.
	 * <p>
	 * If you enable this, you need to make sure your message template does not contain any unescaped user input (such as
	 * the validated value): use {@code addExpressionVariable()} to inject properly escaped variables into the template.
	 *
	 * @since 6.2
	 */
	@Incubating
	default HibernateConstraintViolationBuilder enableExpressionLanguage() {
		return enableExpressionLanguage( ExpressionLanguageFeatureLevel.DEFAULT );
	};

	/**
	 * Enable Expression Language for the constraint violation created by this builder if the chosen
	 * {@code MessageInterpolator} supports it.
	 * <p>
	 * If you enable this, you need to make sure your message template does not contain any unescaped user input (such as
	 * the validated value): use {@code addExpressionVariable()} to inject properly escaped variables into the template.
	 *
	 * @param level The Expression Language features level supported.
	 * @since 6.2
	 */
	@Incubating
	HibernateConstraintViolationBuilder enableExpressionLanguage(ExpressionLanguageFeatureLevel level);
}
