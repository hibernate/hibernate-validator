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

import java.util.Map;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Size;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Check that a string's length is between min and max.
 *
 * @author Hardy Ferentschik
 */
public class SizeValidatorForMap implements ConstraintValidator<Size, Map<?, ?>> {

	private static final Log log = LoggerFactory.make();

	private int min;
	private int max;

	public void initialize(Size parameters) {
		min = parameters.min();
		max = parameters.max();
		validateParameters();
	}

	/**
	 * Checks the number of entries in a map.
	 *
	 * @param map The map to validate.
	 * @param constraintValidatorContext context in which the constraint is evaluated.
	 *
	 * @return Returns <code>true</code> if the map is <code>null</code> or the number of entries in <code>map</code>
	 *         is between the specified <code>min</code> and <code>max</code> values (inclusive),
	 *         <code>false</code> otherwise.
	 */
	public boolean isValid(Map<?, ?> map, ConstraintValidatorContext constraintValidatorContext) {
		if ( map == null ) {
			return true;
		}
		int size = map.size();
		return size >= min && size <= max;
	}

	private void validateParameters() {
		if ( min < 0 ) {
			throw log.getMaxCannotBeNegativeException();
		}
		if ( max < 0 ) {
			throw log.getMaxCannotBeNegativeException();
		}
		if ( max < min ) {
			throw log.getLengthCannotBeNegativeException();
		}
	}
}
