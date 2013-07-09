/*
* JBoss, Home of Professional Open Source
* Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.constraints;

import java.util.List;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ValidationException;

import org.testng.annotations.Test;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintViolationCreationContext;
import org.hibernate.validator.internal.engine.path.PathImpl;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathsAreEqual;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 * Tests for the {@link ConstraintValidatorContextImpl}.
 *
 * @author Hardy Ferentschik
 */
public class ConstraintValidatorContextImplTest {

	private static String message = "message";

	@Test
	public void testIterableIndexed() {
		ConstraintValidatorContextImpl context = createEmptyConstraintValidatorContextImpl();
		context.buildConstraintViolationWithTemplate( message )
				.addPropertyNode( "foo" )
				.addPropertyNode( "bar" ).inIterable().atIndex( 3 )
				.addConstraintViolation();

		List<ConstraintViolationCreationContext> constraintViolationCreationContextList = context.getConstraintViolationCreationContexts();
		assertMessageAndPath( constraintViolationCreationContextList.get( 0 ), message, "foo[3].bar" );
	}

	@Test
	public void testIterableKeyed() {
		ConstraintValidatorContextImpl context = createEmptyConstraintValidatorContextImpl();
		context.buildConstraintViolationWithTemplate( message )
				.addPropertyNode( "foo" )
				.addPropertyNode( null ).inIterable().atKey( "test" )
				.addConstraintViolation();

		List<ConstraintViolationCreationContext> constraintViolationCreationContextList = context.getConstraintViolationCreationContexts();
		assertMessageAndPath( constraintViolationCreationContextList.get( 0 ), message, "foo[test]" );
	}

	@Test
	public void testIterableWithKeyFollowedBySimpleNodes() {
		ConstraintValidatorContextImpl context = createEmptyConstraintValidatorContextImpl();

		context.buildConstraintViolationWithTemplate( message )
				.addPropertyNode( "foo" )
				.addPropertyNode( "bar" ).inIterable().atKey( "test" )
				.addPropertyNode( "fubar" )
				.addConstraintViolation();

		List<ConstraintViolationCreationContext> constraintViolationCreationContextList = context.getConstraintViolationCreationContexts();
		assertMessageAndPath( constraintViolationCreationContextList.get( 0 ), message, "foo[test].bar.fubar" );
	}

	@Test
	public void testIterableKeyedAndIndexed() {
		ConstraintValidatorContextImpl context = createEmptyConstraintValidatorContextImpl();
		context.buildConstraintViolationWithTemplate( message )
				.addPropertyNode( "foo" )
				.addPropertyNode( "bar" ).inIterable().atKey( "test" )
				.addPropertyNode( "fubar" ).inIterable().atIndex( 10 )
				.addConstraintViolation();

		List<ConstraintViolationCreationContext> constraintViolationCreationContextList = context.getConstraintViolationCreationContexts();
		assertMessageAndPath( constraintViolationCreationContextList.get( 0 ), message, "foo[test].bar[10].fubar" );
	}

	@Test
	public void testMultipleInIterable() {
		ConstraintValidatorContextImpl context = createEmptyConstraintValidatorContextImpl();
		context.buildConstraintViolationWithTemplate( message )
				.addPropertyNode( "foo" )
				.addPropertyNode( "bar" ).inIterable().atKey( "test" )
				.addPropertyNode( "fubar" ).inIterable()
				.addConstraintViolation();

		List<ConstraintViolationCreationContext> constraintViolationCreationContextList = context.getConstraintViolationCreationContexts();
		assertMessageAndPath( constraintViolationCreationContextList.get( 0 ), message, "foo[test].bar[].fubar" );
	}

	@Test
	public void testMultipleSimpleNodes() {
		ConstraintValidatorContextImpl context = createEmptyConstraintValidatorContextImpl();
		context.buildConstraintViolationWithTemplate( message )
				.addPropertyNode( "foo" )
				.addPropertyNode( "bar" )
				.addPropertyNode( "test" )
				.addConstraintViolation();

		List<ConstraintViolationCreationContext> constraintViolationCreationContextList = context.getConstraintViolationCreationContexts();
		assertMessageAndPath( constraintViolationCreationContextList.get( 0 ), message, "foo.bar.test" );
	}

	@Test
	public void testLongPath() {
		ConstraintValidatorContextImpl context = createEmptyConstraintValidatorContextImpl();
		context.buildConstraintViolationWithTemplate( message )
				.addPropertyNode( "a" )
				.addPropertyNode( "b" ).inIterable().atKey( "key1" )
				.addPropertyNode( "c" ).inIterable()
				.addPropertyNode( "d" )
				.addPropertyNode( "e" )
				.addPropertyNode( "f" ).inIterable().atKey( "key2" )
				.addConstraintViolation();

		List<ConstraintViolationCreationContext> constraintViolationCreationContextList = context.getConstraintViolationCreationContexts();
		assertMessageAndPath( constraintViolationCreationContextList.get( 0 ), message, "a[key1].b[].c.d.e[key2].f" );
	}

	@Test
	public void testMultipleMessages() {
		String message1 = "message1";
		String message2 = "message2";
		ConstraintValidatorContextImpl context = createEmptyConstraintValidatorContextImpl();
		context.buildConstraintViolationWithTemplate( message1 )
				.addPropertyNode( "foo" )
				.addPropertyNode( "bar" ).inIterable().atKey( "key" )
				.addConstraintViolation();
		context.buildConstraintViolationWithTemplate( message2 )
				.addConstraintViolation();

		List<ConstraintViolationCreationContext> constraintViolationCreationContextList = context.getConstraintViolationCreationContexts();
		assertTrue( constraintViolationCreationContextList.size() == 2 );
		assertMessageAndPath( constraintViolationCreationContextList.get( 0 ), message1, "foo[key].bar" );
		assertMessageAndPath( constraintViolationCreationContextList.get( 1 ), message2, "" );
	}

	@Test(expectedExceptions = ValidationException.class)
	public void testUnwrapToImplementationCausesValidationException() {
		ConstraintValidatorContext context = createEmptyConstraintValidatorContextImpl();
		context.unwrap( ConstraintValidatorContextImpl.class );
	}

	@Test
	public void testUnwrapToPublicTypesSucceeds() {
		ConstraintValidatorContext context = createEmptyConstraintValidatorContextImpl();

		ConstraintValidatorContext asConstraintValidatorContext = context.unwrap( ConstraintValidatorContext.class );
		assertSame( asConstraintValidatorContext, context );

		java.lang.Object asObject = context.unwrap( java.lang.Object.class );
		assertSame( asObject, context );
	}

	private ConstraintValidatorContextImpl createEmptyConstraintValidatorContextImpl() {
		PathImpl path = PathImpl.createRootPath();
		path.addBeanNode();

		ConstraintValidatorContextImpl context = new ConstraintValidatorContextImpl(
				null, path, null
		);
		context.disableDefaultConstraintViolation();
		return context;
	}

	private void assertMessageAndPath(ConstraintViolationCreationContext constraintViolationCreationContext, String expectedMessage, String expectedPath) {
		assertTrue(
				pathsAreEqual( constraintViolationCreationContext.getPath(), PathImpl.createPathFromString( expectedPath ) ),
				"Path do not match. Actual: '" + constraintViolationCreationContext.getPath() + "' Expected: '" + expectedPath + "'"
		);
		assertEquals( constraintViolationCreationContext.getMessage(), expectedMessage, "Wrong message" );
	}
}
