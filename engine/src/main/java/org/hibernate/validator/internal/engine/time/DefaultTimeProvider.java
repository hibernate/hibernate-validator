/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.time;

import org.hibernate.validator.spi.time.TimeProvider;

/**
 * Default {@link TimeProvider} implementation based on the current system time.
 *
 * @author Gunnar Morling
 */
public class DefaultTimeProvider implements TimeProvider {

	private static final DefaultTimeProvider INSTANCE = new DefaultTimeProvider();

	private DefaultTimeProvider() {
	}

	public static final DefaultTimeProvider getInstance() {
		return INSTANCE;
	}

	@Override
	public long getCurrentTime() {
		return System.currentTimeMillis();
	}
}
