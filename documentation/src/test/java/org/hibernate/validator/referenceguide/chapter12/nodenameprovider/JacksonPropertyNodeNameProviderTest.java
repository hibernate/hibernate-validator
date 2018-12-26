package org.hibernate.validator.referenceguide.chapter12.nodenameprovider;

import static org.junit.Assert.assertEquals;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.HibernateValidator;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;

//tag::field[]
public class JacksonPropertyNodeNameProviderTest {
	@Test
	public void nameIsReadFromJacksonAnnotationOnField() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.propertyNodeNameProvider( new JacksonPropertyNodeNameProvider() )
				.buildValidatorFactory();

		Validator validator = validatorFactory.getValidator();

		Person clarkKent = new Person( null, "Kent" );

		Set<ConstraintViolation<Person>> violations = validator.validate( clarkKent );
		ConstraintViolation<Person> violation = violations.iterator().next();

		assertEquals( violation.getPropertyPath().toString(), "first_name" );
	}
//end::field[]

//tag::getter[]
	@Test
	public void nameIsReadFromJacksonAnnotationOnGetter() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.propertyNodeNameProvider( new JacksonPropertyNodeNameProvider() )
				.buildValidatorFactory();

		Validator validator = validatorFactory.getValidator();

		Person clarkKent = new Person( null, "Kent" );

		Set<ConstraintViolation<Person>> violations = validator.validate( clarkKent );
		ConstraintViolation<Person> violation = violations.iterator().next();

		assertEquals( violation.getPropertyPath().toString(), "first_name" );
	}

	public class Person {
		private final String firstName;

		@JsonProperty("last_name")
		private final String lastName;

		public Person(String firstName, String lastName) {
			this.firstName = firstName;
			this.lastName = lastName;
		}

		@NotNull
		@JsonProperty("first_name")
		public String getFirstName() {
			return firstName;
		}
	}
//end::getter[]
}
