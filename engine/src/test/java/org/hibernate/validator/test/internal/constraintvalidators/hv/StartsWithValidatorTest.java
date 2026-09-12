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

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.StartsWithDef;
import org.hibernate.validator.constraints.StartsWith;
import org.hibernate.validator.internal.constraintvalidators.hv.StartsWithValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the {@link StartsWithValidator} constraint validator.
 *
 * @author Koen Aers
 */
@TestForIssue(jiraKey = "HV-2245")
public class StartsWithValidatorTest {

	private ConstraintAnnotationDescriptor.Builder<StartsWith> descriptorBuilder;

	@BeforeMethod
	public void setUp() throws Exception {
		descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( StartsWith.class );
	}

	@Test
	public void testNullIsValid() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo" } );
		StartsWithValidator validator = createValidator();
		assertTrue( validator.isValid( null, null ) );
	}

	@Test
	public void testMatchingPrefixIsValid() {
		descriptorBuilder.setAttribute( "value", new String[] { "fu" } );
		StartsWithValidator validator = createValidator();
		assertTrue( validator.isValid( "fubar", null ) );
	}

	@Test
	public void testNonMatchingPrefixIsInvalid() {
		descriptorBuilder.setAttribute( "value", new String[] { "fu" } );
		StartsWithValidator validator = createValidator();
		assertFalse( validator.isValid( "foobar", null ) );
	}

	@Test
	public void testEmptyPrefixAlwaysMatches() {
		descriptorBuilder.setAttribute( "value", new String[] { "" } );
		StartsWithValidator validator = createValidator();
		assertTrue( validator.isValid( "foobar", null ) );
		assertTrue( validator.isValid( "", null ) );
	}

	@Test
	public void testCaseSensitiveByDefault() {
		descriptorBuilder.setAttribute( "value", new String[] { "FU" } );
		StartsWithValidator validator = createValidator();
		assertFalse( validator.isValid( "fubar", null ) );
	}

	@Test
	public void testIgnoreCaseMatches() {
		descriptorBuilder.setAttribute( "value", new String[] { "FU" } );
		descriptorBuilder.setAttribute( "ignoreCase", true );
		StartsWithValidator validator = createValidator();
		assertTrue( validator.isValid( "fubar", null ) );
	}

	@Test
	public void testMultipleValuesOrSemantics() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo", "fu" } );
		StartsWithValidator validator = createValidator();
		assertTrue( validator.isValid( "foobar", null ) );
		assertTrue( validator.isValid( "fubar", null ) );
		assertFalse( validator.isValid( "barfoo", null ) );
	}

	@Test
	public void testMultipleValuesNoneMatches() {
		descriptorBuilder.setAttribute( "value", new String[] { "foo", "bar" } );
		StartsWithValidator validator = createValidator();
		assertFalse( validator.isValid( "baz", null ) );
	}

	@Test
	public void testProgrammaticDefinition() throws Exception {
		HibernateValidatorConfiguration config = ValidatorUtil.getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Foo.class )
				.field( "string" )
				.constraint( new StartsWithDef().value( "foo" ) );
		config.addMapping( mapping );
		Validator programmaticValidator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<Foo>> violations = programmaticValidator.validate( new Foo( "foobar" ) );
		assertNoViolations( violations );

		violations = programmaticValidator.validate( new Foo( null ) );
		assertNoViolations( violations );

		violations = programmaticValidator.validate( new Foo( "barfoo" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( StartsWith.class )
		);
	}

	private StartsWithValidator createValidator() {
		StartsWith annotation = descriptorBuilder.build().getAnnotation();
		StartsWithValidator validator = new StartsWithValidator();
		validator.initialize( annotation );
		return validator;
	}

	private static class Foo {

		private final String string;

		public Foo(String string) {
			this.string = string;
		}
	}
}
