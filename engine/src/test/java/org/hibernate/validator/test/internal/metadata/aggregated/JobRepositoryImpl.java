/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.metadata.aggregated;

import java.util.UUID;

/**
 * @author Gunnar Morling
 */
public class JobRepositoryImpl implements JobRepository<UUID> {

	@Override
	public void createJob(UUID id) {
	}
}
