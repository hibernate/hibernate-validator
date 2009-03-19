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

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import org.hibernate.validation.constraints.NotNullValidator;
import org.hibernate.validation.engine.Order;
import org.hibernate.validation.engine.constraintcomposition.GermanAddress;
import org.hibernate.validation.util.TestUtil;


/**
 * @author Hardy Ferentschik
 */
public class ConstraintDescriptorTest {
	@Test
	public void testConstraintDescriptorImmutable() {
		ConstraintDescriptor<?> descriptor = TestUtil.getSingleConstraintDescriptorFor( Order.class, "orderNumber" );

		try {
			descriptor.getGroups().add( Default.class );
			fail( "Should be immutable" );
		}
		catch ( UnsupportedOperationException e ) {

		}

		try {
			descriptor.getAttributes().put( "foo", "bar" );
			fail( "Should be immutable" );
		}
		catch ( UnsupportedOperationException e ) {

		}

		try {
			descriptor.getConstraintValidatorClasses().add( null );
			fail( "Should be immutable" );
		}
		catch ( UnsupportedOperationException e ) {

		}

		try {
			descriptor.getComposingConstraints().add( null );
			fail( "Should be immutable" );
		}
		catch ( UnsupportedOperationException e ) {

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
		assertEquals( "Wrong number of composing constraints", 1, descriptor.getComposingConstraints().size() );
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
		assertEquals( "Wrong group", Default.class, groups.iterator().next() );
	}

	@Test
	public void testGetConstraintValidatorClasses() {
		ConstraintDescriptor<?> descriptor = TestUtil.getSingleConstraintDescriptorFor( Order.class, "orderNumber" );
		assertEquals( "Wrong classes", NotNullValidator.class, descriptor.getConstraintValidatorClasses().get( 0 ) );
	}

	@Test
	public void testGetAttributes() {
		ConstraintDescriptor<?> descriptor = TestUtil.getSingleConstraintDescriptorFor( Order.class, "orderNumber" );
		Map<String, Object> attributes = descriptor.getAttributes();
		assertTrue( attributes.containsKey( "message" ) );
		assertTrue( attributes.containsKey( "groups" ) );
	}
}