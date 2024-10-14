/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.metadata.aggregated;

import jakarta.validation.constraints.NotNull;

/**
 * @author Gunnar Morling
 */
public interface JobRepository<T> {

	void createJob(@NotNull T id);
}
