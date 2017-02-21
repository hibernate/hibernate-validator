/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.cascaded;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.internal.engine.path.NodeImpl;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.Test;

@TestForIssue(jiraKey = "HV-1237")
public class NestedCascadedConstraintsTest {

	@Test
	public void testNestedValid() {
		Validator validator = getValidator();

		Set<ConstraintViolation<EmailAddressMap>> constraintViolations = validator.validate( EmailAddressMap.validEmailAddressMap() );

		assertNumberOfViolations( constraintViolations, 0 );

		constraintViolations = validator.validate( EmailAddressMap.invalidEmailAddressMap() );

		assertCorrectPropertyPaths(
				constraintViolations,
				"map[invalid].<map value>[1].email",
				"map[invalid].<map value>[2].email"
		);
		assertCorrectConstraintTypes( constraintViolations, Email.class, Email.class );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "map" )
						.typeArgument( NodeImpl.MAP_VALUE_NODE_NAME, true, "invalid", null )
						.property( "email", true, null, 1 ),
				pathWith()
						.property( "map" )
						.typeArgument( NodeImpl.MAP_VALUE_NODE_NAME, true, "invalid", null )
						.property( "email", true, null, 2 )
		);
	}

	@Test
	public void testMultipleNestedValidWithCustomExtractor() {
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addValueExtractor( new ReferenceValueExtractor() )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<CinemaEmailAddresses>> constraintViolations = validator.validate( CinemaEmailAddresses.validCinemaEmailAddresses() );

		assertNumberOfViolations( constraintViolations, 0 );

		CinemaEmailAddresses invalidCinemaEmailAddresses = CinemaEmailAddresses.invalidCinemaEmailAddresses();
		constraintViolations = validator.validate( invalidCinemaEmailAddresses );

		assertCorrectPropertyPaths(
				constraintViolations,
				"map[Optional[Cinema<cinema2>]].<map value>[1].email",
				"map[Optional[Cinema<cinema2>]].<map value>[2].email",
				"map[Optional[Cinema<cinema3>]].<map value>[0].<iterable element>"
		);
		assertCorrectConstraintTypes( constraintViolations, Email.class, Email.class, NotNull.class );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "map" )
						.typeArgument( NodeImpl.MAP_VALUE_NODE_NAME, true, invalidCinemaEmailAddresses.map.keySet().toArray()[1], null )
						.property( "email", true, null, 1 ),
				pathWith()
						.property( "map" )
						.typeArgument( NodeImpl.MAP_VALUE_NODE_NAME, true, invalidCinemaEmailAddresses.map.keySet().toArray()[1], null )
						.property( "email", true, null, 2 ),
				pathWith()
						.property( "map" )
						.typeArgument( NodeImpl.MAP_VALUE_NODE_NAME, true, invalidCinemaEmailAddresses.map.keySet().toArray()[2], null )
						.typeArgument( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 0 )
		);

		CinemaEmailAddresses invalidKeyCinemaEmailAddresses = CinemaEmailAddresses.invalidKey();
		constraintViolations = validator.validate( invalidKeyCinemaEmailAddresses );

		assertCorrectPropertyPaths(
				constraintViolations,
				"map[Optional[Cinema<cinema4>]].<map key>.visitor.name"
		);
		assertCorrectConstraintTypes( constraintViolations, NotNull.class );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "map" )
						.typeArgument( NodeImpl.MAP_KEY_NODE_NAME, true, invalidKeyCinemaEmailAddresses.map.keySet().toArray()[1], null )
						.property( "visitor" )
						.property( "name" )
		);
	}

	@Test
	public void testNestedOnArray() {
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addValueExtractor( new ReferenceValueExtractor() )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<CinemaArray>> constraintViolations = validator.validate( CinemaArray.validCinemaArray() );

		assertNumberOfViolations( constraintViolations, 0 );

		constraintViolations = validator.validate( CinemaArray.invalidCinemaArray() );

		assertCorrectPropertyPaths(
				constraintViolations,
				"array[0].<iterable element>[0].<iterable element>",
				"array[0].<iterable element>[1].visitor.name"
		);
		assertCorrectConstraintTypes( constraintViolations, NotNull.class, NotNull.class );
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith()
						.property( "array" )
						.typeArgument( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 0 )
						.typeArgument( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 0 ),
				pathWith()
						.property( "array" )
						.typeArgument( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 0 )
						.property( "visitor", true, null, 1 )
						.property( "name" )
		);
	}

	private static class EmailAddressMap {

		private final Map<String, List<@Valid EmailAddress>> map = new LinkedHashMap<>();

		private static EmailAddressMap validEmailAddressMap() {
			List<EmailAddress> validEmailAddresses = Arrays.asList( new EmailAddress( "valid-email-1@example.com" ), new EmailAddress( "valid-email-2@example.com" ) );

			EmailAddressMap emailAddressMap = new EmailAddressMap();
			emailAddressMap.map.put( "valid", validEmailAddresses );

			return emailAddressMap;
		}

		private static EmailAddressMap invalidEmailAddressMap() {
			List<EmailAddress> validEmailAddresses = Arrays.asList( new EmailAddress( "valid-email-1@example.com" ), new EmailAddress( "valid-email-2@example.com" ) );
			List<EmailAddress> emailAddressesContainingInvalidEmails = Arrays.asList( new EmailAddress( "valid-email-3@example.com" ),
					new EmailAddress( "invalid-1" ), new EmailAddress( "invalid-2" ) );

			EmailAddressMap emailAddressMap = new EmailAddressMap();
			emailAddressMap.map.put( "valid", validEmailAddresses );
			emailAddressMap.map.put( "invalid", emailAddressesContainingInvalidEmails );

			return emailAddressMap;
		}
	}

	private static class CinemaEmailAddresses {

		private final Map<@NotNull Optional<@Valid Cinema>, List<@NotNull @Valid EmailAddress>> map = new LinkedHashMap<>();

		private static CinemaEmailAddresses validCinemaEmailAddresses() {
			CinemaEmailAddresses cinemaEmailAddresses = new CinemaEmailAddresses();
			cinemaEmailAddresses.map.put(
					Optional.of( new Cinema( "cinema1", new SomeReference<>( new Visitor( "Name 1" ) ) ) ),
					Arrays.asList( new EmailAddress( "valid-email-1@example.com" ), new EmailAddress( "valid-email-2@example.com" ) )
			);

			return cinemaEmailAddresses;
		}

		private static CinemaEmailAddresses invalidCinemaEmailAddresses() {
			CinemaEmailAddresses cinemaEmailAddresses = validCinemaEmailAddresses();
			cinemaEmailAddresses.map.put(
					Optional.of( new Cinema( "cinema2", new SomeReference<>( new Visitor( "Name 2" ) ) ) ),
					Arrays.asList( new EmailAddress( "valid-email-3@example.com" ), new EmailAddress( "invalid-1" ), new EmailAddress( "invalid-2" ) )
			);
			cinemaEmailAddresses.map.put(
					Optional.of( new Cinema( "cinema3", new SomeReference<>( new Visitor( "Name 3" ) ) ) ),
					Arrays.asList( (EmailAddress) null )
			);

			return cinemaEmailAddresses;
		}

		private static CinemaEmailAddresses invalidKey() {
			CinemaEmailAddresses cinemaEmailAddresses = validCinemaEmailAddresses();
			cinemaEmailAddresses.map.put(
					Optional.of( new Cinema( "cinema4", new SomeReference<>( new Visitor() ) ) ),
					Arrays.asList( new EmailAddress( "valid-email-4@example.com" ) )
			);

			return cinemaEmailAddresses;
		}
	}

	@SuppressWarnings({ "unused", "unchecked" })
	private static class CinemaArray {

		private List<@NotNull @Valid Cinema>[] array;

		private static CinemaArray validCinemaArray() {
			CinemaArray cinemaArray = new CinemaArray();

			cinemaArray.array = new List[] { Arrays.asList( new Cinema( "cinema1", new SomeReference<>( new Visitor( "Name 1" ) ) ) ) };

			return cinemaArray;
		}

		private static CinemaArray invalidCinemaArray() {
			CinemaArray cinemaArray = new CinemaArray();

			cinemaArray.array = new List[] { Arrays.asList( null, new Cinema( "cinema2", new SomeReference<>( new Visitor() ) ) ) };

			return cinemaArray;
		}
	}

}
