package org.hibernate.validator.test.cdi.internal.methodvalidation.inheritance;

import jakarta.validation.constraints.NotNull;

/**
 * @author Gunnar Morling
 */
public class Shipment {

	@NotNull
	private String name;

	public String getName() {
		return name;
	}
}
