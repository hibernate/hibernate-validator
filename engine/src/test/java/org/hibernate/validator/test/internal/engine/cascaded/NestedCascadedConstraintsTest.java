/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.cascaded;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPathStringRepresentations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
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
import org.hibernate.validator.testutils.CandidateForTck;

import org.testng.annotations.Test;

@TestForIssue(jiraKey = "HV-1237")
@CandidateForTck
public class NestedCascadedConstraintsTest {

	@Test
	public void testNestedValid() {
		Validator validator = getValidator();

		Set<ConstraintViolation<EmailAddressMap>> constraintViolations = validator.validate( EmailAddressMap.validEmailAddressMap() );

		assertNoViolations( constraintViolations );

		constraintViolations = validator.validate( EmailAddressMap.invalidEmailAddressMap() );

		assertCorrectPropertyPathStringRepresentations(
				constraintViolations,
				"map[invalid].<map value>[1].email",
				"map[invalid].<map value>[2].email"
		);
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Email.class )
						.withPropertyPath( pathWith()
								.property( "map" )
								.containerElement( NodeImpl.MAP_VALUE_NODE_NAME, true, "invalid", null, Map.class, 1 )
								.property( "email", true, null, 1, List.class, 0 )
						),
				violationOf( Email.class )
						.withPropertyPath( pathWith()
								.property( "map" )
								.containerElement( NodeImpl.MAP_VALUE_NODE_NAME, true, "invalid", null, Map.class, 1 )
								.property( "email", true, null, 2, List.class, 0 )
						)
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

		assertNoViolations( constraintViolations );

		CinemaEmailAddresses invalidCinemaEmailAddresses = CinemaEmailAddresses.invalidCinemaEmailAddresses();
		constraintViolations = validator.validate( invalidCinemaEmailAddresses );

		assertCorrectPropertyPathStringRepresentations(
				constraintViolations,
				"map[Optional[Cinema<cinema2>]].<map value>[1].email",
				"map[Optional[Cinema<cinema2>]].<map value>[2].email",
				"map[Optional[Cinema<cinema3>]].<map value>[0].<list element>"
		);
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Email.class )
						.withPropertyPath( pathWith()
								.property( "map" )
								.containerElement( NodeImpl.MAP_VALUE_NODE_NAME, true, invalidCinemaEmailAddresses.map.keySet().toArray()[1], null, Map.class,
										1
								)
								.property( "email", true, null, 1, List.class, 0 )
						),
				violationOf( Email.class )
						.withPropertyPath( pathWith()
								.property( "map" )
								.containerElement( NodeImpl.MAP_VALUE_NODE_NAME, true, invalidCinemaEmailAddresses.map.keySet().toArray()[1], null, Map.class,
										1
								)
								.property( "email", true, null, 2, List.class, 0 )
						),
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "map" )
								.containerElement( NodeImpl.MAP_VALUE_NODE_NAME, true, invalidCinemaEmailAddresses.map.keySet().toArray()[2], null, Map.class,
										1
								)
								.containerElement( NodeImpl.LIST_ELEMENT_NODE_NAME, true, null, 0, List.class, 0 )
						)
		);

		CinemaEmailAddresses invalidKeyCinemaEmailAddresses = CinemaEmailAddresses.invalidKey();
		constraintViolations = validator.validate( invalidKeyCinemaEmailAddresses );

		assertCorrectPropertyPathStringRepresentations(
				constraintViolations,
				"map<K>[Optional[Cinema<cinema4>]].<map key>.visitor.name"
		);
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "map" )
								.containerElement( NodeImpl.MAP_KEY_NODE_NAME, true, invalidKeyCinemaEmailAddresses.map.keySet().toArray()[1], null, Map.class,
										0
								)
								.property( "visitor", Optional.class, 0 )
								.property( "name", Reference.class, 0 )
						)
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

		assertNoViolations( constraintViolations );

		constraintViolations = validator.validate( CinemaArray.invalidCinemaArray() );

		assertCorrectPropertyPathStringRepresentations(
				constraintViolations,
				"array[0].<iterable element>[0].<list element>",
				"array[0].<iterable element>[1].visitor.name"
		);
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "array" )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 0, Object[].class, null )
								.containerElement( NodeImpl.LIST_ELEMENT_NODE_NAME, true, null, 0, List.class, 0 )
						),
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "array" )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 0, Object[].class, null )
								.property( "visitor", true, null, 1, List.class, 0 )
								.property( "name", Reference.class, 0 )
						)
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1326")
	public void testNestedNullValue() {
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addValueExtractor( new ReferenceValueExtractor() )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<NestedCascadingListWithValidAllAlongTheWay>> constraintViolations = validator
				.validate( NestedCascadingListWithValidAllAlongTheWay.valid() );

		assertNoViolations( constraintViolations );

		constraintViolations = validator.validate( NestedCascadingListWithValidAllAlongTheWay.withNullList() );

		assertCorrectPropertyPathStringRepresentations( constraintViolations, "list[0].<list element>" );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "list" )
								.containerElement( NodeImpl.LIST_ELEMENT_NODE_NAME, true, null, 0, List.class, 0 )
						)
		);
	}

	private static class EmailAddressMap {

		private final Map<String, List<@Valid EmailAddress>> map = new LinkedHashMap<>();

		private static EmailAddressMap validEmailAddressMap() {
			List<EmailAddress> validEmailAddresses = Arrays.asList( new EmailAddress( "valid-email-1@example.com" ),
					new EmailAddress( "valid-email-2@example.com" )
			);

			EmailAddressMap emailAddressMap = new EmailAddressMap();
			emailAddressMap.map.put( "valid", validEmailAddresses );

			return emailAddressMap;
		}

		private static EmailAddressMap invalidEmailAddressMap() {
			List<EmailAddress> validEmailAddresses = Arrays.asList( new EmailAddress( "valid-email-1@example.com" ),
					new EmailAddress( "valid-email-2@example.com" )
			);
			List<EmailAddress> emailAddressesContainingInvalidEmails = Arrays.asList( new EmailAddress( "valid-email-3@example.com" ),
					new EmailAddress( "invalid-1" ), new EmailAddress( "invalid-2" )
			);

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

			cinemaArray.array = new List[]{ Arrays.asList( new Cinema( "cinema1", new SomeReference<>( new Visitor( "Name 1" ) ) ) ) };

			return cinemaArray;
		}

		private static CinemaArray invalidCinemaArray() {
			CinemaArray cinemaArray = new CinemaArray();

			cinemaArray.array = new List[]{ Arrays.asList( null, new Cinema( "cinema2", new SomeReference<>( new Visitor() ) ) ) };

			return cinemaArray;
		}
	}

	@SuppressWarnings({ "unused" })
	private static class NestedCascadingListWithValidAllAlongTheWay {

		private List<@NotNull List<@NotNull @Valid Cinema>> list;

		private static NestedCascadingListWithValidAllAlongTheWay valid() {
			NestedCascadingListWithValidAllAlongTheWay valid = new NestedCascadingListWithValidAllAlongTheWay();

			valid.list = Arrays.asList( Arrays.asList( new Cinema( "cinema1", new SomeReference<>( new Visitor( "Name 1" ) ) ) ) );

			return valid;
		}

		private static NestedCascadingListWithValidAllAlongTheWay withNullList() {
			NestedCascadingListWithValidAllAlongTheWay valid = new NestedCascadingListWithValidAllAlongTheWay();

			valid.list = Arrays.asList( (List<Cinema>) null );

			return valid;
		}
	}
}
