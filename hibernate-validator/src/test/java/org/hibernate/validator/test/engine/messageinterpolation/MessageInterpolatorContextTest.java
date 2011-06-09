/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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


package org.hibernate.validator.test.engine.messageinterpolation;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.PropertyDescriptor;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.MinDef;
import org.hibernate.validator.engine.MessageInterpolatorContext;
import org.hibernate.validator.testutil.ValidatorUtil;

import static java.lang.annotation.ElementType.FIELD;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.testng.Assert.assertTrue;

/**
 * Tests for HV-333
 *
 * @author Hardy Ferentschik
 */
public class MessageInterpolatorContextTest {

	@org.testng.annotations.Test
	public void testInterpolatorContext() throws Exception {
		// use programmatic mapping api to configure constraint
		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type( Test.class )
				.property( "test", FIELD )
				.constraint( new MinDef().value( 10 ).message( "{foo}" ) );

		// use a easy mock message interpolator to verify the right for verifying that the right MessageInterpolatorContext
		// will be passed
		MessageInterpolator mock = createMock( MessageInterpolator.class );
		HibernateValidatorConfiguration config = ValidatorUtil.getConfiguration( HibernateValidator.class );
		config.messageInterpolator( mock );
		config.addMapping( mapping );

		ValidatorFactory factory = config.buildValidatorFactory();
		Validator validator = factory.getValidator();

		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( Test.class );
		PropertyDescriptor propertyDescriptor = beanDescriptor.getConstraintsForProperty( "test" );
		Set<ConstraintDescriptor<?>> constraintDescriptors = propertyDescriptor.getConstraintDescriptors();
		assertTrue( constraintDescriptors.size() == 1 );

		// prepare the mock interpolator to expect the right interpolate call
		String validatedValue = "value";
		expect(
				mock.interpolate(
						"{foo}",
						new MessageInterpolatorContext( constraintDescriptors.iterator().next(), validatedValue )
				)
		).andReturn( "{foo}" );
		replay( mock );

		Set<ConstraintViolation<Test>> violations = validator.validate( new Test( validatedValue ) );
		assertNumberOfViolations( violations, 1 );

		// verify that the right validatedValue was passed
		verify( mock );
	}

	public static class Test {
		private String test;

		public Test(String test) {
			this.test = test;
		}

		public String getTest() {
			return test;
		}

		public void setTest(String test) {
			this.test = test;
		}
	}
}
