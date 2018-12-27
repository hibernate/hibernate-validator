package org.hibernate.validator.test.internal.engine;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Max;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.engine.HibernateConstraintViolation;
import org.hibernate.validator.internal.JsonPropertyPathNodeNameProvider;

import org.junit.Test;

public class JsonPropertyPathNodeNameProviderTest {
	private final JsonPropertyPathNodeNameProvider provider = new JsonPropertyPathNodeNameProvider();

	@Test
	public void test() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.propertyPathNodeNameProvider( provider )
				.buildValidatorFactory();
		Validator validator = validatorFactory.getValidator();

		Foo testInstance = new Foo( "aaa" );

		Set<ConstraintViolation<Foo>> constraintViolations = validator.validateProperty( testInstance, "field1" );
		List<String> resolvedPropertyPath = ( (HibernateConstraintViolation) constraintViolations.iterator().next() ).getResolvedPropertyPath();

		assertEquals( constraintViolations.size(), 1 );
		assertEquals( 1, resolvedPropertyPath.size() );
		assertEquals( "field1", resolvedPropertyPath.get( 0 ) );
	}

	private static class Foo {
		@Max(2)
		private final String field1;

		Foo(String field1) {
			this.field1 = field1;
		}

		String getField1() {
			return field1;
		}
	}
}
