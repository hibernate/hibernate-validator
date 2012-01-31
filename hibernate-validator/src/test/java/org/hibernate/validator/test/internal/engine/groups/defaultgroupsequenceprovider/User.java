/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.internal.engine.groups.defaultgroupsequenceprovider;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.group.GroupSequenceProvider;
import org.hibernate.validator.constraints.Length;

/**
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
@GroupSequenceProvider(DynamicGroupSequenceProvider.class)
public class User {

	private boolean admin;

	//Define message to avoid comparison problem with validation error message
	//with a different locale than en
	@Pattern(regexp = "\\w+", message = "must match \"{regexp}\"")
	@Length(min = 10, max = 20, message = "length must be between {min} and {max}", groups = StrongCheck.class)
	private String password;

	public User(String password) {
		this( password, false );
	}

	public User(String password, boolean admin) {
		this.password = password;
		this.admin = admin;
	}

	public boolean isAdmin() {
		return admin;
	}

}
