package org.hibernate.validator.test.internal.metadata;

import jakarta.validation.constraints.NotEmpty;

/**
 * @author Hardy Ferentschik
 */
public interface Person {
	@NotEmpty(groups = PersonValidation.class)
	String getFirstName();

	String getMiddleName();

	@NotEmpty
	String getLastName();

	interface PersonValidation {
	}
}
