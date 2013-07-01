/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.internal.constraintvalidators;

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
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
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
