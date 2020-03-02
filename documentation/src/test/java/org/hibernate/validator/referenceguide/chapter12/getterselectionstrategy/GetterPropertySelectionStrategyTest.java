package org.hibernate.validator.referenceguide.chapter12.getterselectionstrategy;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;

import org.junit.Test;

public class GetterPropertySelectionStrategyTest {

	@Test
	public void defaultStrategyUsed() {
		//tag::no-strategy[]
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory()
				.getValidator();

		User user = new User( "", "", "not an email" );

		Set<ConstraintViolation<User>> constraintViolations = validator.validate( user );

		// as User has non-standard getters no violations are triggered
		assertEquals( 0, constraintViolations.size() );
		//end::no-strategy[]
	}

	@Test
	public void customNoPrefixStrategyUsed() {
		//tag::custom-strategy[]
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				// Setting a custom getter property selection strategy
				.getterPropertySelectionStrategy( new FluentGetterPropertySelectionStrategy() )
				.buildValidatorFactory()
				.getValidator();

		User user = new User( "", "", "not an email" );

		Set<ConstraintViolation<User>> constraintViolations = validator.validate( user );

		assertEquals( 3, constraintViolations.size() );
		//end::custom-strategy[]
	}
}
