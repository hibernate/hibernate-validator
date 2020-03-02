/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata.descriptor;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintTarget;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;
import jakarta.validation.metadata.BeanDescriptor;
import jakarta.validation.metadata.ConstraintDescriptor;
import jakarta.validation.metadata.CrossParameterDescriptor;
import jakarta.validation.metadata.MethodDescriptor;
import jakarta.validation.metadata.MethodType;
import jakarta.validation.metadata.PropertyDescriptor;

import org.testng.annotations.Test;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.testutils.ValidatorUtil.getBeanDescriptor;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author Hardy Ferentschik
 */
public class ConstraintDescriptorTest {
	@Test
	public void testBasicDescriptorAttributes() {
		BeanDescriptor beanDescriptor = getBeanDescriptor( Foo.class );
		PropertyDescriptor propertyDescriptor = beanDescriptor.getConstraintsForProperty( "fubar" );
		Set<ConstraintDescriptor<?>> constraintDescriptors = propertyDescriptor.getConstraintDescriptors();

		assertEquals( constraintDescriptors.size(), 1 );
		ConstraintDescriptor<?> constraintDescriptor = constraintDescriptors.iterator().next();
		assertEquals( constraintDescriptor.getMessageTemplate(), "bar", "Wrong message" );

		Set<Class<?>> groups = newHashSet();
		groups.add( SnafuGroup.class );
		assertEquals( constraintDescriptor.getGroups(), groups, "Wrong groups" );

		Set<Class<?>> payloads = newHashSet();
		payloads.add( Payload22.class );
		assertEquals( constraintDescriptor.getPayload(), payloads, "Wrong payload" );

		assertNull( constraintDescriptor.getValidationAppliesTo(), "There is no validationAppliedTo attribute" );
	}

	@Test
	public void testValidationAppliesTo() {
		BeanDescriptor beanDescriptor = getBeanDescriptor( Bar.class );
		Set<MethodDescriptor> methodDescriptors = beanDescriptor.getConstrainedMethods( MethodType.NON_GETTER );
		assertEquals( methodDescriptors.size(), 1 );

		CrossParameterDescriptor crossParameterDescriptor = methodDescriptors.iterator()
				.next()
				.getCrossParameterDescriptor();
		Set<ConstraintDescriptor<?>> constraintDescriptors = crossParameterDescriptor.getConstraintDescriptors();
		assertEquals( constraintDescriptors.size(), 1 );

		ConstraintDescriptor<?> constraintDescriptor = constraintDescriptors.iterator().next();
		assertEquals(
				constraintDescriptor.getValidationAppliesTo(),
				ConstraintTarget.PARAMETERS,
				"wrong constraint targets"
		);
	}

	public interface SnafuGroup {
	}

	public static class Payload22 implements Payload {
	}

	public static class Foo {
		@NotNull(message = "bar", groups = SnafuGroup.class, payload = Payload22.class)
		private String fubar;
	}


	@Target({ METHOD, ANNOTATION_TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@Constraint(validatedBy = { VersatileConstraintValidator.class })
	@Documented
	public @interface VersatileConstraint {
		String message() default "";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };

		ConstraintTarget validationAppliesTo() default ConstraintTarget.IMPLICIT;
	}

	@SupportedValidationTarget(value = { ValidationTarget.PARAMETERS, ValidationTarget.ANNOTATED_ELEMENT })
	public static class VersatileConstraintValidator implements ConstraintValidator<VersatileConstraint, Object[]> {

		@Override
		public boolean isValid(Object[] value, ConstraintValidatorContext context) {
			return false;
		}
	}

	public static class Bar {
		@VersatileConstraint(validationAppliesTo = ConstraintTarget.PARAMETERS)
		public String concatenateStrings(String a, String b) {
			return a + b;
		}
	}
}
