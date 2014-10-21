/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.future;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Future;

import org.hibernate.validator.internal.util.IgnoreJava6Requirement;

import org.joda.time.ReadablePartial;

/**
 * Check if Joda Time type who implements
 * {@code org.joda.time.ReadablePartial}
 * is in the future.
 *
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
@IgnoreJava6Requirement
public class FutureValidatorForReadablePartial implements ConstraintValidator<Future, ReadablePartial> {

	@Override
	public void initialize(Future constraintAnnotation) {
	}

	@Override
	public boolean isValid(ReadablePartial value, ConstraintValidatorContext context) {
		//null values are valid
		if ( value == null ) {
			return true;
		}

		return value.toDateTime( null ).isAfterNow();
	}
}
