/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.groups.conversion;

import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPathStringRepresentations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.GroupSequence;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.ConvertGroup;
import jakarta.validation.groups.Default;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.testutils.CandidateForTck;
import org.testng.annotations.Test;

/**
 * Integrative test for group conversion.
 *
 * @author Gunnar Morling
 */
public abstract class AbstractGroupConversionTest {

	private static final Log log = LoggerFactory.make( MethodHandles.lookup() );

	protected Validator validator;

	public abstract void setupValidator();

	@Test
	public void groupConversionOnField() {
		Set<ConstraintViolation<User1>> violations = validator.validate( new User1() );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withPropertyPath( pathWith()
						.property( "addresses" )
						.property( "street1", true, null, null, Set.class, 0 )
				),
				violationOf( Size.class ).withPropertyPath( pathWith()
						.property( "addresses" )
						.property( "zipCode" , true, null, null, Set.class, 0 )
				)
		);
	}

	@Test
	public void groupConversionOnGetter() {
		Set<ConstraintViolation<User2>> violations = validator.validate( new User2() );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withPropertyPath( pathWith()
						.property( "addresses" )
						.property( "street1", true, null, null, Set.class, 0 )
				),
				violationOf( Size.class ).withPropertyPath( pathWith()
						.property( "addresses" )
						.property( "zipCode" , true, null, null, Set.class, 0 )
				)
		);
	}

	@Test
	public void groupConversionOnParameter() throws Exception {
		Set<ConstraintViolation<User6>> violations = getValidator().forExecutables().validateParameters(
				new User6(),
				User6.class.getMethod( "setAddresses", List.class ),
				new List<?>[] { Arrays.asList( new Address() ) }
		);

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withPropertyPath( pathWith()
						.method( "setAddresses" )
						.parameter( "addresses",0 )
						.property( "street1", true, null, 0, List.class, 0 )
				),
				violationOf( Size.class ).withPropertyPath( pathWith()
						.method( "setAddresses" )
						.parameter( "addresses",0 )
						.property( "zipCode" , true, null, 0, List.class, 0 )
				)
		);
	}

	@Test
	public void groupConversionOnReturnValue() throws Exception {
		Set<ConstraintViolation<User7>> violations = getValidator().forExecutables().validateReturnValue(
				new User7(),
				User7.class.getMethod( "findAddresses" ),
				Arrays.asList( new Address() )
		);

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withPropertyPath( pathWith()
						.method( "findAddresses" )
						.returnValue()
						.property( "street1", true, null, 0, List.class, 0 )
				),
				violationOf( Size.class ).withPropertyPath( pathWith()
						.method( "findAddresses" )
						.returnValue()
						.property( "zipCode" , true, null, 0, List.class, 0 )
				)
		);
		assertCorrectPropertyPathStringRepresentations(
				violations,
				"findAddresses.<return value>[0].street1",
				"findAddresses.<return value>[0].zipCode"
		);
	}

	@Test
	public void multipleGroupConversionsOnField() {
		Set<ConstraintViolation<User3>> violations = validator.validate( new User3() );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withPropertyPath( pathWith()
						.property( "addresses" )
						.property( "street1", true, null, null, Set.class, 0 )
				),
				violationOf( Size.class ).withPropertyPath( pathWith()
						.property( "addresses" )
						.property( "zipCode" , true, null, null, Set.class, 0 )
				)
		);

		violations = validator.validate( new User3(), Complete.class );
		for ( ConstraintViolation<User3> constraintViolation : violations ) {
			log.info( constraintViolation.getPropertyPath() );
		}
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withPropertyPath( pathWith()
						.property( "addresses" )
						.property( "street1", true, null, null, Set.class, 0 )
				),
				violationOf( Size.class ).withPropertyPath( pathWith()
						.property( "addresses" )
						.property( "doorCode", true, null, null, Set.class, 0 )
				),
				violationOf( Size.class ).withPropertyPath( pathWith()
						.property( "addresses" )
						.property( "zipCode" , true, null, null, Set.class, 0 )
				)
		);
	}

	@Test
	public void multipleGroupConversionsOnGetter() {
		Set<ConstraintViolation<User4>> violations = validator.validate( new User4() );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withPropertyPath( pathWith()
						.property( "addresses" )
						.property( "street1", true, null, null, Set.class, 0 )
				),
				violationOf( Size.class ).withPropertyPath( pathWith()
						.property( "addresses" )
						.property( "zipCode" , true, null, null, Set.class, 0 )
				)
		);

		violations = validator.validate( new User4(), Complete.class );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withPropertyPath( pathWith()
						.property( "addresses" )
						.property( "street1", true, null, null, Set.class, 0 )
				),
				violationOf( Size.class ).withPropertyPath( pathWith()
						.property( "addresses" )
						.property( "doorCode", true, null, null, Set.class, 0 )
				),
				violationOf( Size.class ).withPropertyPath( pathWith()
						.property( "addresses" )
						.property( "zipCode" , true, null, null, Set.class, 0 )
				)
		);
	}

	@Test
	public void conversionIsEvaluatedPerProperty() {
		Set<ConstraintViolation<User5>> violations = validator.validate( new User5() );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class ).withPropertyPath( pathWith()
						.property( "addresses" )
						.property( "street1", true, null, null, Set.class, 0 )
				),
				violationOf( NotNull.class ).withPropertyPath( pathWith()
						.property( "phoneNumber" )
						.property( "number" )
				),
				violationOf( Size.class ).withPropertyPath( pathWith()
						.property( "addresses" )
						.property( "zipCode" , true, null, null, Set.class, 0 )
				)
		);
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class,
			expectedExceptionsMessageRegExp = "HV000127.*PostalSequence.*")
	public void conversionFromSequenceCausesException() {
		validator.validate( new User8() );
	}

	@Test
	@CandidateForTck
	public void sameBeanDifferentGroups() {
		Set<ConstraintViolation<User9>> violations = validator.validate( new User9() );
		assertThat( violations ).containsOnlyViolations(
				violationOf( AssertTrue.class ).withPropertyPath( pathWith()
						.property( "a" )
						.property( "b" )
				)
		);
	}


	public interface Complete extends Default {
	}

	public interface BasicPostal {
	}

	public interface FullPostal extends BasicPostal {
	}

	private interface BasicNumber {
	}

	@GroupSequence({ BasicPostal.class, FullPostal.class })
	private interface PostalSequence {
	}

	private static class Address {
		@NotNull(groups = BasicPostal.class)
		String street1;

		@NotNull
		String street2;

		@Size(groups = BasicPostal.class, min = 3)
		String zipCode = "12";

		@Size(groups = FullPostal.class, max = 2)
		String doorCode = "ABC";
	}

	private static class PhoneNumber {

		@NotNull(groups = BasicNumber.class)
		String number;
	}

	private static class User1 {

		@Valid
		@ConvertGroup(from = Default.class, to = BasicPostal.class)
		private final Set<Address> addresses = asSet( new Address() );

		public Set<Address> getAddresses() {
			return addresses;
		}
	}

	private static class User2 {

		private final Set<Address> addresses = asSet( new Address() );

		@Valid
		@ConvertGroup(from = Default.class, to = BasicPostal.class)
		public Set<Address> getAddresses() {
			return addresses;
		}
	}

	private static class User3 {

		private final Set<Address> addresses = asSet( new Address() );

		@Valid
		@ConvertGroup.List({
				@ConvertGroup(from = Default.class, to = BasicPostal.class),
				@ConvertGroup(from = Complete.class, to = FullPostal.class)
		})
		public Set<Address> getAddresses() {
			return addresses;
		}
	}

	private static class User4 {

		private final Set<Address> addresses = asSet( new Address() );

		@Valid
		@ConvertGroup.List({
				@ConvertGroup(from = Default.class, to = BasicPostal.class),
				@ConvertGroup(from = Complete.class, to = FullPostal.class)
		})
		public Set<Address> getAddresses() {
			return addresses;
		}
	}

	private static class User5 {

		@Valid
		@ConvertGroup(from = Default.class, to = BasicPostal.class)
		public Set<Address> getAddresses() {
			return asSet( new Address() );
		}

		@Valid
		@ConvertGroup(from = Default.class, to = BasicNumber.class)
		public PhoneNumber getPhoneNumber() {
			return new PhoneNumber();
		}
	}

	private static class User6 {

		public void setAddresses(
				@Valid
				@ConvertGroup(from = Default.class, to = BasicPostal.class)
				List<Address> addresses) {
		}
	}

	private static class User7 {

		private final List<Address> addresses = Arrays.asList( new Address() );

		@Valid
		@ConvertGroup(from = Default.class, to = BasicPostal.class)
		public List<Address> findAddresses() {
			return addresses;
		}
	}

	private static class User8 {

		@Valid
		@ConvertGroup(from = PostalSequence.class, to = BasicPostal.class)
		private final List<Address> addresses = Arrays.asList( new Address() );
	}

	private static class User9 {
		@Valid
		@ConvertGroup(from = Default.class, to = BasicNumber.class)
		User9 a = this;
		@AssertTrue(groups = BasicNumber.class)
		boolean b;
	}
}
