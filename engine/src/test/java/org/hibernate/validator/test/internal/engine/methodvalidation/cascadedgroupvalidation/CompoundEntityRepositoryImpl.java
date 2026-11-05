package org.hibernate.validator.test.internal.engine.methodvalidation.cascadedgroupvalidation;

import jakarta.validation.Valid;

/**
 * @author Jan-Willem Willebrands
 */
public class CompoundEntityRepositoryImpl implements CompoundEntityRepository {
	@Override
	public void store(@Valid CompoundEntity entity) {

	}

	@Override
	public CompoundEntity getEntity(CompoundEntity entity) {
		return entity;
	}
}
