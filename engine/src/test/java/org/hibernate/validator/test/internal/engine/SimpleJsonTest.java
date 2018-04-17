/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import javax.json.Json;
import javax.json.JsonObject;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.defs.LengthDef;
import org.hibernate.validator.cfg.defs.MinDef;
import org.hibernate.validator.cfg.defs.NotBlankDef;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.cfg.json.JsonConstraintMapping;
import org.hibernate.validator.internal.engine.ValidatorImpl;

import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class SimpleJsonTest {

	@Test
	public void testJsonValidation() {
		HibernateValidatorConfiguration configuration = Validation.byProvider( HibernateValidator.class )
				.configure();
		JsonConstraintMapping mapping = configuration.createJsonConstraintMapping();
		mapping.type( User.class )
				// uses string as a type by default
				.property( "name" )
				.constraint( new NotNullDef() ).constraint( new LengthDef().min( 3 ) )
				// if it's not a string we need to pass a type with the name for it
				.property( "age", Integer.class )
				.constraint( new MinDef().value( 18 ) )
				// in case of complex type (inner json) we can pass a type and use valid() on it
				.property( "address", Address.class )
				.valid();
		// and define constraint for the child json as for another type
		mapping.type( Address.class )
				.property( "street" )
				.constraint( new NotBlankDef() )
				.property( "building_number", Long.class )
				.constraint( new NotNullDef() ).constraint( new MinDef().value( 1 ) );

		configuration.addJsonMapping( mapping );

		//create json:

		JsonObject jsonObject = Json.createObjectBuilder()
				.add( "name", "name" )
				.add( "age", 10 )
				.add( "email", "not-an-email-string-but-is-not-validated" )
				.add( "address", Json.createObjectBuilder()
						.add( "street", "" )
						.add( "building_number", -10L )
				)
				.build();

		Validator validator = configuration.buildValidatorFactory().getValidator();
		Set<ConstraintViolation<JsonObject>> violations = ( (ValidatorImpl) validator )
				.forJson()
				.validateJson( jsonObject, User.class );

		assertThat( violations ).containsOnlyViolations(
				violationOf( Min.class ).withProperty( "age" ),
				violationOf( Min.class ).withPropertyPath(
						pathWith()
								.property( "address" )
								.property( "building_number" )
				),
				violationOf( NotBlank.class ).withPropertyPath(
						pathWith()
								.property( "address" )
								.property( "street" )
				)
		);
	}

	private class User {

	}

	private class Address {

	}
}
