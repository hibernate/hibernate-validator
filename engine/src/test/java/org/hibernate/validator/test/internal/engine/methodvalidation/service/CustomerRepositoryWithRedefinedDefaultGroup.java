/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.service;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Min.List;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryWithRedefinedDefaultGroup.ValidationGroup1;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryWithRedefinedDefaultGroup.ValidationGroup2;

/**
 * @author Gunnar Morling
 */
@GroupSequence({ ValidationGroup1.class, ValidationGroup2.class, CustomerRepositoryWithRedefinedDefaultGroup.class })
public interface CustomerRepositoryWithRedefinedDefaultGroup {

	void noConstraintInDefaultGroup(@NotNull(groups = ValidationGroup3.class) String name);

	void constraintInDefaultGroup(@NotNull(groups = ValidationGroup1.class) String name);

	void constraintInLaterPartOfDefaultSequence(@List({
			@Min(groups = ValidationGroup1.class, value = 5), @Min(groups = ValidationGroup2.class, value = 10)
	}) int param);

	void constraintInLaterPartOfDefaultSequenceAtDifferentParameters(@List({
			@Min(groups = ValidationGroup1.class, value = 5), @Min(groups = ValidationGroup2.class, value = 10)
	}) int param1, @Min(groups = ValidationGroup1.class, value = 7) int param2);


	void constraintInLaterPartOfGroupSequence(@List({
			@Min(groups = ValidationGroup2.class, value = 5), @Min(groups = ValidationGroup3.class, value = 10)
	}) int param);

	void constraintInLaterPartOfGroupSequenceAtDifferentParameters(@List({
			@Min(groups = ValidationGroup2.class, value = 5), @Min(groups = ValidationGroup3.class, value = 10)
	}) int param1, @Min(groups = ValidationGroup2.class, value = 7) int param2);

	// methods used for return value validation tests

	@NotNull(groups = ValidationGroup3.class)
	String noConstraintInDefaultGroupAtReturnValue();

	@NotNull(groups = ValidationGroup1.class)
	String constraintInDefaultGroupAtReturnValue();

	@List({
			@Min(groups = ValidationGroup1.class, value = 5), @Min(groups = ValidationGroup2.class, value = 10)
	})
	int constraintsInAllPartOfDefaultSequence();

	@List({
			@Min(groups = ValidationGroup2.class, value = 5), @Min(groups = ValidationGroup3.class, value = 10)
	})
	int constraintsInAllPartsOfGroupSequence();


	public interface ValidationGroup1 {
	}

	public interface ValidationGroup2 {
	}

	public interface ValidationGroup3 {
	}

	@GroupSequence({ ValidationGroup2.class, ValidationGroup3.class })
	public interface ValidationSequence {
	}

}
