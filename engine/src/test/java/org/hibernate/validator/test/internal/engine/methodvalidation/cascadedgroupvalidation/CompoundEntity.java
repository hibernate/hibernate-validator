package org.hibernate.validator.test.internal.engine.methodvalidation.cascadedgroupvalidation;

import jakarta.validation.Valid;

/**
 * @author Jan-Willem Willebrands
 */
public class CompoundEntity {

	@Valid
	Entity entity;

	public CompoundEntity(Entity entity) {
		this.entity = entity;
	}
}
