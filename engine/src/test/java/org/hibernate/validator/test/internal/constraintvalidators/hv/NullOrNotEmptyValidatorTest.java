/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Stream;

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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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

	@ParameterizedTest
	@MethodSource("notEmptyCharSequenceIsValidData")
	public void notEmptyCharSequenceIsValid(String value) {
		assertTrue( charSequenceValidator.isValid( value, null ) );
	}

	private static Stream<Arguments> notEmptyCharSequenceIsValidData() {
		return Stream.of(
				Arguments.of( "a" ),
				Arguments.of( "foobar" ),
				Arguments.of( " " )
		);
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
