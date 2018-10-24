/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.propertyholder;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.defs.EmailDef;
import org.hibernate.validator.cfg.defs.MinDef;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.cfg.defs.SizeDef;
import org.hibernate.validator.cfg.propertyholder.PropertyHolderConstraintMapping;
import org.hibernate.validator.internal.engine.ValidatorImpl;

import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class ValidatorTest {
	@Test(enabled = false)
	public void testSimplePropertyHolder() {
		ValidatorImpl validator = (ValidatorImpl) getValidator();

		Map<String, Object> address = new HashMap<>();
		address.put( "street", "str" );
		address.put( "buildingNumber", -1L );

		Map<String, Object> user = new HashMap<>();
		user.put( "name", "jhon" );
		user.put( "email", "not a mail" );
		user.put( "address", address );

		Set<ConstraintViolation<Map>> constraintViolations = validator.validatePropertyHolder( user, "user" );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class ).withProperty( "name" ),
				violationOf( Email.class ).withProperty( "email" ),
				violationOf( Min.class )
						.withPropertyPath( pathWith()
								.property( "address" )
								.property( "buildingNumber" )
						),
				violationOf( Size.class )
						.withPropertyPath( pathWith()
								.property( "address" )
								.property( "street" )
						),
				violationOf( NotNull.class ).withProperty( "secondaryAddresses" )
		);
	}

	@Test(enabled = false)
	public void testPropertyHolderContainerElements() {
		ValidatorImpl validator = (ValidatorImpl) getValidator();

		Map<String, Object> address = new HashMap<>();
		address.put( "street", "street" );
		address.put( "buildingNumber", 10L );

		Map<String, Object> invaildAddress = new HashMap<>();
		invaildAddress.put( "street", "str" );
		invaildAddress.put( "buildingNumber", -1L );

		Map<String, Object> user = new HashMap<>();
		user.put( "name", "jhon doe" );
		user.put( "email", "kinda@valid.mail" );
		user.put( "address", address );
		user.put( "secondaryAddresses", Collections.singletonList( invaildAddress ) );

		Set<ConstraintViolation<Map>> constraintViolations = validator.validatePropertyHolder( user, "user" );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Min.class )
						.withPropertyPath( pathWith()
								.property( "secondaryAddresses" )
								.property( "buildingNumber", true, null, 0, List.class, 0 )
						),
				violationOf( Size.class )
						.withPropertyPath( pathWith()
								.property( "secondaryAddresses" )
								.property( "street", true, null, 0, List.class, 0 )
						)
		);
	}

	@Test
	public void testProgrammaticMapping() {
		HibernateValidatorConfiguration configuration = Validation.byProvider( HibernateValidator.class )
				.configure();
		PropertyHolderConstraintMapping mapping = configuration.createPropertyHolderConstraintMapping();

		mapping.type( "user" )
				.property( "name", String.class )
					.constraint( new NotNullDef() )
					.constraint( new SizeDef().min( 5 ) )

				.property( "email", String.class )
					.constraint( new NotNullDef() )
					.constraint( new EmailDef() )

				.propertyHolder( "address" )
					.valid( "address" )

				.property( "secondaryAddresses", List.class )
					.constraint( new SizeDef().max( 2 ) )
					.constraint( new NotNullDef() )
//					.containerElementType()
//					.valid( "addess" )
		;
		mapping.type( "address" )
				.property( "street", String.class )
					.constraint( new NotNullDef() )
					.constraint( new SizeDef().min( 5 ).max( 10 ) )
				.property( "buildingNumber", Long.class )
					.constraint( new NotNullDef() )
					.constraint( new MinDef().value( 0L ) );

		configuration.addPropertyHolderMapping( mapping );

		ValidatorImpl validator = (ValidatorImpl) configuration.buildValidatorFactory().getValidator();

		Map<String, Object> address = new HashMap<>();
		address.put( "street", "str" );
		address.put( "buildingNumber", -1L );

		Map<String, Object> user = new HashMap<>();
		user.put( "name", "jhon" );
		user.put( "email", "not a mail" );
		user.put( "address", address );

		Set<ConstraintViolation<Map>> constraintViolations = validator.validatePropertyHolder( user, "user" );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class ).withProperty( "name" ),
				violationOf( Email.class ).withProperty( "email" ),
				violationOf( Min.class )
						.withPropertyPath( pathWith()
								.property( "address" )
								.property( "buildingNumber" )
						),
				violationOf( Size.class )
						.withPropertyPath( pathWith()
								.property( "address" )
								.property( "street" )
						),
				violationOf( NotNull.class ).withProperty( "secondaryAddresses" )
		);

	}
}
