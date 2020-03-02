/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.path;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import jakarta.validation.Payload;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.path.ContainerElementNode;
import org.hibernate.validator.path.PropertyNode;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 * @author Guillaume Smet
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
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( CustomConstraint.class ).withPropertyPath( pathWith().bean() )
		);

		Path path = constraintViolations.iterator().next().getPropertyPath();
		Iterator<Path.Node> nodeIterator = path.iterator();

		Path.Node node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.BEAN, "unexpected node kind" );
	}

	@Test
	public void testValidateCustomClassConstraintInCascadedValidation() {
		D d = new D();

		Set<ConstraintViolation<D>> constraintViolations = validator.validate( d );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( CustomConstraint.class )
						.withPropertyPath( pathWith()
								.property( "c" )
								.bean()
						)
		);

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

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "bs" )
								.property( "b", true, null, 0, Collection.class, 0 )
						)
		);

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

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "bs" )
								.property( "b", true, null, 0, Object[].class, null )
						)
		);

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

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "bs" )
								.property( "b", true, "b", null, Map.class, 1 )
						)
		);

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

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class )
						.withPropertyPath( pathWith()
								.property( "apartments" )
								.property( "resident", true, null, null, Set.class, 0 )
								.property( "name" )
						)
		);

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

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Max.class )
						.withPropertyPath( pathWith()
								.property( "floors" )
								.property( "number", true, null, 2, List.class, 0 )
						)
		);

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
		assertEquals( node.getIndex(), Integer.valueOf( 2 ) );

		assertFalse( nodeIterator.hasNext() );
	}

	@Test
	public void testPropertyNodeGetValueForMap() {
		Building building = new Building();
		building.managers.put( "main", new Person( "Ron" ) );
		building.managers.put( "stand-in", new Person( "Ronnie" ) );

		Set<ConstraintViolation<Building>> constraintViolations = validator.validate( building );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class )
						.withPropertyPath( pathWith()
								.property( "managers" )
								.property( "name", true, "main", null, Map.class, 1 )
						)
		);

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

	@Test
	@TestForIssue(jiraKey = "HV-1419")
	public void testContainerElementNodeGetValueForSet() {
		Validator validator = ValidatorUtil.getConfiguration()
				.addValueExtractor( new CustomContainerValueExtractor() )
				.buildValidatorFactory()
				.getValidator();

		BuildingWithContainerElementConstraints building = new BuildingWithContainerElementConstraints();
		building.apartments.add( "Apartment 123" );
		building.apartments.add( "A" );

		Set<ConstraintViolation<BuildingWithContainerElementConstraints>> constraintViolations = validator.validate( building );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class )
						.withPropertyPath( pathWith()
								.property( "apartments" )
								.containerElement( "<iterable element>", true, null, null, Set.class, 0 )
						)
		);

		Path path = constraintViolations.iterator().next().getPropertyPath();
		Iterator<Path.Node> nodeIterator = path.iterator();

		Path.Node node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.PROPERTY, "unexpected node kind" );

		node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.CONTAINER_ELEMENT, "unexpected node kind" );
		assertEquals( node.as( ContainerElementNode.class ).getValue(), "A" );

		assertFalse( nodeIterator.hasNext() );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1419")
	public void testContainerElementNodeGetValueForList() {
		Validator validator = ValidatorUtil.getConfiguration()
				.addValueExtractor( new CustomContainerValueExtractor() )
				.buildValidatorFactory()
				.getValidator();

		BuildingWithContainerElementConstraints building = new BuildingWithContainerElementConstraints();
		building.floors.add( 9 );
		building.floors.add( 10 );
		building.floors.add( 11 );

		Set<ConstraintViolation<BuildingWithContainerElementConstraints>> constraintViolations = validator.validate( building );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Max.class )
						.withPropertyPath( pathWith()
								.property( "floors" )
								.containerElement( "<list element>", true, null, 2, List.class, 0 )
						)
		);

		Path path = constraintViolations.iterator().next().getPropertyPath();
		Iterator<Path.Node> nodeIterator = path.iterator();

		Path.Node node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.PROPERTY, "unexpected node kind" );

		node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.CONTAINER_ELEMENT, "unexpected node kind" );
		assertEquals( node.as( ContainerElementNode.class ).getValue(), 11 );

		assertFalse( nodeIterator.hasNext() );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1419")
	public void testContainerElementNodeGetValueForMap() {
		Validator validator = ValidatorUtil.getConfiguration()
				.addValueExtractor( new CustomContainerValueExtractor() )
				.buildValidatorFactory()
				.getValidator();

		BuildingWithContainerElementConstraints building = new BuildingWithContainerElementConstraints();
		building.managers.put( "main", "Ron" );
		building.managers.put( "stand-in", "Ronnie" );

		Set<ConstraintViolation<BuildingWithContainerElementConstraints>> constraintViolations = validator.validate( building );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class )
						.withPropertyPath( pathWith()
								.property( "managers" )
								.containerElement( "<map value>", true, "main", null, Map.class, 1 )
						)
		);

		Path path = constraintViolations.iterator().next().getPropertyPath();
		Iterator<Path.Node> nodeIterator = path.iterator();

		Path.Node node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.PROPERTY, "unexpected node kind" );

		node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.CONTAINER_ELEMENT, "unexpected node kind" );
		assertEquals( node.as( ContainerElementNode.class ).getValue(), "Ron" );

		assertFalse( nodeIterator.hasNext() );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1419")
	public void testContainerElementNodeGetValueForCustomContainer() {
		Validator validator = ValidatorUtil.getConfiguration()
				.addValueExtractor( new CustomContainerValueExtractor() )
				.buildValidatorFactory()
				.getValidator();

		BuildingWithContainerElementConstraints building = new BuildingWithContainerElementConstraints();
		building.inhabitants.values.add( "Bo" );
		building.inhabitants.values.add( "Billy" );

		Set<ConstraintViolation<BuildingWithContainerElementConstraints>> constraintViolations = validator.validate( building );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class )
						.withPropertyPath( pathWith()
								.property( "inhabitants" )
								.containerElement( "<iterable element>", true, null, null, CustomContainer.class, 0 )
						)
		);

		Path path = constraintViolations.iterator().next().getPropertyPath();
		Iterator<Path.Node> nodeIterator = path.iterator();

		Path.Node node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.PROPERTY, "unexpected node kind" );

		node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.CONTAINER_ELEMENT, "unexpected node kind" );
		assertEquals( node.as( ContainerElementNode.class ).getValue(), "Bo" );

		assertFalse( nodeIterator.hasNext() );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1419")
	public void testContainerElementNodeGetValueForNestedContainer() {
		Validator validator = ValidatorUtil.getConfiguration()
				.addValueExtractor( new CustomContainerValueExtractor() )
				.buildValidatorFactory()
				.getValidator();

		BuildingWithContainerElementConstraints building = new BuildingWithContainerElementConstraints();

		List<String> floor1Inhabitants = Arrays.asList( "Pa", "Paul" );
		building.inhabitantsPerFloor.put( 1, floor1Inhabitants );

		Set<ConstraintViolation<BuildingWithContainerElementConstraints>> constraintViolations = validator.validate( building );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class )
						.withPropertyPath( pathWith()
								.property( "inhabitantsPerFloor" )
								.containerElement( "<map value>", true, 1, null, Map.class, 1 )
								.containerElement( "<list element>", true, null, 0, List.class, 0 )
						)
		);

		Path path = constraintViolations.iterator().next().getPropertyPath();
		Iterator<Path.Node> nodeIterator = path.iterator();

		Path.Node node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.PROPERTY, "unexpected node kind" );

		node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.CONTAINER_ELEMENT, "unexpected node kind" );
		assertEquals( node.as( ContainerElementNode.class ).getValue(), floor1Inhabitants );

		node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.CONTAINER_ELEMENT, "unexpected node kind" );
		assertEquals( node.as( ContainerElementNode.class ).getValue(), "Pa" );

		assertFalse( nodeIterator.hasNext() );
	}

	private void assertConstraintViolationToOneValidation(Set<ConstraintViolation<AWithB>> constraintViolations) {
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "b" )
								.property( "b" )
						)
		);

		Path path = constraintViolations.iterator().next().getPropertyPath();
		Iterator<Path.Node> nodeIterator = path.iterator();

		Path.Node node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.PROPERTY, "unexpected node kind" );

		node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.PROPERTY, "unexpected node kind" );
	}

	private void assertConstraintViolationPropertyValidation(Set<ConstraintViolation<A>> constraintViolations) {
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class ).withProperty( "a" )
		);

		Path path = constraintViolations.iterator().next().getPropertyPath();
		Iterator<Path.Node> nodeIterator = path.iterator();

		Path.Node node = nodeIterator.next();
		assertEquals( node.getKind(), ElementKind.PROPERTY, "unexpected node kind" );
	}

	private static class A {

		@NotNull
		private String a;
	}

	private static class AWithB {

		@Valid
		private B b;

		public AWithB() {
			b = new B();
		}
	}

	private static class AWithListOfB {

		private Collection<@Valid B> bs;

		public AWithListOfB() {
			bs = new ArrayList<B>();
			bs.add( new B() );
		}
	}

	private static class AWithArrayOfB {

		@Valid
		private B[] bs;

		public AWithArrayOfB() {
			bs = new B[1];
			bs[0] = new B();
		}
	}

	private static class AWithMapOfB {

		private Map<String, @Valid B> bs;

		public AWithMapOfB() {
			bs = new HashMap<String, B>();
			bs.put( "b", new B() );
		}
	}

	private static class B {

		@NotNull
		private String b;
	}

	@CustomConstraint
	private static class C {
	}

	private static class D {

		@Valid
		private C c;

		public D() {
			c = new C();
		}
	}

	private static class Building {

		private Set<@Valid Apartment> apartments = new HashSet<>();

		private List<@Valid Floor> floors = new ArrayList<>();

		private Map<String, @Valid Person> managers = new HashMap<>();
	}

	private class BuildingWithContainerElementConstraints {

		private Set<@Size(min = 3) String> apartments = new HashSet<>();

		private List<@Max(10) Integer> floors = new ArrayList<>();

		private Map<String, @Size(min = 5) String> managers = new HashMap<>();

		private CustomContainer<@Size(min = 3) String> inhabitants = new CustomContainer<>();

		private Map<Integer, List<@Size(min = 3) String>> inhabitantsPerFloor = new HashMap<>();
	}

	// Due to a bug in JDT and Java, this class cannot be static: if static, the @ExtractedValue annotation is not found
	// in the ValueExtractor declaration.
	private class CustomContainer<T> {

		private List<T> values = new ArrayList<>();
	}

	private static class Apartment {

		@Valid
		private Person resident;

		public Apartment(Person resident) {
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

	private static class Floor {

		@Max(10)
		private int number;

		public Floor(int number) {
			this.number = number;
		}
	}

	private static class Person {

		@Size(min = 5)
		private String name;

		public Person(String name) {
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
		public boolean isValid(Object o, ConstraintValidatorContext context) {
			return false;
		}
	}

	private static final class CustomContainerValueExtractor implements ValueExtractor<CustomContainer<@ExtractedValue ?>> {

		@Override
		public void extractValues(CustomContainer<?> originalValue, ValueExtractor.ValueReceiver receiver) {
			for ( Object element : originalValue.values ) {
				receiver.iterableValue( "<iterable element>", element );
			}
		}
	}
}
