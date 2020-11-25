/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator.test.internal.engine.messageinterpolation;

import org.hibernate.validator.internal.engine.MessageInterpolatorContext;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;
import org.hibernate.validator.messageinterpolation.HibernateMessageInterpolatorContext;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import jakarta.validation.Configuration;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.MessageInterpolator.Context;
import jakarta.validation.Path;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Size;
import jakarta.validation.metadata.BeanDescriptor;
import jakarta.validation.metadata.ConstraintDescriptor;
import jakarta.validation.metadata.PropertyDescriptor;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Set;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 * @author Hardy Ferentschik
 */
public class MessageInterpolatorContextTest {

	private static final String MESSAGE = "{foo}";

	Validator validator;

	@BeforeTest
	public void setUp() {
		validator = getConfiguration()
				.messageInterpolator( new PathResourceBundleMessageInterpolator( new TestResourceBundleLocator() ) )
				.buildValidatorFactory()
				.getValidator();
	}

	@Test
	@TestForIssue(jiraKey = "HV-333")
	public void testContextWithRightDescriptorAndValueAndRootBeanClassIsPassedToMessageInterpolator() {

		// use a easy mock message interpolator for verifying that the right MessageInterpolatorContext
		// will be passed
		MessageInterpolator mock = createMock( MessageInterpolator.class );
		Configuration<?> config = ValidatorUtil.getConfiguration().messageInterpolator( mock );

		Validator validator = config.buildValidatorFactory().getValidator();

		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( TestBean.class );
		PropertyDescriptor propertyDescriptor = beanDescriptor.getConstraintsForProperty( "test" );
		Set<ConstraintDescriptor<?>> constraintDescriptors = propertyDescriptor.getConstraintDescriptors();
		assertTrue( constraintDescriptors.size() == 1 );

		// prepare the mock interpolator to expect the right interpolate call
		String validatedValue = "value";
		expect(
				mock.interpolate(
						MESSAGE,
						new MessageInterpolatorContext(
								constraintDescriptors.iterator().next(),
								validatedValue,
								TestBean.class,
								null,
								Collections.<String, Object>emptyMap(),
								Collections.<String, Object>emptyMap(),
								ExpressionLanguageFeatureLevel.BEAN_METHODS,
								false )
				)
		)
				.andReturn( "invalid" );
		replay( mock );

		Set<ConstraintViolation<TestBean>> violations = validator.validate( new TestBean( validatedValue ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class ).withMessage( "invalid" )
		);

		// verify that the right context was passed
		verify( mock );
	}

	@Test(expectedExceptions = ValidationException.class)
	public void testUnwrapToImplementationCausesValidationException() {
		Context context = new MessageInterpolatorContext( null, null, null, null, Collections.<String, Object>emptyMap(),
				Collections.<String, Object>emptyMap(), ExpressionLanguageFeatureLevel.BEAN_METHODS, false );
		context.unwrap( MessageInterpolatorContext.class );
	}

	@Test
	public void testUnwrapToInterfaceTypesSucceeds() {
		Context context = new MessageInterpolatorContext( null, null, null, null, Collections.<String, Object>emptyMap(),
				Collections.<String, Object>emptyMap(), ExpressionLanguageFeatureLevel.BEAN_METHODS, false );

		MessageInterpolator.Context asMessageInterpolatorContext = context.unwrap( MessageInterpolator.Context.class );
		assertSame( asMessageInterpolatorContext, context );

		HibernateMessageInterpolatorContext asHibernateMessageInterpolatorContext = context.unwrap(
				HibernateMessageInterpolatorContext.class
		);
		assertSame( asHibernateMessageInterpolatorContext, context );

		Object asObject = context.unwrap( Object.class );
		assertSame( asObject, context );
	}

