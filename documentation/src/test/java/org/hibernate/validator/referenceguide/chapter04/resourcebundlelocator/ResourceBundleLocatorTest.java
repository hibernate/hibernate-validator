package org.hibernate.validator.referenceguide.chapter04.resourcebundlelocator;

import java.util.Arrays;
import java.util.Set;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.Test;

import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.resourceloading.AggregateResourceBundleLocator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;

import static org.junit.Assert.assertEquals;

public class ResourceBundleLocatorTest {

	@Test
	public void messagesRetrievedFromSpecificBundle() {
		//tag::messagesRetrievedFromSpecificBundle[]
		Validator validator = Validation.byDefaultProvider()
				.configure()
				.messageInterpolator(
						new ResourceBundleMessageInterpolator(
								new PlatformResourceBundleLocator( "MyMessages" )
						)
				)
				.buildValidatorFactory()
				.getValidator();
		//end::messagesRetrievedFromSpecificBundle[]

		Set<ConstraintViolation<Car>> violations = validator.validateProperty(
				new Car(),
				"licensePlate"
		);
		assertEquals( 1, violations.size() );
		assertEquals( "null is not supported", violations.iterator().next().getMessage() );
	}

	@Test
	public void usingAggregateResourceBundleLocator() {
		//tag::usingAggregateResourceBundleLocator[]
		Validator validator = Validation.byDefaultProvider()
				.configure()
				.messageInterpolator(
						new ResourceBundleMessageInterpolator(
								new AggregateResourceBundleLocator(
										Arrays.asList(
												"MyMessages",
												"MyOtherMessages"
										)
								)
						)
				)
				.buildValidatorFactory()
				.getValidator();
		//end::usingAggregateResourceBundleLocator[]

		Set<ConstraintViolation<Car>> violations = validator.validateProperty(
				new Car(),
				"licensePlate"
		);
		assertEquals( 1, violations.size() );
		assertEquals( "null is not supported", violations.iterator().next().getMessage() );

		violations = validator.validateProperty( new Car(), "topSpeed" );
		assertEquals( 1, violations.size() );
		assertEquals( "too high", violations.iterator().next().getMessage() );
	}
}
