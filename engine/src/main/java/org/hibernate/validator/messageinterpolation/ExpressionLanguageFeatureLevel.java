/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.messageinterpolation;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

/**
 * Indicates the level of features enabled for the Expression Language engine.
 *
 * @since 6.2
 */
@Incubating
public enum ExpressionLanguageFeatureLevel {

	/**
	 * The default Expression Language feature level.
	 * <p>
	 * Depends on the context.
	 * <p>
	 * For standard constraint messages, it is {@link ExpressionLanguageFeatureLevel#BEAN_PROPERTIES}.
	 * <p>
	 * For custom violations, the default is {@link ExpressionLanguageFeatureLevel#NONE} and, if Expression Language is
	 * enabled for a given custom violation via the API, the default becomes
	 * {@link ExpressionLanguageFeatureLevel#VARIABLES} in the context of this given custom violation.
	 */
	DEFAULT("default"),

	/**
	 * Expression Language expressions are not interpolated.
	 */
	NONE("none"),

	/**
	 * Only allows access to the variables injected via
	 * {@link HibernateConstraintValidatorContext#addExpressionVariable(String, Object)}, the Jakarta Bean
	 * Validation-defined {@code formatter} and the {@code ResourceBundle}s.
	 */
	VARIABLES("variables"),

	/**
	 * Only allows to what is allowed with the {@code variables} level plus access to bean properties.
	 * <p>
	 * This is the minimal level to have a specification-compliant implementation.
	 */
	BEAN_PROPERTIES("bean-properties"),

	/**
	 * This level allows what is allowed with the {@code bean-properties} level plus bean methods execution and can lead
	 * to serious security issues, including arbitrary code execution, if not very carefully handled.
	 * <p>
	 * If using this level, you need to be sure you are not injecting user input in an expression without properly
	 * escaping it using {@link HibernateConstraintValidatorContext#addExpressionVariable(String, Object)}.
	 */
	BEAN_METHODS("bean-methods");

	private final String externalRepresentation;

	ExpressionLanguageFeatureLevel(String externalRepresentation) {
		this.externalRepresentation = externalRepresentation;
	}

	public static ExpressionLanguageFeatureLevel of(String value) {
		for ( ExpressionLanguageFeatureLevel level : values() ) {
			if ( level.externalRepresentation.equals( value ) ) {
				return level;
			}
		}

		return ExpressionLanguageFeatureLevel.valueOf( value );
	}

	public static ExpressionLanguageFeatureLevel interpretDefaultForConstraints(ExpressionLanguageFeatureLevel value) {
		if ( value == DEFAULT ) {
			return BEAN_PROPERTIES;
		}
		return value;
	}

	public static ExpressionLanguageFeatureLevel interpretDefaultForCustomViolations(ExpressionLanguageFeatureLevel value) {
		if ( value == DEFAULT ) {
			return VARIABLES;
		}
		return value;
	}
}
