package org.hibernate.validator.test.util;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import org.testng.annotations.Test;

import org.hibernate.validator.util.LazyValidatorFactory;

import static org.testng.Assert.assertEquals;

/**
 * @author Emmanuel Bernard
 */
public class LazyValidatorFactoryTest {
	/**
	 * Simple test that makes sure this class works.
	 * The lazy feature is not tested per se
	 * nor is the fact that the default provider is forced to Hibernate Validator
	 */
	@Test
	public void testLazyValidatorFactory() {
		LazyValidatorFactory factory = new LazyValidatorFactory();
		Validator validator = factory.getValidator();
		assertEquals( 1, validator.validate( new A() ).size() );

		factory = new LazyValidatorFactory( Validation.byDefaultProvider().configure() );
		validator = factory.getValidator();
		assertEquals( 1, validator.validate( new A() ).size() );
	}

	public static class A {
		@NotNull String b;
	}
}
