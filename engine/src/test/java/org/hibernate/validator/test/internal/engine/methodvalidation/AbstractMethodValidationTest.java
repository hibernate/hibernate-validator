/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidatingProxy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import jakarta.validation.Path.ParameterNode;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.test.internal.engine.methodvalidation.model.Address;
import org.hibernate.validator.test.internal.engine.methodvalidation.model.Customer;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.ConsistentDateParameters;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepository;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryImpl;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryWithConstrainedVoidMethod;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryWithConstrainedVoidMethodImpl;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.RepositoryBase;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * Integration test for the method-level validation related features of {@link org.hibernate.validator.internal.engine.ValidatorImpl}.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
@Test
public abstract class AbstractMethodValidationTest {
	protected CustomerRepository customerRepositoryOriginalBean;
	protected CustomerRepository customerRepositoryValidatingProxy;
	protected RepositoryBase<Customer> repositoryBase;
	protected Validator validator;

	protected abstract void setUp();

	protected abstract String messagePrefix();

	protected void createProxy(Class<?>... groups) {
		customerRepositoryOriginalBean = new CustomerRepositoryImpl();
		customerRepositoryValidatingProxy = getValidatingProxy(
				customerRepositoryOriginalBean, validator, groups
		);
		repositoryBase = customerRepositoryValidatingProxy;
	}

