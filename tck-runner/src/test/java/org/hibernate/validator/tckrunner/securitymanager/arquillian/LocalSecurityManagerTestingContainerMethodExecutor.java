/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.tckrunner.securitymanager.arquillian;

import org.jboss.arquillian.container.test.spi.ContainerMethodExecutor;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;

/**
 * A custom {@link ContainerMethodExecutor} based on {@link LocalSecurityManagerTestingExecutionEvent}.
 *
 * @author Gunnar Morling
 */
public class LocalSecurityManagerTestingContainerMethodExecutor implements ContainerMethodExecutor {

	@Inject
	private Event<LocalSecurityManagerTestingExecutionEvent> event;

	@Inject
	private Instance<TestResult> testResult;

	@Override
	public TestResult invoke(TestMethodExecutor testMethodExecutor) {
		event.fire( new LocalSecurityManagerTestingExecutionEvent( testMethodExecutor ) );
		return testResult.get();
	}
}
