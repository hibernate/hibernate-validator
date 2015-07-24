/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.validation.ConstraintViolation;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.executable.ExecutableValidator;
import java.util.Set;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;

/**
 * Test for making sure a validated sub-type method can have different constraints than a private super-type method with
 * the same signature, as this is no case of method overriding.
 *
 * @author Gunnar Morling
 *
 */
@TestForIssue(jiraKey = "HV-890")
public class PrivateMethodInSuperClassTest {

	private ExecutableValidator validator;

	@BeforeClass
	public void setUp() {
		validator = ValidatorUtil.getValidator().forExecutables();
	}

	@Test
	public void canValidateMethodWithSameSignatureAsPrivateMethodInSuperClass() throws Exception {
		// Private method in the super-class
		Set<? extends ConstraintViolation<?>> violations = validator.validateParameters(
				new Mammal(),
				Mammal.class.getDeclaredMethod( "eat", String.class ),
				new Object[] { null }
		);

		assertCorrectConstraintTypes( violations, NotNull.class );

		// Public method in the sub-class; same signature but *not* overriding
		violations = validator.validateParameters(
				new GiantPanda(),
				GiantPanda.class.getMethod( "eat", String.class ),
				new Object[] { "fo" }
		);

		assertCorrectConstraintTypes( violations, Size.class );
	}

	public static class Mammal {

		@SuppressWarnings("unused")
		private void eat(@NotNull String food) {
		}
	}

	public static class GiantPanda extends Mammal {

		public void eat(@NotNull @Size(min=3) String food) {
		}
	}
}
