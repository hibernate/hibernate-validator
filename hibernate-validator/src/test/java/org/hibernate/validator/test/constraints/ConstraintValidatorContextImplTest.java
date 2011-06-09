/*
* JBoss, Home of Professional Open Source
* Copyright 2009-2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import org.testng.annotations.Test;

import org.hibernate.validator.engine.ConstraintValidatorContextImpl;
import org.hibernate.validator.engine.MessageAndPath;
import org.hibernate.validator.engine.PathImpl;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathsAreEqual;
import static org.testng.Assert.assertEquals;
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
				.addNode( "foo" )
				.addNode( "bar" ).inIterable().atIndex( 3 )
				.addConstraintViolation();

		List<MessageAndPath> messageAndPathList = context.getMessageAndPathList();
		assertMessageAndPath( messageAndPathList.get( 0 ), message, "foo[3].bar" );
	}

	@Test
	public void testIterableKeyed() {
		ConstraintValidatorContextImpl context = createEmptyConstraintValidatorContextImpl();
		context.buildConstraintViolationWithTemplate( message )
				.addNode( "foo" )
				.addNode( null ).inIterable().atKey( "test" )
				.addConstraintViolation();

		List<MessageAndPath> messageAndPathList = context.getMessageAndPathList();
		assertMessageAndPath( messageAndPathList.get( 0 ), message, "foo[test]" );
	}

	@Test
	public void testIterableWithKeyFollowedBySimpleNodes() {
		ConstraintValidatorContextImpl context = createEmptyConstraintValidatorContextImpl();

		context.buildConstraintViolationWithTemplate( message )
				.addNode( "foo" )
				.addNode( "bar" ).inIterable().atKey( "test" )
				.addNode( "fubar" )
				.addConstraintViolation();

		List<MessageAndPath> messageAndPathList = context.getMessageAndPathList();
		assertMessageAndPath( messageAndPathList.get( 0 ), message, "foo[test].bar.fubar" );
	}

	@Test
	public void testIterableKeyedAndIndexed() {
		ConstraintValidatorContextImpl context = createEmptyConstraintValidatorContextImpl();
		context.buildConstraintViolationWithTemplate( message )
				.addNode( "foo" )
				.addNode( "bar" ).inIterable().atKey( "test" )
				.addNode( "fubar" ).inIterable().atIndex( 10 )
				.addConstraintViolation();

		List<MessageAndPath> messageAndPathList = context.getMessageAndPathList();
		assertMessageAndPath( messageAndPathList.get( 0 ), message, "foo[test].bar[10].fubar" );
	}

	@Test
	public void testMultipleInIterable() {
		ConstraintValidatorContextImpl context = createEmptyConstraintValidatorContextImpl();
		context.buildConstraintViolationWithTemplate( message )
				.addNode( "foo" )
				.addNode( "bar" ).inIterable().atKey( "test" )
				.addNode( "fubar" ).inIterable()
				.addConstraintViolation();

		List<MessageAndPath> messageAndPathList = context.getMessageAndPathList();
		assertMessageAndPath( messageAndPathList.get( 0 ), message, "foo[test].bar[].fubar" );
	}

	@Test
	public void testMultipleSimpleNodes() {
		ConstraintValidatorContextImpl context = createEmptyConstraintValidatorContextImpl();
		context.buildConstraintViolationWithTemplate( message )
				.addNode( "foo" )
				.addNode( "bar" )
				.addNode( "test" )
				.addConstraintViolation();

		List<MessageAndPath> messageAndPathList = context.getMessageAndPathList();
		assertMessageAndPath( messageAndPathList.get( 0 ), message, "foo.bar.test" );
	}

	@Test
	public void testLongPath() {
		ConstraintValidatorContextImpl context = createEmptyConstraintValidatorContextImpl();
		context.buildConstraintViolationWithTemplate( message )
				.addNode( "a" )
				.addNode( "b" ).inIterable().atKey( "key1" )
				.addNode( "c" ).inIterable()
				.addNode( "d" )
				.addNode( "e" )
				.addNode( "f" ).inIterable().atKey( "key2" )
				.addConstraintViolation();

		List<MessageAndPath> messageAndPathList = context.getMessageAndPathList();
		assertMessageAndPath( messageAndPathList.get( 0 ), message, "a[key1].b[].c.d.e[key2].f" );
	}

	@Test
	public void testMultipleMessages() {
		String message1 = "message1";
		String message2 = "message2";
		ConstraintValidatorContextImpl context = createEmptyConstraintValidatorContextImpl();
		context.buildConstraintViolationWithTemplate( message1 )
				.addNode( "foo" )
				.addNode( "bar" ).inIterable().atKey( "key" )
				.addConstraintViolation();
		context.buildConstraintViolationWithTemplate( message2 )
				.addConstraintViolation();

		List<MessageAndPath> messageAndPathList = context.getMessageAndPathList();
		assertTrue( messageAndPathList.size() == 2 );
		assertMessageAndPath( messageAndPathList.get( 0 ), message1, "foo[key].bar" );
		assertMessageAndPath( messageAndPathList.get( 1 ), message2, "" );
	}

	private ConstraintValidatorContextImpl createEmptyConstraintValidatorContextImpl() {
		ConstraintValidatorContextImpl context = new ConstraintValidatorContextImpl(
				PathImpl.createRootPath(), null
		);
		context.disableDefaultConstraintViolation();
		return context;
	}

	private void assertMessageAndPath(MessageAndPath messageAndPath, String expectedMessage, String expectedPath) {
		assertTrue(
				pathsAreEqual( messageAndPath.getPath(), PathImpl.createPathFromString( expectedPath ) ),
				"Path do not match. Actual: '" + messageAndPath.getPath() + "' Expected: '" + expectedPath + "'"
		);
		assertEquals( messageAndPath.getMessage(), expectedMessage, "Wrong message" );
	}
}
