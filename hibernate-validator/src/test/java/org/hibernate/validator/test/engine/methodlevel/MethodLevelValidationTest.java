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

import static org.hibernate.validator.test.util.TestUtil.assertConstraintViolation;
import static org.hibernate.validator.test.util.TestUtil.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.test.util.TestUtil.assertNumberOfViolations;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.lang.reflect.Proxy;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.MethodConstraintViolation;
import org.hibernate.validator.MethodConstraintViolation.Kind;
import org.hibernate.validator.MethodConstraintViolationException;
import org.hibernate.validator.MethodValidator;
import org.hibernate.validator.engine.ValidatorImpl;
import org.hibernate.validator.test.engine.methodlevel.model.Address;
import org.hibernate.validator.test.engine.methodlevel.model.Customer;
import org.hibernate.validator.test.engine.methodlevel.service.CustomerRepository;
import org.hibernate.validator.test.engine.methodlevel.service.CustomerRepositoryImpl;
import org.hibernate.validator.test.engine.methodlevel.service.RepositoryBase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Integration test for the method-level validation related features of {@link ValidatorImpl}.
 *
 * @author Gunnar Morling
 *
 */
public class MethodLevelValidationTest {

	private CustomerRepository customerRepository;

	@BeforeMethod
	public void setUpDefaultMethodValidator() {
		setUpValidatorForGroups();
	}

	private void setUpValidatorForGroups(Class<?>... groups) {

		MethodValidator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.allowMethodLevelConstraints()
				.buildValidatorFactory()
				.getValidator()
				.unwrap( MethodValidator.class );

		customerRepository = ( CustomerRepository ) Proxy.newProxyInstance(
				getClass().getClassLoader(),
				new Class<?>[] { CustomerRepository.class },
				new ValidationInvocationHandler( new CustomerRepositoryImpl(), validator, groups )
		);
	}

	@Test
	public void testPath() {

		Validator validator2 = Validation.buildDefaultValidatorFactory().getValidator();
		Set<ConstraintViolation<Customer>> violations = validator2.validate(new Customer(null, null));
		Path propertyPath = violations.iterator().next().getPropertyPath();
		System.out.println(propertyPath);
	}

	@Test
	public void methodValidationYieldsConstraintViolation() {

		try {
			customerRepository.findCustomerByName(null);
			fail("Expected MethodConstraintViolationException wasn't thrown.");
		}
		catch(MethodConstraintViolationException e) {

			Set<MethodConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
			assertNumberOfViolations(constraintViolations, 1);

			MethodConstraintViolation<?> constraintViolation = constraintViolations.iterator().next();

			assertConstraintViolation(constraintViolation, "may not be null", CustomerRepositoryImpl.class, null);
			assertEquals(constraintViolation.getConstraintDescriptor().getAnnotation().annotationType(), NotNull.class);
			assertEquals(constraintViolation.getMethod().getName(), "findCustomerByName");
			assertEquals(constraintViolation.getParameterIndex(), Integer.valueOf(0));
			assertEquals(constraintViolation.getKind(), Kind.PARAMETER);
			assertEquals(constraintViolation.getRootBean(), customerRepository);
			assertEquals(constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class);
			assertEquals(constraintViolation.getPropertyPath().toString(), "CustomerRepository#findCustomerByName(arg0)");
			assertEquals(constraintViolation.getLeafBean(), customerRepository);
			assertEquals(constraintViolation.getInvalidValue(), null);
		}
	}

	@Test
	public void validationOfMethodWithMultipleParameters() {

		try {
			customerRepository.findCustomerByAgeAndName(30, null);
			fail("Expected MethodConstraintViolationException wasn't thrown.");
		}
		catch(MethodConstraintViolationException e) {

			assertEquals(e.getConstraintViolations().size(), 1);

			MethodConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals(constraintViolation.getMessage(), "may not be null");
			assertEquals(constraintViolation.getMethod().getName(), "findCustomerByAgeAndName");
			assertEquals(constraintViolation.getParameterIndex(), Integer.valueOf(1));
			assertEquals(constraintViolation.getKind(), Kind.PARAMETER);
			assertEquals(constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class);
			assertEquals(constraintViolation.getPropertyPath().toString(), "CustomerRepository#findCustomerByAgeAndName(arg1)");
		}
	}

