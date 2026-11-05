package org.hibernate.validator.test.internal.metadata;

import jakarta.validation.constraints.NotNull;

/**
 * @author Gunnar Morling
 */
public class ChildWithoutAtValid2 extends ParentWithoutAtValid {

	@Override
	@NotNull
	public Order getOrder() {
		return null;
	}

}
