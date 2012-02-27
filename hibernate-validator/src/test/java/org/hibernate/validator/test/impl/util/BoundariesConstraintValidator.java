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
package org.hibernate.validator.test.impl.util;

import java.lang.annotation.Annotation;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Emmanuel Bernard
 */
public abstract class BoundariesConstraintValidator<T extends Annotation> implements ConstraintValidator<T, Integer> {
	private int low;
	private int high;

	protected void initialize(int low, int high) {
		this.low = low;
		this.high = high;
	}

	public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
		return value >= low && value <= high;
	}
}
