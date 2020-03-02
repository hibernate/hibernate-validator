/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.lang.reflect.Method;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class MethodParameterConstraintsInParallelHierarchyTest {

	/**
	 * NOTE: prior to the changes made for HV-1450, this test was failing randomly.
	 */
	@Test
	@TestForIssue(jiraKey = "HV-1450")
	public void testDeepParallelHierarchyIsProcessedCorrectly() throws Exception {

		WebServiceImpl service = new WebServiceImpl();
		Method method = WebServiceImpl.class.getMethod( "getEntityVersion", Long.class );
		Object[] params = new Object[] { null };

		for ( int i = 0; i < 100; i++ ) {
			Validator validator = Validation.byDefaultProvider().configure()
					.buildValidatorFactory().getValidator();

			Set<ConstraintViolation<WebServiceImpl>> violations = validator.forExecutables().validateParameters( service, method, params );

			assertThat( violations ).containsOnlyViolations( violationOf( NotNull.class ) );
		}
	}

	private class WebServiceImpl extends AbstractWebService implements ExtendedWebService {

	}

	private abstract class AbstractWebService implements WebService {

		@Override
		public int getEntityVersion(Long id) {
			return id.intValue();
		}
	}

	private interface ExtendedWebService extends WebService {

		@Override
		int getEntityVersion(Long id);
	}

	private interface WebService {

		int getEntityVersion(@NotNull Long id);
	}
}
