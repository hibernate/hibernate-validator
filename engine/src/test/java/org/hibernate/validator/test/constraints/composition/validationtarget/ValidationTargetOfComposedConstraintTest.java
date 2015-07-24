/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints.composition.validationtarget;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;
import org.testng.annotations.Test;

import javax.validation.ConstraintViolation;
import javax.validation.executable.ExecutableValidator;
import java.lang.reflect.Method;
import java.util.Set;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;

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

		assertCorrectConstraintTypes( constraintViolations, ValidInvoiceAmount.class );
	}
}
