/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.predefinedscope;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.PredefinedScopeHibernateValidator;
import org.testng.annotations.Test;

public class PredefinedScopeValidatorFactoryTest {

	@Test
	public void testValidation() {
		Set<Class<?>> beanMetaDataToInitialize = new HashSet<>();
		beanMetaDataToInitialize.add( Bean.class );

		ValidatorFactory validatorFactory = Validation.byProvider( PredefinedScopeHibernateValidator.class )
				.configure()
				.initializeBeanMetaData( beanMetaDataToInitialize )
				.buildValidatorFactory();

		Validator validator = validatorFactory.getValidator();

		Set<ConstraintViolation<Bean>> violations = validator.validate( new Bean( "property", "test@example.com" ) );
		assertNoViolations( violations );

		violations = validator.validate( new Bean( null, "invalid" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withProperty( "property" ),
				violationOf( Email.class ).withProperty( "email" ) );
	}

	private static class Bean {

		@NotNull
		private String property;

		@Email
		private String email;

		private Bean(String property, String email) {
			this.property = property;
			this.email = email;
		}

		@SuppressWarnings("unused")
		public String getProperty() {
			return property;
		}

		@SuppressWarnings("unused")
		public String getEmail() {
			return email;
		}
	}
}
