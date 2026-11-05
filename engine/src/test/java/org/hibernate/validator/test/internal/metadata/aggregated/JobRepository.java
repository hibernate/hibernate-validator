package org.hibernate.validator.test.internal.metadata.aggregated;

import jakarta.validation.constraints.NotNull;

/**
 * @author Gunnar Morling
 */
public interface JobRepository<T> {

	void createJob(@NotNull T id);
}
