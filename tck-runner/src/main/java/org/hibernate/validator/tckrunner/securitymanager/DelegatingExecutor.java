/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.tckrunner.securitymanager;

/**
 * Executes a given operation.
 * <p>
 * The whole purpose of this class is to establish a frame on the stack which is as close as possible to the running
 * tests and whose protection domain has no permissions at all. It is separated from the other Arquillian-related
 * classes which need special permissions on their own.
 * <p>
 * This will reveal any invocation of security-relevant methods in the HV code base which don't make use of a
 * do-privileged block.
 *
 * @author Gunnar Morling
 *
 */
public class DelegatingExecutor implements Executor {

	private final Executor delegate;

	public DelegatingExecutor(Executor delegate) {
		this.delegate = delegate;
	}

	@Override
	public void invoke(Object... parameters) throws Throwable {
		delegate.invoke( parameters );
	}
}
