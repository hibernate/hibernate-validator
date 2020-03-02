/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.groups.defaultgroupsequenceprovider;

import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.group.GroupSequenceProvider;
import org.hibernate.validator.constraints.Length;

/**
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
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
