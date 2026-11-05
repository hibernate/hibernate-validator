package org.hibernate.validator.test.internal.metadata;

import jakarta.validation.Valid;

/**
 * @author Gunnar Morling
 */
public class ParentWithAtValid {

	@Valid
	public Order getOrder() {
		return null;
	}

}
