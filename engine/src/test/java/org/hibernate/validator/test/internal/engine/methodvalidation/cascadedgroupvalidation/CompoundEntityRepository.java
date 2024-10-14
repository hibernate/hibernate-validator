/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
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
