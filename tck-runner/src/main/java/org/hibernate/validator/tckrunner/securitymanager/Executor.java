/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.tckrunner.securitymanager;

/**
 * Executes a given operation.
 *
 * @author Gunnar Morling
 *
 */
public interface Executor {

	void invoke(Object... parameters) throws Throwable;
}
