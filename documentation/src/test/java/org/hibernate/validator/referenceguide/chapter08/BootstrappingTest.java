package org.hibernate.validator.referenceguide.chapter08;

import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.cfg.ConstraintMapping;

public class BootstrappingTest {

	public void buildDefaultValidatorFactory() {
		//tag::buildDefaultValidatorFactory[]
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		//end::buildDefaultValidatorFactory[]
	}

	public void byProvider() {
		//tag::byProvider[]
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		//end::byProvider[]
	}

	public void byDefaultProvider() {
		//tag::byDefaultProvider[]
		ValidatorFactory validatorFactory = Validation.byDefaultProvider()
				.configure()
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		//end::byDefaultProvider[]
	}

	public void providerResolver() {
		//tag::providerResolver[]
		ValidatorFactory validatorFactory = Validation.byDefaultProvider()
				.providerResolver( new OsgiServiceDiscoverer() )
				.configure()
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		//end::providerResolver[]
	}

	public void messageInterpolator() {
		//tag::messageInterpolator[]
		ValidatorFactory validatorFactory = Validation.byDefaultProvider()
				.configure()
				.messageInterpolator( new MyMessageInterpolator() )
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		//end::messageInterpolator[]
	}

	public void traversableResolver() {
		//tag::traversableResolver[]
		ValidatorFactory validatorFactory = Validation.byDefaultProvider()
				.configure()
				.traversableResolver( new MyTraversableResolver() )
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		//end::traversableResolver[]
	}

	public void clockProvider() {
		//tag::clockProvider[]
		ValidatorFactory validatorFactory = Validation.byDefaultProvider()
				.configure()
				.clockProvider( new FixedClockProvider( ZonedDateTime.of( 2016, 6, 15, 0, 0, 0, 0, ZoneId.of( "Europe/Paris" ) ) ) )
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		//end::clockProvider[]
	}

	public void constraintValidatorFactory() {
		//tag::constraintValidatorFactory[]
		ValidatorFactory validatorFactory = Validation.byDefaultProvider()
				.configure()
				.constraintValidatorFactory( new MyConstraintValidatorFactory() )
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		//end::constraintValidatorFactory[]
	}

	public void parameterNameProvider() {
		//tag::parameterNameProvider[]
		ValidatorFactory validatorFactory = Validation.byDefaultProvider()
				.configure()
				.parameterNameProvider( new MyParameterNameProvider() )
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		//end::parameterNameProvider[]
	}

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

	public void providerSpecificOptionViaAddProperty() {
		//tag::providerSpecificOptionViaAddProperty[]
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addProperty( "hibernate.validator.fail_fast", "true" )
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
		//end::providerSpecificOptionViaAddProperty[]
	}

	public void usingContext() {
		//tag::usingContext[]
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

		Validator validator = validatorFactory.usingContext()
				.messageInterpolator( new MyMessageInterpolator() )
				.traversableResolver( new MyTraversableResolver() )
				.getValidator();
		//end::usingContext[]
	}
}