	@Test
	public void constraintViolationsAtMultipleParameters() {

		try {
			customerRepository.findCustomerByAgeAndName(1, null);
			fail("Expected MethodConstraintViolationException wasn't thrown.");
		}
		catch(MethodConstraintViolationException e) {

			assertEquals(e.getConstraintViolations().size(), 2);
			assertCorrectConstraintViolationMessages(e.getConstraintViolations(), "may not be null", "must be greater than or equal to 5");
		}
	}

	@Test
	public void methodValidationWithCascadingParameter() {

		Customer customer = new Customer(null, null);

		try {
			customerRepository.persistCustomer(customer);
			fail("Expected MethodConstraintViolationException wasn't thrown.");
		}
		catch(MethodConstraintViolationException e) {

			assertEquals(e.getConstraintViolations().size(), 1);

			MethodConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals(constraintViolation.getMessage(), "may not be null");
			assertEquals(constraintViolation.getMethod().getName(), "persistCustomer");
			assertEquals(constraintViolation.getParameterIndex(), Integer.valueOf(0));
			assertEquals(constraintViolation.getKind(), Kind.PARAMETER);
			assertEquals(constraintViolation.getPropertyPath().toString(), "CustomerRepository#persistCustomer(arg0).name");
			assertEquals(constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class);
			assertEquals(constraintViolation.getRootBean(), customerRepository);
			assertEquals(constraintViolation.getLeafBean(), customer);
			assertEquals(constraintViolation.getInvalidValue(), null);
		}
	}

	@Test
	public void methodValidationWithCascadingParameterAndCascadingConstraint() {

		Address address = new Address(null);
		Customer customer = new Customer("Bob", address);

		try {
			customerRepository.persistCustomer(customer);
			fail("Expected MethodConstraintViolationException wasn't thrown.");
		}
		catch(MethodConstraintViolationException e) {

			assertEquals(e.getConstraintViolations().size(), 1);

			MethodConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals(constraintViolation.getMessage(), "may not be null");
			assertEquals(constraintViolation.getMethod().getName(), "persistCustomer");
			assertEquals(constraintViolation.getParameterIndex(), Integer.valueOf(0));
			assertEquals(constraintViolation.getKind(), Kind.PARAMETER);
			assertEquals(constraintViolation.getPropertyPath().toString(), "CustomerRepository#persistCustomer(arg0).address.city");
			assertEquals(constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class);
			assertEquals(constraintViolation.getRootBean(), customerRepository);
			assertEquals(constraintViolation.getLeafBean(), address);
			assertEquals(constraintViolation.getInvalidValue(), null);
		}
	}

	@Test
	public void constraintsAtMethodFromBaseClassAreEvaluated() {

		try {

			customerRepository.findById(null);
			fail("Expected MethodConstraintViolationException wasn't thrown.");
		}
		catch(MethodConstraintViolationException e) {

			assertEquals(e.getConstraintViolations().size(), 1);

			MethodConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals(constraintViolation.getMessage(), "may not be null");
			assertEquals(constraintViolation.getMethod().getName(), "findById");
			assertEquals(constraintViolation.getMethod().getDeclaringClass(), RepositoryBase.class);
			assertEquals(constraintViolation.getParameterIndex(), Integer.valueOf(0));
			assertEquals(constraintViolation.getKind(), Kind.PARAMETER);
			assertEquals(constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class);
		}
	}

	@Test
	public void constraintsAtOverriddenMethodAreEvaluated() {

		try {

			customerRepository.foo(null);
			fail("Expected MethodConstraintViolationException wasn't thrown.");
		}
		catch(MethodConstraintViolationException e) {

			assertEquals(e.getConstraintViolations().size(), 1);

			MethodConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals(constraintViolation.getMessage(), "may not be null");
			assertEquals(constraintViolation.getMethod().getName(), "foo");
			assertEquals(constraintViolation.getMethod().getDeclaringClass(), CustomerRepository.class);
			assertEquals(constraintViolation.getParameterIndex(), Integer.valueOf(0));
			assertEquals(constraintViolation.getKind(), Kind.PARAMETER);
			assertEquals(constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class);
		}
	}

	@Test
	public void validFromOverriddenMethodIsEvaluated() {

		try {

			customerRepository.bar(new Customer(null, null));
			fail("Expected MethodConstraintViolationException wasn't thrown.");
		}
		catch(MethodConstraintViolationException e) {

			assertEquals(e.getConstraintViolations().size(), 1);

			MethodConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals(constraintViolation.getMessage(), "may not be null");
			assertEquals(constraintViolation.getMethod().getName(), "bar");
			assertEquals(constraintViolation.getMethod().getDeclaringClass(), CustomerRepository.class);
			assertEquals(constraintViolation.getParameterIndex(), Integer.valueOf(0));
			assertEquals(constraintViolation.getKind(), Kind.PARAMETER);
			assertEquals(constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class);
			assertEquals(constraintViolation.getPropertyPath().toString(), "CustomerRepository#bar(arg0).name");
		}
	}

