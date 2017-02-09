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
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.util.Arrays;
import java.util.HashMap;
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
	}

	@Test
	public void testMultipleNestedValidWithCustomExtractor() {
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addCascadedValueExtractor( new ReferenceValueExtractor() )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<CinemaEmailAddresses>> constraintViolations = validator.validate( CinemaEmailAddresses.validCinemaEmailAddresses() );

		assertNumberOfViolations( constraintViolations, 0 );

		constraintViolations = validator.validate( CinemaEmailAddresses.invalidCinemaEmailAddresses() );

		assertCorrectPropertyPaths(
				constraintViolations,
				"map[Optional[Cinema[cinema2]]].<map value>[1].email",
				"map[Optional[Cinema[cinema2]]].<map value>[2].email"
		);

		constraintViolations = validator.validate( CinemaEmailAddresses.invalidKey() );

		assertCorrectPropertyPaths(
				constraintViolations,
				"map[Optional[Cinema[cinema3]]].<map key>.visitor.name",
				"map[Optional.empty].<map key>"
		);
	}

	private static class EmailAddressMap {

		private final Map<String, @Valid List<@Valid EmailAddress>> map = new HashMap<>();

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

		private final Map<@NotNull @Valid Optional<@NotNull @Valid Cinema>, @Valid List<@Valid EmailAddress>> map = new HashMap<>();

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

			return cinemaEmailAddresses;
		}

		private static CinemaEmailAddresses invalidKey() {
			CinemaEmailAddresses cinemaEmailAddresses = validCinemaEmailAddresses();
			cinemaEmailAddresses.map.put(
					Optional.of( new Cinema( "cinema3", new SomeReference<>( new Visitor() ) ) ),
					Arrays.asList( new EmailAddress( "valid-email-4@example.com" ) )
			);
			cinemaEmailAddresses.map.put(
					Optional.empty(),
					Arrays.asList( new EmailAddress( "valid-email-5@example.com" ) )
			);

			return cinemaEmailAddresses;
		}
	}

}
