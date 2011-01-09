/*
* JBoss, Home of Professional Open Source
* Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validator.test.engine.methodlevel.service;

import javax.validation.GroupSequence;
import javax.validation.constraints.Min;
import javax.validation.constraints.Min.List;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.test.engine.methodlevel.service.CustomerRepositoryWithRedefinedDefaultGroup.ValidationGroup1;
import org.hibernate.validator.test.engine.methodlevel.service.CustomerRepositoryWithRedefinedDefaultGroup.ValidationGroup2;

/**
 * @author Gunnar Morling
 */
@GroupSequence( { ValidationGroup1.class, ValidationGroup2.class, CustomerRepositoryWithRedefinedDefaultGroup.class })
public interface CustomerRepositoryWithRedefinedDefaultGroup {

	void noConstraintInDefaultGroup(@NotNull(groups = ValidationGroup3.class) String name);

	void constraintInDefaultGroup(@NotNull(groups = ValidationGroup1.class) String name);

	void constraintInLaterPartOfDefaultSequence(@List( {
			@Min(groups = ValidationGroup1.class, value = 5), @Min(groups = ValidationGroup2.class, value = 10)
	}) int param);

	void constraintInLaterPartOfDefaultSequenceAtDifferentParameters(@List( {
			@Min(groups = ValidationGroup1.class, value = 5), @Min(groups = ValidationGroup2.class, value = 10)
	}) int param1, @Min(groups = ValidationGroup1.class, value = 7) int param2);


	void constraintInLaterPartOfGroupSequence(@List( {
			@Min(groups = ValidationGroup2.class, value = 5), @Min(groups = ValidationGroup3.class, value = 10)
	}) int param);

	void constraintInLaterPartOfGroupSequenceAtDifferentParameters(@List( {
			@Min(groups = ValidationGroup2.class, value = 5), @Min(groups = ValidationGroup3.class, value = 10)
	}) int param1, @Min(groups = ValidationGroup2.class, value = 7) int param2);

	// methods used for return value validation tests

	@NotNull(groups = ValidationGroup3.class)
	String noConstraintInDefaultGroupAtReturnValue();

	@NotNull(groups = ValidationGroup1.class)
	String constraintInDefaultGroupAtReturnValue();

	@List( {
			@Min(groups = ValidationGroup1.class, value = 5), @Min(groups = ValidationGroup2.class, value = 10)
	})
	int constraintsInAllPartOfDefaultSequence();

	@List( {
			@Min(groups = ValidationGroup2.class, value = 5), @Min(groups = ValidationGroup3.class, value = 10)
	})
	int constraintsInAllPartsOfGroupSequence();


	public static interface ValidationGroup1 {
	}

	public static interface ValidationGroup2 {
	}

	public static interface ValidationGroup3 {
	}

	@GroupSequence( { ValidationGroup2.class, ValidationGroup3.class })
	public static interface ValidationSequence {
	}

}
