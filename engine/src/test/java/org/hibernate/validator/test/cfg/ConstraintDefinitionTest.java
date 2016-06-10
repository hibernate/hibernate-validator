/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cfg;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;

import java.io.InputStream;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Payload;
import javax.validation.UnexpectedTypeException;
import javax.validation.ValidationException;
import javax.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.internal.cfg.context.DefaultConstraintMapping;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit test for {@link ConstraintMapping#constraintDefinition(Class)} et al.
 *
 * @author Yoann Rodiere
 */
@TestForIssue( jiraKey = "HV-501")
public class ConstraintDefinitionTest {

	private HibernateValidatorConfiguration config;
	private DefaultConstraintMapping mapping;

	@BeforeMethod
	public void setUp() {
		config = ValidatorUtil.getConfiguration( HibernateValidator.class );
		mapping = (DefaultConstraintMapping) config.createConstraintMapping();
	}

	@Test(
			expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp = "HV[0-9]*: The annotation type must not be null when creating a constraint definition."
	)
	public void testNullClass() {
		mapping.constraintDefinition( null );
	}

	@Test(
			expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp = "HV[0-9]*: The annotation type must be annotated with @javax.validation.Constraint when creating a constraint definition."
	)
	public void testNonConstraintAnnotation() {
		mapping.constraintDefinition( NonConstraintAnnotation.class );
	}

	@Test
	public void testConstraintMapping() {
		mapping.constraintDefinition( ConstraintAnnotation.class )
				.validatedBy( NonDefaultLongValidator.class );

		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<? extends ConstraintViolation<?>> violations = validator.validate( new ConstrainedLongFieldBean() );
		assertNumberOfViolations( violations, 1 );
		assertCorrectValidatorTypes( violations, NonDefaultLongValidator.class );
	}

	@Test
	public void testConstraintMappingDefaultsToIncludingExistingValidators() {
		mapping.constraintDefinition( ConstraintAnnotation.class )
				.validatedBy( NonDefaultLongValidator.class );

		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		/* Check that the default string validator was not lost when the new validator was set
		 * (i.e. check that existing validators were actually included)
		 */
		Set<? extends ConstraintViolation<?>> violations = validator.validate( new ConstrainedStringFieldBean() );
		assertNumberOfViolations( violations, 1 );
		assertCorrectValidatorTypes( violations, DefaultStringValidator.class );
	}
	
	@Test
	public void testConstraintMappingIncludingExistingValidators() {
		mapping.constraintDefinition( ConstraintAnnotation.class )
				.includeExistingValidators( true )
				.validatedBy( NonDefaultLongValidator.class );

		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		/* Check that the default string validator was not lost when the new validator was set
		 * (i.e. check that existing validators were actually included)
		 */
		Set<? extends ConstraintViolation<?>> violations = validator.validate( new ConstrainedStringFieldBean() );
		assertNumberOfViolations( violations, 1 );
		assertCorrectValidatorTypes( violations, DefaultStringValidator.class );
	}

	@Test
	public void testConstraintMappingExcludingExistingValidators() {
		mapping.constraintDefinition( ConstraintAnnotation.class )
				.includeExistingValidators( false )
				.validatedBy( NonDefaultIntegerValidator.class );

		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<? extends ConstraintViolation<?>> violations = validator.validate( new ConstrainedIntegerFieldBean() );
		assertNumberOfViolations( violations, 1 );
		assertCorrectValidatorTypes( violations, NonDefaultIntegerValidator.class );
	}

	@Test
	public void testConstraintMappingIncludingExistingValidatorsThenExcludingThem() {
		mapping.constraintDefinition( ConstraintAnnotation.class )
				.includeExistingValidators( true )
				.validatedBy( NonDefaultLongValidator.class )
				.includeExistingValidators( false )
				.validatedBy( NonDefaultIntegerValidator.class );

		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<? extends ConstraintViolation<?>> violations = validator.validate( new ConstrainedLongFieldBean() );
		assertNumberOfViolations( violations, 1 );
		assertCorrectValidatorTypes( violations, NonDefaultLongValidator.class );
		
		violations = validator.validate( new ConstrainedIntegerFieldBean() );
		assertNumberOfViolations( violations, 1 );
		assertCorrectValidatorTypes( violations, NonDefaultIntegerValidator.class );
	}

	@Test(
			expectedExceptions = ValidationException.class,
			expectedExceptionsMessageRegExp = "HV[0-9]*:"
					+ " .*\\$ConstraintAnnotation is configured more than once via the programmatic constraint definition API."
	)
	public void testMultipleDefinitionForSameConstraintOnSameConstraintMapping() {
		mapping.constraintDefinition( ConstraintAnnotation.class )
				.validatedBy( NonDefaultLongValidator.class )
				.constraintDefinition( ConstraintAnnotation.class )
				.includeExistingValidators( false )
				.validatedBy( NonDefaultIntegerValidator.class );
	}

	@Test(
			expectedExceptions = ValidationException.class,
			expectedExceptionsMessageRegExp = "HV[0-9]*:"
					+ " .*\\$ConstraintAnnotation is configured more than once via the programmatic constraint definition API."
	)
	public void testMultipleDefinitionForSameConstraintOnDifferentConstraintMappings() {
		mapping.constraintDefinition( ConstraintAnnotation.class )
				.validatedBy( NonDefaultLongValidator.class );

		ConstraintMapping otherMapping = config.createConstraintMapping();
		otherMapping.constraintDefinition( ConstraintAnnotation.class )
				.includeExistingValidators( false )
				.validatedBy( NonDefaultIntegerValidator.class );
		
		config.addMapping( mapping ); 
		config.addMapping( otherMapping );
		config.buildValidatorFactory().getValidator();
	}

	@Test
	public void testXmlConstraintDefinitionMergedWithProgrammaticConfiguration() {
		final InputStream xmlMapping = ConstraintDefinitionTest.class.getResourceAsStream(
				"ConstraintDefinitionTest_mapping.xml"
		);
		
		config.addMapping( xmlMapping ); // Adds NonDefaultLongValidator and keeps default validators
		
		mapping.constraintDefinition( ConstraintAnnotation.class )
				.validatedBy( NonDefaultShortValidator.class ); // Adds this on top of XML configuration
		
		config.addMapping( mapping ); 
		Validator validator = config.buildValidatorFactory().getValidator();

		// Defaults are untouched
		Set<? extends ConstraintViolation<?>> violations = validator.validate( new ConstrainedStringFieldBean() );
		assertNumberOfViolations( violations, 1 );
		assertCorrectValidatorTypes( violations, DefaultStringValidator.class );

		// XML configuration is taken into account
		violations = validator.validate( new ConstrainedLongFieldBean() );
		assertNumberOfViolations( violations, 1 );
		assertCorrectValidatorTypes( violations, NonDefaultLongValidator.class );

		// Programmatic configuration is also taken into account
		violations = validator.validate( new ConstrainedShortFieldBean() );
		assertNumberOfViolations( violations, 1 );
		assertCorrectValidatorTypes( violations, NonDefaultShortValidator.class );
	}

	@Test
	public void testXmlConstraintDefinitionOverriddenByProgrammaticConfiguration() {
		final InputStream xmlMapping = ConstraintDefinitionTest.class.getResourceAsStream(
				"ConstraintDefinitionTest_mapping.xml"
		);
		
		config.addMapping( xmlMapping ); // Adds NonDefaultLongValidator and keeps default validators
		
		mapping.constraintDefinition( ConstraintAnnotation.class ) // Overrides XML configuration (and defaults)
				.includeExistingValidators( false )
				.validatedBy( NonDefaultIntegerValidator.class )
				.validatedBy( OtherNonDefaultLongValidator.class ); 
		
		config.addMapping( mapping ); 
		Validator validator = config.buildValidatorFactory().getValidator();

		// Defaults are overridden
		Set<? extends ConstraintViolation<?>> violations = validator.validate( new ConstrainedIntegerFieldBean() );
		assertNumberOfViolations( violations, 1 );
		assertCorrectValidatorTypes( violations, NonDefaultIntegerValidator.class );

		// XML configuration is overridden by programmatic configuration
		violations = validator.validate( new ConstrainedLongFieldBean() );
		assertNumberOfViolations( violations, 1 );
		assertCorrectValidatorTypes( violations, OtherNonDefaultLongValidator.class );
	}

	@Test(
			expectedExceptions = UnexpectedTypeException.class,
			expectedExceptionsMessageRegExp = "HV000150:.*"
	)
	public void testMultipleValidatorsForSameType() {
		mapping.constraintDefinition( ConstraintAnnotation.class )
				.includeExistingValidators( true )
				.validatedBy( NonDefaultIntegerValidator.class );
		
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();
		validator.validate( new ConstrainedIntegerFieldBean() );
	}

	@Test(
			expectedExceptions = UnexpectedTypeException.class,
			expectedExceptionsMessageRegExp = "HV000150:.*"
	)
	public void testMultipleValidatorsForSameTypeWithNoCallToIncludeExistingValidators() {
		mapping.constraintDefinition( ConstraintAnnotation.class )
				.validatedBy( NonDefaultIntegerValidator.class );
		
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();
		validator.validate( new ConstrainedIntegerFieldBean() );
	}

	@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
	@Retention(RUNTIME)
	@Documented
	private @interface NonConstraintAnnotation {
	}

	@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
	@Retention(RUNTIME)
	@Documented
	@Constraint(validatedBy = { DefaultIntegerValidator.class, DefaultStringValidator.class })
	private @interface ConstraintAnnotation {
		String message() default "Default ConstraintAnnotation violation message";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	/*
	 * Implementation note: we are compiling in Java 6 mode, so {@code @SafeVarargs} is of no use here. Thus, we work
	 * around the compiler warnings by not restraining the type of classes passed as parameters and just casting them.
	 */
	@SafeVarargs
	@SuppressWarnings("unchecked")
	private static void assertCorrectValidatorTypes(Set<? extends ConstraintViolation<?>> violations,
			Class<? /* extends StubValidator */>... validatorClasses) {
		List<String> expectedMessages = new ArrayList<String>();
		for ( Class<?> validatorClass : validatorClasses ) {
			String identifyingMessage = StubValidator.getIdentifyingMessage(
					(Class<? extends StubValidator<?>>) validatorClass
					);
			expectedMessages.add( identifyingMessage );
		}
		assertCorrectConstraintViolationMessages(
				violations, expectedMessages.toArray( new String[expectedMessages.size()] ) );
	}

	private static class StubValidator<T>
			implements ConstraintValidator<ConstraintAnnotation, T> {

		/**
		 * Returns a message that will allow to uniquely identify the originating validator from a constraint violation
		 * message.
		 * <p>
		 * This is useful for testing purposes.
		 * 
		 * @param validator The validator class
		 * @return The uniquely identifying message for this validator
		 */
		public static String getIdentifyingMessage(
				@SuppressWarnings("rawtypes") Class<? extends StubValidator> validatorClass) {
			return validatorClass.getName();
		}

		@Override
		public void initialize(ConstraintAnnotation constraintAnnotation) {
			// Nothing to do here
		}

		@Override
		public boolean isValid(T value, ConstraintValidatorContext context) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate( getIdentifyingMessage( getClass() ) )
					.addConstraintViolation();
			return false;
		}
	}

	public static class DefaultIntegerValidator extends StubValidator<Integer> {
		/*
		 * Nothing special here: everything is in the parent class, which uses getClass() to enable derived
		 * class-specific behavior.
		 */
	}

	public static class NonDefaultIntegerValidator extends StubValidator<Integer> {
		/*
		 * Nothing special here: everything is in the parent class, which uses getClass() to enable derived
		 * class-specific behavior.
		 */
	}

	public static class DefaultStringValidator extends StubValidator<String> {
		/*
		 * Nothing special here: everything is in the parent class, which uses getClass() to enable derived
		 * class-specific behavior.
		 */
	}

	public static class NonDefaultLongValidator extends StubValidator<Long> {
		/*
		 * Nothing special here: everything is in the parent class, which uses getClass() to enable derived
		 * class-specific behavior.
		 */
	}

	public static class OtherNonDefaultLongValidator extends StubValidator<Long> {
		/*
		 * Nothing special here: everything is in the parent class, which uses getClass() to enable derived
		 * class-specific behavior.
		 */
	}

	public static class NonDefaultShortValidator extends StubValidator<Short> {
		/*
		 * Nothing special here: everything is in the parent class, which uses getClass() to enable derived
		 * class-specific behavior.
		 */
	}

	private static class ConstrainedStringFieldBean {

		@ConstraintAnnotation
		private String field;
	}

	private static class ConstrainedIntegerFieldBean {

		@ConstraintAnnotation
		private Integer field;
	}

	private static class ConstrainedLongFieldBean {

		@ConstraintAnnotation
		private Long field;
	}

	private static class ConstrainedShortFieldBean {

		@ConstraintAnnotation
		private Short field;
	}

}
