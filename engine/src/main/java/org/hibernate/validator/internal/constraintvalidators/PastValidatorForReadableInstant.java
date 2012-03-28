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
package org.hibernate.validator.internal.constraintvalidators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Past;

import org.joda.time.ReadableInstant;

/**
 * Check if Joda Time type who implements
 * {@code org.joda.time.ReadableInstant}
 * is in the past.
 *
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public class PastValidatorForReadableInstant implements ConstraintValidator<Past, ReadableInstant> {

	public void initialize(Past constraintAnnotation) {
	}

	public boolean isValid(ReadableInstant value, ConstraintValidatorContext context) {
		//null values are valid
		if ( value == null ) {
			return true;
		}

		return value.isBefore( null );
	}
}