	@Test
	public void constraintsAtOverridingMethodAreEvaluated() {

		try {

			customerRepository.foo(Long.valueOf(0));
			fail("Expected MethodConstraintViolationException wasn't thrown.");
		}
		catch(MethodConstraintViolationException e) {

			assertEquals(e.getConstraintViolations().size(), 1);

			MethodConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals(constraintViolation.getMessage(), "must be greater than or equal to 1");
			assertEquals(constraintViolation.getMethod().getName(), "foo");
			assertEquals(constraintViolation.getMethod().getDeclaringClass(), CustomerRepository.class);
			assertEquals(constraintViolation.getParameterIndex(), Integer.valueOf(0));
			assertEquals(constraintViolation.getKind(), Kind.PARAMETER);
			assertEquals(constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class);
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
			fail("Expected MethodConstraintViolationException wasn't thrown.");
		}
		catch(MethodConstraintViolationException e) {

			assertNumberOfViolations(e.getConstraintViolations(), 1);

			MethodConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();

			assertEquals(constraintViolation.getMessage(), "must be greater than or equal to 10");
			assertEquals(constraintViolation.getMethod().getName(), "baz");
			assertEquals(constraintViolation.getParameterIndex(), null);
			assertEquals(constraintViolation.getKind(), Kind.RETURN_VALUE);
			assertEquals(constraintViolation.getRootBean(), customerRepository);
			assertEquals(constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class);
			assertEquals(constraintViolation.getPropertyPath().toString(), "CustomerRepository#baz()");
			assertEquals(constraintViolation.getLeafBean(), customerRepository);
			assertEquals(constraintViolation.getInvalidValue(), 9);
		}
	}

	@Test
	public void namedParameters() {

		//param 1
		try {
			customerRepository.namedParameters( null, new Customer( "Bob" ) );
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch ( MethodConstraintViolationException e ) {

			assertEquals( e.getConstraintViolations().size(), 1 );

			MethodConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertEquals( constraintViolation.getParameterIndex(), Integer.valueOf( 0 ) );
			assertEquals( constraintViolation.getParameterName(), "param1" );
			assertEquals( constraintViolation.getKind(), Kind.PARAMETER );
			assertEquals(
					constraintViolation.getPropertyPath().toString(), "CustomerRepository#namedParameters(param1)"
			);
		}

		//param 2
		try {
			customerRepository.namedParameters( "foo", null );
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch ( MethodConstraintViolationException e ) {

			assertEquals( e.getConstraintViolations().size(), 1 );

			MethodConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertEquals( constraintViolation.getParameterIndex(), Integer.valueOf( 1 ) );
			assertEquals( constraintViolation.getParameterName(), "customer" );
			assertEquals( constraintViolation.getKind(), Kind.PARAMETER );
			assertEquals(
					constraintViolation.getPropertyPath().toString(), "CustomerRepository#namedParameters(customer)"
			);
		}
	}

	@Test
	public void cascadingNamedParameter() {

		try {
			customerRepository.namedParameters( "foo", new Customer( null ) );
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch ( MethodConstraintViolationException e ) {

			assertEquals( e.getConstraintViolations().size(), 1 );

			MethodConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
			assertEquals( constraintViolation.getParameterIndex(), Integer.valueOf( 1 ) );
			assertEquals( constraintViolation.getParameterName(), "customer" );
			assertEquals( constraintViolation.getKind(), Kind.PARAMETER );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"CustomerRepository#namedParameters(customer).name"
			);
		}
	}

	@Test
	public void methodValidationSucceedsAsNoConstraintOfValidatedGroupAreViolated() {
		customerRepository.parameterConstraintInGroup(null);
	}

	@Test(expectedExceptions=MethodConstraintViolationException.class)
	public void methodValidationFailsAsConstraintOfValidatedGroupIsViolated() {
		setUpValidatorForGroups(CustomerRepository.ValidationGroup.class);
		customerRepository.parameterConstraintInGroup(null);
	}

	@Test
	public void methodValidationSucceeds() {
		customerRepository.findCustomerByName("Bob");
	}

}
