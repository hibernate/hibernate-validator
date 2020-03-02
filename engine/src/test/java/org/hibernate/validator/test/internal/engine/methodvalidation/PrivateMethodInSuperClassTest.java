/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.executable.ExecutableValidator;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test for making sure a validated sub-type method can have different constraints than a private super-type method with
 * the same signature, as this is no case of method overriding.
 *
 * @author Gunnar Morling
 */
public class PrivateMethodInSuperClassTest {

	private Validator validator;
	private ExecutableValidator executableValidator;

	@BeforeClass
	public void setUp() {
		validator = ValidatorUtil.getValidator();
		executableValidator = validator.forExecutables();
	}

	@Test
	@TestForIssue(jiraKey = "HV-890")
	public void canValidateMethodWithSameSignatureAsPrivateMethodInSuperClass() throws Exception {
		// Private method in the super-class
		Set<? extends ConstraintViolation<?>> violations = executableValidator.validateParameters(
				new Mammal(),
				Mammal.class.getDeclaredMethod( "eat", String.class ),
				new Object[] { null }
		);

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class )
		);

		// Public method in the sub-class; same signature but *not* overriding
		violations = executableValidator.validateParameters(
				new GiantPanda(),
				GiantPanda.class.getMethod( "eat", String.class ),
				new Object[] { "fo" }
		);

		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1018")
	public void canValidateConstraintOnPrivateSuperTypeProperty() {
		Set<ConstraintViolation<GiantPanda>> violations = validator.validate( new GiantPanda() );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Min.class )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1018")
	public void canValidateConstraintOnPrivateSuperTypePropertyReachedThroughCascadedValidation() {
		Set<ConstraintViolation<FavoriteAnimalCollection>> violations = validator.validate( new FavoriteAnimalCollection() );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Min.class )
		);
	}

	public static class Mammal {

		@SuppressWarnings("unused")
		private void eat(@NotNull String food) {
		}

		@Min(0)
		private long getAge() {
			return -1;
		}
	}

	public static class GiantPanda extends Mammal {

		public void eat(@NotNull @Size(min = 3) String food) {
		}
	}

	public static class FavoriteAnimalCollection {

		@Valid
		Mammal favoriteAnimalNo1 = new GiantPanda();
	}
}
