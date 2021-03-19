/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valueextraction;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Optional;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Ensure we can handle a field of type T and a getter of type Optional<T>.
 *
 * @author Gunnar Morling
 */
@TestForIssue(jiraKey = "HV-1080")
public class CascadedOptionalTest {

	private Validator validator;

	@BeforeClass
	public void setupValidator() {
		validator = getValidator();
	}

	@Test
	public void cascadedValueIsRetrievedFromField() {
		PondWithCascadedField pond = new PondWithCascadedField();
		pond.cascadedBean = new CascadedBean();

		Set<ConstraintViolation<PondWithCascadedField>> constraintViolations = validator.validate( pond );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "cascadedBean" )
								.property( "name" )
						)
		);
		assertFalse( pond.getCascadedBeanInvoked );
	}

	@Test
	public void cascadedValueIsRetrievedFromGetterApplyingUnwrapper() {
		PondWithCascadedGetter pond = new PondWithCascadedGetter();
		pond.cascadedBean = new CascadedBean();

		Set<ConstraintViolation<PondWithCascadedGetter>> constraintViolations = validator.validate( pond );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "cascadedBean" )
								.property( "name", false, null, null, Optional.class, 0 )
						)
		);
		assertTrue( pond.getCascadedBeanInvoked );
	}

	private class PondWithCascadedField {

		@Valid
		private CascadedBean cascadedBean;
		private boolean getCascadedBeanInvoked = false;

		@SuppressWarnings("unused")
		public Optional<CascadedBean> getCascadedBean() {
			getCascadedBeanInvoked = true;
			return Optional.ofNullable( cascadedBean );
		}
	}

	private class PondWithCascadedGetter {

		private CascadedBean cascadedBean;
		private boolean getCascadedBeanInvoked = false;

		@Valid
		public Optional<CascadedBean> getCascadedBean() {
			getCascadedBeanInvoked = true;
			return Optional.ofNullable( cascadedBean );
		}
	}

	private class CascadedBean {

		@NotNull
		private String name;
	}
}
