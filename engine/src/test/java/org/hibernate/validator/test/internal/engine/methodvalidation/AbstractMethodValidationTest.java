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
package org.hibernate.validator.test.internal.engine.methodvalidation;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintDeclarationException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ElementKind;
import javax.validation.Path;
import javax.validation.Path.ParameterNode;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import org.joda.time.DateMidnight;
import org.testng.annotations.Test;

import org.hibernate.validator.test.internal.engine.methodvalidation.model.Address;
import org.hibernate.validator.test.internal.engine.methodvalidation.model.Customer;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.ConsistentDateParameters;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepository;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryImpl;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryWithConstrainedVoidMethod;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryWithConstrainedVoidMethodImpl;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.RepositoryBase;
import org.hibernate.validator.testutil.TestForIssue;

import static org.fest.assertions.Assertions.assertThat;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertConstraintViolation;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNodeKinds;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNodeNames;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidatingProxy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

/**
 * Integration test for the method-level validation related features of {@link org.hibernate.validator.internal.engine.ValidatorImpl}.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
@Test
public abstract class AbstractMethodValidationTest {
	protected CustomerRepository customerRepository;
	protected RepositoryBase<Customer> repositoryBase;
	protected Validator validator;

	protected abstract void setUp();

	protected abstract String messagePrefix();

	protected void createProxy(Class<?>... groups) {
		customerRepository = getValidatingProxy(
				new CustomerRepositoryImpl(), validator, groups
		);
		repositoryBase = customerRepository;
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
					messagePrefix() + "may not be null",
					CustomerRepositoryImpl.class,
					null
			);
			assertEquals(
					constraintViolation.getConstraintDescriptor().getAnnotation().annotationType(), NotNull.class
			);
			assertMethod( constraintViolation, "findCustomerByName", String.class );
			assertParameterIndex( constraintViolation, 0 );
			assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
			assertEquals( constraintViolation.getRootBean(), customerRepository );
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"findCustomerByName.arg0"
			);
			assertEquals( constraintViolation.getLeafBean(), customerRepository );
			assertEquals( constraintViolation.getInvalidValue(), null );
			assertEquals( constraintViolation.getExecutableParameters(), new Object[] { null } );
			assertEquals( constraintViolation.getExecutableReturnValue(), null );
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
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "may not be null" );
			assertMethod( constraintViolation, "findCustomerByAgeAndName", Integer.class, String.class );
			assertParameterIndex( constraintViolation, 1 );
			assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"findCustomerByAgeAndName.arg1"
			);
			assertEquals( constraintViolation.getExecutableParameters(), new Object[] { 30, null } );
			assertEquals( constraintViolation.getExecutableReturnValue(), null );
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
					e.getConstraintViolations(),
					messagePrefix() + "may not be null",
					messagePrefix() + "must be greater than or equal to 5"
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
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "may not be null" );
			assertMethod( constraintViolation, "persistCustomer", Customer.class );
			assertParameterIndex( constraintViolation, 0 );
			assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
			assertEquals(
					constraintViolation.getPropertyPath().toString(), "persistCustomer.arg0.name"
			);
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getRootBean(), customerRepository );
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
			customerRepository.persistCustomer( customer );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {
			assertEquals( e.getConstraintViolations().size(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "may not be null" );
			assertMethod( constraintViolation, "persistCustomer", Customer.class );
			assertParameterIndex( constraintViolation, 0 );
			assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"persistCustomer.arg0.address.city"
			);
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getRootBean(), customerRepository );
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
			customerRepository.cascadingMapParameter( customers );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {
			assertEquals( e.getConstraintViolations().size(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "may not be null" );
			assertMethod( constraintViolation, "cascadingMapParameter", Map.class );
			assertParameterIndex( constraintViolation, 0 );
			assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"cascadingMapParameter.arg0[Bob].name"
			);
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getRootBean(), customerRepository );
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
			customerRepository.cascadingIterableParameter( customers );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {
			assertEquals( e.getConstraintViolations().size(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "may not be null" );
			assertMethod( constraintViolation, "cascadingIterableParameter", List.class );
			assertParameterIndex( constraintViolation, 0 );
			assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"cascadingIterableParameter.arg0[1].name"
			);
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getRootBean(), customerRepository );
			assertEquals( constraintViolation.getLeafBean(), customer );
			assertEquals( constraintViolation.getInvalidValue(), null );
			assertEquals( constraintViolation.getExecutableParameters(), new Object[] { customers } );
			assertEquals( constraintViolation.getExecutableReturnValue(), null );
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
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "may not be null" );
			assertMethod( constraintViolation, "cascadingArrayParameter", Customer[].class );
			assertParameterIndex( constraintViolation, 0 );
			assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"cascadingArrayParameter.arg0[1].name"
			);
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getRootBean(), customerRepository );
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
			customerRepository.findById( null );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {
			assertEquals( e.getConstraintViolations().size(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "may not be null" );
			assertMethod( constraintViolation, "findById", Long.class );
			assertParameterIndex( constraintViolation, 0 );
			assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
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
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "may not be null" );
			assertMethod( constraintViolation, "foo", Long.class );
			assertParameterIndex( constraintViolation, 0 );
			assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
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
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "may not be null" );
			assertMethod( constraintViolation, "bar", Customer.class );
			assertParameterIndex( constraintViolation, 0 );
			assertMethodValidationType( constraintViolation, ElementKind.PARAMETER );
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getPropertyPath().toString(), "bar.arg0.name" );
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
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "must be greater than or equal to 10" );
			assertMethod( constraintViolation, "baz" );
			assertMethodValidationType( constraintViolation, ElementKind.RETURN_VALUE );
			assertEquals( constraintViolation.getRootBean(), customerRepository );
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getPropertyPath().toString(), "baz.<return value>" );
			assertEquals( constraintViolation.getLeafBean(), customerRepository );
			assertEquals( constraintViolation.getInvalidValue(), 9 );
			assertEquals( constraintViolation.getExecutableParameters(), null );
			assertEquals( constraintViolation.getExecutableReturnValue(), 9 );
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
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "may not be null" );
			assertMethod( constraintViolation, "cascadingReturnValue" );
			assertMethodValidationType( constraintViolation, ElementKind.RETURN_VALUE );
			assertEquals( constraintViolation.getRootBean(), customerRepository );
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
			customerRepository.overriddenMethodWithCascadingReturnValue();
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {
			assertNumberOfViolations( e.getConstraintViolations(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "may not be null" );
			assertMethod( constraintViolation, "overriddenMethodWithCascadingReturnValue" );
			assertMethodValidationType( constraintViolation, ElementKind.RETURN_VALUE );

			assertEquals( constraintViolation.getRootBean(), customerRepository );
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
			customerRepository.cascadingIterableReturnValue();
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {
			assertEquals( e.getConstraintViolations().size(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "may not be null" );
			assertMethod( constraintViolation, "cascadingIterableReturnValue" );
			assertMethodValidationType( constraintViolation, ElementKind.RETURN_VALUE );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"cascadingIterableReturnValue.<return value>[1].name"
			);
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getRootBean(), customerRepository );
			assertEquals( constraintViolation.getLeafBean(), new Customer( null ) );
			assertEquals( constraintViolation.getInvalidValue(), null );
			assertEquals( constraintViolation.getExecutableParameters(), null );
			assertEquals( constraintViolation.getExecutableReturnValue(), Arrays.asList( null, new Customer( null ) ) );
		}
	}

	@Test
	public void cascadingMapReturnValue() {
		try {
			customerRepository.cascadingMapReturnValue();
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {
			Customer customer = new Customer( null );
			Map<String, Customer> expectedReturnValue = newHashMap();
			expectedReturnValue.put( "Bob", customer );

			assertEquals( e.getConstraintViolations().size(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "may not be null" );
			assertMethod( constraintViolation, "cascadingMapReturnValue" );
			assertMethodValidationType( constraintViolation, ElementKind.RETURN_VALUE );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"cascadingMapReturnValue.<return value>[Bob].name"
			);
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getRootBean(), customerRepository );
			assertEquals( constraintViolation.getLeafBean(), customer );
			assertEquals( constraintViolation.getInvalidValue(), null );
			assertEquals( constraintViolation.getExecutableParameters(), null );
			assertEquals( constraintViolation.getExecutableReturnValue(), expectedReturnValue );
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
			assertEquals( constraintViolation.getMessage(), messagePrefix() + "may not be null" );
			assertMethod( constraintViolation, "cascadingArrayReturnValue" );
			assertMethodValidationType( constraintViolation, ElementKind.RETURN_VALUE );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"cascadingArrayReturnValue.<return value>[1].name"
			);
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getRootBean(), customerRepository );
			assertEquals( constraintViolation.getLeafBean(), new Customer( null ) );
			assertEquals( constraintViolation.getInvalidValue(), null );
			assertEquals( constraintViolation.getExecutableParameters(), null );
			assertEquals( constraintViolation.getExecutableReturnValue(), new Object[] { null, new Customer( null ) } );
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
					messagePrefix() + "must be greater than or equal to 5",
					messagePrefix() + "must be greater than or equal to 10"
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
					messagePrefix() + "must be greater than or equal to 5",
					messagePrefix() + "must be greater than or equal to 10"
			);
		}
	}

	@Test
	public void methodValidationSucceedsAsNoConstraintOfValidatedGroupAreViolated() {
		customerRepository.parameterConstraintInGroup( null );
	}

	@Test(expectedExceptions = ConstraintViolationException.class)
	public void methodValidationFailsAsConstraintOfValidatedGroupIsViolated() {
		createProxy( CustomerRepository.ValidationGroup.class );
		customerRepository.parameterConstraintInGroup( null );
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
		customerRepository.getFoo( "" );
	}

	@Test
	public void validationOfCrossParameterConstraint() {
		//given
		DateMidnight startDate = new DateMidnight( 2012, 11, 5 );
		DateMidnight endDate = new DateMidnight( 2012, 11, 4 );

		try {
			//when
			customerRepository.methodWithCrossParameterConstraint( startDate, endDate );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {
			//then
			assertThat( e.getConstraintViolations() ).hasSize( 1 );
			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertCorrectConstraintViolationMessages(
					e.getConstraintViolations(),
					messagePrefix() + "{ConsistentDateParameters.message}"
			);
			assertThat( constraintViolation.getConstraintDescriptor().getAnnotation().annotationType() ).isEqualTo(
					ConsistentDateParameters.class
			);
			assertThat( constraintViolation.getInvalidValue() ).isEqualTo( new Object[] { startDate, endDate } );
			assertEquals( constraintViolation.getLeafBean(), customerRepository );
			assertEquals( constraintViolation.getRootBean(), customerRepository );
			assertThat( constraintViolation.getRootBeanClass() ).isEqualTo( CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getExecutableParameters(), new Object[] { startDate, endDate } );
			assertEquals( constraintViolation.getExecutableReturnValue(), null );

			assertMethod(
					constraintViolation,
					"methodWithCrossParameterConstraint",
					DateMidnight.class,
					DateMidnight.class
			);

			assertNodeNames(
					constraintViolation.getPropertyPath(),
					"methodWithCrossParameterConstraint",
					"<cross-parameter>"
			);
			assertNodeKinds( constraintViolation.getPropertyPath(), ElementKind.METHOD, ElementKind.CROSS_PARAMETER );
		}
	}

	@Test
	public void methodValidationSucceeds() {
		customerRepository.findCustomerByName( "Bob" );
	}

	private void assertMethod(ConstraintViolation<?> constraintViolation, String methodName, Class<?>... parameterTypes) {
		Iterator<Path.Node> nodeIterator = constraintViolation.getPropertyPath().iterator();

		Path.Node node = nodeIterator.next();
		assertNotNull( node );
		assertEquals( node.getName(), methodName );
		assertEquals( node.getKind(), ElementKind.METHOD );
		assertEquals( node.as( Path.MethodNode.class ).getParameterTypes(), Arrays.asList( parameterTypes ) );
	}

	private void assertParameterIndex(ConstraintViolation<?> constraintViolation, Integer index) {
		Iterator<Path.Node> nodeIterator = constraintViolation.getPropertyPath().iterator();

		// first node is method descriptor
		nodeIterator.next();
		Path.Node node = nodeIterator.next();
		ParameterNode parameterNode = node.as( ParameterNode.class );
		assertEquals( parameterNode.getParameterIndex(), index.intValue() );
	}

	private void assertMethodValidationType(ConstraintViolation<?> constraintViolation, ElementKind kind) {
		Iterator<Path.Node> nodeIterator = constraintViolation.getPropertyPath().iterator();

		// first node is method descriptor
		nodeIterator.next();
		Path.Node node = nodeIterator.next();
		assertEquals( node.getKind(), kind );
	}
}
