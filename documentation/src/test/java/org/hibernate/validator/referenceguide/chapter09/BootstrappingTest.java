package org.hibernate.validator.referenceguide.chapter09;

import java.io.InputStream;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.junit.Test;

public class BootstrappingTest {

	@Test
	@SuppressWarnings("unused")
	public void buildDefaultValidatorFactory() {
		//tag::buildDefaultValidatorFactory[]
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		//end::buildDefaultValidatorFactory[]
	}

	@Test
	@SuppressWarnings("unused")
	public void byProvider() {
		//tag::byProvider[]
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		//end::byProvider[]
	}

	@Test
	@SuppressWarnings("unused")
	public void byDefaultProvider() {
		//tag::byDefaultProvider[]
		ValidatorFactory validatorFactory = Validation.byDefaultProvider()
				.configure()
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		//end::byDefaultProvider[]
	}

	/**
	 * This one cannot be tested
	 */
	@SuppressWarnings("unused")
	public void providerResolver() {
		//tag::providerResolver[]
		ValidatorFactory validatorFactory = Validation.byDefaultProvider()
				.providerResolver( new OsgiServiceDiscoverer() )
				.configure()
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		//end::providerResolver[]
	}

	@Test
	@SuppressWarnings("unused")
	public void messageInterpolator() {
		//tag::messageInterpolator[]
		ValidatorFactory validatorFactory = Validation.byDefaultProvider()
				.configure()
				.messageInterpolator( new MyMessageInterpolator() )
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		//end::messageInterpolator[]
	}

	@Test
	@SuppressWarnings("unused")
	public void traversableResolver() {
		//tag::traversableResolver[]
		ValidatorFactory validatorFactory = Validation.byDefaultProvider()
				.configure()
				.traversableResolver( new MyTraversableResolver() )
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		//end::traversableResolver[]
	}

	@Test
	@SuppressWarnings("unused")
	public void traversableResolverDisableCache() {
		//tag::traversableResolverDisableCache[]
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.traversableResolver( new MyFastTraversableResolver() )
				.enableTraversableResolverResultCache( false )
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		//end::traversableResolverDisableCache[]
	}

	@Test
	@SuppressWarnings("unused")
	public void clockProvider() {
		//tag::clockProvider[]
		ValidatorFactory validatorFactory = Validation.byDefaultProvider()
				.configure()
				.clockProvider( new FixedClockProvider( ZonedDateTime.of( 2016, 6, 15, 0, 0, 0, 0, ZoneId.of( "Europe/Paris" ) ) ) )
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		//end::clockProvider[]
	}

	@Test
	@SuppressWarnings("unused")
	public void temporalValidationTolerance() {
		//tag::temporalValidationTolerance[]
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.temporalValidationTolerance( Duration.ofMillis( 10 ) )
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		//end::temporalValidationTolerance[]
	}

	@Test
	@SuppressWarnings("unused")
	public void valueExtractor() {
		//tag::valueExtractor[]
		ValidatorFactory validatorFactory = Validation.byDefaultProvider()
				.configure()
				.addValueExtractor( new MultimapKeyValueExtractor() )
				.addValueExtractor( new MultimapValueValueExtractor() )
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		//end::valueExtractor[]
	}

	@Test
	@SuppressWarnings("unused")
	public void constraintValidatorFactory() {
		//tag::constraintValidatorFactory[]
		ValidatorFactory validatorFactory = Validation.byDefaultProvider()
				.configure()
				.constraintValidatorFactory( new MyConstraintValidatorFactory() )
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		//end::constraintValidatorFactory[]
	}

	@Test
	@SuppressWarnings("unused")
	public void parameterNameProvider() {
		//tag::parameterNameProvider[]
		ValidatorFactory validatorFactory = Validation.byDefaultProvider()
				.configure()
				.parameterNameProvider( new MyParameterNameProvider() )
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		//end::parameterNameProvider[]
	}

	/**
	 * This one cannot be tested
	 */
	@SuppressWarnings("unused")
	public void addMapping() {
		//tag::addMapping[]
		InputStream constraintMapping1 = null;
		InputStream constraintMapping2 = null;
		ValidatorFactory validatorFactory = Validation.byDefaultProvider()
				.configure()
				.addMapping( constraintMapping1 )
				.addMapping( constraintMapping2 )
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		//end::addMapping[]
	}

	/**
	 * This one cannot be tested
	 */
	@SuppressWarnings("unused")
	public void providerSpecificOptions() {
		//tag::providerSpecificOptions[]
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.failFast( true )
				.addMapping( (ConstraintMapping) null )
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		//end::providerSpecificOptions[]
	}

	@Test
	@SuppressWarnings("unused")
	public void providerSpecificOptionViaAddProperty() {
		//tag::providerSpecificOptionViaAddProperty[]
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addProperty( "hibernate.validator.fail_fast", "true" )
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		//end::providerSpecificOptionViaAddProperty[]
	}

	@Test
	@SuppressWarnings("unused")
	public void usingContext() {
		//tag::usingContext[]
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

		Validator validator = validatorFactory.usingContext()
				.messageInterpolator( new MyMessageInterpolator() )
				.traversableResolver( new MyTraversableResolver() )
				.getValidator();
		//end::usingContext[]
	}

	@Test
	@SuppressWarnings("unused")
	public void scriptEvaluatorFactoryProgrammatically() {
		//tag::scriptEvaluatorFactoryProgrammatically[]
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.scriptEvaluatorFactory( new CustomScriptEvaluatorFactory() )
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		//end::scriptEvaluatorFactoryProgrammatically[]
	}
}