	@Test
	public void methodValidationYieldsConstraintViolation() {
		try {
			customerRepositoryValidatingProxy.findCustomerByName( null );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch (ConstraintViolationException e) {

			Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( NotNull.class )
							.withPropertyPath( pathWith()
									.method( "findCustomerByName" )
									.parameter( "name", 0 )
							)
							.withMessage( messagePrefix() + "must not be null" )
							.withRootBeanClass( CustomerRepositoryImpl.class )
							.withInvalidValue( null )
			);

			ConstraintViolation<?> constraintViolation = constraintViolations.iterator().next();

			assertMethod( constraintViolation, "findCustomerByName", String.class );
			assertParameterIndex( constraintViolation, 0 );
			assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
			assertEquals( constraintViolation.getRootBean(), customerRepositoryOriginalBean );
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"findCustomerByName.name"
			);
			assertEquals( constraintViolation.getLeafBean(), customerRepositoryOriginalBean );
			assertEquals( constraintViolation.getInvalidValue(), null );
			assertEquals( constraintViolation.getExecutableParameters(), new Object[] { null } );
			assertEquals( constraintViolation.getExecutableReturnValue(), null );
		}
	}

	@Test
	public void validationOfMethodWithMultipleParameters() {
		try {
			customerRepositoryValidatingProxy.findCustomerByAgeAndName( 30, null );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( NotNull.class )
							.withPropertyPath( pathWith()
									.method( "findCustomerByAgeAndName" )
									.parameter( "name", 1 )
							)
							.withMessage( messagePrefix() + "must not be null" )
							.withRootBeanClass( CustomerRepositoryImpl.class )
							.withInvalidValue( null )
			);

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "must not be null" );
			assertMethod( constraintViolation, "findCustomerByAgeAndName", Integer.class, String.class );
			assertParameterIndex( constraintViolation, 1 );
			assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"findCustomerByAgeAndName.name"
			);
			assertEquals( constraintViolation.getExecutableParameters(), new Object[] { 30, null } );
			assertEquals( constraintViolation.getExecutableReturnValue(), null );
		}
	}

	@Test
	public void constraintViolationsAtMultipleParameters() {
		try {
			customerRepositoryValidatingProxy.findCustomerByAgeAndName( 1, null );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( NotNull.class )
							.withPropertyPath( pathWith()
									.method( "findCustomerByAgeAndName" )
									.parameter( "name", 1 )
							)
							.withMessage( messagePrefix() + "must not be null" )
							.withRootBeanClass( CustomerRepositoryImpl.class )
							.withInvalidValue( null ),
					violationOf( Min.class )
							.withPropertyPath( pathWith()
									.method( "findCustomerByAgeAndName" )
									.parameter( "age", 0 )
							)
							.withMessage( messagePrefix() + "must be greater than or equal to 5" )
							.withRootBeanClass( CustomerRepositoryImpl.class )
							.withInvalidValue( 1 )
			);
		}
	}

	@Test
	public void methodValidationWithCascadingParameter() {
		Customer customer = new Customer( null, null );
		try {
			customerRepositoryValidatingProxy.persistCustomer( customer );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( NotNull.class )
							.withPropertyPath( pathWith()
									.method( "persistCustomer" )
									.parameter( "customer", 0 )
									.property( "name" )
							)
							.withMessage( messagePrefix() + "must not be null" )
							.withRootBeanClass( CustomerRepositoryImpl.class )
							.withInvalidValue( null )
			);

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "must not be null" );
			assertMethod( constraintViolation, "persistCustomer", Customer.class );
			assertParameterIndex( constraintViolation, 0 );
			assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
			assertEquals(
					constraintViolation.getPropertyPath().toString(), "persistCustomer.customer.name"
			);
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getRootBean(), customerRepositoryOriginalBean );
			assertEquals( constraintViolation.getLeafBean(), customer );
			assertEquals( constraintViolation.getInvalidValue(), null );
			assertEquals( constraintViolation.getExecutableParameters(), new Object[] { customer } );
			assertEquals( constraintViolation.getExecutableReturnValue(), null );
		}
	}

	@Test
	public void methodValidationWithCascadingParameterAndCascadingConstraint() {
		Address address = new Address( null );
		Customer customer = new Customer( "Bob", address );

		try {
			customerRepositoryValidatingProxy.persistCustomer( customer );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( NotNull.class )
							.withPropertyPath( pathWith()
									.method( "persistCustomer" )
									.parameter( "customer", 0 )
									.property( "address" )
									.property( "city" )
							)
							.withMessage( messagePrefix() + "must not be null" )
							.withRootBeanClass( CustomerRepositoryImpl.class )
							.withInvalidValue( null )
			);

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "must not be null" );
			assertMethod( constraintViolation, "persistCustomer", Customer.class );
			assertParameterIndex( constraintViolation, 0 );
			assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"persistCustomer.customer.address.city"
			);
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getRootBean(), customerRepositoryOriginalBean );
			assertEquals( constraintViolation.getLeafBean(), address );
			assertEquals( constraintViolation.getInvalidValue(), null );
			assertEquals( constraintViolation.getExecutableParameters(), new Object[] { customer } );
			assertEquals( constraintViolation.getExecutableReturnValue(), null );
		}
	}

	@Test
	public void cascadingMapParameter() {
		Map<String, Customer> customers = newHashMap();
		Customer bob = new Customer( null );
		customers.put( "Bob", bob );

		try {
			customerRepositoryValidatingProxy.cascadingMapParameter( customers );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( NotNull.class )
							.withPropertyPath( pathWith()
									.method( "cascadingMapParameter" )
									.parameter( "customer", 0 )
									.property( "name", true, "Bob", null, Map.class, 1 )
							)
							.withMessage( messagePrefix() + "must not be null" )
							.withRootBeanClass( CustomerRepositoryImpl.class )
							.withInvalidValue( null )
			);

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "must not be null" );
			assertMethod( constraintViolation, "cascadingMapParameter", Map.class );
			assertParameterIndex( constraintViolation, 0 );
			assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"cascadingMapParameter.customer[Bob].name"
			);
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getRootBean(), customerRepositoryOriginalBean );
			assertEquals( constraintViolation.getLeafBean(), bob );
			assertEquals( constraintViolation.getInvalidValue(), null );
			assertEquals( constraintViolation.getExecutableParameters(), new Object[] { customers } );
			assertEquals( constraintViolation.getExecutableReturnValue(), null );
		}
	}

	@Test
	public void cascadingIterableParameter() {
		Customer customer = new Customer( null );
		List<Customer> customers = Arrays.asList( null, customer );

		try {
			customerRepositoryValidatingProxy.cascadingIterableParameter( customers );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( NotNull.class )
							.withPropertyPath( pathWith()
									.method( "cascadingIterableParameter" )
									.parameter( "customer", 0 )
									.property( "name", true, null, 1, List.class, 0 )
							)
							.withMessage( messagePrefix() + "must not be null" )
							.withRootBeanClass( CustomerRepositoryImpl.class )
							.withInvalidValue( null )
			);

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "must not be null" );
			assertMethod( constraintViolation, "cascadingIterableParameter", List.class );
			assertParameterIndex( constraintViolation, 0 );
			assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"cascadingIterableParameter.customer[1].name"
			);
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getRootBean(), customerRepositoryOriginalBean );
			assertEquals( constraintViolation.getLeafBean(), customer );
			assertEquals( constraintViolation.getInvalidValue(), null );
			assertEquals( constraintViolation.getExecutableParameters(), new Object[] { customers } );
			assertEquals( constraintViolation.getExecutableReturnValue(), null );
		}
	}

	// HV-1428 Container element support is disabled for arrays
	@Test(enabled = false)
	public void cascadingArrayParameter() {
		Customer customer = new Customer( null );

		try {
			customerRepositoryValidatingProxy.cascadingArrayParameter( null, customer );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( NotNull.class )
							.withPropertyPath( pathWith()
									.method( "cascadingArrayParameter" )
									.parameter( "customer", 0 )
									.property( "name", true, null, 1, Object[].class, null )
							)
							.withMessage( messagePrefix() + "must not be null" )
							.withRootBeanClass( CustomerRepositoryImpl.class )
							.withInvalidValue( null )
			);

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "must not be null" );
			assertMethod( constraintViolation, "cascadingArrayParameter", Customer[].class );
			assertParameterIndex( constraintViolation, 0 );
			assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"cascadingArrayParameter.customer[1].name"
			);
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getRootBean(), customerRepositoryOriginalBean );
			assertEquals( constraintViolation.getLeafBean(), customer );
			assertEquals( constraintViolation.getInvalidValue(), null );
			assertEquals(
					constraintViolation.getExecutableParameters(),
					new Object[] { new Object[] { null, customer } }
			);
			assertEquals( constraintViolation.getExecutableReturnValue(), null );
		}
	}

	@Test
	public void constraintsAtMethodFromBaseClassAreEvaluated() {
		try {
			customerRepositoryValidatingProxy.findById( null );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( NotNull.class )
							.withPropertyPath( pathWith()
									.method( "findById" )
									.parameter( "id", 0 )
							)
							.withMessage( messagePrefix() + "must not be null" )
							.withRootBeanClass( CustomerRepositoryImpl.class )
							.withInvalidValue( null )
			);

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "must not be null" );
			assertMethod( constraintViolation, "findById", Long.class );
			assertParameterIndex( constraintViolation, 0 );
			assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
		}
	}

	@Test
	public void constraintsAtOverriddenMethodAreEvaluated() {
		try {
			customerRepositoryValidatingProxy.foo( null );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( NotNull.class )
							.withPropertyPath( pathWith()
									.method( "foo" )
									.parameter( "id", 0 )
							)
							.withMessage( messagePrefix() + "must not be null" )
							.withRootBeanClass( CustomerRepositoryImpl.class )
							.withInvalidValue( null )
			);

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "must not be null" );
			assertMethod( constraintViolation, "foo", Long.class );
			assertParameterIndex( constraintViolation, 0 );
			assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
		}
	}

	@Test
	public void validFromOverriddenMethodIsEvaluated() {
		try {
			customerRepositoryValidatingProxy.bar( new Customer( null, null ) );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( NotNull.class )
							.withPropertyPath( pathWith()
									.method( "bar" )
									.parameter( "customer", 0 )
									.property( "name" )
							)
							.withMessage( messagePrefix() + "must not be null" )
							.withRootBeanClass( CustomerRepositoryImpl.class )
							.withInvalidValue( null )
			);

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "must not be null" );
			assertMethod( constraintViolation, "bar", Customer.class );
			assertParameterIndex( constraintViolation, 0 );
			assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getPropertyPath().toString(), "bar.customer.name" );
		}
	}

	@Test
	public void parameterValidationOfParameterlessMethod() {
		customerRepositoryValidatingProxy.boz();
	}

	@Test
	public void returnValueValidationYieldsConstraintViolation() {
		try {
			customerRepositoryValidatingProxy.baz();
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( Min.class )
							.withPropertyPath( pathWith()
									.method( "baz" )
									.returnValue()
							)
							.withMessage( messagePrefix() + "must be greater than or equal to 10" )
							.withRootBeanClass( CustomerRepositoryImpl.class )
							.withInvalidValue( 9 )
			);

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "must be greater than or equal to 10" );
			assertMethod( constraintViolation, "baz" );
			assertMethodValidationType( constraintViolation, ElementKind.RETURN_VALUE );
			assertEquals( constraintViolation.getRootBean(), customerRepositoryOriginalBean );
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getPropertyPath().toString(), "baz.<return value>" );
			assertEquals( constraintViolation.getLeafBean(), customerRepositoryOriginalBean );
			assertEquals( constraintViolation.getInvalidValue(), 9 );
			assertEquals( constraintViolation.getExecutableParameters(), null );
			assertEquals( constraintViolation.getExecutableReturnValue(), 9 );
		}
	}

	@Test
	public void cascadingReturnValue() {
		try {
			customerRepositoryValidatingProxy.cascadingReturnValue();
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( NotNull.class )
							.withPropertyPath( pathWith()
									.method( "cascadingReturnValue" )
									.returnValue()
									.property( "name" )
							)
							.withMessage( messagePrefix() + "must not be null" )
							.withRootBeanClass( CustomerRepositoryImpl.class )
			);

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "must not be null" );
			assertMethod( constraintViolation, "cascadingReturnValue" );
			assertMethodValidationType( constraintViolation, ElementKind.RETURN_VALUE );
			assertEquals( constraintViolation.getRootBean(), customerRepositoryOriginalBean );
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"cascadingReturnValue.<return value>.name"
			);
			assertEquals( constraintViolation.getLeafBean().getClass(), Customer.class );
			assertEquals( constraintViolation.getInvalidValue(), null );
			assertEquals( constraintViolation.getExecutableParameters(), null );
			assertEquals( constraintViolation.getExecutableReturnValue(), new Customer( null ) );
		}
	}

	@Test
	public void cascadingReturnValueFromSuperType() {
		try {
			customerRepositoryValidatingProxy.overriddenMethodWithCascadingReturnValue();
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( NotNull.class )
							.withPropertyPath( pathWith()
									.method( "overriddenMethodWithCascadingReturnValue" )
									.returnValue()
									.property( "name" )
							)
							.withMessage( messagePrefix() + "must not be null" )
							.withRootBeanClass( CustomerRepositoryImpl.class )
			);

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "must not be null" );
			assertMethod( constraintViolation, "overriddenMethodWithCascadingReturnValue" );
			assertMethodValidationType( constraintViolation, ElementKind.RETURN_VALUE );

			assertEquals( constraintViolation.getRootBean(), customerRepositoryOriginalBean );
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"overriddenMethodWithCascadingReturnValue.<return value>.name"
			);
			assertEquals( constraintViolation.getLeafBean().getClass(), Customer.class );
			assertEquals( constraintViolation.getInvalidValue(), null );
			assertEquals( constraintViolation.getExecutableParameters(), null );
			assertEquals( constraintViolation.getExecutableReturnValue(), new Customer( null ) );
		}
	}

	@Test
	public void cascadingIterableReturnValue() {
		try {
			customerRepositoryValidatingProxy.cascadingIterableReturnValue();
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( NotNull.class )
							.withPropertyPath( pathWith()
									.method( "cascadingIterableReturnValue" )
									.returnValue()
									.property( "name", true, null, 1, List.class, 0 )
							)
							.withMessage( messagePrefix() + "must not be null" )
							.withRootBeanClass( CustomerRepositoryImpl.class )
							.withInvalidValue( null )
			);

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "must not be null" );
			assertMethod( constraintViolation, "cascadingIterableReturnValue" );
			assertMethodValidationType( constraintViolation, ElementKind.RETURN_VALUE );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"cascadingIterableReturnValue.<return value>[1].name"
			);
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getRootBean(), customerRepositoryOriginalBean );
			assertEquals( constraintViolation.getLeafBean(), new Customer( null ) );
			assertEquals( constraintViolation.getInvalidValue(), null );
			assertEquals( constraintViolation.getExecutableParameters(), null );
			assertEquals( constraintViolation.getExecutableReturnValue(), Arrays.asList( null, new Customer( null ) ) );
		}
	}

	@Test
	public void cascadingMapReturnValue() {
		try {
			customerRepositoryValidatingProxy.cascadingMapReturnValue();
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			Customer customer = new Customer( null );
			Map<String, Customer> expectedReturnValue = newHashMap();
			expectedReturnValue.put( "Bob", customer );

			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( NotNull.class )
							.withPropertyPath( pathWith()
									.method( "cascadingMapReturnValue" )
									.returnValue()
									.property( "name", true, "Bob", null, Map.class, 1 )
							)
							.withMessage( messagePrefix() + "must not be null" )
							.withRootBeanClass( CustomerRepositoryImpl.class )
							.withInvalidValue( null )
			);

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "must not be null" );
			assertMethod( constraintViolation, "cascadingMapReturnValue" );
			assertMethodValidationType( constraintViolation, ElementKind.RETURN_VALUE );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"cascadingMapReturnValue.<return value>[Bob].name"
			);
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getRootBean(), customerRepositoryOriginalBean );
			assertEquals( constraintViolation.getLeafBean(), customer );
			assertEquals( constraintViolation.getInvalidValue(), null );
			assertEquals( constraintViolation.getExecutableParameters(), null );
			assertEquals( constraintViolation.getExecutableReturnValue(), expectedReturnValue );
		}
	}

	@Test
	public void cascadingArrayReturnValue() {
		try {
			customerRepositoryValidatingProxy.cascadingArrayReturnValue();
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( NotNull.class )
							.withPropertyPath( pathWith()
									.method( "cascadingArrayReturnValue" )
									.returnValue()
									.property( "name", true, null, 1, Object[].class, null )
							)
							.withMessage( messagePrefix() + "must not be null" )
							.withRootBeanClass( CustomerRepositoryImpl.class )
							.withInvalidValue( null )
			);

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "must not be null" );
			assertMethod( constraintViolation, "cascadingArrayReturnValue" );
			assertMethodValidationType( constraintViolation, ElementKind.RETURN_VALUE );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"cascadingArrayReturnValue.<return value>[1].name"
			);
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getRootBean(), customerRepositoryOriginalBean );
			assertEquals( constraintViolation.getLeafBean(), new Customer( null ) );
			assertEquals( constraintViolation.getInvalidValue(), null );
			assertEquals( constraintViolation.getExecutableParameters(), null );
			assertEquals( constraintViolation.getExecutableReturnValue(), new Object[] { null, new Customer( null ) } );
		}
	}

	@Test
	public void overridingMethodStrengthensReturnValueConstraint() {
		try {
			customerRepositoryValidatingProxy.overriddenMethodWithReturnValueConstraint();
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( Min.class )
							.withPropertyPath( pathWith()
									.method( "overriddenMethodWithReturnValueConstraint" )
									.returnValue()
							)
							.withMessage( messagePrefix() + "must be greater than or equal to 5" )
							.withRootBeanClass( CustomerRepositoryImpl.class )
							.withInvalidValue( 3 ),
					violationOf( Min.class )
							.withPropertyPath( pathWith()
									.method( "overriddenMethodWithReturnValueConstraint" )
									.returnValue()
							)
							.withMessage( messagePrefix() + "must be greater than or equal to 10" )
							.withRootBeanClass( CustomerRepositoryImpl.class )
							.withInvalidValue( 3 )
			);
		}
	}

	@Test
	public void runtimeTypeDefinesConstraintsToApply() {
		try {
			repositoryBase.overriddenMethodWithReturnValueConstraint();
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( Min.class )
							.withPropertyPath( pathWith()
									.method( "overriddenMethodWithReturnValueConstraint" )
									.returnValue()
							)
							.withMessage( messagePrefix() + "must be greater than or equal to 5" )
							.withRootBeanClass( CustomerRepositoryImpl.class )
							.withInvalidValue( 3 ),
					violationOf( Min.class )
							.withPropertyPath( pathWith()
									.method( "overriddenMethodWithReturnValueConstraint" )
									.returnValue()
							)
							.withMessage( messagePrefix() + "must be greater than or equal to 10" )
							.withRootBeanClass( CustomerRepositoryImpl.class )
							.withInvalidValue( 3 )
			);
		}
	}

	@Test
	public void methodValidationSucceedsAsNoConstraintOfValidatedGroupAreViolated() {
		customerRepositoryValidatingProxy.parameterConstraintInGroup( null );
	}

	@Test(expectedExceptions = ConstraintViolationException.class)
	public void methodValidationFailsAsConstraintOfValidatedGroupIsViolated() {
		createProxy( CustomerRepository.ValidationGroup.class );
		customerRepositoryValidatingProxy.parameterConstraintInGroup( null );
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000132.*")
	public void voidMethodWithReturnValueConstraintCausesConstraintDeclarationException() {
		CustomerRepositoryWithConstrainedVoidMethod customerRepository = getValidatingProxy(
				new CustomerRepositoryWithConstrainedVoidMethodImpl(), validator
		);

		customerRepository.voidMethodWithIllegalReturnValueConstraint();
	}

	@TestForIssue(jiraKey = "HV-601")
	@Test(expectedExceptions = ConstraintViolationException.class)
	public void shouldValidateGetterLikeNamedMethodWithParameter() {
		customerRepositoryValidatingProxy.getFoo( "" );
	}

	@Test
	public void validationOfCrossParameterConstraint() {
		//given
		LocalDate startDate = LocalDate.of( 2012, 11, 5 );
		LocalDate endDate = LocalDate.of( 2012, 11, 4 );

		try {
			//when
			customerRepositoryValidatingProxy.methodWithCrossParameterConstraint( startDate, endDate );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch (ConstraintViolationException e) {
			//then
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( ConsistentDateParameters.class )
							.withPropertyPath( pathWith()
									.method( "methodWithCrossParameterConstraint" )
									.crossParameter()
							)
							.withMessage( messagePrefix() + "{ConsistentDateParameters.message}" )
							.withRootBeanClass( CustomerRepositoryImpl.class )
			);
			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getConstraintDescriptor().getAnnotation().annotationType(), ConsistentDateParameters.class );
			assertEquals( constraintViolation.getInvalidValue(), new Object[] { startDate, endDate } );
			assertEquals( constraintViolation.getLeafBean(), customerRepositoryOriginalBean );
			assertEquals( constraintViolation.getRootBean(), customerRepositoryOriginalBean );
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getExecutableParameters(), new Object[] { startDate, endDate } );
			assertEquals( constraintViolation.getExecutableReturnValue(), null );

			assertMethod(
					constraintViolation,
					"methodWithCrossParameterConstraint",
					LocalDate.class,
					LocalDate.class
			);
		}
	}

	@Test
	public void methodValidationSucceeds() {
		customerRepositoryValidatingProxy.findCustomerByName( "Bob" );
	}

	protected void assertMethod(ConstraintViolation<?> constraintViolation, String methodName, Class<?>... parameterTypes) {
		Iterator<Path.Node> nodeIterator = constraintViolation.getPropertyPath().iterator();

		Path.Node node = nodeIterator.next();
		assertNotNull( node );
		assertEquals( node.getName(), methodName );
		assertEquals( node.getKind(), ElementKind.METHOD );
		assertEquals( node.as( Path.MethodNode.class ).getParameterTypes(), Arrays.asList( parameterTypes ) );
	}

	protected void assertParameterIndex(ConstraintViolation<?> constraintViolation, Integer index) {
		Iterator<Path.Node> nodeIterator = constraintViolation.getPropertyPath().iterator();

		// first node is method descriptor
		nodeIterator.next();
		Path.Node node = nodeIterator.next();
		ParameterNode parameterNode = node.as( ParameterNode.class );
		assertEquals( parameterNode.getParameterIndex(), index.intValue() );
	}

	protected void assertMethodValidationType(ConstraintViolation<?> constraintViolation, ElementKind kind) {
		Iterator<Path.Node> nodeIterator = constraintViolation.getPropertyPath().iterator();

		// first node is method descriptor
		nodeIterator.next();
		Path.Node node = nodeIterator.next();
		assertEquals( node.getKind(), kind );
	}
}
