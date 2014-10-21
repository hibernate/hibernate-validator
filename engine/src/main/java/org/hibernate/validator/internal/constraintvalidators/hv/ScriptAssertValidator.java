/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.ScriptAssert;
import org.hibernate.validator.internal.util.Contracts;

import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

/**
 * Validator for the {@link ScriptAssert} constraint annotation.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public class ScriptAssertValidator implements ConstraintValidator<ScriptAssert, Object> {

	private String alias;
	private ScriptAssertContext scriptAssertContext;

	@Override
	public void initialize(ScriptAssert constraintAnnotation) {
		validateParameters( constraintAnnotation );

		this.alias = constraintAnnotation.alias();
		this.scriptAssertContext = new ScriptAssertContext( constraintAnnotation.lang(), constraintAnnotation.script() );
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
		return scriptAssertContext.evaluateScriptAssertExpression( value, alias );
	}

	private void validateParameters(ScriptAssert constraintAnnotation) {
		Contracts.assertNotEmpty( constraintAnnotation.script(), MESSAGES.parameterMustNotBeEmpty( "script" ) );
		Contracts.assertNotEmpty( constraintAnnotation.lang(), MESSAGES.parameterMustNotBeEmpty( "lang" ) );
		Contracts.assertNotEmpty( constraintAnnotation.alias(), MESSAGES.parameterMustNotBeEmpty( "alias" ) );
	}
}
