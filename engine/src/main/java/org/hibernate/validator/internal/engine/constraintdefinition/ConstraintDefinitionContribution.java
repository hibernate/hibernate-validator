/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.constraintdefinition;

import java.lang.annotation.Annotation;
import java.util.List;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorDescriptor;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * Type-safe wrapper class for a constraint annotation and its potential list of constraint validators.
 *
 * @author Hardy Ferentschik
 */
public class ConstraintDefinitionContribution<A extends Annotation> {
	private final Class<A> constraintType;
	@Immutable
	private final List<ConstraintValidatorDescriptor<A>> validatorDescriptors;
	private final boolean includeExisting;

	public ConstraintDefinitionContribution(Class<A> constraintType,
			List<ConstraintValidatorDescriptor<A>> validatorDescriptors,
			boolean includeExisting) {
		this.constraintType = constraintType;
		this.validatorDescriptors = CollectionHelper.toImmutableList( validatorDescriptors );
		this.includeExisting = includeExisting;
	}

	/**
	 * Returns the constraint annotation type for which this instance provides constraint validator instances.
	 */
	public Class<A> getConstraintType() {
		return constraintType;
	}

	/**
	 * Returns a list of constraint validator descriptors for the constraint type of this instance.
	 */
	public List<ConstraintValidatorDescriptor<A>> getValidatorDescriptors() {
		return validatorDescriptors;
	}

	/**
	 * Whether or not the existing constraint validators should be kept or not.
	 *
	 * @return {@code true} if the existing constraint validators for the constraint type wrapped by this
	 * instance should be kept, {@code false} otherwise.
	 */
	public boolean includeExisting() {
		return includeExisting;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		ConstraintDefinitionContribution<?> that = (ConstraintDefinitionContribution<?>) o;

		if ( !constraintType.equals( that.constraintType ) ) {
			return false;
		}
		if ( !validatorDescriptors.equals( that.validatorDescriptors ) ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = constraintType.hashCode();
		result = 31 * result + validatorDescriptors.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "ConstraintDefinitionContribution{" +
				"constraintType=" + constraintType +
				", validatorDescriptors=" + validatorDescriptors +
				", includeExisting=" + includeExisting +
				'}';
	}
}
