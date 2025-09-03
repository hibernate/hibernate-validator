/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.constraintvalidation;

import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.Incubating;

/**
 * Allows observing the basic validator factory lifecycle events.
 * <p>
 * Observers are required to handle exceptions internally and must <b>not</b> propagate them.
 *
 * @see org.hibernate.validator.HibernateValidatorConfiguration#addHibernateValidatorFactoryObserver(HibernateValidatorFactoryObserver)
 * @since 9.1.0
 */
@Incubating
public interface HibernateValidatorFactoryObserver {
	/**
	 * A callback invoked upon the successful creation of a factory.
	 *
	 * @param factory The fully initialized factory.
	 */
	default void factoryCreated(HibernateValidatorFactory factory) {
	}

	/**
	 * A callback invoked just before the factory is shut down.
	 *
	 * @param factory The factory that is about to be closed.
	 */
	default void factoryClosing(HibernateValidatorFactory factory) {
	}

	/**
	 * A callback invoked after the factory has been successfully shut down.
	 *
	 * @param factory The closed factory.
	 */
	default void factoryClosed(HibernateValidatorFactory factory) {
	}
}
