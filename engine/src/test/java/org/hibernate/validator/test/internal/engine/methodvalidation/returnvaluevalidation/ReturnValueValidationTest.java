/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.returnvaluevalidation;

import static org.hibernate.validator.testutils.ValidatorUtil.getValidatingProxy;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.groups.Default;

import org.testng.annotations.Test;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

/**
 * @author Hardy Ferentschik
 */
public class ReturnValueValidationTest {

	@Test(expectedExceptions = ConstraintViolationException.class)
	@TestForIssue(jiraKey = "HV-656")
	public void methodValidationYieldsConstraintViolation() {
		ContactService serviceProxy = getValidatingProxy(
				new ContactServiceImpl(), ValidatorUtil.getValidator(), Default.class
		);
		serviceProxy.validateValidBeanParamConstraint( new ContactBean() );
	}
}
