/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import org.hibernate.validator.constraints.CodePointLength;
import org.hibernate.validator.internal.constraintvalidators.hv.CodePointLengthValidator;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.hibernate.validator.testutil.MyCustomStringImpl;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 * Tests the {@code CodePointLengthConstraint}.
 *
 * @author Kazuki Shimizu
 */
public class CodePointLengthValidatorTest {

	@Test
	public void testIsValid() {
		AnnotationDescriptor<CodePointLength> descriptor = new AnnotationDescriptor<CodePointLength>( CodePointLength.class );
		descriptor.setValue( "min", 1 );
		descriptor.setValue( "max", 3 );
		descriptor.setValue( "message", "{validator.codePointLength}" );
		CodePointLength l = AnnotationFactory.create( descriptor );
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
		AnnotationDescriptor<CodePointLength> descriptor = new AnnotationDescriptor<CodePointLength>( CodePointLength.class );
		descriptor.setValue( "min", 1 );
		descriptor.setValue( "max", 3 );
		CodePointLength l = AnnotationFactory.create( descriptor );
		CodePointLengthValidator constraint = new CodePointLengthValidator();
		constraint.initialize( l );
		assertTrue( constraint.isValid( new MyCustomStringImpl( "foo" ), null ) );
		assertFalse( constraint.isValid( new MyCustomStringImpl( "foobar" ), null ) );
		assertTrue( constraint.isValid( new MyCustomStringImpl( "\uD842\uDFB7ab" ), null ) ); // \uD842\uDFB7 = 𠮷 (Surrogate Pair char)
		assertFalse( constraint.isValid( new MyCustomStringImpl( "\uD842\uDFB7abc" ), null ) );
	}

	@Test
	public void testIsValidNormalizationStrategyIsNONE() {
		{
			AnnotationDescriptor<CodePointLength> descriptor = new AnnotationDescriptor<CodePointLength>( CodePointLength.class );
			descriptor.setValue( "min", 3 );
			descriptor.setValue( "max", 3 );
			descriptor.setValue( "normalizationStrategy", CodePointLength.NormalizationStrategy.NONE );
			CodePointLength l = AnnotationFactory.create( descriptor );
			CodePointLengthValidator constraint = new CodePointLengthValidator();
			constraint.initialize( l );
			assertTrue( constraint.isValid( "があ", null ) );
		}
		{
			AnnotationDescriptor<CodePointLength> descriptor = new AnnotationDescriptor<CodePointLength>( CodePointLength.class );
			descriptor.setValue( "min", 2 );
			descriptor.setValue( "max", 2 );
			CodePointLength l = AnnotationFactory.create( descriptor );
			CodePointLengthValidator constraint = new CodePointLengthValidator();
			constraint.initialize( l );
			assertTrue( constraint.isValid( "があ", null ) );
		}
	}

	@Test
	public void testIsValidNormalizationStrategyIsNfc() {
		AnnotationDescriptor<CodePointLength> descriptor = new AnnotationDescriptor<CodePointLength>( CodePointLength.class );
		descriptor.setValue( "min", 3 );
		descriptor.setValue( "max", 3 );
		descriptor.setValue( "normalizationStrategy", CodePointLength.NormalizationStrategy.NFC );
		CodePointLength l = AnnotationFactory.create( descriptor );
		CodePointLengthValidator constraint = new CodePointLengthValidator();
		constraint.initialize( l );
		assertTrue( constraint.isValid( "ががあ", null ) );
	}

	@Test
	public void testIsValidNormalizationStrategyIsNfkc() {
		AnnotationDescriptor<CodePointLength> descriptor = new AnnotationDescriptor<CodePointLength>( CodePointLength.class );
		descriptor.setValue( "min", 1 );
		descriptor.setValue( "max", 1 );
		descriptor.setValue( "normalizationStrategy", CodePointLength.NormalizationStrategy.NFKC );
		CodePointLength l = AnnotationFactory.create( descriptor );
		CodePointLengthValidator constraint = new CodePointLengthValidator();
		constraint.initialize( l );
		assertTrue( constraint.isValid( "\u3131\u314F\u3133", null ) );
	}

	@Test
	public void testIsValidNormalizationStrategyIsNfd() {
		AnnotationDescriptor<CodePointLength> descriptor = new AnnotationDescriptor<CodePointLength>( CodePointLength.class );
		descriptor.setValue( "min", 5 );
		descriptor.setValue( "max", 5 );
		descriptor.setValue( "normalizationStrategy", CodePointLength.NormalizationStrategy.NFD );
		CodePointLength l = AnnotationFactory.create( descriptor );
		CodePointLengthValidator constraint = new CodePointLengthValidator();
		constraint.initialize( l );
		assertTrue( constraint.isValid( "ががあ", null ) );
	}

	@Test
	public void testIsValidNormalizationStrategyIsNfkd() {
		AnnotationDescriptor<CodePointLength> descriptor = new AnnotationDescriptor<CodePointLength>( CodePointLength.class );
		descriptor.setValue( "min", 4 );
		descriptor.setValue( "max", 4 );
		descriptor.setValue( "normalizationStrategy", CodePointLength.NormalizationStrategy.NFKD );
		CodePointLength l = AnnotationFactory.create( descriptor );
		CodePointLengthValidator constraint = new CodePointLengthValidator();
		constraint.initialize( l );
		assertTrue( constraint.isValid( "1㌦", null ) );
	}

	@Test
	public void testNormalizationStrategyValueIsNullOrEmpty() {
		assertNull( CodePointLength.NormalizationStrategy.NFC.normalize( null ) );
		String emptyValue = new String();
		assertSame( emptyValue, CodePointLength.NormalizationStrategy.NFC.normalize( emptyValue ) );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNegativeMinValue() {
		AnnotationDescriptor<CodePointLength> descriptor = new AnnotationDescriptor<CodePointLength>( CodePointLength.class );
		descriptor.setValue( "min", -1 );
		descriptor.setValue( "max", 1 );
		descriptor.setValue( "message", "{validator.codePointLength}" );
		CodePointLength l = AnnotationFactory.create( descriptor );

		CodePointLengthValidator constraint = new CodePointLengthValidator();
		constraint.initialize( l );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNegativeMaxValue() {
		AnnotationDescriptor<CodePointLength> descriptor = new AnnotationDescriptor<CodePointLength>( CodePointLength.class );
		descriptor.setValue( "min", 1 );
		descriptor.setValue( "max", -1 );
		descriptor.setValue( "message", "{validator.codePointLength}" );
		CodePointLength l = AnnotationFactory.create( descriptor );

		CodePointLengthValidator constraint = new CodePointLengthValidator();
		constraint.initialize( l );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNegativeLength() {
		AnnotationDescriptor<CodePointLength> descriptor = new AnnotationDescriptor<CodePointLength>( CodePointLength.class );
		descriptor.setValue( "min", 5 );
		descriptor.setValue( "max", 4 );
		descriptor.setValue( "message", "{validator.codePointLength}" );
		CodePointLength l = AnnotationFactory.create( descriptor );

		CodePointLengthValidator constraint = new CodePointLengthValidator();
		constraint.initialize( l );
	}
}
