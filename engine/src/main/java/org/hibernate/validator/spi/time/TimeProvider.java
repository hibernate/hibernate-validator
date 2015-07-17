/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.spi.time;

/**
 * Contract for obtaining the current time when validating the {@code @Future} and {@code @Past} constraints.
 * <p>
 * The default implementation will return the current system time. Plugging in custom implementations may be useful for
 * instance in batch applications which need to run with a specific logical date, e.g. with yesterday's date when
 * re-running a failed batch job execution.
 * <p>
 * Implementations must be safe for access from several threads at the same time.
 *
 * @author Gunnar Morling
 * @see org.hibernate.validator.HibernateValidatorConfiguration#timeProvider(TimeProvider)
 * @hv.experimental This API is considered experimental and may change in future revisions
 * @since 5.2
 */
public interface TimeProvider {

	/**
	 * Returns the current time.
	 *
	 * @return The current time; Must not be {@code null}
	 */
	long getCurrentTime();
}
