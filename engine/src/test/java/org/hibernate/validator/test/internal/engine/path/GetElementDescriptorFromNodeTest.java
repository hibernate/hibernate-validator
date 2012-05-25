/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.internal.engine.path;

import java.util.Iterator;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.metadata.ElementDescriptor;
import javax.validation.metadata.PropertyDescriptor;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;
import static org.testng.Assert.assertEquals;

/**
 * @author Hardy Ferentschik
 */
public class GetElementDescriptorFromNodeTest {
	private Validator validator;

	@BeforeClass
	public void setUp() {
		validator = getValidator();
	}

	@Test
	public void testValidate() {
		A a = new A();

		Set<ConstraintViolation<A>> constraintViolations = validator.validate( a );
		assertConstraintViolationPropertyValidation( constraintViolations );
	}

	@Test
	public void testValidateProperty() {
		A a = new A();

		Set<ConstraintViolation<A>> constraintViolations = validator.validateProperty( a, "a" );
		assertConstraintViolationPropertyValidation( constraintViolations );
	}

	@Test
	public void testValidateValue() {
		Set<ConstraintViolation<A>> constraintViolations = validator.validateValue( A.class, "a", null );
		assertConstraintViolationPropertyValidation( constraintViolations );
	}

	@Test
	public void testToOneCascadeValidate() {
		AWithB a = new AWithB();

		Set<ConstraintViolation<AWithB>> constraintViolations = validator.validate( a );
		assertConstraintViolationToOneValidation( constraintViolations );
	}

	@Test
	public void testToOneCascadeValidateProperty() {
		AWithB a = new AWithB();

		Set<ConstraintViolation<AWithB>> constraintViolations = validator.validateProperty( a, "b.b" );
		assertConstraintViolationToOneValidation( constraintViolations );
	}

	@Test
	public void testToOneCascadeValidateValue() {
		Set<ConstraintViolation<AWithB>> constraintViolations = validator.validateValue( AWithB.class, "b.b", null );
		assertConstraintViolationToOneValidation( constraintViolations );
	}

	private void assertConstraintViolationToOneValidation(Set<ConstraintViolation<AWithB>> constraintViolations) {
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "b.b" );

		Path path = constraintViolations.iterator().next().getPropertyPath();
		Iterator<Path.Node> nodeIterator = path.iterator();

		Path.Node node = nodeIterator.next();
		ElementDescriptor descriptor = node.getElementDescriptor();

		assertEquals( descriptor.getKind(), ElementDescriptor.Kind.PROPERTY, "unexpected descriptor type" );
		PropertyDescriptor propertyDescriptor = descriptor.as( PropertyDescriptor.class );
		assertEquals( propertyDescriptor.getElementClass(), B.class, "unexpected bean class" );

		node = nodeIterator.next();
		descriptor = node.getElementDescriptor();

		assertEquals( descriptor.getKind(), ElementDescriptor.Kind.PROPERTY, "unexpected descriptor type" );
		propertyDescriptor = descriptor.as( PropertyDescriptor.class );
		assertEquals( propertyDescriptor.getElementClass(), String.class, "unexpected bean class" );
	}

	private void assertConstraintViolationPropertyValidation(Set<ConstraintViolation<A>> constraintViolations) {
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "a" );

		Path path = constraintViolations.iterator().next().getPropertyPath();
		Iterator<Path.Node> nodeIterator = path.iterator();

		Path.Node node = nodeIterator.next();
		ElementDescriptor descriptor = node.getElementDescriptor();

		assertEquals( descriptor.getKind(), ElementDescriptor.Kind.PROPERTY, "unexpected descriptor type" );
		PropertyDescriptor propertyDescriptor = descriptor.as( PropertyDescriptor.class );
		assertEquals( propertyDescriptor.getElementClass(), String.class, "unexpected bean class" );
	}

	@SuppressWarnings("unused")
	class A {
		@NotNull
		String a;
	}

	class AWithB {
		@Valid
		B b;

		public AWithB() {
			b = new B();
		}
	}

	@SuppressWarnings("unused")
	class B {
		@NotNull
		String b;
	}
}
