package org.hibernate.validator.test.engine.cascaded;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Payload;
import javax.validation.Valid;
import javax.validation.Validator;

import org.testng.annotations.Test;

import org.hibernate.validator.testutil.ValidatorUtil;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;

/**
 * @author Hardy Ferentschik
 */
public class CascadedClassConstraintTest {

	@Test
	public void testCascadedValidation() {
		Validator validator = ValidatorUtil.getValidator();
		Set<ConstraintViolation<Bar>> violations = validator.validate( new Bar() );

		//fails as pathes currently are "foos[1]", "foos[1]"
		assertCorrectPropertyPaths( violations, "foos[0]", "foos[1]" );
	}

	@ValidFoo
	private static class Foo {
	}

	private static class Bar {
		@Valid
		private List<Foo> foos = Arrays.asList( new Foo(), new Foo() );
	}

	@Constraint(validatedBy = { ValidFooValidator.class })
	@Target({ TYPE })
	@Retention(RUNTIME)
	public @interface ValidFoo {
		String message() default "{ValidFoo.message}";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	public static class ValidFooValidator implements ConstraintValidator<ValidFoo, Foo> {

		public void initialize(ValidFoo annotation) {
		}

		public boolean isValid(Foo foo, ConstraintValidatorContext context) {
			return false;
		}
	}
}


