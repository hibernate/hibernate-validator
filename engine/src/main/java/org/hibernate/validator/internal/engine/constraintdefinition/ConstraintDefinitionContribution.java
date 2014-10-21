/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.constraintdefinition;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintValidator;

/**
 * Type-safe wrapper class for a constraint annotation and its potential list of constraint validators.
 *
 * @author Hardy Ferentschik
 */
public class ConstraintDefinitionContribution<A extends Annotation> {
	private final Class<A> constraintType;
	private final List<Class<? extends ConstraintValidator<A, ?>>> constraintValidators = new ArrayList<Class<? extends ConstraintValidator<A, ?>>>();
	private final boolean keepDefaults;

	public ConstraintDefinitionContribution(Class<A> constraintType,
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
	 * @return a list of constraint validator types for the constraint type of this instance.
	 */
	public List<Class<? extends ConstraintValidator<A, ?>>> getConstraintValidators() {
		return constraintValidators;
	}

	/**
	 * Whether or not the default constraint validators should be kept or not.
	 *
	 * @return {@code true} if the default constraint validator instances for the constraint type wrapped by this
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

		ConstraintDefinitionContribution that = (ConstraintDefinitionContribution) o;

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
		return "ConstraintDefinitionContribution{" +
				"constraintType=" + constraintType +
				", constraintValidators=" + constraintValidators +
				", keepDefaults=" + keepDefaults +
				'}';
	}
}


