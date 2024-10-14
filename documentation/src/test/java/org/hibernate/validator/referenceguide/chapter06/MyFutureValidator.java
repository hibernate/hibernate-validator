/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter06;

//end::include[]
import java.time.Clock;
import java.time.Instant;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;

@SuppressWarnings("unused")
//tag::include[]
public class MyFutureValidator implements HibernateConstraintValidator<MyFuture, Instant> {

	private Clock clock;

	private boolean orPresent;

	@Override
	public void initialize(ConstraintDescriptor<MyFuture> constraintDescriptor,
			HibernateConstraintValidatorInitializationContext initializationContext) {
		this.orPresent = constraintDescriptor.getAnnotation().orPresent();
		this.clock = initializationContext.getClockProvider().getClock();
	}

	@Override
	public boolean isValid(Instant instant, ConstraintValidatorContext constraintContext) {
		//...

		return false;
	}
}
//end::include[]
