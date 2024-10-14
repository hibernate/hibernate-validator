/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.referenceguide.chapter10;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.metadata.BeanDescriptor;
import jakarta.validation.metadata.ConstraintDescriptor;
import jakarta.validation.metadata.ContainerElementTypeDescriptor;
import jakarta.validation.metadata.PropertyDescriptor;

import org.junit.BeforeClass;
import org.junit.Test;

public class LibraryTest {

	private static Validator validator;

	@BeforeClass
	public static void setUpValidator() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	@Test
	public void testContainerElementTypeDescriptor() {
		BeanDescriptor libraryDescriptor = validator.getConstraintsForClass( Library.class );

		//tag::testContainerElementTypeDescriptor[]
		PropertyDescriptor booksDescriptor = libraryDescriptor.getConstraintsForProperty(
				"books"
		);

		Set<ContainerElementTypeDescriptor> booksContainerElementTypeDescriptors =
				booksDescriptor.getConstrainedContainerElementTypes();
		ContainerElementTypeDescriptor booksContainerElementTypeDescriptor =
				booksContainerElementTypeDescriptors.iterator().next();

		assertTrue( booksContainerElementTypeDescriptor.hasConstraints() );
		assertTrue( booksContainerElementTypeDescriptor.isCascaded() );
		assertEquals(
				0,
				booksContainerElementTypeDescriptor.getTypeArgumentIndex().intValue()
		);
		assertEquals(
				List.class,
				booksContainerElementTypeDescriptor.getContainerClass()
		);

		Set<ConstraintDescriptor<?>> constraintDescriptors =
				booksContainerElementTypeDescriptor.getConstraintDescriptors();
		ConstraintDescriptor<?> constraintDescriptor =
				constraintDescriptors.iterator().next();

		assertEquals(
				NotNull.class,
				constraintDescriptor.getAnnotation().annotationType()
		);
		//end::testContainerElementTypeDescriptor[]
	}
}
