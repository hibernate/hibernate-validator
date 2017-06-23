/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator.test.internal.engine.messageinterpolation;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.Set;

import javax.validation.Configuration;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.MessageInterpolator.Context;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.constraints.Size;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.PropertyDescriptor;

import org.hibernate.validator.internal.engine.MessageInterpolatorContext;
import org.hibernate.validator.messageinterpolation.HibernateMessageInterpolatorContext;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class MessageInterpolatorContextTest {

	private static final String MESSAGE = "{foo}";

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
								Collections.<String, Object>emptyMap(),
								Collections.<String, Object>emptyMap()
						)
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
		Context context = new MessageInterpolatorContext( null, null, null, Collections.<String, Object>emptyMap(), Collections.<String, Object>emptyMap() );
		context.unwrap( MessageInterpolatorContext.class );
	}

	@Test
	public void testUnwrapToInterfaceTypesSucceeds() {
		Context context = new MessageInterpolatorContext( null, null, null, Collections.<String, Object>emptyMap(), Collections.<String, Object>emptyMap() );

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
				Collections.<String, Object>emptyMap(),
				Collections.<String, Object>emptyMap()
		);

		assertSame( context.unwrap( HibernateMessageInterpolatorContext.class ).getRootBeanType(), rootBeanType );
	}

	private static class TestBean {
		@Size(min = 10, message = MESSAGE)
		private final String test;

		public TestBean(String test) {
			this.test = test;
		}
	}
}
