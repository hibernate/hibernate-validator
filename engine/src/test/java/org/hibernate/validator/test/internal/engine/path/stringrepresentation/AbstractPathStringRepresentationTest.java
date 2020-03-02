/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.path.stringrepresentation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.testng.annotations.BeforeClass;

/**
 * @author Marko Bekhta
 */
public abstract class AbstractPathStringRepresentationTest {

	protected Validator validator;

	@BeforeClass
	public void setupValidator() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	@ValidLyonZipCode
	protected static class Address {

		@NotNull
		private String street;
		@Valid
		private City city;
		private String zipCode;

		public Address(@NotNull String street, @Valid City city) {
			this.street = street;
			this.city = city;
		}

		@Valid
		public Address(String street, City city, String zipCode) {
			this.street = street;
			this.city = city;
			this.zipCode = zipCode;
		}

		@Override
		public String toString() {
			return street + ", " + city;
		}
	}

	protected static class City {

		@Size(min = 3)
		private String name;

		@Valid
		public City(@Size(min = 3) String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

	}

	@Target({ TYPE, ANNOTATION_TYPE })
	@Retention(RUNTIME)
	@Constraint(validatedBy = { ValidLyonZipCodeValidator.class })
	@Documented
	public @interface ValidLyonZipCode {

		String message() default "{org.hibernate.validator.test.internal.engine.valuehandling.ValidLyonZipCode.message}";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	public static class ValidLyonZipCodeValidator implements ConstraintValidator<ValidLyonZipCode, Address> {

		@Override
		public boolean isValid(Address address, ConstraintValidatorContext context) {
			if ( address == null || address.zipCode == null || address.city == null || !"Lyon".equals( address.city.name ) ) {
				return true;
			}

			return address.zipCode.length() == 5 && address.zipCode.startsWith( "6900" );
		}
	}
}
