/*
* JBoss, Home of Professional Open Source
* Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import org.hibernate.validator.constraints.EAN;

/**
 * Checks that a given character sequence (e.g. string) is a valid EAN barcode.
 *
 * @author Hardy Ferentschik
 */
public class EANValidator implements ConstraintValidator<EAN, CharSequence> {

	private int size;

	@Override
	public void initialize(EAN constraintAnnotation) {
		switch ( constraintAnnotation.type() ) {
			case EAN8: {
				size = 8;
				break;
			}
			case EAN13: {
				size = 13;
				break;
			}
		}
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}
		int length = value.length();
		return length == size;
	}
}
