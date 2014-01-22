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
package org.hibernate.validator.test.internal.engine.valuehandling.model;

import javax.validation.constraints.Size;

import org.hibernate.validator.valuehandling.UnwrapValidatedValue;

/**
 * @author Gunnar Morling
 */
public class Customer {

	@Size(min = 4)
	@UnwrapValidatedValue
	private Property<String> name = new Property<String>( "Bob" );

	@Size(min = 4)
	@UnwrapValidatedValue
	private final StringProperty lastName = new StringProperty( "Foo" );

	@Size(min = 4, groups = CustomValidationGroup.class)
	@UnwrapValidatedValue
	private final StringProperty middleName = new StringProperty( "Foo" );

	@Size(min = 4)
	@UnwrapValidatedValue
	private final UiInput<String> nameInput = new UiInput<String>( "Bob" );

	public void setName(@Size(min = 4) @UnwrapValidatedValue Property<String> name) {
		this.name = name;
	}

	@Size(min = 4)
	@UnwrapValidatedValue
	public Property<String> retrieveName() {
		return name;
	}

	public interface CustomValidationGroup {
	}
}
