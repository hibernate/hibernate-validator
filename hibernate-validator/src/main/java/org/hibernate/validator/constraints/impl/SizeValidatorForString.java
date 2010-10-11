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
import javax.validation.constraints.Size;

/**
 * Check that a string's length is between min and max.
 *
 * @author Emmanuel Bernard
 * @author Gavin King
 * @author Hardy Ferentschik
 */
public class SizeValidatorForString implements ConstraintValidator<Size, String> {
	private int min;
	private int max;

	public void initialize(Size parameters) {
		min = parameters.min();
		max = parameters.max();
		validateParameters();
	}

	/**
	 * Checks the length of the specified string.
	 *
	 * @param s The string to validate.
	 * @param constraintValidatorContext context in which the constraint is evaluated.
	 *
	 * @return Returns <code>true</code> if the string is <code>null</code> or the length of <code>s</code> between the specified
	 *         <code>min</code> and <code>max</code> values (inclusive), <code>false</code> otherwise.
	 */
	public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
		if ( s == null ) {
			return true;
		}
		int length = s.length();
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
