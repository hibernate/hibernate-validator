package org.hibernate.validator.test.internal.engine.methodvalidation.cascadedgroupvalidation;

import jakarta.validation.Valid;

/**
 * @author Jan-Willem Willebrands
 */
public interface CompoundEntityRepository {
	void store(@Valid CompoundEntity entity);

	@Valid
	CompoundEntity getEntity(CompoundEntity entity);
}
