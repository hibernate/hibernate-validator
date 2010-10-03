// $Id: MethodLevelValidationTest.java 19033 Sep 19, 2010 9:51:37 AM gunnar.morling $
/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

import java.lang.reflect.Proxy;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;

import org.hibernate.validator.MethodValidator;
import org.hibernate.validator.MethodConstraintViolation;
import org.hibernate.validator.test.engine.methodlevel.model.Address;
import org.hibernate.validator.test.engine.methodlevel.model.Customer;
import org.hibernate.validator.test.engine.methodlevel.service.CustomerRepository;
import org.hibernate.validator.test.engine.methodlevel.service.CustomerRepositoryImpl;
import org.hibernate.validator.test.engine.methodlevel.service.RepositoryBase;

import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Gunnar Morling
 *
 */
public class MethodLevelValidationTest {

	private MethodValidator validator;
	private CustomerRepository proxiedRepo;
	
	@BeforeMethod
	public void setUpValidator() {
		validator = Validation.buildDefaultValidatorFactory().getValidator().unwrap(MethodValidator.class);

		proxiedRepo = (CustomerRepository)Proxy.newProxyInstance(
				getClass().getClassLoader(), 
				new Class<?>[]{CustomerRepository.class}, 
				new ValidationInvocationHandler(new CustomerRepositoryImpl(), validator));
	}
	
	@Test
	public void methodValidationYieldsConstraintViolation() {
		
		try {
		
			proxiedRepo.findCustomerByName(null);
			fail("Expected ConstraintViolationException wasn't thrown.");
		}
		catch(ConstraintViolationException e) {
			
			assertEquals(e.getConstraintViolations().size(), 1);
			
			MethodConstraintViolation<?> constraintViolation = (MethodConstraintViolation<?>) e.getConstraintViolations().iterator().next();
			assertEquals(constraintViolation.getMessage(), "may not be null");
			assertEquals(constraintViolation.getMethod().getName(), "findCustomerByName");
			assertEquals(constraintViolation.getParameterIndex(), 0);
			assertEquals(constraintViolation.getRootBeanClass(), null);
			System.out.println(constraintViolation);

		}
	}
	
	@Test
	public void multipleParameters() {
		
		try {
		
			proxiedRepo.findCustomerByAgeAndName(30, null);
			fail("Expected ConstraintViolationException wasn't thrown.");
		}
		catch(ConstraintViolationException e) {
			
			assertEquals(e.getConstraintViolations().size(), 1);
			
			MethodConstraintViolation<?> constraintViolation = (MethodConstraintViolation<?>) e.getConstraintViolations().iterator().next();
			assertEquals(constraintViolation.getMessage(), "may not be null");
			assertEquals(constraintViolation.getMethod().getName(), "findCustomerByAgeAndName");
			assertEquals(constraintViolation.getParameterIndex(), 1);
			assertEquals(constraintViolation.getRootBeanClass(), null);
			System.out.println(constraintViolation);

		}
	}
	
	
	@Test
	public void methodValidationWithCascadingParameter() {
		
		try {
			
			proxiedRepo.persistCustomer(new Customer(null, null));
			fail("Expected ConstraintViolationException wasn't thrown.");
		}
		catch(ConstraintViolationException e) {
			
			assertEquals(e.getConstraintViolations().size(), 1);
			
			MethodConstraintViolation<?> constraintViolation = (MethodConstraintViolation<?>) e.getConstraintViolations().iterator().next();
			assertEquals(constraintViolation.getMessage(), "may not be null");
			assertEquals(constraintViolation.getMethod().getName(), "persistCustomer");
			assertEquals(constraintViolation.getParameterIndex(), 0);
			assertEquals(constraintViolation.getPropertyPath().toString(), "name");
			assertEquals(constraintViolation.getRootBeanClass(), Customer.class);
			
//			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
//			assertEquals(constraintViolation.getMessage(), "may not be null");
//			assertEquals(constraintViolation.getPropertyPath().toString(), "name");
			
			
			System.out.println(constraintViolation);

		}
	}
	
