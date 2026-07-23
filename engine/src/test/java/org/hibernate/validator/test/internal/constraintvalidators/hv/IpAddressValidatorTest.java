/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Stream;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.IpAddressDef;
import org.hibernate.validator.constraints.IpAddress;
import org.hibernate.validator.internal.constraintvalidators.hv.IpAddressValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.TestForIssue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests for {@link IpAddress} constraint validator ({@link IpAddress}).
 *
 * @author Ivan Malutin
 */
@TestForIssue(jiraKey = "HV-2137")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IpAddressValidatorTest {

	private IpAddressValidator validator;

	@BeforeAll
	public void setUp() {
		validator = new IpAddressValidator();
	}

	@ParameterizedTest
	@MethodSource("validIPv4AddressesData")
	public void validIPv4Addresses(String ipAddress) {
		validator.initialize( initializeAnnotation( IpAddress.Type.IPv4 ) );
		assertValidIpAddress( ipAddress );
	}

	private static Stream<Arguments> validIPv4AddressesData() {
		return Stream.of(
				Arguments.of( "192.168.1.1" ),
				Arguments.of( "10.0.0.255" ),
				Arguments.of( "172.16.254.1" ),
				Arguments.of( "255.255.255.255" ),
				Arguments.of( "0.0.0.0" ),
				Arguments.of( "127.0.0.1" ),
				Arguments.of( "8.8.8.8" ),
				Arguments.of( "169.254.1.1" ),
				Arguments.of( "192.0.2.1" ),
				Arguments.of( "198.51.100.1" )
		);
	}


	@ParameterizedTest
	@MethodSource("invalidIPv4AddressesData")
	public void invalidIPv4Addresses(String ipAddress) {
		validator.initialize( initializeAnnotation( IpAddress.Type.IPv4 ) );
		assertInvalidIpAddress( ipAddress );
	}

	private static Stream<Arguments> invalidIPv4AddressesData() {
		return Stream.of(
				Arguments.of( "256.1.1.1" ),
				Arguments.of( "192.168.1" ),
				Arguments.of( "192.168.1.1.1" ),
				Arguments.of( "192.168.1." ),
				Arguments.of( "192.0168.1." ),
				Arguments.of( "192.168..1" ),
				Arguments.of( "192.168.1.01" ),
				Arguments.of( "qwe.qwe.qwe.qwe" ),
				Arguments.of( "192.168.1.1 " ),
				Arguments.of( "192 .168.1.1" ),
				Arguments.of( "192.168.1.-1" ),
				Arguments.of( "192.168.1." ),
				Arguments.of( "192.16.8.1." ),
				Arguments.of( "19.2.16.8.1." )
		);
	}

	@ParameterizedTest
	@MethodSource("validIPv6AddressesData")
	public void validIPv6Addresses(String ipAddress) {
		validator.initialize( initializeAnnotation( IpAddress.Type.IPv6 ) );
		assertValidIpAddress( ipAddress );
	}

	private static Stream<Arguments> validIPv6AddressesData() {
		return Stream.of(
				Arguments.of( "2001:0db8:85a3:0000:0000:8a2e:0370:7334" ),
				Arguments.of( "2001:db8:85a3:0:0:8a2e:370:7334" ),
				Arguments.of( "2001:db8:85a3::8a2e:370:7334" ),
				Arguments.of( "2001:db8::" ),
				Arguments.of( "::ffff:192.168.1.1" ),
				Arguments.of( "ff02::1" ),
				Arguments.of( "2001:0:0:1::1" ),
				Arguments.of( "2001:db8:1234:5678:90ab:cdef:1234:5678" ),
				Arguments.of( "::ffff:c0a8:101" ),
				Arguments.of( "64:ff9b::192.168.1.1" ),
				Arguments.of( "2001:20::1" ),
				Arguments.of( "fc00::1" ),
				Arguments.of( "2001:db8:a::123" ),
				Arguments.of( "1:2:3:4:5:6:7:8" ),
				Arguments.of( "a:b:c:d:e:f:1:2" ),
				Arguments.of( "fe80::215:5dff:fe00:402" ),
				Arguments.of( "2001:db8:85a3:8d3:1319:8a2e:370:7348" ),
				Arguments.of( "::" ),
				Arguments.of( "::1" ),
				Arguments.of( "1::" ),
				Arguments.of( "::1234:5678" ),
				Arguments.of( "1:2:3:4:5:6:7::" ),
				Arguments.of( "2001:db8:85a3:8d3:1319:8a2e:370:7348%1234556" ),
				Arguments.of( "2001:db8:85a3:8d3:1319:8a2e:370:7348%eth0" ),
				Arguments.of( "2001:db8:85a3:8d3:1319:8a2e:370:7348%eth1" ),
				Arguments.of( "2001:db8:85a3:8d3:1319:8a2e:370:7348%eth2" ),
				Arguments.of( "2001:db8:85a3:8d3:1319:8a2e:370:7348%smth" )
		);
	}

	@ParameterizedTest
	@MethodSource("invalidIPv6AddressesData")
	public void invalidIPv6Addresses(String ipAddress) {
		validator.initialize( initializeAnnotation( IpAddress.Type.IPv6 ) );
		assertInvalidIpAddress( ipAddress );
	}

	private static Stream<Arguments> invalidIPv6AddressesData() {
		return Stream.of(
				Arguments.of( "2001:db8:85a3:8d3:1319:8a2e:370" ),
				Arguments.of( "2001::85a3::8a2e" ),
				Arguments.of( "2001:db8:85a3:8d3:1319:8a2e:370:7334:" ),
				Arguments.of( ":2001:db8:85a3:8d3:1319:8a2e:370:7334" ),
				Arguments.of( "2001:db8:85a3:8d3:1319:8a2e:370:733g" ),
				Arguments.of( "2001:db8:85a3:8d3:1319:8a2e:370:73345" ),
				Arguments.of( "2001:db8:85a3:8d3:1319:8a2e:370:" ),
				Arguments.of( "2001:db8:::8a2e:370:7334" ),
				Arguments.of( "2001:db8:85a3:8d3:1319:8a2e:370:7334:abcd" ),
				Arguments.of( "2001:0db8:85a3:00000:0000:8a2e:0370:7334" ),
				Arguments.of( "02001:00db8:085a3:00000:00000:08a2e:00370:07334" ),
				Arguments.of( "::ffff:192.168.300.1" ),
				Arguments.of( "::ffff:192.168.1" ),
				Arguments.of( "::ffff:192.168.1.0.1" ),
				Arguments.of( "2001:db8:::192.1.1.1:370:7334" ),
				Arguments.of( "2001:db8:85a3:8d3:1319:8a2e:370:7334 :" ),
				Arguments.of( "2001:db8:85a3-8d3:1319:8a2e:370:7334" ),
				Arguments.of( "64:192.168.1.1::ff9b" ),
				Arguments.of( "2001::db8::1" ),
				Arguments.of( "ff02:::1" ),
				Arguments.of( "2001:db8:85a3:0:0:0:0:0:0" ),
				Arguments.of( ":1" ),
				Arguments.of( "2001:db8:85a3:8d3:1319:8a2e:370:" ),
				Arguments.of( "2001:db8:xyz::1" ),
				Arguments.of( ":::" ),
				Arguments.of( "1:2:3:4:5:6:7:8:9" ),
				Arguments.of( "1:2:3:4:5:6:7" ),
				Arguments.of( "1::2::3" ),
				Arguments.of( "1:2:3:4:5:6:7:8g" ),
				Arguments.of( "2001:0db8:85a3:0000:0000::8a2e:0370:7334" ),
				Arguments.of( "2001:0db8:85a3:0000:0000::8a2e:0370:0370:7334" )
		);
	}

	@ParameterizedTest
	@MethodSource("testAnyValidData")
	public void testAnyValid(String ipAddress) {
		validator.initialize( initializeAnnotation( IpAddress.Type.ANY ) );
		assertValidIpAddress( ipAddress );
	}

	private static Stream<Arguments> testAnyValidData() {
		return Stream.concat( validIPv4AddressesData(), validIPv6AddressesData() );
	}

	@ParameterizedTest
	@MethodSource("testAnyInvalidData")
	public void testAnyInvalid(String ipAddress) {
		validator.initialize( initializeAnnotation( IpAddress.Type.ANY ) );
		assertInvalidIpAddress( ipAddress );
	}

	private static Stream<Arguments> testAnyInvalidData() {
		return Stream.concat( invalidIPv4AddressesData(), invalidIPv6AddressesData() );
	}

	@Test
	public void testIpAddressDef() {
		HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Foo.class )
				.field( "ipAddress" )
				.constraint( new IpAddressDef().type( IpAddress.Type.IPv4 ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( new Foo( "127.0.0.1" ) );
		assertNoViolations( constraintViolations );

		constraintViolations = validator.validate( new Foo( "256.256.256.256" ) );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( IpAddress.class )
		);
	}

	private void assertValidIpAddress(String ipAddress) {
		assertTrue( validator.isValid( ipAddress, null ), ipAddress + " should be a valid IP address" );
	}

	private void assertInvalidIpAddress(String ipAddress) {
		assertFalse( validator.isValid( ipAddress, null ), ipAddress + " should be an invalid IP address" );
	}

	private IpAddress initializeAnnotation(IpAddress.Type type) {
		ConstraintAnnotationDescriptor.Builder<IpAddress> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( IpAddress.class );
		descriptorBuilder.setAttribute( "type", type );
		return descriptorBuilder.build().getAnnotation();
	}

	private static class Foo {
		private final String ipAddress;

		public Foo(String ipAddress) {
			this.ipAddress = ipAddress;
		}
	}
}
