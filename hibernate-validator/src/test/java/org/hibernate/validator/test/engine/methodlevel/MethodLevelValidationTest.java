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
package org.hibernate.validator.test.engine.methodlevel;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.engine.ValidatorImpl;
import org.hibernate.validator.method.MethodConstraintViolation;
import org.hibernate.validator.method.MethodConstraintViolation.Kind;
import org.hibernate.validator.method.MethodConstraintViolationException;
import org.hibernate.validator.test.engine.methodlevel.model.Address;
import org.hibernate.validator.test.engine.methodlevel.model.Customer;
import org.hibernate.validator.test.engine.methodlevel.service.CustomerRepository;
import org.hibernate.validator.test.engine.methodlevel.service.CustomerRepositoryImpl;
import org.hibernate.validator.test.engine.methodlevel.service.RepositoryBase;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertConstraintViolation;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidatingProxy;
import static org.hibernate.validator.util.CollectionHelper.newHashMap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * Integration test for the method-level validation related features of {@link ValidatorImpl}.
 *
 * @author Gunnar Morling
 */
public class MethodLevelValidationTest {

	private CustomerRepository customerRepository;

	private RepositoryBase<Customer> repositoryBase;

	@BeforeMethod
	public void setUpDefaultMethodValidator() {
		setUpValidator();
	}

	private void setUpValidator(Integer parameterIndex, Class<?>... groups) {
		repositoryBase = customerRepository = getValidatingProxy(
				new CustomerRepositoryImpl(), parameterIndex, groups
		);
	}

	private void setUpValidator(Class<?>... groups) {
		setUpValidator( null, groups );
	}

	@Test
	public void testPath() {

		Validator validator2 = Validation.buildDefaultValidatorFactory().getValidator();
		Set<ConstraintViolation<Customer>> violations = validator2.validate( new Customer( null, null ) );
		Path propertyPath = violations.iterator().next().getPropertyPath();
		System.out.println( propertyPath );
	}

