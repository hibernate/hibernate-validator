/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hibernate.validator.constraints.CodePointLength;
import org.hibernate.validator.internal.constraintvalidators.hv.CodePointLengthValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.MyCustomStringImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link CodePointLength} constraint.
 *
 * @author Kazuki Shimizu
 */
public class CodePointLengthValidatorTest {

	private ConstraintAnnotationDescriptor.Builder<CodePointLength> descriptorBuilder;

	@BeforeEach
	public void setUp() throws Exception {
		descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( CodePointLength.class );
	}

	@Test
	public void testIsValid() {
		descriptorBuilder.setAttribute( "min", 1 );
		descriptorBuilder.setAttribute( "max", 3 );
		descriptorBuilder.setMessage( "{validator.codePointLength}" );
		CodePointLength l = descriptorBuilder.build().getAnnotation();
		CodePointLengthValidator constraint = new CodePointLengthValidator();
		constraint.initialize( l );
		assertTrue( constraint.isValid( null, null ) );
		assertFalse( constraint.isValid( "", null ) );
		assertTrue( constraint.isValid( "f", null ) );
		assertTrue( constraint.isValid( "fo", null ) );
		assertTrue( constraint.isValid( "foo", null ) );
		assertFalse( constraint.isValid( "foobar", null ) );
		assertTrue( constraint.isValid( "\uD842\uDFB7ab", null ) ); // \uD842\uDFB7 = 𠮷 (Surrogate Pair char)
		assertFalse( constraint.isValid( "\uD842\uDFB7abc", null ) );
	}

	@Test
	public void testIsValidCharSequence() {
		descriptorBuilder.setAttribute( "min", 1 );
		descriptorBuilder.setAttribute( "max", 3 );
		CodePointLength l = descriptorBuilder.build().getAnnotation();
		CodePointLengthValidator constraint = new CodePointLengthValidator();
		constraint.initialize( l );
		assertTrue( constraint.isValid( new MyCustomStringImpl( "foo" ), null ) );
		assertFalse( constraint.isValid( new MyCustomStringImpl( "foobar" ), null ) );
		assertTrue( constraint.isValid( new MyCustomStringImpl( "\uD842\uDFB7ab" ), null ) ); // \uD842\uDFB7 = 𠮷 (Surrogate Pair char)
		assertFalse( constraint.isValid( new MyCustomStringImpl( "\uD842\uDFB7abc" ), null ) );
	}

	@Test
	public void testIsValidNormalizationStrategyIsNONESpecified() {
		descriptorBuilder.setAttribute( "min", 3 );
		descriptorBuilder.setAttribute( "max", 3 );
		descriptorBuilder.setAttribute( "normalizationStrategy", CodePointLength.NormalizationStrategy.NONE );
		CodePointLength l = descriptorBuilder.build().getAnnotation();
		CodePointLengthValidator constraint = new CodePointLengthValidator();
		constraint.initialize( l );
		assertTrue( constraint.isValid( "があ", null ) );
	}

	@Test
	public void testIsValidNormalizationStrategyIsNONENotSpecified() {
		descriptorBuilder.setAttribute( "min", 2 );
		descriptorBuilder.setAttribute( "max", 2 );
		CodePointLength l = descriptorBuilder.build().getAnnotation();
		CodePointLengthValidator constraint = new CodePointLengthValidator();
		constraint.initialize( l );
		assertTrue( constraint.isValid( "があ", null ) );
	}

	@Test
	public void testIsValidNormalizationStrategyIsNfc() {
		descriptorBuilder.setAttribute( "min", 3 );
		descriptorBuilder.setAttribute( "max", 3 );
		descriptorBuilder.setAttribute( "normalizationStrategy", CodePointLength.NormalizationStrategy.NFC );
		CodePointLength l = descriptorBuilder.build().getAnnotation();
		CodePointLengthValidator constraint = new CodePointLengthValidator();
		constraint.initialize( l );
		assertTrue( constraint.isValid( "ががあ", null ) );
	}

	@Test
	public void testIsValidNormalizationStrategyIsNfkc() {
		descriptorBuilder.setAttribute( "min", 1 );
		descriptorBuilder.setAttribute( "max", 1 );
		descriptorBuilder.setAttribute( "normalizationStrategy", CodePointLength.NormalizationStrategy.NFKC );
		CodePointLength l = descriptorBuilder.build().getAnnotation();
		CodePointLengthValidator constraint = new CodePointLengthValidator();
		constraint.initialize( l );
		assertTrue( constraint.isValid( "\u3131\u314F\u3133", null ) );
	}

	@Test
	public void testIsValidNormalizationStrategyIsNfd() {
		descriptorBuilder.setAttribute( "min", 5 );
		descriptorBuilder.setAttribute( "max", 5 );
		descriptorBuilder.setAttribute( "normalizationStrategy", CodePointLength.NormalizationStrategy.NFD );
		CodePointLength l = descriptorBuilder.build().getAnnotation();
		CodePointLengthValidator constraint = new CodePointLengthValidator();
		constraint.initialize( l );
		assertTrue( constraint.isValid( "ががあ", null ) );
	}

	@Test
	public void testIsValidNormalizationStrategyIsNfkd() {
		descriptorBuilder.setAttribute( "min", 4 );
		descriptorBuilder.setAttribute( "max", 4 );
		descriptorBuilder.setAttribute( "normalizationStrategy", CodePointLength.NormalizationStrategy.NFKD );
		CodePointLength l = descriptorBuilder.build().getAnnotation();
		CodePointLengthValidator constraint = new CodePointLengthValidator();
		constraint.initialize( l );
		assertTrue( constraint.isValid( "1㌦", null ) );
	}

	@Test
	public void testNormalizationStrategyValueIsNullOrEmpty() {
		assertNull( CodePointLength.NormalizationStrategy.NFC.normalize( null ) );
		String emptyValue = new String();
		assertSame( CodePointLength.NormalizationStrategy.NFC.normalize( emptyValue ), emptyValue );
	}

	@Test
	public void testNegativeMinValue() {
		descriptorBuilder.setAttribute( "min", -1 );
		descriptorBuilder.setAttribute( "max", 1 );
		descriptorBuilder.setMessage( "{validator.codePointLength}" );
		CodePointLength l = descriptorBuilder.build().getAnnotation();

		CodePointLengthValidator constraint = new CodePointLengthValidator();
		assertThatThrownBy( () -> constraint.initialize( l ) )
				.isInstanceOf( IllegalArgumentException.class );
	}

	@Test
	public void testNegativeMaxValue() {
		descriptorBuilder.setAttribute( "min", 1 );
		descriptorBuilder.setAttribute( "max", -1 );
		descriptorBuilder.setMessage( "{validator.codePointLength}" );
		CodePointLength l = descriptorBuilder.build().getAnnotation();

		CodePointLengthValidator constraint = new CodePointLengthValidator();
		assertThatThrownBy( () -> constraint.initialize( l ) )
				.isInstanceOf( IllegalArgumentException.class );
	}

	@Test
	public void testNegativeLength() {
		descriptorBuilder.setAttribute( "min", 5 );
		descriptorBuilder.setAttribute( "max", 4 );
		descriptorBuilder.setMessage( "{validator.codePointLength}" );
		CodePointLength l = descriptorBuilder.build().getAnnotation();

		CodePointLengthValidator constraint = new CodePointLengthValidator();
		assertThatThrownBy( () -> constraint.initialize( l ) )
				.isInstanceOf( IllegalArgumentException.class );
	}
}
