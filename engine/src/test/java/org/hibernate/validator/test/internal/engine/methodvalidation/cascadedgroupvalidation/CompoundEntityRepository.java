/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
