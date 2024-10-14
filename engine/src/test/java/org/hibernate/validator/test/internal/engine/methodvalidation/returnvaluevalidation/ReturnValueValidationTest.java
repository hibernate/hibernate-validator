/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.returnvaluevalidation;

import static org.hibernate.validator.testutils.ValidatorUtil.getValidatingProxy;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.groups.Default;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

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
