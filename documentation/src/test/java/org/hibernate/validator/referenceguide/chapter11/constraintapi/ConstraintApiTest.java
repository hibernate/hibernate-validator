package org.hibernate.validator.referenceguide.chapter11.constraintapi;

import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.Test;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.GenericConstraintDef;
import org.hibernate.validator.cfg.defs.MaxDef;
import org.hibernate.validator.cfg.defs.MinDef;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.cfg.defs.SizeDef;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

public class ConstraintApiTest {

	@Test
	public void constraintMapping() {
		HibernateValidatorConfiguration configuration = Validation
				.byProvider( HibernateValidator.class )
				.configure();

		ConstraintMapping constraintMapping = configuration.createConstraintMapping();

		constraintMapping
			.type( Car.class )
				.property( "manufacturer", FIELD )
					.constraint( new NotNullDef() )
				.property( "licensePlate", FIELD )
					.ignoreAnnotations()
					.constraint( new NotNullDef() )
					.constraint( new SizeDef().min( 2 ).max( 14 ) )
			.type( RentalCar.class )
				.property( "rentalStation", METHOD )
					.constraint( new NotNullDef() );

		Validator validator = configuration.addMapping( constraintMapping )
				.buildValidatorFactory()
				.getValidator();
	}
	
	@Test
	public void genericConstraintDef() {
		HibernateValidatorConfiguration configuration = Validation
				.byProvider( HibernateValidator.class )
				.configure();

		ConstraintMapping constraintMapping = configuration.createConstraintMapping();

		constraintMapping
			.type( Car.class )
				.property( "licensePlate", FIELD )
					.constraint( new GenericConstraintDef<CheckCase>( CheckCase.class )
						.param( "value", CaseMode.UPPER )
					);
	}	
	
	@Test
	public void cascaded() {
		HibernateValidatorConfiguration configuration = Validation
				.byProvider( HibernateValidator.class )
				.configure();

		ConstraintMapping constraintMapping = configuration.createConstraintMapping();

		constraintMapping
			.type( Car.class )
				.property( "driver", FIELD )
					.constraint( new NotNullDef() )
					.valid()
			.type( Person.class )
				.property( "name", FIELD )
					.constraint( new NotNullDef() );
	}
	
	@Test
	public void methodConfiguration() {
		HibernateValidatorConfiguration configuration = Validation
				.byProvider( HibernateValidator.class )
				.configure();

		ConstraintMapping constraintMapping = configuration.createConstraintMapping();

		constraintMapping
			.type( Car.class )
				.method( "drive", int.class )
					.parameter( 0 )
						.constraint( new MaxDef().value ( 75 ) )
				.method( "getDriver" )
					.returnValue()
						.constraint( new NotNullDef() )
						.valid(); 
	}
	
	@Test
	public void defaultGroupSequence() {
		HibernateValidatorConfiguration configuration = Validation
				.byProvider( HibernateValidator.class )
				.configure();

		ConstraintMapping constraintMapping = configuration.createConstraintMapping();

		constraintMapping
			.type( Car.class )
				.defaultGroupSequence( Car.class, CarChecks.class )
			.type( RentalCar.class )
				.defaultGroupSequenceProviderClass( RentalCarGroupSequenceProvider.class );
	}
}
