/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.internal.engine.methodvalidation.service;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;

import org.joda.time.DateMidnight;

/**
 * @author Gunnar Morling
 */
@SupportedValidationTarget( value = ValidationTarget.PARAMETERS)
public class ConsistentDateParametersValidator implements ConstraintValidator<ConsistentDateParameters, Object[]> {

	@Override
	public void initialize(ConsistentDateParameters constraintAnnotation) {
		//nothing to do
	}

	@Override
	public boolean isValid(Object[] value, ConstraintValidatorContext context) {
		if ( value.length != 2 ) {
			throw new IllegalArgumentException( "Unexpected method signature" );
		}

		if ( value[0] == null || value[1] == null ) {
			return true;
		}

		if ( !( value[0] instanceof DateMidnight ) || !( value[1] instanceof DateMidnight ) ) {
			throw new IllegalArgumentException( "Unexpected method signature" );
		}

		return ( (DateMidnight) value[0] ).isBefore( (DateMidnight) value[1] );
	}
}
