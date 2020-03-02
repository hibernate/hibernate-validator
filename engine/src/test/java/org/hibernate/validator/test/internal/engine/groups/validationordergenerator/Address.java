/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.groups.validationordergenerator;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;

/**
 * @author Emmanuel Bernard
 */
@GroupSequence({ Address.class, Address.HighLevelCoherence.class })
@ZipCodeCoherenceChecker(groups = Address.HighLevelCoherence.class)
public class Address {
	@NotNull
	@Size(max = 50)
	private String street;

	@NotNull
	@Size(max = 5)
	private String zipcode;

	@NotNull
	@Size(max = 30)
	private String city;

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * Check conherence on the overall object
	 * Needs basic checking to be green first
	 */
	public interface HighLevelCoherence {
	}

	/**
	 * Check both basic constraints and high level ones.
	 * High level constraints are not checked if basic constraints fail.
	 */
	@GroupSequence(value = { Default.class, HighLevelCoherence.class })
	public interface Complete {
	}
}
