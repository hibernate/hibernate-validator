/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.service;

import jakarta.validation.GroupSequence;

import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryWithRedefinedDefaultGroup.ValidationGroup1;

/**
 * @author Gunnar Morling
 */
@GroupSequence({ ValidationGroup1.class, CustomerRepositoryWithRedefinedDefaultGroupImpl.class })
public class CustomerRepositoryWithRedefinedDefaultGroupImpl implements CustomerRepositoryWithRedefinedDefaultGroup {

	@Override
	public void noConstraintInDefaultGroup(String name) {
	}

	@Override
	public void constraintInDefaultGroup(String name) {
	}

	@Override
	public void constraintInLaterPartOfDefaultSequence(int param) {
	}

	@Override
	public void constraintInLaterPartOfDefaultSequenceAtDifferentParameters(int param1, int param2) {
	}

	@Override
	public void constraintInLaterPartOfGroupSequence(int param) {
	}

	@Override
	public void constraintInLaterPartOfGroupSequenceAtDifferentParameters(int param1, int param2) {
	}

	// methods used for return value validation tests

	@Override
	public String noConstraintInDefaultGroupAtReturnValue() {
		return null;
	}

	@Override
	public String constraintInDefaultGroupAtReturnValue() {
		return null;
	}

	@Override
	public int constraintsInAllPartOfDefaultSequence() {
		return 1;
	}

	@Override
	public int constraintsInAllPartsOfGroupSequence() {
		return 1;
	}
}