	@Test
	public void methodValidationWithCascadingParameterAndCascadingConstraint() {
		
		Address address = new Address(null);
		Customer customer = new Customer("Bob", address);

		try {
			proxiedRepo.persistCustomer(customer);
			fail("Expected ConstraintViolationException wasn't thrown.");
		}
		catch(ConstraintViolationException e) {
			
			assertEquals(e.getConstraintViolations().size(), 1);
			
			MethodConstraintViolation<?> constraintViolation = (MethodConstraintViolation<?>) e.getConstraintViolations().iterator().next();
			assertEquals(constraintViolation.getMessage(), "may not be null");
			assertEquals(constraintViolation.getMethod().getName(), "persistCustomer");
			assertEquals(constraintViolation.getParameterIndex(), 0);
			assertEquals(constraintViolation.getPropertyPath().toString(), "address.city");
			assertEquals(constraintViolation.getRootBeanClass(), Customer.class);
			assertEquals(constraintViolation.getRootBean(), customer);
			assertEquals(constraintViolation.getLeafBean(), address);
			assertEquals(constraintViolation.getInvalidValue(), null);
		}
	}
	
	@Test
	public void constraintsAtMethodFromBaseClassAreEvaluated() {
		
		try {
			
			proxiedRepo.findById(null);
			fail("Expected ConstraintViolationException wasn't thrown.");
		}
		catch(ConstraintViolationException e) {
			
			assertEquals(e.getConstraintViolations().size(), 1);
			
			MethodConstraintViolation<?> constraintViolation = (MethodConstraintViolation<?>) e.getConstraintViolations().iterator().next();
			assertEquals(constraintViolation.getMessage(), "may not be null");
			assertEquals(constraintViolation.getMethod().getName(), "findById");
			assertEquals(constraintViolation.getMethod().getDeclaringClass(), RepositoryBase.class);
			assertEquals(constraintViolation.getParameterIndex(), 0);
			assertEquals(constraintViolation.getRootBeanClass(), null);
		}
	}
	
	@Test
	public void constraintsAtOverriddenMethodAreEvaluated() {
		
		try {
			
			proxiedRepo.foo(null);
			fail("Expected ConstraintViolationException wasn't thrown.");
		}
		catch(ConstraintViolationException e) {
			
			assertEquals(e.getConstraintViolations().size(), 1);
			
			MethodConstraintViolation<?> constraintViolation = (MethodConstraintViolation<?>) e.getConstraintViolations().iterator().next();
			assertEquals(constraintViolation.getMessage(), "may not be null");
			assertEquals(constraintViolation.getMethod().getName(), "foo");
			assertEquals(constraintViolation.getMethod().getDeclaringClass(), CustomerRepository.class);
			assertEquals(constraintViolation.getParameterIndex(), 0);
			assertEquals(constraintViolation.getRootBeanClass(), null);
		}
	}
	
	@Test
	public void validFromOverriddenMethodIsEvaluated() {
		
		try {
			
			proxiedRepo.bar(new Customer(null, null));
			fail("Expected ConstraintViolationException wasn't thrown.");
		}
		catch(ConstraintViolationException e) {
			
			assertEquals(e.getConstraintViolations().size(), 1);
			
			MethodConstraintViolation<?> constraintViolation = (MethodConstraintViolation<?>) e.getConstraintViolations().iterator().next();
			assertEquals(constraintViolation.getMessage(), "may not be null");
			assertEquals(constraintViolation.getMethod().getName(), "bar");
			assertEquals(constraintViolation.getMethod().getDeclaringClass(), CustomerRepository.class);
			assertEquals(constraintViolation.getParameterIndex(), 0);
			assertEquals(constraintViolation.getRootBeanClass(), Customer.class);
			assertEquals(constraintViolation.getPropertyPath().toString(), "name");
		}
	}
	
	@Test
	public void constraintsAtOverridingMethodAreEvaluated() {
		
		try {
			
			proxiedRepo.foo(Long.valueOf(0));
			fail("Expected ConstraintViolationException wasn't thrown.");
		}
		catch(ConstraintViolationException e) {
			
			assertEquals(e.getConstraintViolations().size(), 1);
			
			MethodConstraintViolation<?> constraintViolation = (MethodConstraintViolation<?>) e.getConstraintViolations().iterator().next();
			assertEquals(constraintViolation.getMessage(), "must be greater than or equal to 1");
			assertEquals(constraintViolation.getMethod().getName(), "foo");
			assertEquals(constraintViolation.getMethod().getDeclaringClass(), CustomerRepository.class);
			assertEquals(constraintViolation.getParameterIndex(), 0);
			assertEquals(constraintViolation.getRootBeanClass(), null);
		}
	}
	@Test
	public void methodValidationSucceeds() {
		
		proxiedRepo.findCustomerByName("Bob");
	}
	
}
