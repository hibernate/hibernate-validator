/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.internal.metadata.provider;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.metadata.ConstraintDescriptor;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.constraints.ScriptAssert;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.provider.AnnotationMetaDataProvider;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;
import org.hibernate.validator.testutil.TestForIssue;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertIterableSize;
import static org.testng.Assert.assertEquals;

/**
 * Unit test for {@link AnnotationMetaDataProvider}.
 *
 * @author Gunnar Morling
 */
public class AnnotationMetaDataProviderTest {

	private AnnotationMetaDataProvider provider;

	@BeforeMethod
	public void setUpProvider() {
		provider = new AnnotationMetaDataProvider(
				new ConstraintHelper(),
				new AnnotationProcessingOptions()
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-626")
	public void onlyLocallyDefinedConstraintsAreConsidered() {

		List<BeanConfiguration<? super Person>> beanConfigurations = provider.getBeanConfigurationForHierarchy( Person.class );

		ConstrainedType personType = findConstrainedType( beanConfigurations, Person.class );
		assertIterableSize( personType.getConstraints(), 1 );
		ConstraintDescriptor<?> constraintInSubType = personType.getConstraints()
				.iterator()
				.next()
				.getDescriptor();
		assertEquals( constraintInSubType.getAnnotation().annotationType(), ScriptAssert.class );

		ConstrainedType personBaseType = findConstrainedType( beanConfigurations, PersonBase.class );
		assertIterableSize( personBaseType.getConstraints(), 1 );

		ConstraintDescriptor<?> constraintInSuperType = personBaseType.getConstraints()
				.iterator()
				.next()
				.getDescriptor();
		assertEquals( constraintInSuperType.getAnnotation().annotationType(), ClassLevelConstraint.class );
	}

	private <T> ConstrainedType findConstrainedType(Iterable<BeanConfiguration<? super T>> beanConfigurations, Class<? super T> type) {
		for ( BeanConfiguration<?> oneConfiguration : beanConfigurations ) {
			for ( ConstrainedElement constrainedElement : oneConfiguration.getConstrainedElements() ) {
				if ( constrainedElement.getLocation().getElementType() == ElementType.TYPE ) {
					ConstrainedType constrainedType = (ConstrainedType) constrainedElement;
					if ( constrainedType.getLocation().getBeanClass().equals( type ) ) {
						return constrainedType;
					}
				}
			}
		}

		throw new RuntimeException( "Found no constrained element for type " + type );
	}

	@ClassLevelConstraint("some script")
	private static class PersonBase {
	}

	@ScriptAssert(lang = "javascript", script = "some script")
	private static class Person extends PersonBase {
	}

	@Target({ TYPE })
	@Retention(RUNTIME)
	@Constraint(validatedBy = { })
	@Documented
	@Inherited
	public @interface ClassLevelConstraint {

		String message() default "{ClassLevelConstraint.message}";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };

		String value();
	}
}
