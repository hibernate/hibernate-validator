// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.engine.metadata;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintDescriptor;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

import org.slf4j.Logger;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

import org.hibernate.validation.constraints.impl.NotNullValidator;
import org.hibernate.validation.engine.Order;
import org.hibernate.validation.engine.constraintcomposition.GermanAddress;
import org.hibernate.validation.util.LoggerFactory;
import org.hibernate.validation.util.TestUtil;


/**
 * @author Hardy Ferentschik
 */
public class ConstraintDescriptorTest {
	private static final Logger log = LoggerFactory.make();

	@Test
	public void testConstraintDescriptorImmutable() {
		ConstraintDescriptor<?> descriptor = TestUtil.getSingleConstraintDescriptorFor( Order.class, "orderNumber" );

		try {
			descriptor.getGroups().add( Default.class );
			fail( "Should be immutable" );
		}
		catch ( UnsupportedOperationException e ) {
			log.trace( "success" );
		}

		try {
			descriptor.getAttributes().put( "foo", "bar" );
			fail( "Should be immutable" );
		}
		catch ( UnsupportedOperationException e ) {
			log.trace( "success" );
		}

		try {
			descriptor.getConstraintValidatorClasses().add( null );
			fail( "Should be immutable" );
		}
		catch ( UnsupportedOperationException e ) {
			log.trace( "success" );
		}

		try {
			descriptor.getComposingConstraints().add( null );
			fail( "Should be immutable" );
		}
		catch ( UnsupportedOperationException e ) {
			log.trace( "success" );
		}
	}

	@Test
	public void testReportAsSingleViolation() {
		ConstraintDescriptor<?> descriptor = TestUtil.getSingleConstraintDescriptorFor( Order.class, "orderNumber" );
		assertFalse( descriptor.isReportAsSingleViolation() );

		descriptor = TestUtil.getSingleConstraintDescriptorFor( GermanAddress.class, "zipCode" );
		assertTrue( descriptor.isReportAsSingleViolation() );
	}

	@Test
	public void testComposingConstraints() {
		ConstraintDescriptor<?> descriptor = TestUtil.getSingleConstraintDescriptorFor( Order.class, "orderNumber" );
		assertTrue( descriptor.getComposingConstraints().isEmpty() );
	}

	/**
	 * @todo Is getComposingConstraints() recursive and hence the result should be 4?
	 */
	@Test
	public void testEmptyComposingConstraints() {
		ConstraintDescriptor<?> descriptor = TestUtil.getSingleConstraintDescriptorFor(
				GermanAddress.class, "zipCode"
		);
		assertEquals( descriptor.getComposingConstraints().size(), 1, "Wrong number of composing constraints" );
	}

	@Test
	public void testGetAnnotation() {
		ConstraintDescriptor<?> descriptor = TestUtil.getSingleConstraintDescriptorFor( Order.class, "orderNumber" );
		Annotation annotation = descriptor.getAnnotation();
		assertNotNull( annotation );
		assertTrue( annotation instanceof NotNull );
	}

	@Test
	public void testDefaultGroupIsReturned() {
		ConstraintDescriptor<?> descriptor = TestUtil.getSingleConstraintDescriptorFor( Order.class, "orderNumber" );
		Set<Class<?>> groups = descriptor.getGroups();
		assertTrue( groups.size() == 1 );
		assertEquals( groups.iterator().next(), Default.class, "Wrong group" );
	}

	@Test
	public void testGetConstraintValidatorClasses() {
		ConstraintDescriptor<?> descriptor = TestUtil.getSingleConstraintDescriptorFor( Order.class, "orderNumber" );
		assertEquals( descriptor.getConstraintValidatorClasses().get( 0 ), NotNullValidator.class, "Wrong classes" );
	}

	@Test
	public void testGetAttributes() {
		ConstraintDescriptor<?> descriptor = TestUtil.getSingleConstraintDescriptorFor( Order.class, "orderNumber" );
		Map<String, Object> attributes = descriptor.getAttributes();
		assertTrue( attributes.containsKey( "message" ) );
		assertTrue( attributes.containsKey( "groups" ) );
	}
}