	@Test
	public void methodValidationYieldsConstraintViolation() {

		try {
			customerRepository.findCustomerByName( null );
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch ( MethodConstraintViolationException e ) {

			Set<MethodConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
			assertNumberOfViolations( constraintViolations, 1 );

			MethodConstraintViolation<?> constraintViolation = constraintViolations.iterator().next();

			assertConstraintViolation(
					constraintViolation,
					"may not be null",
					CustomerRepositoryImpl.class,
					null
			);
			assertEquals(
					constraintViolation.getConstraintDescriptor().getAnnotation().annotationType(), NotNull.class
			);
			assertEquals( constraintViolation.getMethod().getName(), "findCustomerByName" );
			assertEquals( constraintViolation.getParameterIndex(), Integer.valueOf( 0 ) );
			assertEquals( constraintViolation.getKind(), Kind.PARAMETER );
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
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch ( MethodConstraintViolationException e ) {

			assertEquals( e.getConstraintViolations().size(), 1 );

			MethodConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertEquals( constraintViolation.getMethod().getName(), "findCustomerByAgeAndName" );
			assertEquals( constraintViolation.getParameterIndex(), Integer.valueOf( 1 ) );
			assertEquals( constraintViolation.getKind(), Kind.PARAMETER );
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
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch ( MethodConstraintViolationException e ) {

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
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch ( MethodConstraintViolationException e ) {

			assertEquals( e.getConstraintViolations().size(), 1 );

			MethodConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertEquals( constraintViolation.getMethod().getName(), "persistCustomer" );
			assertEquals( constraintViolation.getParameterIndex(), Integer.valueOf( 0 ) );
			assertEquals( constraintViolation.getKind(), Kind.PARAMETER );
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
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch ( MethodConstraintViolationException e ) {

			assertEquals( e.getConstraintViolations().size(), 1 );

			MethodConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertEquals( constraintViolation.getMethod().getName(), "persistCustomer" );
			assertEquals( constraintViolation.getParameterIndex(), Integer.valueOf( 0 ) );
			assertEquals( constraintViolation.getKind(), Kind.PARAMETER );
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
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch ( MethodConstraintViolationException e ) {

			assertEquals( e.getConstraintViolations().size(), 1 );

			MethodConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertEquals( constraintViolation.getMethod().getName(), "cascadingMapParameter" );
			assertEquals( constraintViolation.getParameterIndex(), Integer.valueOf( 0 ) );
			assertEquals( constraintViolation.getKind(), Kind.PARAMETER );
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
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch ( MethodConstraintViolationException e ) {

			assertEquals( e.getConstraintViolations().size(), 1 );

			MethodConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertEquals( constraintViolation.getMethod().getName(), "cascadingIterableParameter" );
			assertEquals( constraintViolation.getParameterIndex(), Integer.valueOf( 0 ) );
			assertEquals( constraintViolation.getKind(), Kind.PARAMETER );
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
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch ( MethodConstraintViolationException e ) {

			assertEquals( e.getConstraintViolations().size(), 1 );

			MethodConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertEquals( constraintViolation.getMethod().getName(), "cascadingArrayParameter" );
			assertEquals( constraintViolation.getParameterIndex(), Integer.valueOf( 0 ) );
			assertEquals( constraintViolation.getKind(), Kind.PARAMETER );
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
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch ( MethodConstraintViolationException e ) {

			assertEquals( e.getConstraintViolations().size(), 1 );

			MethodConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertEquals( constraintViolation.getMethod().getName(), "findById" );
			assertEquals( constraintViolation.getMethod().getDeclaringClass(), RepositoryBase.class );
			assertEquals( constraintViolation.getParameterIndex(), Integer.valueOf( 0 ) );
			assertEquals( constraintViolation.getKind(), Kind.PARAMETER );
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
		}
	}

	@Test
	public void constraintsAtOverriddenMethodAreEvaluated() {

		try {

			customerRepository.foo( null );
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch ( MethodConstraintViolationException e ) {

			assertEquals( e.getConstraintViolations().size(), 1 );

			MethodConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertEquals( constraintViolation.getMethod().getName(), "foo" );
			assertEquals( constraintViolation.getMethod().getDeclaringClass(), CustomerRepository.class );
			assertEquals( constraintViolation.getParameterIndex(), Integer.valueOf( 0 ) );
			assertEquals( constraintViolation.getKind(), Kind.PARAMETER );
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
		}
	}

	@Test
	public void validFromOverriddenMethodIsEvaluated() {

		try {

			customerRepository.bar( new Customer( null, null ) );
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch ( MethodConstraintViolationException e ) {

			assertEquals( e.getConstraintViolations().size(), 1 );

			MethodConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertEquals( constraintViolation.getMethod().getName(), "bar" );
			assertEquals( constraintViolation.getMethod().getDeclaringClass(), CustomerRepository.class );
			assertEquals( constraintViolation.getParameterIndex(), Integer.valueOf( 0 ) );
			assertEquals( constraintViolation.getKind(), Kind.PARAMETER );
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getPropertyPath().toString(), "CustomerRepository#bar(arg0).name" );
		}
	}

	@Test
	public void parameterValidationOfParameterlessMethod() {
		customerRepository.boz();
	}

	/**
	 * The constraints at both parameters are violated, but as only the 2nd
	 * parameter is validated, only one constraint violation is expected.
	 */
	@Test
	public void singleParameterValidation() {

		setUpValidator( 1 );

		try {
			customerRepository.findCustomerByAgeAndName( 1, null );
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch ( MethodConstraintViolationException e ) {

			assertEquals( e.getConstraintViolations().size(), 1 );

			MethodConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertEquals( constraintViolation.getMethod().getName(), "findCustomerByAgeAndName" );
			assertEquals( constraintViolation.getMethod().getDeclaringClass(), CustomerRepository.class );
			assertEquals( constraintViolation.getParameterIndex(), Integer.valueOf( 1 ) );
			assertEquals( constraintViolation.getKind(), Kind.PARAMETER );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"CustomerRepository#findCustomerByAgeAndName(arg1)"
			);
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
		}
	}

	@Test
	public void cascadingSingleParameterValidation() {

		setUpValidator( 1 );

		try {
			customerRepository.cascadingParameter( null, new Customer( null ) );
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch ( MethodConstraintViolationException e ) {

			assertEquals( e.getConstraintViolations().size(), 1 );

			MethodConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertEquals( constraintViolation.getMethod().getName(), "cascadingParameter" );
			assertEquals( constraintViolation.getMethod().getDeclaringClass(), CustomerRepository.class );
			assertEquals( constraintViolation.getParameterIndex(), Integer.valueOf( 1 ) );
			assertEquals( constraintViolation.getKind(), Kind.PARAMETER );
			assertEquals(
					constraintViolation.getPropertyPath().toString(), "CustomerRepository#cascadingParameter(arg1).name"
			);
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
		}
	}

	@Test
	public void returnValueValidationYieldsConstraintViolation() {

		try {
			customerRepository.baz();
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch ( MethodConstraintViolationException e ) {

			assertNumberOfViolations( e.getConstraintViolations(), 1 );

			MethodConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();

			assertEquals( constraintViolation.getMessage(), "must be greater than or equal to 10" );
			assertEquals( constraintViolation.getMethod().getName(), "baz" );
			assertEquals( constraintViolation.getParameterIndex(), null );
			assertEquals( constraintViolation.getKind(), Kind.RETURN_VALUE );
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
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch ( MethodConstraintViolationException e ) {

			assertNumberOfViolations( e.getConstraintViolations(), 1 );

			MethodConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();

			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertEquals( constraintViolation.getMethod().getName(), "cascadingReturnValue" );
			assertEquals( constraintViolation.getParameterIndex(), null );
			assertEquals( constraintViolation.getParameterName(), null );
			assertEquals( constraintViolation.getKind(), Kind.RETURN_VALUE );
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
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch ( MethodConstraintViolationException e ) {

			assertNumberOfViolations( e.getConstraintViolations(), 1 );

			MethodConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();

			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertEquals( constraintViolation.getMethod().getName(), "overriddenMethodWithCascadingReturnValue" );
			assertEquals( constraintViolation.getParameterIndex(), null );
			assertEquals( constraintViolation.getParameterName(), null );
			assertEquals( constraintViolation.getKind(), Kind.RETURN_VALUE );
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
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch ( MethodConstraintViolationException e ) {

			assertEquals( e.getConstraintViolations().size(), 1 );

			MethodConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertEquals( constraintViolation.getMethod().getName(), "cascadingIterableReturnValue" );
			assertEquals( constraintViolation.getKind(), Kind.RETURN_VALUE );
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
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch ( MethodConstraintViolationException e ) {

			assertEquals( e.getConstraintViolations().size(), 1 );

			MethodConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertEquals( constraintViolation.getMethod().getName(), "cascadingMapReturnValue" );
			assertEquals( constraintViolation.getKind(), Kind.RETURN_VALUE );
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
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch ( MethodConstraintViolationException e ) {

			assertEquals( e.getConstraintViolations().size(), 1 );

			MethodConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertEquals( constraintViolation.getMethod().getName(), "cascadingArrayReturnValue" );
			assertEquals( constraintViolation.getKind(), Kind.RETURN_VALUE );
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
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch ( MethodConstraintViolationException e ) {

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
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch ( MethodConstraintViolationException e ) {

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

	@Test(expectedExceptions = MethodConstraintViolationException.class)
	public void methodValidationFailsAsConstraintOfValidatedGroupIsViolated() {
		setUpValidator( CustomerRepository.ValidationGroup.class );
		customerRepository.parameterConstraintInGroup( null );
	}

	@Test
	public void methodValidationSucceeds() {
		customerRepository.findCustomerByName( "Bob" );
	}
}
