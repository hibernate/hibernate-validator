/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter09;

//end::include[]
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;

//tag::include[]
public class MyConstraintValidatorFactory implements ConstraintValidatorFactory {

	@Override
	public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
		//...
		return null;
	}

	@Override
	public void releaseInstance(ConstraintValidator<?, ?> instance) {
		//...
	}
}
//end::include[]
