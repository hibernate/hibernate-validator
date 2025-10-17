/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests for {@link IpAddress} constraint validator ({@link IpAddress}).
 *
 * @author Ivan Malutin
 */
@TestForIssue(jiraKey = "HV-2137")
public class IpAddressValidatorTest {

	private IpAddressValidator validator;

	@BeforeClass
	public void setUp() {
		validator = new IpAddressValidator();
	}

	@Test(dataProvider = "validIPv4Addresses")
	public void validIPv4Addresses(String ipAddress) {
		validator.initialize( initializeAnnotation( IpAddress.Type.IPv4 ) );
		assertValidIpAddress( ipAddress );
	}

	@DataProvider(name = "validIPv4Addresses")
	String[] validIPv4AddressesData() {
		return new String[] {
				"192.168.1.1",
				"10.0.0.255",
				"172.16.254.1",
				"255.255.255.255",
				"0.0.0.0",
				"127.0.0.1",
				"8.8.8.8",
				"169.254.1.1",
				"192.0.2.1",
				"198.51.100.1"
		};
	}


	@Test(dataProvider = "invalidIPv4Addresses")
	public void invalidIPv4Addresses(String ipAddress) {
		validator.initialize( initializeAnnotation( IpAddress.Type.IPv4 ) );
		assertInvalidIpAddress( ipAddress );
	}

	@DataProvider(name = "invalidIPv4Addresses")
	String[] invalidIPv4AddressesData() {
		return new String[] {
				"256.1.1.1",
				"192.168.1",
				"192.168.1.1.1",
				"192.168.1.",
				"192.0168.1.",
				"192.168..1",
				"192.168.1.01",
				"qwe.qwe.qwe.qwe",
				"192.168.1.1 ",
				"192 .168.1.1",
				"192.168.1.-1",
				"192.168.1.",
				"192.16.8.1.",
				"19.2.16.8.1."
		};
	}

	@Test(dataProvider = "validIPv6Addresses")
	public void validIPv6Addresses(String ipAddress) {
		validator.initialize( initializeAnnotation( IpAddress.Type.IPv6 ) );
		assertValidIpAddress( ipAddress );
	}

	@DataProvider(name = "validIPv6Addresses")
	String[] validIPv6AddressesData() {
		return new String[] {
				"2001:0db8:85a3:0000:0000:8a2e:0370:7334",
				"2001:db8:85a3:0:0:8a2e:370:7334",
				"2001:db8:85a3::8a2e:370:7334",
				"2001:db8::",
				"::ffff:192.168.1.1",
				"ff02::1",
				"2001:0:0:1::1",
				"2001:db8:1234:5678:90ab:cdef:1234:5678",
				"::ffff:c0a8:101",
				"64:ff9b::192.168.1.1",
				"2001:20::1",
				"fc00::1",
				"2001:db8:a::123",
				"1:2:3:4:5:6:7:8",
				"a:b:c:d:e:f:1:2",
				"fe80::215:5dff:fe00:402",
				"2001:db8:85a3:8d3:1319:8a2e:370:7348",
				"::",
				"::1",
				"1::",
				"::1234:5678",
				"1:2:3:4:5:6:7::",
				"2001:db8:85a3:8d3:1319:8a2e:370:7348%1234556",
				"2001:db8:85a3:8d3:1319:8a2e:370:7348%eth0",
				"2001:db8:85a3:8d3:1319:8a2e:370:7348%eth1",
				"2001:db8:85a3:8d3:1319:8a2e:370:7348%eth2",
				"2001:db8:85a3:8d3:1319:8a2e:370:7348%smth",
		};
	}

	@Test(dataProvider = "invalidIPv6Addresses")
	public void invalidIPv6Addresses(String ipAddress) {
		validator.initialize( initializeAnnotation( IpAddress.Type.IPv6 ) );
		assertInvalidIpAddress( ipAddress );
	}

	@DataProvider(name = "invalidIPv6Addresses")
	String[] invalidIPv6AddressesData() {
		return new String[] {
				"2001:db8:85a3:8d3:1319:8a2e:370",
				"2001::85a3::8a2e",
				"2001:db8:85a3:8d3:1319:8a2e:370:7334:",
				":2001:db8:85a3:8d3:1319:8a2e:370:7334",
				"2001:db8:85a3:8d3:1319:8a2e:370:733g",
				"2001:db8:85a3:8d3:1319:8a2e:370:73345",
				"2001:db8:85a3:8d3:1319:8a2e:370:",
				"2001:db8:::8a2e:370:7334",
				"2001:db8:85a3:8d3:1319:8a2e:370:7334:abcd",
				"2001:0db8:85a3:00000:0000:8a2e:0370:7334",
				"02001:00db8:085a3:00000:00000:08a2e:00370:07334",
				"::ffff:192.168.300.1",
				"::ffff:192.168.1",
				"::ffff:192.168.1.0.1",
				"2001:db8:::192.1.1.1:370:7334",
				"2001:db8:85a3:8d3:1319:8a2e:370:7334 :",
				"2001:db8:85a3-8d3:1319:8a2e:370:7334",
				"64:192.168.1.1::ff9b",
				"2001::db8::1",
				"ff02:::1",
				"2001:db8:85a3:0:0:0:0:0:0",
				":1",
				"2001:db8:85a3:8d3:1319:8a2e:370:",
				"2001:db8:xyz::1",
				":::",
				"1:2:3:4:5:6:7:8:9",
				"1:2:3:4:5:6:7",
				"1::2::3",
				"1:2:3:4:5:6:7:8g",
				"2001:0db8:85a3:0000:0000::8a2e:0370:7334",
				"2001:0db8:85a3:0000:0000::8a2e:0370:0370:7334",
		};
	}

	@Test(dataProvider = "testAnyValid")
	public void testAnyValid(String ipAddress) {
		validator.initialize( initializeAnnotation( IpAddress.Type.ANY ) );
		assertValidIpAddress( ipAddress );
	}

	@DataProvider(name = "testAnyValid")
	String[] testAnyValidData() {
		return Stream.concat( Arrays.stream( validIPv4AddressesData() ), Arrays.stream( validIPv6AddressesData() ) )
				.toArray( String[]::new );
	}

	@Test(dataProvider = "testAnyInvalid")
	public void testAnyInvalid(String ipAddress) {
		validator.initialize( initializeAnnotation( IpAddress.Type.ANY ) );
		assertInvalidIpAddress( ipAddress );
	}

	@DataProvider(name = "testAnyInvalid")
	String[] testAnyInvalidData() {
		return Stream.concat( Arrays.stream( invalidIPv4AddressesData() ), Arrays.stream( invalidIPv6AddressesData() ) )
				.toArray( String[]::new );
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
