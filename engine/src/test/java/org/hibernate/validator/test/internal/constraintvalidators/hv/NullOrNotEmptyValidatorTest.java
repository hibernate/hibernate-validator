/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.NullOrNotEmptyDef;
import org.hibernate.validator.constraints.NullOrNotEmpty;
import org.hibernate.validator.internal.constraintvalidators.hv.NullOrNotEmptyValidatorForArray;
import org.hibernate.validator.internal.constraintvalidators.hv.NullOrNotEmptyValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.hv.NullOrNotEmptyValidatorForCollection;
import org.hibernate.validator.internal.constraintvalidators.hv.NullOrNotEmptyValidatorForMap;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

/**
 * Tests the {@link NullOrNotEmpty} constraint validators.
 */
public class NullOrNotEmptyValidatorTest {

	private final NullOrNotEmptyValidatorForCharSequence charSequenceValidator = new NullOrNotEmptyValidatorForCharSequence();
	private final NullOrNotEmptyValidatorForCollection collectionValidator = new NullOrNotEmptyValidatorForCollection();
	private final NullOrNotEmptyValidatorForMap mapValidator = new NullOrNotEmptyValidatorForMap();
	private final NullOrNotEmptyValidatorForArray arrayValidator = new NullOrNotEmptyValidatorForArray();

	@Test
	public void nullCharSequenceIsValid() {
		assertTrue( charSequenceValidator.isValid( null, null ) );
	}

	@Test
	public void notEmptyCharSequenceIsValid() {
		assertTrue( charSequenceValidator.isValid( "a", null ) );
		assertTrue( charSequenceValidator.isValid( "foobar", null ) );
		assertTrue( charSequenceValidator.isValid( " ", null ) );
	}

	@Test
	public void emptyCharSequenceIsInvalid() {
		assertFalse( charSequenceValidator.isValid( "", null ) );
	}

	@Test
	public void nullCollectionIsValid() {
		assertTrue( collectionValidator.isValid( null, null ) );
	}

	@Test
	public void notEmptyCollectionIsValid() {
		assertTrue( collectionValidator.isValid( Collections.singletonList( "a" ), null ) );
	}

	@Test
	public void emptyCollectionIsInvalid() {
		assertFalse( collectionValidator.isValid( Collections.emptyList(), null ) );
	}

	@Test
	public void nullMapIsValid() {
		assertTrue( mapValidator.isValid( null, null ) );
	}

	@Test
	public void notEmptyMapIsValid() {
		assertTrue( mapValidator.isValid( Collections.singletonMap( "key", "value" ), null ) );
	}

	@Test
	public void emptyMapIsInvalid() {
		assertFalse( mapValidator.isValid( new HashMap<>(), null ) );
	}

	@Test
	public void nullArrayIsValid() {
		assertTrue( arrayValidator.isValid( null, null ) );
	}

	@Test
	public void notEmptyArrayIsValid() {
		assertTrue( arrayValidator.isValid( new Object[] { "a" }, null ) );
	}

	@Test
	public void emptyArrayIsInvalid() {
		assertFalse( arrayValidator.isValid( new Object[] { }, null ) );
	}

	@Test
	public void testProgrammaticDefinition() throws Exception {
		HibernateValidatorConfiguration config = ValidatorUtil.getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Foo.class )
				.field( "string" )
				.constraint( new NullOrNotEmptyDef() );
		config.addMapping( mapping );
		Validator programmaticValidator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<Foo>> violations = programmaticValidator.validate( new Foo( "foobar" ) );
		assertNoViolations( violations );

		violations = programmaticValidator.validate( new Foo( null ) );
		assertNoViolations( violations );

		violations = programmaticValidator.validate( new Foo( "" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NullOrNotEmpty.class )
		);
	}

	private static class Foo {

		private final String string;

		public Foo(String string) {
			this.string = string;
		}
	}
}
