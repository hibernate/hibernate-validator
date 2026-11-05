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
