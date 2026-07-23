/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv.ru;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.hibernate.validator.constraints.ru.INN;
import org.hibernate.validator.internal.constraintvalidators.hv.ru.INNValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * A set of tests for {@link INN} constraint validator ({@link INNValidator}),
 * which make sure that validation is performed correctly.
 *
 * @author Artem Boiarshinov
 * @see <a href="http://mellarius.ru/random-inn">fake INN generator</a>
 */
public class INNValidatorTest {

	private INNValidator validator;

	@BeforeEach
	public void setUp() {
		validator = new INNValidator();
	}

	@ParameterizedTest
	@MethodSource("validIndividualTypeINNData")
	public void validIndividualTypeINN(String inn) {
		validator.initialize( initializeAnnotation( INN.Type.INDIVIDUAL ) );

		assertValidINN( inn );
	}

	private static Stream<Arguments> validIndividualTypeINNData() {
		return Stream.of(
				Arguments.of( (String) null ),
				Arguments.of( "246964567008" ),
				Arguments.of( "356393289962" ),
				Arguments.of( "279837166431" ),
				Arguments.of( "827175083460" ),
				Arguments.of( "789429596404" ),
				Arguments.of( "929603416330" ),
				Arguments.of( "086229647992" )
		);
	}

	@ParameterizedTest
	@MethodSource("invalidIndividualTypeINNData")
	public void invalidIndividualTypeINN(String inn) {
		validator.initialize( initializeAnnotation( INN.Type.INDIVIDUAL ) );

		assertInvalidINN( inn );
	}

	private static Stream<Arguments> invalidIndividualTypeINNData() {
		return Stream.of(
				//invalid checksum
				Arguments.of( "012345678912" ),
				Arguments.of( "246964567009" ),

				//invalid symbols
				Arguments.of( "a46964567008" ),

				//invalid length
				Arguments.of( "" ),
				Arguments.of( "90660563173" ),
				Arguments.of( "9066056317378" ),

				//invalid type
				Arguments.of( "4546366155" )
		);
	}

	@ParameterizedTest
	@MethodSource("validJuridicalTypeINNData")
	public void validJuridicalTypeINN(String inn) {
		validator.initialize( initializeAnnotation( INN.Type.JURIDICAL ) );

		assertValidINN( inn );
	}

	private static Stream<Arguments> validJuridicalTypeINNData() {
		return Stream.of(
				Arguments.of( (String) null ),
				Arguments.of( "0305773929" ),
				Arguments.of( "5496344268" ),
				Arguments.of( "0314580754" ),
				Arguments.of( "8652697156" ),
				Arguments.of( "3527694367" ),
				Arguments.of( "8771236130" ),
				Arguments.of( "9254906927" )
		);
	}

	@ParameterizedTest
	@MethodSource("invalidJuridicalTypeINNData")
	public void invalidJuridicalTypeINN(String inn) {
		validator.initialize( initializeAnnotation( INN.Type.JURIDICAL ) );

		assertInvalidINN( inn );
	}

	private static Stream<Arguments> invalidJuridicalTypeINNData() {
		return Stream.of(
				//invalid checksum
				Arguments.of( "0123456789" ),
				Arguments.of( "0305773928" ),

				//invalid symbols
				Arguments.of( "a305773929" ),

				//invalid length
				Arguments.of( "" ),
				Arguments.of( "906605631" ),
				Arguments.of( "90660563173" ),

				//invalid type
				Arguments.of( "246964567008" )
		);
	}

	@Test
	public void testAnyTypeINN() {
		validator.initialize( initializeAnnotation( INN.Type.ANY ) );

		final String personalValidINN = "246964567008";
		final String juridicalValidINN = "5496344268";
		final String personalInvalidINN = "246964567009";
		final String juridicalInvalidINN = "0305773928";

		assertValidINN( null );
		assertValidINN( personalValidINN );
		assertValidINN( juridicalValidINN );
		assertInvalidINN( personalInvalidINN );
		assertInvalidINN( juridicalInvalidINN );
	}

	private void assertValidINN(String inn) {
		assertTrue( validator.isValid( inn, null ), inn + " should be a valid INN" );
	}

	private void assertInvalidINN(String inn) {
		assertFalse( validator.isValid( inn, null ), inn + " should be a invalid INN" );
	}

	private INN initializeAnnotation(INN.Type type) {
		ConstraintAnnotationDescriptor.Builder<INN> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( INN.class );
		descriptorBuilder.setAttribute( "type", type );
		return descriptorBuilder.build().getAnnotation();
	}
}
