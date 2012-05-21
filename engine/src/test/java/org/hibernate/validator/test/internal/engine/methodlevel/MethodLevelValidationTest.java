/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validator.test.internal.engine.methodlevel;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.UnexpectedTypeException;
import javax.validation.constraints.NotNull;
import javax.validation.metadata.ElementDescriptor;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.engine.ValidatorImpl;
import org.hibernate.validator.test.internal.engine.methodlevel.model.Address;
import org.hibernate.validator.test.internal.engine.methodlevel.model.Customer;
import org.hibernate.validator.test.internal.engine.methodlevel.service.CustomerRepository;
import org.hibernate.validator.test.internal.engine.methodlevel.service.CustomerRepositoryImpl;
import org.hibernate.validator.test.internal.engine.methodlevel.service.RepositoryBase;

import static javax.validation.metadata.ElementDescriptor.Kind.PARAMETER;
import static javax.validation.metadata.ElementDescriptor.Kind.RETURN_VALUE;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertConstraintViolation;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidatingProxy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * Integration test for the method-level validation related features of {@link ValidatorImpl}.
 *
 * @author Gunnar Morling
 */
@Test
public class MethodLevelValidationTest {
	private CustomerRepository customerRepository;
	private RepositoryBase<Customer> repositoryBase;

	@BeforeMethod
	public void setUpDefaultMethodValidator() {
		setUpValidator();
	}

	private void setUpValidator(Class<?>... groups) {
		repositoryBase = customerRepository = getValidatingProxy(
				new CustomerRepositoryImpl(), groups
		);
	}

