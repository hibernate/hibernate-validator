package org.hibernate.validator.test.internal.metadata;


/**
 * @author Gunnar Morling
 */
public class ChildWithoutAtValid extends ParentWithAtValid {

	@Override
	public Order getOrder() {
		return null;
	}

}
