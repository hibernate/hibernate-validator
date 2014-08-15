package org.hibernate.validator.referenceguide.chapter02;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.BeforeClass;
import org.junit.Test;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.referenceguide.chapter02.propertylevel.Car;
import org.hibernate.validator.referenceguide.chapter02.typeargument.CountryList;
import org.hibernate.validator.referenceguide.chapter02.typeargument.CountryMap;
import org.hibernate.validator.referenceguide.chapter02.typeargument.CountryOptional;
import org.hibernate.validator.referenceguide.chapter02.typeargument.FooHolder;
import org.hibernate.validator.referenceguide.chapter02.typeargument.FooUnwrapper;

import static org.junit.Assert.assertEquals;

public class ValidationTest {

	private static Validator validator;

	@BeforeClass
	public static void setUpValidator() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void validate() {
		Car car = new Car( null, true );

		Set<ConstraintViolation<Car>> constraintViolations = validator.validate( car );

		assertEquals( 1, constraintViolations.size() );
		assertEquals( "may not be null", constraintViolations.iterator().next().getMessage() );
	}

	@Test
	public void validateProperty() {
		Car car = new Car( null, true );

		Set<ConstraintViolation<Car>> constraintViolations = validator.validateProperty(
				car,
				"manufacturer"
		);

		assertEquals( 1, constraintViolations.size() );
		assertEquals( "may not be null", constraintViolations.iterator().next().getMessage() );
	}

	@Test
	public void validateValue() {
		Set<ConstraintViolation<Car>> constraintViolations = validator.validateValue(
				Car.class,
				"manufacturer",
				null
		);

		assertEquals( 1, constraintViolations.size() );
		assertEquals( "may not be null", constraintViolations.iterator().next().getMessage() );
	}

	@Test
	public void validateListTypeArgumentConstraint() {
		CountryList country = new CountryList();
		Set<ConstraintViolation<CountryList>> constraintViolations = validator.validate( country );
		assertEquals( 1, constraintViolations.size() );
		assertEquals( "may not be empty", constraintViolations.iterator().next().getMessage() );
		assertEquals( "cities[1]", constraintViolations.iterator().next().getPropertyPath().toString() );
	}

	@Test
	public void validateMapTypeArgumentConstraint() {
		CountryMap country = new CountryMap();
		country.cities.put( 100, "First" );
		country.cities.put( 200, "" );
		country.cities.put( 300, "Third" );

		Set<ConstraintViolation<CountryMap>> constraintViolations = validator.validate( country );
		assertEquals( 1, constraintViolations.size() );
		assertEquals( "may not be empty", constraintViolations.iterator().next().getMessage() );
		assertEquals( "cities[200]", constraintViolations.iterator().next().getPropertyPath().toString() );
	}

	@Test
	public void validateOptionalTypeArgumentConstraint() {
		CountryOptional country = new CountryOptional();
		Set<ConstraintViolation<CountryOptional>> constraintViolations = validator.validate( country );
		assertEquals( 1, constraintViolations.size() );
		assertEquals( "may not be empty", constraintViolations.iterator().next().getMessage() );
		assertEquals( "city", constraintViolations.iterator().next().getPropertyPath().toString() );
	}

	@Test
	public void validateCustomTypeArgumentConstraint() {
		ValidatorFactory factory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addValidatedValueHandler( new FooUnwrapper() )
				.buildValidatorFactory();
		validator = factory.getValidator();

		FooHolder fooHolder = new FooHolder();
		Set<ConstraintViolation<FooHolder>> constraintViolations = validator.validate( fooHolder );
		assertEquals( 1, constraintViolations.size() );
		assertEquals( "may not be empty", constraintViolations.iterator().next().getMessage() );
		assertEquals( "foo", constraintViolations.iterator().next().getPropertyPath().toString() );
	}
}
