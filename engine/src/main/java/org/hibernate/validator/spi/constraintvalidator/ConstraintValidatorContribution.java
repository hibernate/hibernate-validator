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
package org.hibernate.validator.spi.constraintvalidator;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintValidator;

/**
 * Type-safe wrapper class for a constraint annotation and its potential list of constraint validators.
 *
 * @author Hardy Ferentschik
 */
public class ConstraintValidatorContribution<A extends Annotation> {
	private final Class<A> constraintType;
	private final List<Class<? extends ConstraintValidator<A, ?>>> constraintValidators = new ArrayList<Class<? extends ConstraintValidator<A, ?>>>();
	private final boolean keepDefaults;

	public ConstraintValidatorContribution(Class<A> constraintType,
			List<Class<? extends ConstraintValidator<A, ?>>> constraintValidators,
			boolean keepDefaults) {
		this.constraintType = constraintType;
		this.constraintValidators.addAll( constraintValidators );
		this.keepDefaults = keepDefaults;
	}

	/**
	 * Returns the constraint annotation type for which this instance provides constraint validator instances.
	 *
	 * @return the constraint annotation type for which this instance provides constraint validator instances.
	 */
	public Class<A> getConstraintType() {
		return constraintType;
	}

	/**
	 * Returns a list of constraint validator types for the constraint type of this instance.
	 *
	 * @return Returns a list of constraint validator types for the constraint type of this instance.
	 */
	public List<Class<? extends ConstraintValidator<A, ?>>> getConstraintValidators() {
		return constraintValidators;
	}

	/**
	 * Whether or not the default constraint validators should be kept or not.
	 *
	 * @return Returns {@code true} if the default constraint validator instances for the constraint type wrapped by this
	 * instance should be kept, {@code false} otherwise.
	 */
	public boolean keepDefaults() {
		return keepDefaults;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		ConstraintValidatorContribution that = (ConstraintValidatorContribution) o;

		if ( !constraintType.equals( that.constraintType ) ) {
			return false;
		}
		if ( !constraintValidators.equals( that.constraintValidators ) ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = constraintType.hashCode();
		result = 31 * result + constraintValidators.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "ConstraintValidatorContribution{" +
				"constraintType=" + constraintType +
				", constraintValidators=" + constraintValidators +
				'}';
	}
}


