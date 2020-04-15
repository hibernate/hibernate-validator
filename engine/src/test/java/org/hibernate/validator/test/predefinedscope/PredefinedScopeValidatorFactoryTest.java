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
import static org.testng.Assert.fail;

import java.lang.annotation.ElementType;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.Path;
import jakarta.validation.Path.Node;
import jakarta.validation.TraversableResolver;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import org.assertj.core.api.Assertions;
import org.hibernate.validator.PredefinedScopeHibernateValidator;
import org.hibernate.validator.metadata.BeanMetaDataClassNormalizer;
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

	@Test
	public void testValidationOnUnknownBean() throws NoSuchMethodException, SecurityException {
		Validator validator = getValidator();

		Set<ConstraintViolation<UnknownBean>> violations = validator.validate( new UnknownBean() );
		assertNoViolations( violations );
	}

	@Test
	public void testValidationOnUnknownBeanMethodParameter() throws NoSuchMethodException, SecurityException {
		Validator validator = getValidator();

		Set<ConstraintViolation<UnknownBean>> violations = validator.validate( new UnknownBean() );
		assertNoViolations( violations );

		violations = validator.forExecutables()
				.validateParameters( new UnknownBean(), UnknownBean.class.getMethod( "setMethod", String.class ),
						new Object[]{ "" } );
		assertNoViolations( violations );
	}

	@Test
	public void testValidationOnUnknownBeanMethodReturnValue() throws NoSuchMethodException, SecurityException {
		Validator validator = getValidator();

		Set<ConstraintViolation<UnknownBean>> violations = validator.forExecutables()
				.validateReturnValue( new UnknownBean(), UnknownBean.class.getMethod( "getMethod" ), "" );
		assertNoViolations( violations );
	}

	@Test
	public void testValidationOnUnknownBeanConstructorParameters() throws NoSuchMethodException, SecurityException {
		Validator validator = getValidator();

		Set<ConstraintViolation<UnknownBean>> violations = validator.forExecutables()
				.validateConstructorParameters( UnknownBean.class.getConstructor( String.class ),
						new Object[]{ "" } );
		assertNoViolations( violations );
	}

	@Test
	public void testValidationOnUnknownBeanConstructorReturnValue() throws NoSuchMethodException, SecurityException {
		Validator validator = getValidator();

		Set<ConstraintViolation<UnknownBean>> violations = validator.forExecutables()
				.validateConstructorReturnValue( UnknownBean.class.getConstructor( String.class ), new UnknownBean() );
		assertNoViolations( violations );
	}

	@Test
	public void testExistingInitializedLocale() {
		Locale defaultLocale = Locale.getDefault();

		try {
			Locale.setDefault( Locale.FRANCE );

			Validator validator = getValidatorWithInitializedLocale( Locale.FRANCE );

			Set<ConstraintViolation<Bean>> violations = validator.validate( new Bean( "", "invalid" ) );
			assertThat( violations ).containsOnlyViolations(
					violationOf( Email.class ).withProperty( "email" ).withMessage( "doit être une adresse électronique syntaxiquement correcte" ) );
		}
		finally {
			Locale.setDefault( defaultLocale );
		}
	}

	@Test
	public void testUnavailableInitializedLocale() {
		Locale defaultLocale = Locale.getDefault();

		try {
			Locale georgianLocale = new Locale( "ka", "GE" );

			Locale.setDefault( georgianLocale );

			Validator validator = getValidatorWithInitializedLocale( georgianLocale );

			Set<ConstraintViolation<Bean>> violations = validator.validate( new Bean( "", "invalid" ) );
			assertThat( violations ).containsOnlyViolations(
					violationOf( Email.class ).withProperty( "email" ).withMessage( "must be a well-formed email address" ) );
		}
		finally {
			Locale.setDefault( defaultLocale );
		}
	}

	@TestForIssue(jiraKey = "HV-1681")
	@Test
	public void testValidOnUnknownBean() {
		Set<ConstraintViolation<AnotherBean>> violations = getValidator().validate( new AnotherBean() );
		assertNoViolations( violations );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000250:.*")
	public void testUninitializedLocale() {
		Locale defaultLocale = Locale.getDefault();

		try {
			Locale.setDefault( Locale.FRANCE );

			ValidatorFactory validatorFactory = getValidatorFactoryWithInitializedLocale( Locale.ENGLISH );

			Validator validator = validatorFactory
					.usingContext()
					.messageInterpolator( new MessageInterpolator() {

						@Override
						public String interpolate(String messageTemplate, Context context, Locale locale) {
							return validatorFactory.getMessageInterpolator().interpolate( messageTemplate, context, locale );
						}

						@Override
						public String interpolate(String messageTemplate, Context context) {
							return validatorFactory.getMessageInterpolator().interpolate( messageTemplate, context, Locale.GERMAN );
						}
					} ).getValidator();

			Set<ConstraintViolation<Bean>> violations = validator.validate( new Bean( "", "invalid" ) );
			assertThat( violations ).containsOnlyViolations(
					violationOf( Email.class ).withProperty( "email" ).withMessage( "doit être une adresse électronique syntaxiquement correcte" ) );
		}
		finally {
			Locale.setDefault( defaultLocale );
		}
	}

	@Test
	public void testBeanMetaDataClassNormalizerNoNormalizer() {
		// In this case, as we haven't registered any metadata for the hierarchy, even if we have constraints,
		// we won't have any violations.
		Set<ConstraintViolation<Bean>> violations = getValidator().validate( new BeanProxy() );
		assertNoViolations( violations );

		// Now let's register the metadata for Bean and see how it goes
		Set<Class<?>> beanMetaDataToInitialize = new HashSet<>();
		beanMetaDataToInitialize.add( Bean.class );

		ValidatorFactory validatorFactory = Validation.byProvider( PredefinedScopeHibernateValidator.class )
				.configure()
				.initializeBeanMetaData( beanMetaDataToInitialize )
				.locales( Collections.singleton( Locale.getDefault() ) )
				.buildValidatorFactory();

		// As we don't have any metadata for BeanProxy, we consider it is not constrained at all.
		violations = validatorFactory.getValidator().validate( new BeanProxy() );
		assertNoViolations( violations );
	}

	@Test
	public void testBeanMetaDataClassNormalizer() {
		Set<Class<?>> beanMetaDataToInitialize = new HashSet<>();
		beanMetaDataToInitialize.add( Bean.class );

		ValidatorFactory validatorFactory = Validation.byProvider( PredefinedScopeHibernateValidator.class )
				.configure()
				.builtinConstraints( new HashSet<>( Arrays.asList( Email.class.getName(), NotNull.class.getName() ) ) )
				.initializeBeanMetaData( beanMetaDataToInitialize )
				.locales( Collections.singleton( Locale.getDefault() ) )
				.beanMetaDataClassNormalizer( new MyProxyInterfaceBeanMetaDataClassNormalizer() )
				.buildValidatorFactory();

		Validator validator = validatorFactory.getValidator();

		Set<ConstraintViolation<Bean>> violations = validator.validate( new BeanProxy() );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withProperty( "property" ) );
	}

	@Test
	public void validatorSpecificTraversableResolver() {
		Set<Class<?>> beanMetaDataToInitialize = new HashSet<>();
		beanMetaDataToInitialize.add( Bean.class );
		beanMetaDataToInitialize.add( AnotherBean.class );
		beanMetaDataToInitialize.add( SomeEnum.class );

		ValidatorFactory validatorFactory = Validation.byProvider( PredefinedScopeHibernateValidator.class )
				.configure()
				.builtinConstraints( new HashSet<>( Arrays.asList( Email.class.getName(), NotNull.class.getName() ) ) )
				.initializeBeanMetaData( beanMetaDataToInitialize )
				.buildValidatorFactory();

		try {
			Validator validator = validatorFactory.usingContext().traversableResolver( new ThrowExceptionTraversableResolver() )
					.getValidator();
			validator.validate( new Bean() );
			fail();
		}
		catch (ValidationException e) {
			Assertions.assertThat( e ).hasCauseExactlyInstanceOf( ValidatorSpecificTraversableResolverUsedException.class );
		}
	}

	@Test
	public void variousObjectTypes() {
		Set<Class<?>> beanMetaDataToInitialize = new HashSet<>();
		beanMetaDataToInitialize.add( Bean.class );
		beanMetaDataToInitialize.add( AnotherBean.class );
		beanMetaDataToInitialize.add( SomeEnum.class );
		beanMetaDataToInitialize.add( Values.Itr.class );

		ValidatorFactory validatorFactory = Validation.byProvider( PredefinedScopeHibernateValidator.class )
				.configure()
				.builtinConstraints( new HashSet<>( Arrays.asList( Email.class.getName(), NotNull.class.getName() ) ) )
				.initializeBeanMetaData( beanMetaDataToInitialize )
				.buildValidatorFactory();

		Validator validator = validatorFactory.getValidator();
		validator.validate( new Bean() );
	}

	@Test
	public void customizingMessageInterpolator() {
		ValidatorFactory validatorFactory = getValidatorFactory();

		Validator validator = validatorFactory.getValidator();
		Set<ConstraintViolation<Bean>> violations = validator.validate( new Bean( null, null ) );
		assertThat( violations ).containsOnlyViolations( violationOf( NotNull.class ).withMessage( "must not be null" ) );

		validator = validatorFactory.usingContext()
				.messageInterpolator( new MessageInterpolator() {

					@Override
					public String interpolate(String messageTemplate, Context context, Locale locale) {
						return "another string";
					}

					@Override
					public String interpolate(String messageTemplate, Context context) {
						return "another string";
					}
				} ).getValidator();

		violations = validator.validate( new Bean( null, null ) );
		assertThat( violations ).containsOnlyViolations( violationOf( NotNull.class ).withMessage( "another string" ) );
	}

	private static ValidatorFactory getValidatorFactory() {
		Set<Class<?>> beanMetaDataToInitialize = new HashSet<>();
		beanMetaDataToInitialize.add( Bean.class );
		beanMetaDataToInitialize.add( AnotherBean.class );

		return Validation.byProvider( PredefinedScopeHibernateValidator.class )
				.configure()
				.builtinConstraints( new HashSet<>( Arrays.asList( Email.class.getName(), NotNull.class.getName() ) ) )
				.initializeBeanMetaData( beanMetaDataToInitialize )
				.buildValidatorFactory();
	}

	private static Validator getValidator() {
		return getValidatorFactory().getValidator();
	}

	private static ValidatorFactory getValidatorFactoryWithInitializedLocale(Locale locale) {
		Set<Class<?>> beanMetaDataToInitialize = new HashSet<>();
		beanMetaDataToInitialize.add( Bean.class );

		return Validation.byProvider( PredefinedScopeHibernateValidator.class )
				.configure()
				.builtinConstraints( new HashSet<>( Arrays.asList( Email.class.getName(), NotNull.class.getName() ) ) )
				.initializeBeanMetaData( beanMetaDataToInitialize )
				.locales( Collections.singleton( locale ) )
				.buildValidatorFactory();
	}

	private static Validator getValidatorWithInitializedLocale(Locale locale) {
		return getValidatorFactoryWithInitializedLocale( locale ).getValidator();
	}

	private enum SomeEnum {
		VALUE;
	}

	final class Values extends AbstractCollection<String> implements Collection<String> {
		public Iterator<String> iterator() {
			return new Itr( null );
		}

		public int size() {
			return 0;
		}

		public boolean isEmpty() {
			return true;
		}

		final class Itr implements Iterator<String> {
			private final Iterator<String> iterator;

			Itr(final Iterator<String> iterator) {
				this.iterator = iterator;
			}

			public boolean hasNext() {
				return iterator.hasNext();
			}

			public String next() {
				return "";
			}
		}
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

	private static class AnotherBean {

		@Valid
		private final UnknownBean bean;


		private AnotherBean() {
			bean = new UnknownBean();
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

	private interface MyProxyInterface {
	}

	private static class MyProxyInterfaceBeanMetaDataClassNormalizer implements BeanMetaDataClassNormalizer {

		@Override
		public <T> Class<? super T> normalize(Class<T> beanClass) {
			if ( MyProxyInterface.class.isAssignableFrom( beanClass ) ) {
				return beanClass.getSuperclass();
			}

			return beanClass;
		}
	}

	private static class BeanProxy extends Bean implements MyProxyInterface {
	}

	private static class ThrowExceptionTraversableResolver implements TraversableResolver {

		@Override
		public boolean isReachable(Object traversableObject, Node traversableProperty, Class<?> rootBeanType, Path pathToTraversableObject,
				ElementType elementType) {
			throw new ValidatorSpecificTraversableResolverUsedException();
		}

		@Override
		public boolean isCascadable(Object traversableObject, Node traversableProperty, Class<?> rootBeanType, Path pathToTraversableObject,
				ElementType elementType) {
			throw new ValidatorSpecificTraversableResolverUsedException();
		}
	}

	private static class ValidatorSpecificTraversableResolverUsedException extends RuntimeException {
	}
}
