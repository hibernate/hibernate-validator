/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints.composition.validationtarget;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.lang.reflect.Method;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.executable.ExecutableValidator;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

/**
 *
 * @author Gunnar Morling
 *
 */
public class ValidationTargetOfComposedConstraintTest {

	@Test
	@TestForIssue(jiraKey = "HV-847")
	public void canUseValidationTargetToResolveAmbiguityOfPurelyComposedConstraint() throws Exception {
		ExecutableValidator validator = ValidatorUtil.getValidator().forExecutables();
		InvoiceService invoiceService = new InvoiceService();
		Method method = InvoiceService.class.getMethod( "getInvoiceAmount", String.class );
		Object returnValue = 0L;

		Set<ConstraintViolation<InvoiceService>> constraintViolations = validator.validateReturnValue(
				invoiceService,
				method,
				returnValue
		);
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( ValidInvoiceAmount.class )
						.withPropertyPath( pathWith()
								.method( "getInvoiceAmount" )
								.returnValue()
						)
		);
	}
}
