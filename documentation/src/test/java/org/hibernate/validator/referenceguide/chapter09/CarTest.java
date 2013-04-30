package org.hibernate.validator.referenceguide.chapter09;

import java.lang.annotation.ElementType;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintTarget;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.ConstructorDescriptor;
import javax.validation.metadata.CrossParameterDescriptor;
import javax.validation.metadata.GroupConversionDescriptor;
import javax.validation.metadata.MethodDescriptor;
import javax.validation.metadata.MethodType;
import javax.validation.metadata.ParameterDescriptor;
import javax.validation.metadata.PropertyDescriptor;
import javax.validation.metadata.ReturnValueDescriptor;
import javax.validation.metadata.Scope;

import org.junit.BeforeClass;
import org.junit.Test;

import org.hibernate.validator.referenceguide.chapter09.Car.SeverityInfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CarTest {

	private static Validator validator;

	@BeforeClass
	public static void setUpValidator() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	@Test
	public void testBeanDescriptor() {
		BeanDescriptor carDescriptor = validator.getConstraintsForClass( Car.class );

		assertTrue( carDescriptor.isBeanConstrained() );

		//one class-level constraint
		assertEquals( 1, carDescriptor.getConstraintDescriptors().size() );

		//manufacturer, licensePlate, driver
		assertEquals( 3, carDescriptor.getConstrainedProperties().size() );

		//property has constraint
		assertNotNull( carDescriptor.getConstraintsForProperty( "licensePlate" ) );

		//property is marked with @Valid
		assertNotNull( carDescriptor.getConstraintsForProperty( "driver" ) );

		//constraints from getter method in interface and implementation class are returned
		assertEquals(
				2,
				carDescriptor.getConstraintsForProperty( "manufacturer" )
						.getConstraintDescriptors()
						.size()
		);

		//property is not constrained
		assertNull( carDescriptor.getConstraintsForProperty( "modelName" ) );

		//driveAway(int), load(List<Person>, List<PieceOfLuggage>)
		assertEquals( 2, carDescriptor.getConstrainedMethods( MethodType.NON_GETTER ).size() );

		//driveAway(int), getManufacturer(), getDriver(), load(List<Person>, List<PieceOfLuggage>)
		assertEquals(
				4,
				carDescriptor.getConstrainedMethods( MethodType.NON_GETTER, MethodType.GETTER )
						.size()
		);

		//driveAway(int)
		assertNotNull( carDescriptor.getConstraintsForMethod( "driveAway", int.class ) );

		//getManufacturer()
		assertNotNull( carDescriptor.getConstraintsForMethod( "getManufacturer" ) );

		//setManufacturer() is not constrained
		assertNull( carDescriptor.getConstraintsForMethod( "setManufacturer", String.class ) );

		//Car(String, String, Person, String)
		assertEquals( 1, carDescriptor.getConstrainedConstructors().size() );

		//Car(String, String, Person, String)
		assertNotNull(
				carDescriptor.getConstraintsForConstructor(
						String.class,
						String.class,
						Person.class,
						String.class
				)
		);
	}

	@Test
	public void testPropertyDescriptor() {
		BeanDescriptor carDescriptor = validator.getConstraintsForClass( Car.class );

		PropertyDescriptor licensePlateDescriptor = carDescriptor.getConstraintsForProperty(
				"licensePlate"
		);

		//"licensePlate" has two constraints, is not marked with @Valid and defines no group conversions
		assertEquals( "licensePlate", licensePlateDescriptor.getPropertyName() );
		assertEquals( 2, licensePlateDescriptor.getConstraintDescriptors().size() );
		assertTrue( licensePlateDescriptor.hasConstraints() );
		assertFalse( licensePlateDescriptor.isCascaded() );
		assertTrue( licensePlateDescriptor.getGroupConversions().isEmpty() );

		PropertyDescriptor driverDescriptor = carDescriptor.getConstraintsForProperty( "driver" );

		//"driver" has no constraints, is marked with @Valid and defines one group conversion
		assertEquals( "driver", driverDescriptor.getPropertyName() );
		assertTrue( driverDescriptor.getConstraintDescriptors().isEmpty() );
		assertFalse( driverDescriptor.hasConstraints() );
		assertTrue( driverDescriptor.isCascaded() );
		assertEquals( 1, driverDescriptor.getGroupConversions().size() );
	}

	@Test
	public void testMethodAndConstructorDescriptor() {
		BeanDescriptor carDescriptor = validator.getConstraintsForClass( Car.class );

		//driveAway(int) has a constrained parameter and an unconstrained return value
		MethodDescriptor driveAwayDescriptor = carDescriptor.getConstraintsForMethod(
				"driveAway",
				int.class
		);
		assertEquals( "driveAway", driveAwayDescriptor.getName() );
		assertTrue( driveAwayDescriptor.hasConstrainedParameters() );
		assertFalse( driveAwayDescriptor.hasConstrainedReturnValue() );

		//always returns an empty set; constraints are retrievable by navigating to
		//one of the sub-descriptors, e.g. for the return value
		assertTrue( driveAwayDescriptor.getConstraintDescriptors().isEmpty() );

		ParameterDescriptor speedDescriptor = driveAwayDescriptor.getParameterDescriptors()
				.get( 0 );

		//The "speed" parameter is located at index 0, has one constraint and is not cascaded
		//nor does it define group conversions
		assertEquals( "arg0", speedDescriptor.getName() );
		assertEquals( 0, speedDescriptor.getIndex() );
		assertEquals( 1, speedDescriptor.getConstraintDescriptors().size() );
		assertFalse( speedDescriptor.isCascaded() );
		assert speedDescriptor.getGroupConversions().isEmpty();

		//getDriver() has no constrained parameters but its return value is marked for cascaded
		//validation and declares one group conversion
		MethodDescriptor getDriverDescriptor = carDescriptor.getConstraintsForMethod(
				"getDriver"
		);
		assertFalse( getDriverDescriptor.hasConstrainedParameters() );
		assertTrue( getDriverDescriptor.hasConstrainedReturnValue() );

		ReturnValueDescriptor returnValueDescriptor = getDriverDescriptor.getReturnValueDescriptor();
		assertTrue( returnValueDescriptor.getConstraintDescriptors().isEmpty() );
		assertTrue( returnValueDescriptor.isCascaded() );
		assertEquals( 1, returnValueDescriptor.getGroupConversions().size() );

		//load(List<Person>, List<PieceOfLuggage>) has one cross-parameter constraint
		MethodDescriptor loadDescriptor = carDescriptor.getConstraintsForMethod(
				"load",
				List.class,
				List.class
		);
		assertTrue( loadDescriptor.hasConstrainedParameters() );
		assertFalse( loadDescriptor.hasConstrainedReturnValue() );
		assertEquals(
				1,
				loadDescriptor.getCrossParameterDescriptor().getConstraintDescriptors().size()
		);

		//Car(String, String, Person, String) has one constrained parameter
		ConstructorDescriptor constructorDescriptor = carDescriptor.getConstraintsForConstructor(
				String.class,
				String.class,
				Person.class,
				String.class
		);

		assertEquals( "Car", constructorDescriptor.getName() );
		assertFalse( constructorDescriptor.hasConstrainedReturnValue() );
		assertTrue( constructorDescriptor.hasConstrainedParameters() );
		assertEquals(
				1,
				constructorDescriptor.getParameterDescriptors()
						.get( 0 )
						.getConstraintDescriptors()
						.size()
		);
	}

	@Test
	public void testElementDescriptor() {
		BeanDescriptor carDescriptor = validator.getConstraintsForClass( Car.class );

		PropertyDescriptor manufacturerDescriptor = carDescriptor.getConstraintsForProperty(
				"manufacturer"
		);

		assertTrue( manufacturerDescriptor.hasConstraints() );
		assertEquals( String.class, manufacturerDescriptor.getElementClass() );

		CrossParameterDescriptor loadCrossParameterDescriptor = carDescriptor.getConstraintsForMethod(
				"load",
				List.class,
				List.class
		).getCrossParameterDescriptor();

		assertTrue( loadCrossParameterDescriptor.hasConstraints() );
		assertEquals( Object[].class, loadCrossParameterDescriptor.getElementClass() );
	}

	@Test
	public void testConstraintFinderApi() {
		BeanDescriptor carDescriptor = validator.getConstraintsForClass( Car.class );

		PropertyDescriptor manufacturerDescriptor = carDescriptor.getConstraintsForProperty(
				"manufacturer"
		);

		//"manufacturer" constraints are declared on the getter, not the field
		assertTrue(
				manufacturerDescriptor.findConstraints()
						.declaredOn( ElementType.FIELD )
						.getConstraintDescriptors()
						.isEmpty()
		);

		//@NotNull on Vehicle#getManufacturer() is part of another group
		assertEquals(
				1,
				manufacturerDescriptor.findConstraints()
						.unorderedAndMatchingGroups( Default.class )
						.getConstraintDescriptors()
						.size()
		);

		//@Size on Car#getManufacturer()
		assertEquals(
				1,
				manufacturerDescriptor.findConstraints()
						.lookingAt( Scope.LOCAL_ELEMENT )
						.getConstraintDescriptors()
						.size()
		);

		//@Size on Car#getManufacturer() and @NotNull on Vehicle#getManufacturer()
		assertEquals(
				2,
				manufacturerDescriptor.findConstraints()
						.lookingAt( Scope.HIERARCHY )
						.getConstraintDescriptors()
						.size()
		);

		//Combining several filter options
		assertEquals(
				1,
				manufacturerDescriptor.findConstraints()
						.declaredOn( ElementType.METHOD )
						.lookingAt( Scope.HIERARCHY )
						.unorderedAndMatchingGroups( Vehicle.Basic.class )
						.getConstraintDescriptors()
						.size()
		);
	}

	@Test
	public void testGroupConversionDescriptor() {
		BeanDescriptor carDescriptor = validator.getConstraintsForClass( Car.class );
		PropertyDescriptor driverDescriptor = carDescriptor.getConstraintsForProperty( "driver" );

		Set<GroupConversionDescriptor> groupConversions = driverDescriptor.getGroupConversions();
		assertEquals( 1, groupConversions.size() );

		GroupConversionDescriptor groupConversionDescriptor = groupConversions.iterator()
				.next();
		assertEquals( Default.class, groupConversionDescriptor.getFrom() );
		assertEquals( Person.Basic.class, groupConversionDescriptor.getTo() );
	}

	@Test
	public void testConstraintDescriptor() {
		BeanDescriptor carDescriptor = validator.getConstraintsForClass( Car.class );

		//descriptor for the @LuggageCountMatchesPassengerCount constraint on the
		//load(List<Person>, List<PieceOfLuggage>) method
		ConstraintDescriptor<?> constraintDescriptor = carDescriptor.getConstraintsForMethod(
				"load",
				List.class,
				List.class
		).getCrossParameterDescriptor().getConstraintDescriptors().iterator().next();

		//constraint type
		assertEquals(
				LuggageCountMatchesPassengerCount.class,
				constraintDescriptor.getAnnotation().annotationType()
		);

		//standard constraint attributes
		assertEquals( SeverityInfo.class, constraintDescriptor.getPayload().iterator().next() );
		assertEquals(
				ConstraintTarget.PARAMETERS,
				constraintDescriptor.getValidationAppliesTo()
		);
		assertEquals( Default.class, constraintDescriptor.getGroups().iterator().next() );
		assertEquals(
				"There must not be more than {piecesOfLuggagePerPassenger} pieces of luggage per " +
				"passenger.",
				constraintDescriptor.getMessageTemplate()
		);

		//custom constraint attribute
		assertEquals(
				2,
				constraintDescriptor.getAttributes().get( "piecesOfLuggagePerPassenger" )
		);

		//no composing constraints
		assertTrue( constraintDescriptor.getComposingConstraints().isEmpty() );

		//validator class
		assertEquals(
				Arrays.<Class<?>>asList( LuggageCountMatchesPassengerCount.Validator.class ),
				constraintDescriptor.getConstraintValidatorClasses()
		);
	}
}
