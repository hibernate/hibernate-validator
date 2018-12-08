/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.predefinedscope;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.PredefinedScopeHibernateValidator;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.Test;

@TestForIssue(jiraKey = "HV-1667")
public class PredefinedScopeValidatorFactoryTest {

	@Test
	public void testValidation() throws NoSuchMethodException, SecurityException {
		Validator validator = getValidator();

		Set<ConstraintViolation<Bean>> violations = validator.validate( new Bean( "property", "test@example.com" ) );
		assertNoViolations( violations );

		violations = validator.validate( new Bean( null, "invalid" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withProperty( "property" ),
				violationOf( Email.class ).withProperty( "email" ) );

		violations = validator.forExecutables()
				.validateParameters( new Bean(), Bean.class.getMethod( "setEmail", String.class ),
						new Object[]{ "invalid" } );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Email.class )
						.withPropertyPath( pathWith().method( "setEmail" ).parameter( "email", 0 ) ) );

		violations = validator.forExecutables()
				.validateReturnValue( new Bean(), Bean.class.getMethod( "getEmail" ), "invalid" );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Email.class )
						.withPropertyPath( pathWith().method( "getEmail" ).returnValue() ) );

		violations = validator.forExecutables()
				.validateConstructorParameters( Bean.class.getConstructor( String.class, String.class ),
						new Object[]{ null, "invalid" } );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith().constructor( Bean.class ).parameter( "property", 0 ) ),
				violationOf( Email.class )
						.withPropertyPath( pathWith().constructor( Bean.class ).parameter( "email", 1 ) ) );

		violations = validator.forExecutables()
				.validateConstructorReturnValue( Bean.class.getConstructor( String.class, String.class ),
						new Bean( null, "invalid" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith().constructor( Bean.class ).returnValue()
								.property( "property" ) ),
				violationOf( Email.class )
						.withPropertyPath( pathWith().constructor( Bean.class ).returnValue()
								.property( "email" ) ) );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000249:.*")
	public void testValidationOnUnknownBean() throws NoSuchMethodException, SecurityException {
		Validator validator = getValidator();

		Set<ConstraintViolation<UnknownBean>> violations = validator.validate( new UnknownBean() );
		assertNoViolations( violations );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000249:.*")
	public void testValidationOnUnknownBeanMethodParameter() throws NoSuchMethodException, SecurityException {
		Validator validator = getValidator();

		Set<ConstraintViolation<UnknownBean>> violations = validator.validate( new UnknownBean() );
		assertNoViolations( violations );

		violations = validator.forExecutables()
				.validateParameters( new UnknownBean(), UnknownBean.class.getMethod( "setMethod", String.class ),
						new Object[]{ "" } );
		assertNoViolations( violations );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000249:.*")
	public void testValidationOnUnknownBeanMethodReturnValue() throws NoSuchMethodException, SecurityException {
		Validator validator = getValidator();

		Set<ConstraintViolation<UnknownBean>> violations = validator.forExecutables()
				.validateReturnValue( new UnknownBean(), UnknownBean.class.getMethod( "getMethod" ), "" );
		assertNoViolations( violations );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000249:.*")
	public void testValidationOnUnknownBeanConstructorParameters() throws NoSuchMethodException, SecurityException {
		Validator validator = getValidator();

		Set<ConstraintViolation<UnknownBean>> violations = validator.forExecutables()
				.validateConstructorParameters( UnknownBean.class.getConstructor( String.class ),
						new Object[]{ "" } );
		assertNoViolations( violations );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000249:.*")
	public void testValidationOnUnknownBeanConstructorReturnValue() throws NoSuchMethodException, SecurityException {
		Validator validator = getValidator();

		Set<ConstraintViolation<UnknownBean>> violations = validator.forExecutables()
				.validateConstructorReturnValue( UnknownBean.class.getConstructor( String.class ), new UnknownBean() );
		assertNoViolations( violations );
	}

	private static Validator getValidator() {
		Set<Class<?>> beanMetaDataToInitialize = new HashSet<>();
		beanMetaDataToInitialize.add( Bean.class );

		ValidatorFactory validatorFactory = Validation.byProvider( PredefinedScopeHibernateValidator.class )
				.configure()
				.initializeBeanMetaData( beanMetaDataToInitialize )
				.buildValidatorFactory();

		return validatorFactory.getValidator();
	}

	private static class Bean {

		@NotNull
		private String property;

		@Email
		private String email;

		public Bean() {
		}

		@Valid
		public Bean(@NotNull String property, @Email String email) {
			this.property = property;
			this.email = email;
		}

		@SuppressWarnings("unused")
		public String getProperty() {
			return property;
		}

		public @Email String getEmail() {
			return email;
		}

		@SuppressWarnings("unused")
		public void setEmail(@Email String email) {
			this.email = email;
		}
	}

	private static class UnknownBean {

		public UnknownBean() {
		}

		@SuppressWarnings("unused")
		public UnknownBean(String parameter) {
		}

		@SuppressWarnings("unused")
		public String getMethod() {
			return null;
		}

		@SuppressWarnings("unused")
		public void setMethod(String parameter) {
		}
	}
}
