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

import javax.validation.constraints.Size;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Check that the length of an array is between <i>min</i> and <i>max</i>
 *
 * @author Hardy Ferentschik
 */
public abstract class SizeValidatorForArraysOfPrimitives {
	
	private  static final Log log = LoggerFactory.make();

	protected int min;
	protected int max;

	public void initialize(Size parameters) {
		min = parameters.min();
		max = parameters.max();
		validateParameters();
	}

	private void validateParameters() {
		if ( min < 0 ) {
			throw log.getMinCannotBeNegativeException();
		}
		if ( max < 0 ) {
			throw log.getMaxCannotBeNegativeException();
		}
		if ( max < min ) {
			throw log.getLengthCannotBeNegativeException();
		}
	}
}
