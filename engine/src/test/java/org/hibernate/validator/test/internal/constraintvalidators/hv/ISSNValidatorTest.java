/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.hibernate.validator.constraints.ISSN;
import org.hibernate.validator.internal.constraintvalidators.hv.ISSNValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * Tests for {@link ISSNValidator}.
 *
 * @author Andrea Boriero
 */
@TestForIssue(jiraKey = "HV-2252")
public class ISSNValidatorTest {

	@Test
	public void testNullValue() {
		ISSNValidator validator = initializeValidator( ISSN.Type.ISSN_8 );
		assertTrue( validator.isValid( null, null ) );
	}

	@Test
	public void testValidISSN8() {
		ISSNValidator validator = initializeValidator( ISSN.Type.ISSN_8 );

		// Valid ISSN-8 samples
		assertTrue( validator.isValid( "03178471", null ), "Valid ISSN-8 without separator" );
		assertTrue( validator.isValid( "0317-8471", null ), "Valid ISSN-8 with hyphen" );
		assertTrue( validator.isValid( "1050-124X", null ), "Valid ISSN-8 with X check digit" );
		assertTrue( validator.isValid( "0259-000X", null ), "Valid ISSN-8 with X check digit" );
		assertTrue( validator.isValid( "2049-3630", null ), "Valid ISSN-8" );

		// With spaces
		assertTrue( validator.isValid( "0317 8471", null ), "Valid ISSN-8 with space" );

		// With various separators (all should be stripped)
		assertTrue( validator.isValid( "0317.8471", null ), "Valid ISSN-8 with dot separator" );
	}

	@Test
	public void testInvalidISSN8() {
		ISSNValidator validator = initializeValidator( ISSN.Type.ISSN_8 );

		// Invalid check digits
		assertFalse( validator.isValid( "0317-8470", null ), "Invalid check digit" );
		assertFalse( validator.isValid( "1050-1240", null ), "Invalid check digit (should be X)" );

		// Wrong length
		assertFalse( validator.isValid( "0317-847", null ), "Too short" );
		assertFalse( validator.isValid( "0317-84711", null ), "Too long" );
		assertFalse( validator.isValid( "031784", null ), "Too short without separator" );

		// Non-numeric characters (except X)
		assertFalse( validator.isValid( "0317-847A", null ), "Invalid character A" );
		assertFalse( validator.isValid( "X317-8471", null ), "X in wrong position" );

		// Empty/blank
		assertFalse( validator.isValid( "", null ), "Empty string" );
		assertFalse( validator.isValid( "        ", null ), "Blank string" );
	}

	@Test
	public void testValidISSN13() {
		ISSNValidator validator = initializeValidator( ISSN.Type.ISSN_13 );

		// Valid ISSN-13 (EAN-13 format with 977 prefix)
		// Using manually calculated valid EAN-13 barcodes with 977 prefix
		assertTrue( validator.isValid( "9770378595002", null ), "Valid ISSN-13" );
		assertTrue( validator.isValid( "9771234567003", null ), "Valid ISSN-13" );
		assertTrue( validator.isValid( "9770000000003", null ), "Valid ISSN-13" );

		// With separators (should be stripped)
		assertTrue( validator.isValid( "977-0378595-00-2", null ), "Valid ISSN-13 with hyphens" );
		assertTrue( validator.isValid( "977 1234567 00 3", null ), "Valid ISSN-13 with spaces" );
	}

	@Test
	public void testInvalidISSN13() {
		ISSNValidator validator = initializeValidator( ISSN.Type.ISSN_13 );

		// Invalid check digit (should be 2, not 3)
		assertFalse( validator.isValid( "9770378595003", null ), "Invalid check digit" );
		assertFalse( validator.isValid( "9771234567002", null ), "Invalid check digit" );

		// Wrong length
		assertFalse( validator.isValid( "977037859500", null ), "Too short" );
		assertFalse( validator.isValid( "97703785950022", null ), "Too long" );

		// Wrong prefix (not 977)
		assertFalse( validator.isValid( "9780378595002", null ), "Wrong prefix (978 is for ISBN)" );
		assertFalse( validator.isValid( "1230378595002", null ), "Wrong prefix" );

		// Contains 'X' (not valid in ISSN-13)
		assertFalse( validator.isValid( "977037859500X", null ), "X not valid in ISSN-13" );
	}

	@Test
	public void testAnyType() {
		ISSNValidator validator = initializeValidator( ISSN.Type.ANY );

		// Valid ISSN-8
		assertTrue( validator.isValid( "0317-8471", null ), "Valid ISSN-8" );
		assertTrue( validator.isValid( "1050-124X", null ), "Valid ISSN-8 with X" );

		// Valid ISSN-13
		assertTrue( validator.isValid( "9770378595002", null ), "Valid ISSN-13" );
		assertTrue( validator.isValid( "9771234567003", null ), "Valid ISSN-13" );

		// Invalid for both
		assertFalse( validator.isValid( "0317-8470", null ), "Invalid ISSN-8" );
		assertFalse( validator.isValid( "9770378595003", null ), "Invalid ISSN-13 (wrong check digit)" );

		// Wrong length (neither 8 nor 13)
		assertFalse( validator.isValid( "03178471234", null ), "Wrong length (11 digits)" );
	}

	@Test
	public void testDefaultTypeIsISSN13() {
		// Default should be ISSN_13
		ISSN annotation = new ConstraintAnnotationDescriptor.Builder<>( ISSN.class ).build().getAnnotation();
		ISSNValidator validator = new ISSNValidator();
		validator.initialize( annotation );

		// Should accept valid ISSN-13
		assertTrue( validator.isValid( "9770378595002", null ) );

		// Should reject valid ISSN-8 (since default is ISSN_13)
		assertFalse( validator.isValid( "0317-8471", null ) );
	}

	@Test
	public void testCheckDigitCalculationISSN8() {
		ISSNValidator validator = initializeValidator( ISSN.Type.ISSN_8 );

		// Test specific check digit calculations
		// ISSN 0378-5955: (0×8 + 3×7 + 7×6 + 8×5 + 5×4 + 9×3 + 5×2 + 5×1) mod 11 = 0
		assertTrue( validator.isValid( "0378-5955", null ) );

		// ISSN 0024-9319: check digit 9
		assertTrue( validator.isValid( "0024-9319", null ) );

		// ISSN ending in X (check digit 10)
		assertTrue( validator.isValid( "2434-561X", null ) );
	}

	private ISSNValidator initializeValidator(ISSN.Type type) {
		ConstraintAnnotationDescriptor.Builder<ISSN> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( ISSN.class );
		descriptorBuilder.setAttribute( "type", type );
		ISSN annotation = descriptorBuilder.build().getAnnotation();

		ISSNValidator validator = new ISSNValidator();
		validator.initialize( annotation );
		return validator;
	}
}
