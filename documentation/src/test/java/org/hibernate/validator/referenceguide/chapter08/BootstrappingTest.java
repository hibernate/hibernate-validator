package org.hibernate.validator.referenceguide.chapter08;

import java.io.InputStream;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.cfg.ConstraintMapping;

public class BootstrappingTest {

	public void buildDefaultValidatorFactory() {
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		Validator validator = validatorFactory.getValidator();
	}

	public void byProvider() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
	}

	public void byDefaultProvider() {
		ValidatorFactory validatorFactory = Validation.byDefaultProvider()
				.configure()
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
	}

	public void providerResolver() {
		ValidatorFactory validatorFactory = Validation.byDefaultProvider()
				.providerResolver( new OsgiServiceDiscoverer() )
				.configure()
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
	}

	public void messageInterpolator() {
		ValidatorFactory validatorFactory = Validation.byDefaultProvider()
				.configure()
				.messageInterpolator( new MyMessageInterpolator() )
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
	}

	public void traversableResolver() {
		ValidatorFactory validatorFactory = Validation.byDefaultProvider()
				.configure()
				.traversableResolver( new MyTraversableResolver() )
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
	}

	public void constraintValidatorFactory() {
		ValidatorFactory validatorFactory = Validation.byDefaultProvider()
				.configure()
				.constraintValidatorFactory( new MyConstraintValidatorFactory() )
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
	}

	public void parameterNameProvider() {
		ValidatorFactory validatorFactory = Validation.byDefaultProvider()
				.configure()
				.parameterNameProvider( new MyParameterNameProvider() )
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
	}

	public void addMapping() {
		InputStream constraintMapping1 = null;
		InputStream constraintMapping2 = null;
		ValidatorFactory validatorFactory = Validation.byDefaultProvider()
				.configure()
				.addMapping( constraintMapping1 )
				.addMapping( constraintMapping2 )
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
	}

	public void providerSpecificOptions() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.failFast( true )
				.addMapping( (ConstraintMapping) null )
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
	}

	public void providerSpecificOptionViaAddProperty() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addProperty( "hibernate.validator.fail_fast", "true" )
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();
	}

	public void usingContext() {
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

		Validator validator = validatorFactory.usingContext()
				.messageInterpolator( new MyMessageInterpolator() )
				.traversableResolver(
						new MyTraversableResolver()
				)
				.getValidator();
	}
}
