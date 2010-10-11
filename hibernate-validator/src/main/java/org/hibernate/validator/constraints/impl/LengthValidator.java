/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.constraints.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Length;

/**
 * Check that a string's length is between min and max.
 *
 * @author Emmanuel Bernard
 * @author Gavin King
 */
public class LengthValidator implements ConstraintValidator<Length, String> {
	private int min;
	private int max;

	public void initialize(Length parameters) {
		min = parameters.min();
		max = parameters.max();
		validateParameters();
	}

	public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null ) {
			return true;
		}
		int length = value.length();
		return length >= min && length <= max;
	}

	private void validateParameters() {
		if ( min < 0 ) {
			throw new IllegalArgumentException( "The min parameter cannot be negative." );
		}
		if ( max < 0 ) {
			throw new IllegalArgumentException( "The max parameter cannot be negative." );
		}
		if ( max < min ) {
			throw new IllegalArgumentException( "The length cannot be negative." );
		}
	}
}
