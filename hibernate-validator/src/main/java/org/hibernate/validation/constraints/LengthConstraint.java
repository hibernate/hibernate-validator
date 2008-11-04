// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.constraints;

import javax.validation.Constraint;
import javax.validation.ConstraintContext;

/**
 * Check that a string's length is between min and max.
 *
 * @author Emmanuel Bernard
 * @author Gavin King
 */
public class LengthConstraint implements Constraint<Length> {
	private int min;
	private int max;

	public void initialize(Length parameters) {
		min = parameters.min();
		max = parameters.max();
	}

	public boolean isValid(Object value, ConstraintContext constraintContext) {
		if ( value == null ) {
			return true;
		}
		if ( !( value instanceof String ) ) {
			throw new IllegalArgumentException( "Expected String type." );
		}
		String string = ( String ) value;
		int length = string.length();
		return length >= min && length <= max;
	}

}