	@Test
	public void testGetRootBeanType() {
		Class<Object> rootBeanType = Object.class;
		MessageInterpolator.Context context = new MessageInterpolatorContext(
				null,
				null,
				rootBeanType,
				null,
				Collections.<String, Object>emptyMap(),
				Collections.<String, Object>emptyMap(),
				ExpressionLanguageFeatureLevel.BEAN_METHODS,
				false );

		assertSame( context.unwrap( HibernateMessageInterpolatorContext.class ).getRootBeanType(), rootBeanType );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1657")
	public void testGetPropertyPath() {
		Path pathMock = createMock( Path.class );
		MessageInterpolator.Context context = new MessageInterpolatorContext(
				null,
				null,
				null,
				pathMock,
				Collections.<String, Object>emptyMap(),
				Collections.<String, Object>emptyMap(),
				ExpressionLanguageFeatureLevel.BEAN_METHODS,
				false );

		assertSame( context.unwrap( HibernateMessageInterpolatorContext.class ).getPropertyPath(), pathMock );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1657")
	public void testUsageOfPathInInterpolation() {
		Employee employee = createEmployee( "farTooLongStreet", "workPlaza" );
		Set<ConstraintViolation<Employee>> constraintViolations = validator.validate( employee );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class )
						.withMessage( "Employee Street should be smaller than 15" )
		);

		employee = createEmployee( "mySquare", "farTooLongStreet" );
		constraintViolations = validator.validate( employee );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class )
						.withMessage( "Company Street should be smaller than 15" )
		);
	}

	private static class TestBean {
		@Size(min = 10, message = MESSAGE)
		private final String test;

		public TestBean(String test) {
			this.test = test;
		}
	}

	/**
	 * Interpolator demonstrator for {@link MessageInterpolatorContextTest#testUsageOfPathInInterpolation}
	 */
	public class PathResourceBundleMessageInterpolator extends ResourceBundleMessageInterpolator {

		public PathResourceBundleMessageInterpolator(ResourceBundleLocator userResourceBundleLocator) {
			super( userResourceBundleLocator );
		}

		@Override
		public String interpolate(String message, Context context) {
			String newMessage = super.interpolate( message, context );
			newMessage = newMessage.replace( "#path#", "{" + pathToString( context ) + "}" );
			return super.interpolate( newMessage, context );
		}

		private String pathToString(Context context) {
			HibernateMessageInterpolatorContext hContext = context.unwrap( HibernateMessageInterpolatorContext.class );
			StringBuilder baseNodeBuilder = new StringBuilder( hContext.getRootBeanType().getSimpleName() );
			for ( Path.Node node : hContext.getPropertyPath() ) {
				if ( node.getName() != null ) {
					baseNodeBuilder.append( "." ).append( node.getName() );
				}
			}
			return baseNodeBuilder.toString();
		}

	}

	/**
	 * creating a test employee with 2 properties of the same type (same annotation).
	 *
	 * @param employeeStreet
	 * @param employerStreet
	 * @return
	 */
	public static Employee createEmployee(String employeeStreet, String employerStreet) {
		Employee employee = new Employee();
		employee.address = new Address();
		employee.address.street = employeeStreet;
		employee.employer = new Employer();
		employee.employer.address = new Address();
		employee.employer.address.street = employerStreet;
		return employee;
	}

	/**
	 * Test bean for {@link MessageInterpolatorContextTest#testUsageOfPathInInterpolation}
	 */
	public static class Address {

		@Size(max = 15)
		private String street;

	}

	/**
	 * Test bean for {@link MessageInterpolatorContextTest#testUsageOfPathInInterpolation}
	 */
	public static class Employee {

		@Valid
		private Address address;

		@Valid
		private Employer employer;
	}

	/**
	 * Test bean for {@link MessageInterpolatorContextTest#testUsageOfPathInInterpolation}
	 */
	public static class Employer {

		@Valid
		private Address address;
	}

	/**
	 * A dummy locator for {@link MessageInterpolatorContextTest#testUsageOfPathInInterpolation}
	 */
	private static class TestResourceBundleLocator implements ResourceBundleLocator {

		private final ResourceBundle resourceBundle;

		public TestResourceBundleLocator() {
			this( new TestResourceBundle() );
		}

		public TestResourceBundleLocator(ResourceBundle bundle) {
			resourceBundle = bundle;
		}

		@Override
		public ResourceBundle getResourceBundle(Locale locale) {
			return resourceBundle;
		}
	}

	/**
	 * A dummy resource bundle for {@link MessageInterpolatorContextTest#testUsageOfPathInInterpolation}
	 */
	private static class TestResourceBundle extends ResourceBundle implements Enumeration<String> {
		private final Map<String, String> testResources;
		Iterator<String> iter;

		public TestResourceBundle() {
			testResources = new HashMap<String, String>();
			// add some test messages
			testResources.put( "Employee.address.street", "Employee Street" );
			testResources.put( "Employee.employer.address.street", "Company Street" );
			testResources.put( "jakarta.validation.constraints.Size.message", "#path# should be smaller than {max}" );
			iter = testResources.keySet().iterator();
		}

		@Override
		public Object handleGetObject(String key) {
			return testResources.get( key );
		}

		@Override
		public Enumeration<String> getKeys() {
			return this;
		}

		@Override
		public boolean hasMoreElements() {
			return iter.hasNext();
		}

		@Override
		public String nextElement() {
			if ( hasMoreElements() ) {
				return iter.next();
			}
			else {
				throw new NoSuchElementException();
			}
		}
	}
}
