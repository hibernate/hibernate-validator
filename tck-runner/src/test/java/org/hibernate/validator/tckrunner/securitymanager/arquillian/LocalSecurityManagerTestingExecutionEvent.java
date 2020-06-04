/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.tckrunner.securitymanager.arquillian;

import java.lang.reflect.Method;

import org.hibernate.validator.tckrunner.securitymanager.DelegatingExecutor;
import org.hibernate.validator.tckrunner.securitymanager.Executor;
import org.jboss.arquillian.container.test.impl.execution.event.LocalExecutionEvent;
import org.jboss.arquillian.test.spi.TestMethodExecutor;

/**
 * A custom {@link LocalExecutionEvent} which channels execution through a specific protection domain.
 *
 * @author Gunnar Morling
 */
public class LocalSecurityManagerTestingExecutionEvent extends LocalExecutionEvent {

	public LocalSecurityManagerTestingExecutionEvent(TestMethodExecutor executor) {
		super( new DelegatingTestMethodExecutor( executor ) );
	}

	/**
	 * Invokes the given test method via {@link DelegatingExecutor} which lives in its own protection domain.
	 */
	private static class DelegatingTestMethodExecutor implements TestMethodExecutor {

		private final Executor delegate;
		private final Method method;
		private final Object instance;

		public DelegatingTestMethodExecutor(TestMethodExecutor delegate) {
			this.method = delegate.getMethod();
			this.instance = delegate.getInstance();
			this.delegate = new DelegatingExecutor( new ArquillianExecutor( delegate ) );
		}

		@Override
		public Method getMethod() {
			return method;
		}

		@Override
		public String getMethodName() {
			return method.getName();
		}

		@Override
		public Object getInstance() {
			return instance;
		}

		@Override
		public void invoke(Object... parameters) throws Throwable {
			delegate.invoke( parameters );
		}
	}

	/**
	 * Executes a given test via a given Arquillian test executor.
	 */
	private static class ArquillianExecutor implements Executor {

		private final TestMethodExecutor delegate;

		public ArquillianExecutor(TestMethodExecutor delegate) {
			this.delegate = delegate;
		}

		@Override
		public void invoke(Object... parameters) throws Throwable {
			delegate.invoke( parameters );
		}
	}
}
