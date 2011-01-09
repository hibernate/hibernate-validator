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

import org.hibernate.validator.test.engine.methodlevel.service.CustomerRepositoryWithRedefinedDefaultGroup.ValidationGroup1;

/**
 * @author Gunnar Morling
 */
@GroupSequence( { ValidationGroup1.class, CustomerRepositoryWithRedefinedDefaultGroupImpl.class })
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
