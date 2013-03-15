/*
* JBoss, Home of Professional Open Source
* Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.test.internal.metadata.descriptor;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;
import javax.validation.Constraint;
import javax.validation.ConstraintTarget;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.constraints.NotNull;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.CrossParameterDescriptor;
import javax.validation.metadata.MethodDescriptor;
import javax.validation.metadata.MethodType;
import javax.validation.metadata.PropertyDescriptor;

import org.testng.annotations.Test;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.testutil.ValidatorUtil.getBeanDescriptor;
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
	public class VersatileConstraintValidator implements ConstraintValidator<VersatileConstraint, Object[]> {
		@Override
		public void initialize(VersatileConstraint constraintAnnotation) {
		}

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
