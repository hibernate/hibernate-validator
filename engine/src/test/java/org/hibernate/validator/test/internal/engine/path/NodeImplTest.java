/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.path;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.path.PropertyNode;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;

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

	@Test
	public void testPropertyNodeGetValueForSet() {
		Building building = new Building();
		building.apartments.add( new Apartment( new Person( "Bob" ) ) );
		building.apartments.add( new Apartment( new Person( "Alice" ) ) );

		Set<ConstraintViolation<Building>> constraintViolations = validator.validate( building );

		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "apartments[].resident.name" );

		Path path = constraintViolations.iterator().next().getPropertyPath();
		Iterator<Path.Node> nodeIterator = path.iterator();

		Path.Node node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.PROPERTY, "unexpected node kind" );
		assertEquals( node.getName(), "apartments" );
		assertEquals( node.as( PropertyNode.class ).getValue(), new Apartment( new Person( "Bob" ) ) );

		node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.PROPERTY, "unexpected node kind" );
		assertEquals( node.getName(), "resident" );
		assertEquals( node.as( PropertyNode.class ).getValue(), new Person( "Bob" ) );

		node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.PROPERTY, "unexpected node kind" );
		assertEquals( node.getName(), "name" );
		assertEquals( node.as( PropertyNode.class ).getValue(), "Bob" );

		assertFalse( nodeIterator.hasNext() );
	}

	@Test
	public void testPropertyNodeGetValueForList() {
		Building building = new Building();
		building.floors.add( new Floor( 9 ) );
		building.floors.add( new Floor( 10 ) );
		building.floors.add( new Floor( 11 ) );

		Set<ConstraintViolation<Building>> constraintViolations = validator.validate( building );

		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "floors[2].number" );

		Path path = constraintViolations.iterator().next().getPropertyPath();
		Iterator<Path.Node> nodeIterator = path.iterator();

		Path.Node node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.PROPERTY, "unexpected node kind" );
		assertEquals( node.getName(), "floors" );
		assertSame( node.as( PropertyNode.class ).getValue(), building.floors.get( 2 ) );

		node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.PROPERTY, "unexpected node kind" );
		assertEquals( node.getName(), "number" );
		assertEquals( node.as( PropertyNode.class ).getValue(), 11 );
		assertEquals( node.getIndex(), Integer.valueOf ( 2 ) );

		assertFalse( nodeIterator.hasNext() );
	}

	@Test
	public void testPropertyNodeGetValueForMap() {
		Building building = new Building();
		building.managers.put( "main", new Person( "Ron" ) );
		building.managers.put( "stand-in", new Person( "Ronnie" ) );

		Set<ConstraintViolation<Building>> constraintViolations = validator.validate( building );

		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "managers[main].name" );

		Path path = constraintViolations.iterator().next().getPropertyPath();
		Iterator<Path.Node> nodeIterator = path.iterator();

		Path.Node node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.PROPERTY, "unexpected node kind" );
		assertEquals( node.getName(), "managers" );
		assertSame( node.as( PropertyNode.class ).getValue(), building.managers.get( "main" ) );

		node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.PROPERTY, "unexpected node kind" );
		assertEquals( node.getName(), "name" );
		assertEquals( node.as( PropertyNode.class ).getValue(), "Ron" );
		assertEquals( node.getKey(), "main" );

		assertFalse( nodeIterator.hasNext() );
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

	static class Building {

		@Valid
		Set<Apartment> apartments = new HashSet<Apartment>();

		@Valid
		List<Floor> floors = new ArrayList<Floor>();

		@Valid
		Map<String, Person> managers = new HashMap<String, Person>();
	}

	static class Apartment {

		@Valid
		Person resident;

		Apartment(Person resident) {
			this.resident = resident;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ( ( resident == null ) ? 0 : resident.hashCode() );
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if ( this == obj ) {
				return true;
			}
			if ( obj == null ) {
				return false;
			}
			if ( getClass() != obj.getClass() ) {
				return false;
			}
			Apartment other = (Apartment) obj;
			if ( resident == null ) {
				if ( other.resident != null ) {
					return false;
				}
			}
			else if ( !resident.equals( other.resident ) ) {
				return false;
			}
			return true;
		}
	}

	static class Floor {

		@Max(10)
		int number;

		public Floor(int number) {
			this.number = number;
		}
	}

	static class Person {

		@Size(min=5)
		String name;

		Person(String name) {
			this.name = name;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if ( this == obj ) {
				return true;
			}
			if ( obj == null ) {
				return false;
			}
			if ( getClass() != obj.getClass() ) {
				return false;
			}
			Person other = (Person) obj;
			if ( name == null ) {
				if ( other.name != null ) {
					return false;
				}
			}
			else if ( !name.equals( other.name ) ) {
				return false;
			}
			return true;
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
