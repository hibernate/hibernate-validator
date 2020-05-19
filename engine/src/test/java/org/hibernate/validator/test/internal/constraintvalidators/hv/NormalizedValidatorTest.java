/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.hibernate.validator.constraints.Normalized;
import org.hibernate.validator.internal.constraintvalidators.hv.NormalizedValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.MyCustomStringImpl;

import java.text.Normalizer;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the {@link Normalized} constraint.
 *
 * @author Kazuki Shimizu
 */
public class NormalizedValidatorTest {

	private ConstraintAnnotationDescriptor.Builder<Normalized> descriptorBuilder;

	@BeforeMethod
	public void setUp() throws Exception {
		descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Normalized.class );
	}

	@Test
	public void testIsValid() {
		descriptorBuilder.setMessage( "{validator.Normalized}" );
		Normalized l = descriptorBuilder.build().getAnnotation();
		NormalizedValidator constraint = new NormalizedValidator();
		constraint.initialize( l );
		assertTrue( constraint.isValid( null, null ) );
		assertTrue( constraint.isValid( "", null ) );
		assertTrue( constraint.isValid( "foobar", null ) );
		assertTrue( constraint.isValid( "\uFE64script\uFE65", null ) );
	}

	@Test
	public void testIsValidCharSequence() {
		Normalized l = descriptorBuilder.build().getAnnotation();
		NormalizedValidator constraint = new NormalizedValidator();
		constraint.initialize( l );
		assertTrue( constraint.isValid( new MyCustomStringImpl( "foobar" ), null ) );
		assertTrue( constraint.isValid( new MyCustomStringImpl( "\uFE64script\uFE65" ), null ) );
	}

	@Test
	public void testIsValidNormalizationStrategyIsNfc() {
		descriptorBuilder.setAttribute( "form", Normalizer.Form.NFC );
		Normalized l = descriptorBuilder.build().getAnnotation();
		NormalizedValidator constraint = new NormalizedValidator();
		constraint.initialize( l );
		assertTrue( constraint.isValid( "foobar", null ) );
		assertTrue( constraint.isValid( "\uFE64script\uFE65", null ) );
	}

	@Test
	public void testIsValidNormalizationStrategyIsNfkc() {
		descriptorBuilder.setAttribute( "form", Normalizer.Form.NFKC );
		Normalized l = descriptorBuilder.build().getAnnotation();
		NormalizedValidator constraint = new NormalizedValidator();
		constraint.initialize( l );
		assertTrue( constraint.isValid( "foobar", null ) );
		assertFalse( constraint.isValid( "\uFE64script\uFE65", null ) );
	}

	@Test
	public void testIsValidNormalizationStrategyIsNfd() {
		descriptorBuilder.setAttribute( "form", Normalizer.Form.NFD );
		Normalized l = descriptorBuilder.build().getAnnotation();
		NormalizedValidator constraint = new NormalizedValidator();
		constraint.initialize( l );
		assertTrue( constraint.isValid( "foobar", null ) );
		assertTrue( constraint.isValid( "\uFE64script\uFE65", null ) );
	}

	@Test
	public void testIsValidNormalizationStrategyIsNfkd() {
		descriptorBuilder.setAttribute( "form", Normalizer.Form.NFKD );
		Normalized l = descriptorBuilder.build().getAnnotation();
		NormalizedValidator constraint = new NormalizedValidator();
		constraint.initialize( l );
		assertTrue( constraint.isValid( "foobar", null ) );
		assertFalse( constraint.isValid( "\uFE64script\uFE65", null ) );
	}
}
