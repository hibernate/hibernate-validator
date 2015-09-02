/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata.aggregated;

import java.util.UUID;

/**
 * @author Gunnar Morling
 */
public class JobRepositoryImpl implements JobRepository<UUID> {

	@Override
	public void createJob(UUID id) {}
}