	@Test
	public void methodValidationYieldsConstraintViolation() {
		try {
			customerRepository.findCustomerByName( null );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {

			Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
			assertNumberOfViolations( constraintViolations, 1 );

			ConstraintViolation<?> constraintViolation = constraintViolations.iterator().next();

			assertConstraintViolation(
					constraintViolation,
					"may not be null",
					CustomerRepositoryImpl.class,
					null
			);
			assertEquals(
					constraintViolation.getConstraintDescriptor().getAnnotation().annotationType(), NotNull.class
			);
			assertMethodNameParameterIndexAndElementKind( constraintViolation, "findCustomerByName", 0, PARAMETER );
			assertEquals( constraintViolation.getRootBean(), customerRepository );
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals(
					constraintViolation.getPropertyPath().toString(), "CustomerRepository#findCustomerByName(arg0)"
			);
			assertEquals( constraintViolation.getLeafBean(), customerRepository );
			assertEquals( constraintViolation.getInvalidValue(), null );
		}
	}

	@Test
	public void validationOfMethodWithMultipleParameters() {
		try {
			customerRepository.findCustomerByAgeAndName( 30, null );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {
			assertEquals( e.getConstraintViolations().size(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertMethodNameParameterIndexAndElementKind(
					constraintViolation,
					"findCustomerByAgeAndName",
					1,
					PARAMETER
			);
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"CustomerRepository#findCustomerByAgeAndName(arg1)"
			);
		}
	}

	@Test
	public void constraintViolationsAtMultipleParameters() {
		try {
			customerRepository.findCustomerByAgeAndName( 1, null );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {
			assertEquals( e.getConstraintViolations().size(), 2 );
			assertCorrectConstraintViolationMessages(
					e.getConstraintViolations(), "may not be null", "must be greater than or equal to 5"
			);
		}
	}

	@Test
	public void methodValidationWithCascadingParameter() {
		Customer customer = new Customer( null, null );
		try {
			customerRepository.persistCustomer( customer );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {
			assertEquals( e.getConstraintViolations().size(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertMethodNameParameterIndexAndElementKind( constraintViolation, "persistCustomer", 0, PARAMETER );
			assertEquals(
					constraintViolation.getPropertyPath().toString(), "CustomerRepository#persistCustomer(arg0).name"
			);
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getRootBean(), customerRepository );
			assertEquals( constraintViolation.getLeafBean(), customer );
			assertEquals( constraintViolation.getInvalidValue(), null );
		}
	}

	@Test
	public void methodValidationWithCascadingParameterAndCascadingConstraint() {
		Address address = new Address( null );
		Customer customer = new Customer( "Bob", address );

		try {
			customerRepository.persistCustomer( customer );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {
			assertEquals( e.getConstraintViolations().size(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertMethodNameParameterIndexAndElementKind( constraintViolation, "persistCustomer", 0, PARAMETER );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"CustomerRepository#persistCustomer(arg0).address.city"
			);
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getRootBean(), customerRepository );
			assertEquals( constraintViolation.getLeafBean(), address );
			assertEquals( constraintViolation.getInvalidValue(), null );
		}
	}

	@Test
	public void cascadingMapParameter() {
		Map<String, Customer> customers = newHashMap();
		Customer bob = new Customer( null );
		customers.put( "Bob", bob );

		try {
			customerRepository.cascadingMapParameter( customers );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {
			assertEquals( e.getConstraintViolations().size(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertMethodNameParameterIndexAndElementKind( constraintViolation, "cascadingMapParameter", 0, PARAMETER );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"CustomerRepository#cascadingMapParameter(arg0)[Bob].name"
			);
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getRootBean(), customerRepository );
			assertEquals( constraintViolation.getLeafBean(), bob );
			assertEquals( constraintViolation.getInvalidValue(), null );
		}
	}

	@Test
	public void cascadingIterableParameter() {
		Customer customer = new Customer( null );

		try {
			customerRepository.cascadingIterableParameter( Arrays.asList( null, customer ) );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {
			assertEquals( e.getConstraintViolations().size(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertMethodNameParameterIndexAndElementKind(
					constraintViolation,
					"cascadingIterableParameter",
					0,
					PARAMETER
			);
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"CustomerRepository#cascadingIterableParameter(arg0)[1].name"
			);
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getRootBean(), customerRepository );
			assertEquals( constraintViolation.getLeafBean(), customer );
			assertEquals( constraintViolation.getInvalidValue(), null );
		}
	}

	@Test
	public void cascadingArrayParameter() {
		Customer customer = new Customer( null );

		try {
			customerRepository.cascadingArrayParameter( null, customer );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {
			assertEquals( e.getConstraintViolations().size(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertMethodNameParameterIndexAndElementKind(
					constraintViolation,
					"cascadingArrayParameter",
					0,
					PARAMETER
			);
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"CustomerRepository#cascadingArrayParameter(arg0)[1].name"
			);
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getRootBean(), customerRepository );
			assertEquals( constraintViolation.getLeafBean(), customer );
			assertEquals( constraintViolation.getInvalidValue(), null );
		}
	}

	@Test
	public void constraintsAtMethodFromBaseClassAreEvaluated() {
		try {
			customerRepository.findById( null );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {
			assertEquals( e.getConstraintViolations().size(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertMethodNameParameterIndexAndElementKind( constraintViolation, "findById", 0, PARAMETER );
// TODO - HV-571
//			assertEquals( constraintViolation.getMethod().getDeclaringClass(), RepositoryBase.class );
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
		}
	}

	@Test
	public void constraintsAtOverriddenMethodAreEvaluated() {
		try {
			customerRepository.foo( null );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {
			assertEquals( e.getConstraintViolations().size(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertMethodNameParameterIndexAndElementKind( constraintViolation, "foo", 0, PARAMETER );
// TODO - HV-571
//			assertEquals( constraintViolation.getMethod().getDeclaringClass(), CustomerRepository.class );
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
		}
	}

	@Test
	public void validFromOverriddenMethodIsEvaluated() {
		try {
			customerRepository.bar( new Customer( null, null ) );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {
			assertEquals( e.getConstraintViolations().size(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertMethodNameParameterIndexAndElementKind( constraintViolation, "bar", 0, PARAMETER );
// TODO - HV-571
//			assertEquals( constraintViolation.getMethod().getDeclaringClass(), CustomerRepository.class );
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getPropertyPath().toString(), "CustomerRepository#bar(arg0).name" );
		}
	}

	@Test
	public void parameterValidationOfParameterlessMethod() {
		customerRepository.boz();
	}

	@Test
	public void returnValueValidationYieldsConstraintViolation() {
		try {
			customerRepository.baz();
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {
			assertNumberOfViolations( e.getConstraintViolations(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();

			assertEquals( constraintViolation.getMessage(), "must be greater than or equal to 10" );
			assertMethodNameParameterIndexAndElementKind( constraintViolation, "baz", null, RETURN_VALUE );
			assertEquals( constraintViolation.getRootBean(), customerRepository );
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getPropertyPath().toString(), "CustomerRepository#baz()" );
			assertEquals( constraintViolation.getLeafBean(), customerRepository );
			assertEquals( constraintViolation.getInvalidValue(), 9 );
		}
	}

	@Test
	public void cascadingReturnValue() {
		try {
			customerRepository.cascadingReturnValue();
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {
			assertNumberOfViolations( e.getConstraintViolations(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();

			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertMethodNameParameterIndexAndElementKind(
					constraintViolation,
					"cascadingReturnValue",
					null,
					RETURN_VALUE
			);
			assertEquals( constraintViolation.getRootBean(), customerRepository );
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals(
					constraintViolation.getPropertyPath().toString(), "CustomerRepository#cascadingReturnValue().name"
			);
			assertEquals( constraintViolation.getLeafBean().getClass(), Customer.class );
			assertEquals( constraintViolation.getInvalidValue(), null );
		}
	}

	@Test
	public void cascadingReturnValueFromSuperType() {
		try {
			customerRepository.overriddenMethodWithCascadingReturnValue();
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {
			assertNumberOfViolations( e.getConstraintViolations(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();

			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertMethodNameParameterIndexAndElementKind(
					constraintViolation,
					"overriddenMethodWithCascadingReturnValue",
					null,
					RETURN_VALUE
			);
			assertEquals( constraintViolation.getRootBean(), customerRepository );
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"CustomerRepository#overriddenMethodWithCascadingReturnValue().name"
			);
			assertEquals( constraintViolation.getLeafBean().getClass(), Customer.class );
			assertEquals( constraintViolation.getInvalidValue(), null );
		}
	}

	@Test
	public void cascadingIterableReturnValue() {

		try {
			customerRepository.cascadingIterableReturnValue();
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {

			assertEquals( e.getConstraintViolations().size(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertMethodNameParameterIndexAndElementKind(
					constraintViolation,
					"cascadingIterableReturnValue",
					null,
					RETURN_VALUE
			);
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"CustomerRepository#cascadingIterableReturnValue()[1].name"
			);
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getRootBean(), customerRepository );
			assertEquals( constraintViolation.getLeafBean().getClass(), Customer.class );
			assertEquals( constraintViolation.getInvalidValue(), null );
		}
	}

	@Test
	public void cascadingMapReturnValue() {

		try {
			customerRepository.cascadingMapReturnValue();
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {

			assertEquals( e.getConstraintViolations().size(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertMethodNameParameterIndexAndElementKind(
					constraintViolation,
					"cascadingMapReturnValue",
					null,
					RETURN_VALUE
			);
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"CustomerRepository#cascadingMapReturnValue()[Bob].name"
			);
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getRootBean(), customerRepository );
			assertEquals( constraintViolation.getLeafBean().getClass(), Customer.class );
			assertEquals( constraintViolation.getInvalidValue(), null );
		}
	}

	@Test
	public void cascadingArrayReturnValue() {

		try {
			customerRepository.cascadingArrayReturnValue();
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {

			assertEquals( e.getConstraintViolations().size(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertMethodNameParameterIndexAndElementKind(
					constraintViolation,
					"cascadingArrayReturnValue",
					null,
					RETURN_VALUE
			);
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"CustomerRepository#cascadingArrayReturnValue()[1].name"
			);
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getRootBean(), customerRepository );
			assertEquals( constraintViolation.getLeafBean().getClass(), Customer.class );
			assertEquals( constraintViolation.getInvalidValue(), null );
		}
	}

	@Test
	public void overridingMethodStrengthensReturnValueConstraint() {

		try {
			customerRepository.overriddenMethodWithReturnValueConstraint();
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {

			assertCorrectConstraintViolationMessages(
					e.getConstraintViolations(),
					"must be greater than or equal to 5",
					"must be greater than or equal to 10"
			);
		}
	}

	@Test
	public void runtimeTypeDefinesConstraintsToApply() {

		try {
			repositoryBase.overriddenMethodWithReturnValueConstraint();
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {

			assertCorrectConstraintViolationMessages(
					e.getConstraintViolations(),
					"must be greater than or equal to 5",
					"must be greater than or equal to 10"
			);
		}
	}

	@Test
	public void methodValidationSucceedsAsNoConstraintOfValidatedGroupAreViolated() {
		customerRepository.parameterConstraintInGroup( null );
	}

	@Test(expectedExceptions = ConstraintViolationException.class)
	public void methodValidationFailsAsConstraintOfValidatedGroupIsViolated() {
		setUpValidator( CustomerRepository.ValidationGroup.class );
		customerRepository.parameterConstraintInGroup( null );
	}

	@Test(expectedExceptions = UnexpectedTypeException.class, expectedExceptionsMessageRegExp = "HV000030.*")
	public void voidMethodWithReturnValueConstraintCausesUnexpectedTypeException() {
		customerRepository.voidMethodWithIllegalReturnValueConstraint();
	}

	@Test
	public void methodValidationSucceeds() {
		customerRepository.findCustomerByName( "Bob" );
	}

	// TODO - HV-571
	private void assertMethodNameParameterIndexAndElementKind(ConstraintViolation<?> constraintViolation, String methodName, Integer index, ElementDescriptor.Kind kind) {
//		assertEquals( constraintViolation.getMethod().getName(), methodName );
//		assertEquals( constraintViolation.getParameterIndex(), Integer.valueOf( index ) );
//		assertEquals( constraintViolation.getKind(), kind );
	}
}
