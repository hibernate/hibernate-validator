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

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.ElementKind;
import javax.validation.Path;
import javax.validation.Payload;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;
import static org.testng.Assert.assertEquals;

/**
 * @author Hardy Ferentschik
 */
public class NodeImplTest {
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

	@Test
	public void testValidateCustomClassConstraint() {
		C c = new C();

		Set<ConstraintViolation<C>> constraintViolations = validator.validate( c );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "" );

		Path path = constraintViolations.iterator().next().getPropertyPath();
		Iterator<Path.Node> nodeIterator = path.iterator();

		Path.Node node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.BEAN, "unexpected node kind" );
	}

	@Test
	public void testValidateCustomClassConstraintInCascadedValidation() {
		D d = new D();

		Set<ConstraintViolation<D>> constraintViolations = validator.validate( d );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "c" );

		Path path = constraintViolations.iterator().next().getPropertyPath();
		Iterator<Path.Node> nodeIterator = path.iterator();

		Path.Node node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.PROPERTY, "unexpected node kind" );

		node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.BEAN, "unexpected node kind" );
	}

	@Test
	//TODO HV-571: Add tests where runtime type differs between elements in cascaded list
	public void testToManyCascadeValidate() {
		AWithListOfB a = new AWithListOfB();

		Set<ConstraintViolation<AWithListOfB>> constraintViolations = validator.validate( a );

		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "bs[0].b" );

		Path path = constraintViolations.iterator().next().getPropertyPath();
		Iterator<Path.Node> nodeIterator = path.iterator();

		Path.Node node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.PROPERTY, "unexpected node kind" );

		node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.PROPERTY, "unexpected node kind" );
	}

	@Test
	public void testToArrayCascadeValidate() {
		AWithArrayOfB a = new AWithArrayOfB();

		Set<ConstraintViolation<AWithArrayOfB>> constraintViolations = validator.validate( a );

		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "bs[0].b" );

		Path path = constraintViolations.iterator().next().getPropertyPath();
		Iterator<Path.Node> nodeIterator = path.iterator();

		Path.Node node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.PROPERTY, "unexpected node kind" );

		node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.PROPERTY, "unexpected node kind" );
	}

	@Test
	public void testToMapCascadeValidate() {
		AWithMapOfB a = new AWithMapOfB();

		Set<ConstraintViolation<AWithMapOfB>> constraintViolations = validator.validate( a );

		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "bs[b].b" );

		Path path = constraintViolations.iterator().next().getPropertyPath();
		Iterator<Path.Node> nodeIterator = path.iterator();

		Path.Node node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.PROPERTY, "unexpected node kind" );

		node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.PROPERTY, "unexpected node kind" );
	}

	private void assertConstraintViolationToOneValidation(Set<ConstraintViolation<AWithB>> constraintViolations) {
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "b.b" );

		Path path = constraintViolations.iterator().next().getPropertyPath();
		Iterator<Path.Node> nodeIterator = path.iterator();

		Path.Node node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.PROPERTY, "unexpected node kind" );

		node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.PROPERTY, "unexpected node kind" );
	}

	private void assertConstraintViolationPropertyValidation(Set<ConstraintViolation<A>> constraintViolations) {
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "a" );

		Path path = constraintViolations.iterator().next().getPropertyPath();
		Iterator<Path.Node> nodeIterator = path.iterator();

		Path.Node node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.PROPERTY, "unexpected node kind" );
	}

	static class A {
		@NotNull
		String a;
	}

	static class AWithB {
		@Valid
		B b;

		public AWithB() {
			b = new B();
		}
	}

	static class AWithListOfB {
		@Valid
		Collection<B> bs;

		public AWithListOfB() {
			bs = new ArrayList<B>();
			bs.add( new B() );
		}
	}

	static class AWithArrayOfB {
		@Valid
		B[] bs;

		public AWithArrayOfB() {
			bs = new B[1];
			bs[0] = new B();
		}
	}

	static class AWithMapOfB {
		@Valid
		Map<String, B> bs;

		public AWithMapOfB() {
			bs = new HashMap<String, B>();
			bs.put( "b", new B() );
		}
	}

	static class B {
		@NotNull
		String b;
	}

	@CustomConstraint
	static class C {
	}

	static class D {
		@Valid
		C c;

		public D() {
			c = new C();
		}
	}

	@Target({ TYPE })
	@Retention(RUNTIME)
	@Constraint(validatedBy = { CustomConstraintValidator.class })
	public @interface CustomConstraint {
		String message() default "custom constraint";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	public static class CustomConstraintValidator implements ConstraintValidator<CustomConstraint, Object> {
		@Override
		public void initialize(CustomConstraint constraintAnnotation) {
		}

		@Override
		public boolean isValid(Object o, ConstraintValidatorContext context) {
			return false;
		}
	}
}
