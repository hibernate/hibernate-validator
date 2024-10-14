/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
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
