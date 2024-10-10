/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.referenceguide.chapter12.constraintapi;

import java.util.List;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.GenericConstraintDef;
import org.hibernate.validator.cfg.defs.MaxDef;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.cfg.defs.SizeDef;
import org.hibernate.validator.constraints.URL;
import org.hibernate.validator.constraintvalidators.RegexpURLValidator;

import org.junit.Test;

public class ConstraintApiTest {

	@Test
	public void constraintMapping() {
		// @formatter:off
		//tag::constraintMapping[]
		HibernateValidatorConfiguration configuration = Validation
				.byProvider( HibernateValidator.class )
				.configure();

		ConstraintMapping constraintMapping = configuration.createConstraintMapping();

		constraintMapping
			.type( Car.class )
				.field( "manufacturer" )
					.constraint( new NotNullDef() )
				.field( "licensePlate" )
					.ignoreAnnotations( true )
					.constraint( new NotNullDef() )
					.constraint( new SizeDef().min( 2 ).max( 14 ) )
			.type( RentalCar.class )
				.getter( "rentalStation" )
					.constraint( new NotNullDef() );

		Validator validator = configuration.addMapping( constraintMapping )
				.buildValidatorFactory()
				.getValidator();
		//end::constraintMapping[]
		// @formatter:on
	}

	@Test
	public void genericConstraintDef() {
		HibernateValidatorConfiguration configuration = Validation
				.byProvider( HibernateValidator.class )
				.configure();
		// @formatter:off
		//tag::genericConstraintDef[]
		ConstraintMapping constraintMapping = configuration.createConstraintMapping();

		constraintMapping
			.type( Car.class )
				.field( "licensePlate" )
					.constraint( new GenericConstraintDef<>( CheckCase.class )
						.param( "value", CaseMode.UPPER )
					);
		//end::genericConstraintDef[]
		// @formatter:on
	}

	@Test
	public void nestedContainerElementConstraint() {
		HibernateValidatorConfiguration configuration = Validation
				.byProvider( HibernateValidator.class )
				.configure();
		// @formatter:off
		//tag::nestedContainerElementConstraint[]
		ConstraintMapping constraintMapping = configuration.createConstraintMapping();

		constraintMapping
			.type( Car.class )
				.field( "manufacturer" )
					.constraint( new NotNullDef() )
				.field( "licensePlate" )
					.ignoreAnnotations( true )
					.constraint( new NotNullDef() )
					.constraint( new SizeDef().min( 2 ).max( 14 ) )
				.field( "partManufacturers" )
					.containerElementType( 0 )
						.constraint( new NotNullDef() )
					.containerElementType( 1, 0 )
						.constraint( new NotNullDef() )
			.type( RentalCar.class )
				.getter( "rentalStation" )
					.constraint( new NotNullDef() );
		//end::nestedContainerElementConstraint[]
		// @formatter:on
	}


	@Test
	public void cascaded() {
		HibernateValidatorConfiguration configuration = Validation
				.byProvider( HibernateValidator.class )
				.configure();

		// @formatter:off
		//tag::cascaded[]
		ConstraintMapping constraintMapping = configuration.createConstraintMapping();

		constraintMapping
			.type( Car.class )
				.field( "driver" )
					.constraint( new NotNullDef() )
					.valid()
					.convertGroup( Default.class ).to( PersonDefault.class )
				.field( "partManufacturers" )
					.containerElementType( 0 )
						.valid()
					.containerElementType( 1, 0 )
						.valid()
			.type( Person.class )
				.field( "name" )
					.constraint( new NotNullDef().groups( PersonDefault.class ) );
		//end::cascaded[]
		// @formatter:on
	}

	@Test
	public void executableConfiguration() {
		HibernateValidatorConfiguration configuration = Validation
				.byProvider( HibernateValidator.class )
				.configure();

		// @formatter:off
		//tag::executableConfiguration[]
		ConstraintMapping constraintMapping = configuration.createConstraintMapping();

		constraintMapping
			.type( Car.class )
				.constructor( String.class )
					.parameter( 0 )
						.constraint( new SizeDef().min( 3 ).max( 50 ) )
					.returnValue()
						.valid()
				.method( "drive", int.class )
					.parameter( 0 )
						.constraint( new MaxDef().value( 75 ) )
				.method( "load", List.class, List.class )
					.crossParameter()
						.constraint( new GenericConstraintDef<>(
								LuggageCountMatchesPassengerCount.class ).param(
									"piecesOfLuggagePerPassenger", 2
								)
						)
				.method( "getDriver" )
					.returnValue()
						.constraint( new NotNullDef() )
						.valid();
		//end::executableConfiguration[]
		// @formatter:on
	}

	@Test
	public void defaultGroupSequence() {
		HibernateValidatorConfiguration configuration = Validation
				.byProvider( HibernateValidator.class )
				.configure();

		// @formatter:off
		//tag::defaultGroupSequence[]
		ConstraintMapping constraintMapping = configuration.createConstraintMapping();

		constraintMapping
			.type( Car.class )
				.defaultGroupSequence( Car.class, CarChecks.class )
			.type( RentalCar.class )
				.defaultGroupSequenceProviderClass( RentalCarGroupSequenceProvider.class );
		//end::defaultGroupSequence[]
		// @formatter:on
	}

	@Test
	public void constraintDefinition() {
		HibernateValidatorConfiguration configuration = Validation
				.byProvider( HibernateValidator.class )
				.configure();

		// @formatter:off
		//tag::constraintDefinition[]
		ConstraintMapping constraintMapping = configuration.createConstraintMapping();

		constraintMapping
				.constraintDefinition( ValidPassengerCount.class )
				.validatedBy( ValidPassengerCountValidator.class );
		//end::constraintDefinition[]
		// @formatter:on

		configuration.addMapping( constraintMapping );
	}

	@Test
	public void constraintDefinitionUsingLambda() {
		HibernateValidatorConfiguration configuration = Validation
				.byProvider( HibernateValidator.class )
				.configure();

		// @formatter:off
		//tag::constraintDefinitionUsingLambda[]
		ConstraintMapping constraintMapping = configuration.createConstraintMapping();

		constraintMapping
				.constraintDefinition( ValidPassengerCount.class )
					.validateType( Bus.class )
						.with( b -> b.getSeatCount() >= b.getPassengers().size() );
		//end::constraintDefinitionUsingLambda[]
		// @formatter:on

		configuration.addMapping( constraintMapping );
	}

	@Test
	public void urlValidationOverride() {
		HibernateValidatorConfiguration configuration = Validation
				.byProvider( HibernateValidator.class )
				.configure();

		// @formatter:off
		//tag::urlValidationOverride[]
		ConstraintMapping constraintMapping = configuration.createConstraintMapping();

		constraintMapping
				.constraintDefinition( URL.class )
				.includeExistingValidators( false )
				.validatedBy( RegexpURLValidator.class );
		//end::urlValidationOverride[]
		// @formatter:on

		configuration.addMapping( constraintMapping );
	}

	public interface PersonDefault {
	}
}
