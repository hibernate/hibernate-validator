/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.testutil;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public class CountValidationCallsValidator implements ConstraintValidator<CountValidationCalls, Object> {
	private static final ThreadLocal<Integer> NUMBER_OF_VALIDATION_CALLS = new ThreadLocal<Integer>() {
		@Override
		protected Integer initialValue() {
			return 0;
		}
	};

	public static void init() {
		NUMBER_OF_VALIDATION_CALLS.set( 0 );
	}

	public static int getNumberOfValidationCall() {
		return NUMBER_OF_VALIDATION_CALLS.get();
	}

	public void initialize(CountValidationCalls constraintAnnotation) {
	}

	public boolean isValid(Object value, ConstraintValidatorContext context) {
		NUMBER_OF_VALIDATION_CALLS.set( NUMBER_OF_VALIDATION_CALLS.get() + 1 );
		return true;
	}
}
