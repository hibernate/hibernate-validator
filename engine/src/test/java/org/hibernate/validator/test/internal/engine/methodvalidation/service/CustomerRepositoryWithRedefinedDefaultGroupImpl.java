/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.service;

import javax.validation.GroupSequence;

import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryWithRedefinedDefaultGroup.ValidationGroup1;

/**
 * @author Gunnar Morling
 */
@GroupSequence({ ValidationGroup1.class, CustomerRepositoryWithRedefinedDefaultGroupImpl.class })
public class CustomerRepositoryWithRedefinedDefaultGroupImpl implements CustomerRepositoryWithRedefinedDefaultGroup {

	public void noConstraintInDefaultGroup(String name) {
	}

	public void constraintInDefaultGroup(String name) {
	}

	public void constraintInLaterPartOfDefaultSequence(int param) {
	}

	public void constraintInLaterPartOfDefaultSequenceAtDifferentParameters(int param1, int param2) {
	}

	public void constraintInLaterPartOfGroupSequence(int param) {
	}

	public void constraintInLaterPartOfGroupSequenceAtDifferentParameters(int param1, int param2) {
	}

	// methods used for return value validation tests

	public String noConstraintInDefaultGroupAtReturnValue() {
		return null;
	}

	public String constraintInDefaultGroupAtReturnValue() {
		return null;
	}

	public int constraintsInAllPartOfDefaultSequence() {
		return 1;
	}

	public int constraintsInAllPartsOfGroupSequence() {
		return 1;
	}
}
