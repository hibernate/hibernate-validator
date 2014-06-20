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
package org.hibernate.validator.test.constraintvalidator;

import java.util.List;
import javax.validation.ConstraintValidator;

import org.hibernate.validator.spi.constraintvalidator.ConstraintValidatorContribution;
import org.hibernate.validator.spi.constraintvalidator.ConstraintValidatorLocator;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

/**
 * @author Hardy Ferentschik
 */
public class AcmeConstraintValidatorLocator implements ConstraintValidatorLocator {
	private final boolean keepDefaults;

	public AcmeConstraintValidatorLocator(boolean keepDefaults) {
		this.keepDefaults = keepDefaults;
	}

	@Override
	public List<ConstraintValidatorContribution<?>> getConstraintValidatorContributions() {
		List<ConstraintValidatorContribution<?>> constraintValidatorContributions = newArrayList();

		List<Class<? extends ConstraintValidator<AcmeConstraint, ?>>> acmeConstraintValidators = newArrayList();
		acmeConstraintValidators.add( AcmeConstraint.AcmeConstraintValidator.class );
		ConstraintValidatorContribution<?> validatorContribution = new ConstraintValidatorContribution<AcmeConstraint>(
				AcmeConstraint.class, acmeConstraintValidators, keepDefaults
		);
		constraintValidatorContributions.add( validatorContribution );

		List<Class<? extends ConstraintValidator<AcmeConstraintWithDefaultValidator, ?>>> defaultAcmeConstraintValidators = newArrayList();
		defaultAcmeConstraintValidators.add( AcmeConstraintWithDefaultValidator.DefaultAcmeConstraintValidator.class );
		validatorContribution = new ConstraintValidatorContribution<AcmeConstraintWithDefaultValidator>(
				AcmeConstraintWithDefaultValidator.class, defaultAcmeConstraintValidators, keepDefaults
		);
		constraintValidatorContributions.add( validatorContribution );

		return constraintValidatorContributions;
	}
}


