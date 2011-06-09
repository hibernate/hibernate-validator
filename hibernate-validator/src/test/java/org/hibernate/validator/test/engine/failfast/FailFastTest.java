package org.hibernate.validator.test.engine.failfast;

import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.slf4j.Logger;
import org.testng.annotations.Test;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.method.MethodConstraintViolationException;
import org.hibernate.validator.method.MethodValidator;
import org.hibernate.validator.testutil.ValidatorUtil;
import org.hibernate.validator.util.LoggerFactory;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidatingProxy;
import static org.testng.Assert.fail;

/**
 * See HV-381
 *
 * @author Emmanuel Bernard
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public class FailFastTest {
	private static final Logger log = LoggerFactory.make();

	private final A testInstance = new A();

	@Test
	public void testFailFastDefaultBehaviour() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );
		final ValidatorFactory factory = configuration.buildValidatorFactory();

		final Validator validator = factory.getValidator();

		Set<ConstraintViolation<A>> constraintViolations = validator.validate( testInstance );
		assertNumberOfViolations( constraintViolations, 2 );
	}

	@Test
	public void testFailFastMethodValidationDefaultBehaviour() {

		TestService service = getValidatingProxy( new TestServiceImpl() );

		try {
			service.testMethod( " ", null );
			fail();
		}
		catch ( MethodConstraintViolationException e ) {
			assertNumberOfViolations( e.getConstraintViolations(), 3 );
		}
	}

	@Test
	public void testFailFastSetOnConfiguration() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );
		final ValidatorFactory factory = configuration.failFast( true ).buildValidatorFactory();

		final Validator validator = factory.getValidator();
		Set<ConstraintViolation<A>> constraintViolations = validator.validate( testInstance );
		assertNumberOfViolations( constraintViolations, 1 );
	}

	@Test
	public void testFailFastMethodValidationOnConfiguration() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );
		final ValidatorFactory factory = configuration.failFast( true ).buildValidatorFactory();

		final Validator validator = factory.getValidator();
		final MethodValidator methodValidator = validator.unwrap( MethodValidator.class );

		TestService service = getValidatingProxy( new TestServiceImpl(), methodValidator );

		try {
			service.testMethod( "a", null );
			fail();
		}
		catch ( MethodConstraintViolationException e ) {
			assertNumberOfViolations( e.getConstraintViolations(), 1 );
		}
	}

	@Test
	public void testFailFastSetOnValidatorFactory() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );
		final ValidatorFactory factory = configuration.buildValidatorFactory();

		final Validator validator =
				factory.unwrap( HibernateValidatorFactory.class )
						.usingContext()
						.failFast( true )
						.getValidator();
		Set<ConstraintViolation<A>> constraintViolations = validator.validate( testInstance );
		assertNumberOfViolations( constraintViolations, 1 );
	}

	@Test
	public void testFailFastMethodValidationSetOnValidatorFactory() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );
		final ValidatorFactory factory = configuration.buildValidatorFactory();

		final Validator validator =
				factory.unwrap( HibernateValidatorFactory.class )
						.usingContext()
						.failFast( true )
						.getValidator();
		final MethodValidator methodValidator = validator.unwrap( MethodValidator.class );

		TestService service = getValidatingProxy( new TestServiceImpl(), methodValidator );

		try {
			service.testMethod( " ", null );
			fail();
		}
		catch ( MethodConstraintViolationException e ) {
			assertNumberOfViolations( e.getConstraintViolations(), 1 );
		}
	}

	@Test
	public void testFailFastSetWithProperty() {
		// with fail fast
		HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );
		ValidatorFactory factory = configuration.addProperty( HibernateValidatorConfiguration.FAIL_FAST, "true" )
				.buildValidatorFactory();

		Validator validator = factory.getValidator();

		Set<ConstraintViolation<A>> constraintViolations = validator.validate( testInstance );
		assertNumberOfViolations( constraintViolations, 1 );

		// without fail fast
		configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );
		factory = configuration.addProperty( HibernateValidatorConfiguration.FAIL_FAST, "false" )
				.buildValidatorFactory();

		validator = factory.getValidator();
		constraintViolations = validator.validate( testInstance );
		assertNumberOfViolations( constraintViolations, 2 );
	}

	@Test
	public void testFailFastMethodValidationSetWithProperty() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );
		final ValidatorFactory factory = configuration.addProperty( HibernateValidatorConfiguration.FAIL_FAST, "true" )
				.buildValidatorFactory();

		final Validator validator = factory.getValidator();
		final MethodValidator methodValidator = validator.unwrap( MethodValidator.class );

		TestService service = getValidatingProxy( new TestServiceImpl(), methodValidator );

		try {
			service.testMethod( " ", null );
			fail();
		}
		catch ( MethodConstraintViolationException e ) {
			assertNumberOfViolations( e.getConstraintViolations(), 1 );
		}
	}

	@Test
	public void testFailFastSetWithInvalidProperty() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );

		//Default fail fast property value is false
		final ValidatorFactory factory = configuration.addProperty(
				HibernateValidatorConfiguration.FAIL_FAST, "not correct"
		).buildValidatorFactory();

		final Validator validator = factory.getValidator();
		Set<ConstraintViolation<A>> constraintViolations = validator.validate( testInstance );
		assertNumberOfViolations( constraintViolations, 2 );
	}

	@Test(expectedExceptions = ValidationException.class,
			expectedExceptionsMessageRegExp = "Inconsistent fail fast configuration.*")
	public void testFailFastSetWithInconsistentConfiguration() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );

		//Default fail fast property value is false
		final ValidatorFactory factory = configuration.addProperty(
				HibernateValidatorConfiguration.FAIL_FAST, "false"
		).failFast( true ).buildValidatorFactory();

		factory.getValidator();
	}

	public void testFailSafePerf() {
		final Validator regularValidator = ValidatorUtil.getConfiguration().buildValidatorFactory().getValidator();
		final Validator failFastValidator = ValidatorUtil.getConfiguration()
				.buildValidatorFactory()
				.unwrap( HibernateValidatorFactory.class )
				.usingContext()
				.failFast( true )
				.getValidator();

		final int looptime = 50000;
		for ( int i = 0; i < looptime; i++ ) {
			validateBatch( regularValidator );
		}

		for ( int i = 0; i < looptime; i++ ) {
			validateBatch( failFastValidator );
		}

		long start = System.nanoTime();
		for ( int i = 0; i < looptime; i++ ) {
			validateBatch( regularValidator );
		}
		long timeOfRegular = System.nanoTime() - start;

		start = System.nanoTime();
		for ( int i = 0; i < looptime; i++ ) {
			validateBatch( failFastValidator );
		}
		long timeOfFailFast = System.nanoTime() - start;

		log.debug( "Regular = " + timeOfRegular + "\n FailFast:" + timeOfFailFast );
	}

	private void validateBatch(Validator validator) {
		validator.validate( buildA() );
	}

	static int i = 0;

	A buildA() {
		A a = new A();
		a.b = "bbb" + i++;
		a.file = "test" + i++ + ".txt";
		a.bs.add( buildB() );
		a.bs.add( buildB() );
		a.bs.add( buildB() );
		a.bs.add( buildB() );
		return a;
	}

	B buildB() {
		B b = new B();
		b.size = 45 + i++;
		return b;
	}

	class A {
		@NotNull
		String b;

		@NotNull
		@Email
		String c;

		@Pattern(regexp = ".*\\.txt$")
		String file;

		@Valid
		Set<B> bs = new HashSet<B>();
	}

	class B {
		@Min(value = 10)
		@Max(value = 30)
		@NotNull
		Integer size;
	}

	interface TestService {
		void testMethod(@Min(2) @NotBlank String param1, @NotNull String param2);
	}

	class TestServiceImpl implements TestService {
		public void testMethod(String param1, String param2) {
		}
	}
}
