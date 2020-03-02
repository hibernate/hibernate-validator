package org.hibernate.validator.referenceguide.chapter09;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.constraints.ScriptAssert;

import org.junit.Test;

public class CustomScriptEvaluatorFactoryForSpringELTest {

	@Test
	public void testCreateNewScriptEvaluator() throws Exception {
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.scriptEvaluatorFactory( new SpringELScriptEvaluatorFactory() )
				.buildValidatorFactory()
				.getValidator();
		assertTrue( validator.validate( new Foo( 1 ) ).isEmpty() );
		assertEquals( 1, validator.validate( new Foo( -1 ) ).size() );
	}

	//tag::include[]
	@ScriptAssert(script = "value > 0", lang = "spring")
	public class Foo {

		private final int value;

		private Foo(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}
	//end::include[]
}
