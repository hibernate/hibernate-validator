/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.methodvalidation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidatingProxy;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Integration test for the method-level validation related features of {@link org.hibernate.validator.internal.engine.ValidatorImpl}.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
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
		assertThatThrownBy( () -> customerRepositoryValidatingProxy.findCustomerByName( null ) )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( exception -> {
					ConstraintViolationException e = (ConstraintViolationException) exception;

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
					assertEquals( customerRepositoryOriginalBean, constraintViolation.getRootBean() );
					assertEquals( CustomerRepositoryImpl.class, constraintViolation.getRootBeanClass() );
					assertEquals(
							"findCustomerByName.name",
							constraintViolation.getPropertyPath().toString() );
					assertEquals( customerRepositoryOriginalBean, constraintViolation.getLeafBean() );
					assertNull( constraintViolation.getInvalidValue() );
					assertArrayEquals( new Object[] { null }, constraintViolation.getExecutableParameters() );
					assertNull( constraintViolation.getExecutableReturnValue() );
				} );
	}

	@Test
	public void validationOfMethodWithMultipleParameters() {
		assertThatThrownBy( () -> customerRepositoryValidatingProxy.findCustomerByAgeAndName( 30, null ) )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( exception -> {
					ConstraintViolationException e = (ConstraintViolationException) exception;
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
					assertEquals( messagePrefix() + "must not be null", constraintViolation.getMessage() );
					assertMethod( constraintViolation, "findCustomerByAgeAndName", Integer.class, String.class );
					assertParameterIndex( constraintViolation, 1 );
					assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
					assertEquals( CustomerRepositoryImpl.class, constraintViolation.getRootBeanClass() );
					assertEquals(
							"findCustomerByAgeAndName.name",
							constraintViolation.getPropertyPath().toString() );
					assertArrayEquals( new Object[] { 30, null }, constraintViolation.getExecutableParameters() );
					assertNull( constraintViolation.getExecutableReturnValue() );
				} );
	}

	@Test
	public void constraintViolationsAtMultipleParameters() {
		assertThatThrownBy( () -> customerRepositoryValidatingProxy.findCustomerByAgeAndName( 1, null ) )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( exception -> {
					ConstraintViolationException e = (ConstraintViolationException) exception;
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
				} );
	}

	@Test
	public void methodValidationWithCascadingParameter() {
		Customer customer = new Customer( null, null );
		assertThatThrownBy( () -> customerRepositoryValidatingProxy.persistCustomer( customer ) )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( exception -> {
					ConstraintViolationException e = (ConstraintViolationException) exception;
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
					assertEquals( messagePrefix() + "must not be null", constraintViolation.getMessage() );
					assertMethod( constraintViolation, "persistCustomer", Customer.class );
					assertParameterIndex( constraintViolation, 0 );
					assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
					assertEquals( "persistCustomer.customer.name",
							constraintViolation.getPropertyPath().toString() );
					assertEquals( CustomerRepositoryImpl.class, constraintViolation.getRootBeanClass() );
					assertEquals( customerRepositoryOriginalBean, constraintViolation.getRootBean() );
					assertEquals( customer, constraintViolation.getLeafBean() );
					assertNull( constraintViolation.getInvalidValue() );
					assertArrayEquals( new Object[] { customer }, constraintViolation.getExecutableParameters() );
					assertNull( constraintViolation.getExecutableReturnValue() );
				} );
	}

	@Test
	public void methodValidationWithCascadingParameterAndCascadingConstraint() {
		Address address = new Address( null );
		Customer customer = new Customer( "Bob", address );

		assertThatThrownBy( () -> customerRepositoryValidatingProxy.persistCustomer( customer ) )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( exception -> {
					ConstraintViolationException e = (ConstraintViolationException) exception;
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
					assertEquals( messagePrefix() + "must not be null", constraintViolation.getMessage() );
					assertMethod( constraintViolation, "persistCustomer", Customer.class );
					assertParameterIndex( constraintViolation, 0 );
					assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
					assertEquals(
							"persistCustomer.customer.address.city",
							constraintViolation.getPropertyPath().toString() );
					assertEquals( CustomerRepositoryImpl.class, constraintViolation.getRootBeanClass() );
					assertEquals( customerRepositoryOriginalBean, constraintViolation.getRootBean() );
					assertEquals( address, constraintViolation.getLeafBean() );
					assertNull( constraintViolation.getInvalidValue() );
					assertArrayEquals( new Object[] { customer }, constraintViolation.getExecutableParameters() );
					assertNull( constraintViolation.getExecutableReturnValue() );
				} );
	}

	@Test
	public void cascadingMapParameter() {
		Map<String, Customer> customers = newHashMap();
		Customer bob = new Customer( null );
		customers.put( "Bob", bob );

		assertThatThrownBy( () -> customerRepositoryValidatingProxy.cascadingMapParameter( customers ) )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( exception -> {
					ConstraintViolationException e = (ConstraintViolationException) exception;
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
					assertEquals( messagePrefix() + "must not be null", constraintViolation.getMessage() );
					assertMethod( constraintViolation, "cascadingMapParameter", Map.class );
					assertParameterIndex( constraintViolation, 0 );
					assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
					assertEquals(
							"cascadingMapParameter.customer[Bob].name",
							constraintViolation.getPropertyPath().toString() );
					assertEquals( CustomerRepositoryImpl.class, constraintViolation.getRootBeanClass() );
					assertEquals( customerRepositoryOriginalBean, constraintViolation.getRootBean() );
					assertEquals( bob, constraintViolation.getLeafBean() );
					assertNull( constraintViolation.getInvalidValue() );
					assertArrayEquals( new Object[] { customers }, constraintViolation.getExecutableParameters() );
					assertNull( constraintViolation.getExecutableReturnValue() );
				} );
	}

	@Test
	public void cascadingIterableParameter() {
		Customer customer = new Customer( null );
		List<Customer> customers = Arrays.asList( null, customer );

		assertThatThrownBy( () -> customerRepositoryValidatingProxy.cascadingIterableParameter( customers ) )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( exception -> {
					ConstraintViolationException e = (ConstraintViolationException) exception;
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
					assertEquals( messagePrefix() + "must not be null", constraintViolation.getMessage() );
					assertMethod( constraintViolation, "cascadingIterableParameter", List.class );
					assertParameterIndex( constraintViolation, 0 );
					assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
					assertEquals(
							"cascadingIterableParameter.customer[1].name",
							constraintViolation.getPropertyPath().toString() );
					assertEquals( CustomerRepositoryImpl.class, constraintViolation.getRootBeanClass() );
					assertEquals( customerRepositoryOriginalBean, constraintViolation.getRootBean() );
					assertEquals( customer, constraintViolation.getLeafBean() );
					assertNull( constraintViolation.getInvalidValue() );
					assertArrayEquals( new Object[] { customers }, constraintViolation.getExecutableParameters() );
					assertNull( constraintViolation.getExecutableReturnValue() );
				} );
	}

	// HV-1428 Container element support is disabled for arrays
	@Disabled
	@Test
	public void cascadingArrayParameter() {
		Customer customer = new Customer( null );

		assertThatThrownBy( () -> customerRepositoryValidatingProxy.cascadingArrayParameter( null, customer ) )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( exception -> {
					ConstraintViolationException e = (ConstraintViolationException) exception;
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
					assertEquals( messagePrefix() + "must not be null", constraintViolation.getMessage() );
					assertMethod( constraintViolation, "cascadingArrayParameter", Customer[].class );
					assertParameterIndex( constraintViolation, 0 );
					assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
					assertEquals(
							"cascadingArrayParameter.customer[1].name",
							constraintViolation.getPropertyPath().toString() );
					assertEquals( CustomerRepositoryImpl.class, constraintViolation.getRootBeanClass() );
					assertEquals( customerRepositoryOriginalBean, constraintViolation.getRootBean() );
					assertEquals( customer, constraintViolation.getLeafBean() );
					assertNull( constraintViolation.getInvalidValue() );
					assertArrayEquals(
							new Object[] { new Object[] { null, customer } },
							constraintViolation.getExecutableParameters()
					);
					assertNull( constraintViolation.getExecutableReturnValue() );
				} );
	}

	@Test
	public void constraintsAtMethodFromBaseClassAreEvaluated() {
		assertThatThrownBy( () -> customerRepositoryValidatingProxy.findById( null ) )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( exception -> {
					ConstraintViolationException e = (ConstraintViolationException) exception;
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
					assertEquals( messagePrefix() + "must not be null", constraintViolation.getMessage() );
					assertMethod( constraintViolation, "findById", Long.class );
					assertParameterIndex( constraintViolation, 0 );
					assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
					assertEquals( CustomerRepositoryImpl.class, constraintViolation.getRootBeanClass() );
				} );
	}

	@Test
	public void constraintsAtOverriddenMethodAreEvaluated() {
		assertThatThrownBy( () -> customerRepositoryValidatingProxy.foo( null ) )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( exception -> {
					ConstraintViolationException e = (ConstraintViolationException) exception;
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
					assertEquals( messagePrefix() + "must not be null", constraintViolation.getMessage() );
					assertMethod( constraintViolation, "foo", Long.class );
					assertParameterIndex( constraintViolation, 0 );
					assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
					assertEquals( CustomerRepositoryImpl.class, constraintViolation.getRootBeanClass() );
				} );
	}

	@Test
	public void validFromOverriddenMethodIsEvaluated() {
		assertThatThrownBy( () -> customerRepositoryValidatingProxy.bar( new Customer( null, null ) ) )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( exception -> {
					ConstraintViolationException e = (ConstraintViolationException) exception;
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
					assertEquals( messagePrefix() + "must not be null", constraintViolation.getMessage() );
					assertMethod( constraintViolation, "bar", Customer.class );
					assertParameterIndex( constraintViolation, 0 );
					assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
					assertEquals( CustomerRepositoryImpl.class, constraintViolation.getRootBeanClass() );
					assertEquals( "bar.customer.name", constraintViolation.getPropertyPath().toString() );
				} );
	}

	@Test
	public void parameterValidationOfParameterlessMethod() {
		customerRepositoryValidatingProxy.boz();
	}

	@Test
	public void returnValueValidationYieldsConstraintViolation() {
		assertThatThrownBy( () -> customerRepositoryValidatingProxy.baz() )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( exception -> {
					ConstraintViolationException e = (ConstraintViolationException) exception;
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
					assertEquals( messagePrefix() + "must be greater than or equal to 10", constraintViolation.getMessage() );
					assertMethod( constraintViolation, "baz" );
					assertMethodValidationType( constraintViolation, ElementKind.RETURN_VALUE );
					assertEquals( customerRepositoryOriginalBean, constraintViolation.getRootBean() );
					assertEquals( CustomerRepositoryImpl.class, constraintViolation.getRootBeanClass() );
					assertEquals( "baz.<return value>", constraintViolation.getPropertyPath().toString() );
					assertEquals( customerRepositoryOriginalBean, constraintViolation.getLeafBean() );
					assertEquals( 9, constraintViolation.getInvalidValue() );
					assertNull( constraintViolation.getExecutableParameters() );
					assertEquals( 9, constraintViolation.getExecutableReturnValue() );
				} );
	}

	@Test
	public void cascadingReturnValue() {
		assertThatThrownBy( () -> customerRepositoryValidatingProxy.cascadingReturnValue() )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( exception -> {
					ConstraintViolationException e = (ConstraintViolationException) exception;
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
					assertEquals( messagePrefix() + "must not be null", constraintViolation.getMessage() );
					assertMethod( constraintViolation, "cascadingReturnValue" );
					assertMethodValidationType( constraintViolation, ElementKind.RETURN_VALUE );
					assertEquals( customerRepositoryOriginalBean, constraintViolation.getRootBean() );
					assertEquals( CustomerRepositoryImpl.class, constraintViolation.getRootBeanClass() );
					assertEquals(
							"cascadingReturnValue.<return value>.name",
							constraintViolation.getPropertyPath().toString() );
					assertEquals( Customer.class, constraintViolation.getLeafBean().getClass() );
					assertNull( constraintViolation.getInvalidValue() );
					assertNull( constraintViolation.getExecutableParameters() );
					assertEquals( new Customer( null ), constraintViolation.getExecutableReturnValue() );
				} );
	}

	@Test
	public void cascadingReturnValueFromSuperType() {
		assertThatThrownBy( () -> customerRepositoryValidatingProxy.overriddenMethodWithCascadingReturnValue() )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( exception -> {
					ConstraintViolationException e = (ConstraintViolationException) exception;
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
					assertEquals( messagePrefix() + "must not be null", constraintViolation.getMessage() );
					assertMethod( constraintViolation, "overriddenMethodWithCascadingReturnValue" );
					assertMethodValidationType( constraintViolation, ElementKind.RETURN_VALUE );

					assertEquals( customerRepositoryOriginalBean, constraintViolation.getRootBean() );
					assertEquals( CustomerRepositoryImpl.class, constraintViolation.getRootBeanClass() );
					assertEquals(
							"overriddenMethodWithCascadingReturnValue.<return value>.name",
							constraintViolation.getPropertyPath().toString() );
					assertEquals( Customer.class, constraintViolation.getLeafBean().getClass() );
					assertNull( constraintViolation.getInvalidValue() );
					assertNull( constraintViolation.getExecutableParameters() );
					assertEquals( new Customer( null ), constraintViolation.getExecutableReturnValue() );
				} );
	}

	@Test
	public void cascadingIterableReturnValue() {
		assertThatThrownBy( () -> customerRepositoryValidatingProxy.cascadingIterableReturnValue() )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( exception -> {
					ConstraintViolationException e = (ConstraintViolationException) exception;
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
					assertEquals( messagePrefix() + "must not be null", constraintViolation.getMessage() );
					assertMethod( constraintViolation, "cascadingIterableReturnValue" );
					assertMethodValidationType( constraintViolation, ElementKind.RETURN_VALUE );
					assertEquals(
							"cascadingIterableReturnValue.<return value>[1].name",
							constraintViolation.getPropertyPath().toString() );
					assertEquals( CustomerRepositoryImpl.class, constraintViolation.getRootBeanClass() );
					assertEquals( customerRepositoryOriginalBean, constraintViolation.getRootBean() );
					assertEquals( new Customer( null ), constraintViolation.getLeafBean() );
					assertNull( constraintViolation.getInvalidValue() );
					assertNull( constraintViolation.getExecutableParameters() );
					assertEquals( Arrays.asList( null, new Customer( null ) ), constraintViolation.getExecutableReturnValue() );
				} );
	}

	@Test
	public void cascadingMapReturnValue() {
		assertThatThrownBy( () -> customerRepositoryValidatingProxy.cascadingMapReturnValue() )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( exception -> {
					ConstraintViolationException e = (ConstraintViolationException) exception;
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
					assertEquals( messagePrefix() + "must not be null", constraintViolation.getMessage() );
					assertMethod( constraintViolation, "cascadingMapReturnValue" );
					assertMethodValidationType( constraintViolation, ElementKind.RETURN_VALUE );
					assertEquals(
							"cascadingMapReturnValue.<return value>[Bob].name",
							constraintViolation.getPropertyPath().toString() );
					assertEquals( CustomerRepositoryImpl.class, constraintViolation.getRootBeanClass() );
					assertEquals( customerRepositoryOriginalBean, constraintViolation.getRootBean() );
					assertEquals( customer, constraintViolation.getLeafBean() );
					assertNull( constraintViolation.getInvalidValue() );
					assertNull( constraintViolation.getExecutableParameters() );
					assertEquals( expectedReturnValue, constraintViolation.getExecutableReturnValue() );
				} );
	}

	@Test
	public void cascadingArrayReturnValue() {
		assertThatThrownBy( () -> customerRepositoryValidatingProxy.cascadingArrayReturnValue() )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( exception -> {
					ConstraintViolationException e = (ConstraintViolationException) exception;
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
					assertEquals( messagePrefix() + "must not be null", constraintViolation.getMessage() );
					assertMethod( constraintViolation, "cascadingArrayReturnValue" );
					assertMethodValidationType( constraintViolation, ElementKind.RETURN_VALUE );
					assertEquals(
							"cascadingArrayReturnValue.<return value>[1].name",
							constraintViolation.getPropertyPath().toString() );
					assertEquals( CustomerRepositoryImpl.class, constraintViolation.getRootBeanClass() );
					assertEquals( customerRepositoryOriginalBean, constraintViolation.getRootBean() );
					assertEquals( new Customer( null ), constraintViolation.getLeafBean() );
					assertNull( constraintViolation.getInvalidValue() );
					assertNull( constraintViolation.getExecutableParameters() );
					assertArrayEquals( new Object[] { null, new Customer( null ) }, (Object[]) constraintViolation.getExecutableReturnValue() );
				} );
	}

	@Test
	public void overridingMethodStrengthensReturnValueConstraint() {
		assertThatThrownBy( () -> customerRepositoryValidatingProxy.overriddenMethodWithReturnValueConstraint() )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( exception -> {
					ConstraintViolationException e = (ConstraintViolationException) exception;
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
				} );
	}

	@Test
	public void runtimeTypeDefinesConstraintsToApply() {
		assertThatThrownBy( () -> repositoryBase.overriddenMethodWithReturnValueConstraint() )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( exception -> {
					ConstraintViolationException e = (ConstraintViolationException) exception;
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
				} );
	}

	@Test
	public void methodValidationSucceedsAsNoConstraintOfValidatedGroupAreViolated() {
		customerRepositoryValidatingProxy.parameterConstraintInGroup( null );
	}

	@Test
	public void methodValidationFailsAsConstraintOfValidatedGroupIsViolated() {
		createProxy( CustomerRepository.ValidationGroup.class );
		assertThatThrownBy( () -> customerRepositoryValidatingProxy.parameterConstraintInGroup( null ) )
				.isInstanceOf( ConstraintViolationException.class );
	}

	@Test
	public void voidMethodWithReturnValueConstraintCausesConstraintDeclarationException() {
		CustomerRepositoryWithConstrainedVoidMethod customerRepository = getValidatingProxy(
				new CustomerRepositoryWithConstrainedVoidMethodImpl(), validator
		);

		assertThatThrownBy( () -> customerRepository.voidMethodWithIllegalReturnValueConstraint() )
				.isInstanceOf( ConstraintDeclarationException.class )
				.hasMessageMatching( "HV000132.*" );
	}

	@TestForIssue(jiraKey = "HV-601")
	@Test
	public void shouldValidateGetterLikeNamedMethodWithParameter() {
		assertThatThrownBy( () -> customerRepositoryValidatingProxy.getFoo( "" ) )
				.isInstanceOf( ConstraintViolationException.class );
	}

	@Test
	public void validationOfCrossParameterConstraint() {
		//given
		LocalDate startDate = LocalDate.of( 2012, 11, 5 );
		LocalDate endDate = LocalDate.of( 2012, 11, 4 );

		//when
		assertThatThrownBy( () -> customerRepositoryValidatingProxy.methodWithCrossParameterConstraint( startDate, endDate ) )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( exception -> {
					//then
					ConstraintViolationException e = (ConstraintViolationException) exception;
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
					assertEquals( ConsistentDateParameters.class, constraintViolation.getConstraintDescriptor().getAnnotation().annotationType() );
					assertArrayEquals( new Object[] { startDate, endDate }, (Object[]) constraintViolation.getInvalidValue() );
					assertEquals( customerRepositoryOriginalBean, constraintViolation.getLeafBean() );
					assertEquals( customerRepositoryOriginalBean, constraintViolation.getRootBean() );
					assertEquals( CustomerRepositoryImpl.class, constraintViolation.getRootBeanClass() );
					assertArrayEquals( new Object[] { startDate, endDate }, constraintViolation.getExecutableParameters() );
					assertNull( constraintViolation.getExecutableReturnValue() );

					assertMethod(
							constraintViolation,
							"methodWithCrossParameterConstraint",
							LocalDate.class,
							LocalDate.class
					);
				} );
	}

	@Test
	public void methodValidationSucceeds() {
		customerRepositoryValidatingProxy.findCustomerByName( "Bob" );
	}

	protected void assertMethod(ConstraintViolation<?> constraintViolation, String methodName, Class<?>... parameterTypes) {
		Iterator<Path.Node> nodeIterator = constraintViolation.getPropertyPath().iterator();

		Path.Node node = nodeIterator.next();
		assertNotNull( node );
		assertEquals( methodName, node.getName() );
		assertEquals( ElementKind.METHOD, node.getKind() );
		assertEquals( Arrays.asList( parameterTypes ), node.as( Path.MethodNode.class ).getParameterTypes() );
	}

	protected void assertParameterIndex(ConstraintViolation<?> constraintViolation, Integer index) {
		Iterator<Path.Node> nodeIterator = constraintViolation.getPropertyPath().iterator();

		// first node is method descriptor
		nodeIterator.next();
		Path.Node node = nodeIterator.next();
		ParameterNode parameterNode = node.as( ParameterNode.class );
		assertEquals( index.intValue(), parameterNode.getParameterIndex() );
	}

	protected void assertMethodValidationType(ConstraintViolation<?> constraintViolation, ElementKind kind) {
		Iterator<Path.Node> nodeIterator = constraintViolation.getPropertyPath().iterator();

		// first node is method descriptor
		nodeIterator.next();
		Path.Node node = nodeIterator.next();
		assertEquals( kind, node.getKind() );
	}
}
