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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.UnexpectedTypeException;
import javax.validation.constraints.NotNull;
import javax.validation.metadata.ElementDescriptor;
import javax.validation.metadata.MethodDescriptor;
import javax.validation.metadata.ParameterDescriptor;

import org.joda.time.DateMidnight;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.engine.ValidatorImpl;
import org.hibernate.validator.test.internal.engine.methodlevel.model.Address;
import org.hibernate.validator.test.internal.engine.methodlevel.model.Customer;
import org.hibernate.validator.test.internal.engine.methodlevel.service.ConsistentDateParameters;
import org.hibernate.validator.test.internal.engine.methodlevel.service.CustomerRepository;
import org.hibernate.validator.test.internal.engine.methodlevel.service.CustomerRepositoryImpl;
import org.hibernate.validator.test.internal.engine.methodlevel.service.RepositoryBase;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;

import static javax.validation.metadata.ElementDescriptor.Kind.PARAMETER;
import static javax.validation.metadata.ElementDescriptor.Kind.RETURN_VALUE;
import static org.fest.assertions.Assertions.assertThat;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertConstraintViolation;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidatingProxy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Integration test for the method-level validation related features of {@link ValidatorImpl}.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
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
				new CustomerRepositoryImpl(), ValidatorUtil.getValidator(), groups
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
			assertMethodName( constraintViolation, "findCustomerByName" );
			assertParameterIndex( constraintViolation, 0 );
			assertMethodValidationType( constraintViolation, PARAMETER );
			assertEquals( constraintViolation.getRootBean(), customerRepository );
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"findCustomerByName.arg0"
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
			assertMethodName( constraintViolation, "findCustomerByAgeAndName" );
			assertParameterIndex( constraintViolation, 1 );
			assertMethodValidationType( constraintViolation, PARAMETER );
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"findCustomerByAgeAndName.arg1"
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
			assertMethodName( constraintViolation, "persistCustomer" );
			assertParameterIndex( constraintViolation, 0 );
			assertMethodValidationType( constraintViolation, PARAMETER );
			assertEquals(
					constraintViolation.getPropertyPath().toString(), "persistCustomer.arg0.name"
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
			assertMethodName( constraintViolation, "persistCustomer" );
			assertParameterIndex( constraintViolation, 0 );
			assertMethodValidationType( constraintViolation, PARAMETER );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"persistCustomer.arg0.address.city"
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
			assertMethodName( constraintViolation, "cascadingMapParameter" );
			assertParameterIndex( constraintViolation, 0 );
			assertMethodValidationType( constraintViolation, PARAMETER );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"cascadingMapParameter.arg0[Bob].name"
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
			assertMethodName( constraintViolation, "cascadingIterableParameter" );
			assertParameterIndex( constraintViolation, 0 );
			assertMethodValidationType( constraintViolation, PARAMETER );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"cascadingIterableParameter.arg0[1].name"
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
			assertMethodName( constraintViolation, "cascadingArrayParameter" );
			assertParameterIndex( constraintViolation, 0 );
			assertMethodValidationType( constraintViolation, PARAMETER );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"cascadingArrayParameter.arg0[1].name"
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
			assertMethodName( constraintViolation, "findById" );
			assertParameterIndex( constraintViolation, 0 );
			assertMethodValidationType( constraintViolation, PARAMETER );
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
			assertMethodName( constraintViolation, "foo" );
			assertParameterIndex( constraintViolation, 0 );
			assertMethodValidationType( constraintViolation, PARAMETER );
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
			assertMethodName( constraintViolation, "bar" );
			assertParameterIndex( constraintViolation, 0 );
			assertMethodValidationType( constraintViolation, PARAMETER );
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

			assertEquals( constraintViolation.getMessage(), "must be greater than or equal to 10" );
			assertMethodName( constraintViolation, "baz" );
			assertMethodValidationType( constraintViolation, RETURN_VALUE );
			assertEquals( constraintViolation.getRootBean(), customerRepository );
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals( constraintViolation.getPropertyPath().toString(), "baz.$retval" );
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
			assertMethodName( constraintViolation, "cascadingReturnValue" );
			assertMethodValidationType( constraintViolation, RETURN_VALUE );
			assertEquals( constraintViolation.getRootBean(), customerRepository );
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"cascadingReturnValue.$retval.name"
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
			assertMethodName( constraintViolation, "overriddenMethodWithCascadingReturnValue" );
			assertMethodValidationType( constraintViolation, RETURN_VALUE );
			assertEquals( constraintViolation.getRootBean(), customerRepository );
			assertEquals( constraintViolation.getRootBeanClass(), CustomerRepositoryImpl.class );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"overriddenMethodWithCascadingReturnValue.$retval.name"
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
			assertMethodName( constraintViolation, "cascadingIterableReturnValue" );
			assertMethodValidationType( constraintViolation, RETURN_VALUE );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"cascadingIterableReturnValue.$retval[1].name"
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
			assertMethodName( constraintViolation, "cascadingMapReturnValue" );
			assertMethodValidationType( constraintViolation, RETURN_VALUE );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"cascadingMapReturnValue.$retval[Bob].name"
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
			assertMethodName( constraintViolation, "cascadingArrayReturnValue" );
			assertMethodValidationType( constraintViolation, RETURN_VALUE );
			assertEquals(
					constraintViolation.getPropertyPath().toString(),
					"cascadingArrayReturnValue.$retval[1].name"
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
					"{ConsistentDateParameters.message}"
			);
			assertThat( constraintViolation.getConstraintDescriptor().getAnnotation().annotationType() ).isEqualTo(
					ConsistentDateParameters.class
			);
			assertThat( constraintViolation.getInvalidValue() ).isEqualTo( new Object[] { startDate, endDate } );

			//TODO BVAL-337: Does that make sense?
			assertEquals( constraintViolation.getLeafBean(), customerRepository );
			assertEquals( constraintViolation.getRootBean(), customerRepository );
			assertThat( constraintViolation.getRootBeanClass() ).isEqualTo( CustomerRepositoryImpl.class );

			assertMethodName( constraintViolation, "methodWithCrossParameterConstraint" );
		}
	}

	@Test
	public void methodValidationSucceeds() {
		customerRepository.findCustomerByName( "Bob" );
	}

	private void assertMethodName(ConstraintViolation<?> constraintViolation, String methodName) {
		Iterator<Path.Node> nodeIterator = constraintViolation.getPropertyPath().iterator();

		Path.Node node = nodeIterator.next();
		assertNotNull( node );
		assertEquals( node.getName(), methodName );
		ElementDescriptor descriptor = node.getElementDescriptor();
		assertNotNull( descriptor );

		assertEquals( ElementDescriptor.Kind.METHOD, descriptor.getKind() );
		MethodDescriptor methodDescriptor = descriptor.as( MethodDescriptor.class );
		assertEquals( methodDescriptor.getName(), methodName );
	}

	private void assertParameterIndex(ConstraintViolation<?> constraintViolation, Integer index) {
		Iterator<Path.Node> nodeIterator = constraintViolation.getPropertyPath().iterator();

		// first node is method descriptor
		nodeIterator.next();
		Path.Node node = nodeIterator.next();
		ParameterDescriptor parameterDescriptor = node.getElementDescriptor().as( ParameterDescriptor.class );
		assertEquals( parameterDescriptor.getIndex(), index.intValue() );
	}

	private void assertMethodValidationType(ConstraintViolation<?> constraintViolation, ElementDescriptor.Kind kind) {
		Iterator<Path.Node> nodeIterator = constraintViolation.getPropertyPath().iterator();

		// first node is method descriptor
		nodeIterator.next();
		Path.Node node = nodeIterator.next();
		ElementDescriptor descriptor = node.getElementDescriptor();
		assertNotNull( descriptor );
		assertTrue( kind.equals( descriptor.getKind() ) );
	}
}
