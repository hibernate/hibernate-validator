/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertPathEquals;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ValidationException;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintViolationCreationContext;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;
import org.hibernate.validator.testutil.ConstraintViolationAssert.PathExpectation;
import org.testng.annotations.Test;

/**
 * Tests for the {@link ConstraintValidatorContextImpl}.
 *
 * @author Hardy Ferentschik
 * @author Guillaume Smet
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
		assertMessageAndPath( constraintViolationCreationContextList.get( 0 ), message, pathWith()
				.property( "foo" )
				.property( "bar", true, null, 3 ) );
	}

	@Test
	public void testIterableKeyed() {
		ConstraintValidatorContextImpl context = createEmptyConstraintValidatorContextImpl();
		context.buildConstraintViolationWithTemplate( message )
				.addPropertyNode( "foo" )
				.addPropertyNode( null ).inIterable().atKey( "test" )
				.addConstraintViolation();

		List<ConstraintViolationCreationContext> constraintViolationCreationContextList = context.getConstraintViolationCreationContexts();
		assertMessageAndPath( constraintViolationCreationContextList.get( 0 ), message, pathWith()
				.property( "foo" )
				.property( null, true, "test", null ) );
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
		assertMessageAndPath( constraintViolationCreationContextList.get( 0 ), message, pathWith()
				.property( "foo" )
				.property( "bar", true, "test", null )
				.property( "fubar" ) );
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
		assertMessageAndPath( constraintViolationCreationContextList.get( 0 ), message, pathWith()
				.property( "foo" )
				.property( "bar", true, "test", null )
				.property( "fubar", true, null, 10 ) );
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
		assertMessageAndPath( constraintViolationCreationContextList.get( 0 ), message, pathWith()
				.property( "foo" )
				.property( "bar", true, "test", null )
				.property( "fubar", true, null, null ) );
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
		assertMessageAndPath( constraintViolationCreationContextList.get( 0 ), message, pathWith()
				.property( "foo" )
				.property( "bar" )
				.property( "test" ) );
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
		assertMessageAndPath( constraintViolationCreationContextList.get( 0 ), message, pathWith()
				.property( "a" )
				.property( "b", true, "key1", null )
				.property( "c", true, null, null )
				.property( "d" )
				.property( "e" )
				.property( "f", true, "key2", null ) );
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
		assertMessageAndPath( constraintViolationCreationContextList.get( 0 ), message1, pathWith()
				.property( "foo" )
				.property( "bar", true, "key", null ) );
		assertMessageAndPath( constraintViolationCreationContextList.get( 1 ), message2, pathWith().bean() );
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

	@Test
	public void testInContainer() {
		ConstraintValidatorContextImpl context = createEmptyConstraintValidatorContextImpl();
		context.buildConstraintViolationWithTemplate( message )
				.addPropertyNode( "foo" )
				.addPropertyNode( "bar" ).inContainer( Map.class, 1 ).inIterable().atKey( "test" )
				.addPropertyNode( "fubar" ).inIterable()
				.addConstraintViolation();

		List<ConstraintViolationCreationContext> constraintViolationCreationContextList = context.getConstraintViolationCreationContexts();
		assertMessageAndPath( constraintViolationCreationContextList.get( 0 ), message, pathWith()
				.property( "foo" )
				.property( "bar", true, "test", null, Map.class, 1 )
				.property( "fubar", true, null, null ) );
	}

	@Test
	public void testContainerElementNode() {
		ConstraintValidatorContextImpl context = createEmptyConstraintValidatorContextImpl();
		context.buildConstraintViolationWithTemplate( message )
				.addPropertyNode( "foo" )
				.addPropertyNode( "bar" ).inContainer( Map.class, 0 ).inIterable().atKey( "test" )
				.addContainerElementNode( "<map value>", Map.class, 1 )
						.inIterable().atKey( "key" )
				.addContainerElementNode( "<list element>", List.class, 0 )
						.inIterable().atIndex( 3 )
				.addConstraintViolation();

		List<ConstraintViolationCreationContext> constraintViolationCreationContextList = context.getConstraintViolationCreationContexts();
		assertMessageAndPath( constraintViolationCreationContextList.get( 0 ), message, pathWith()
				.property( "foo" )
				.property( "bar", true, "test", null, Map.class, 0 )
				.containerElement( "<map value>", true, "key", null, Map.class, 1 )
				.containerElement( "<list element>", true, null, 3, List.class, 0 ) );
	}

	private ConstraintValidatorContextImpl createEmptyConstraintValidatorContextImpl() {
		PathImpl path = PathImpl.createRootPath();
		path.addBeanNode();

		ConstraintValidatorContextImpl context = new ConstraintValidatorContextImpl( null, path, null, null, ExpressionLanguageFeatureLevel.BEAN_PROPERTIES,
				ExpressionLanguageFeatureLevel.NONE );
		context.disableDefaultConstraintViolation();
		return context;
	}

	private void assertMessageAndPath(ConstraintViolationCreationContext constraintViolationCreationContext, String expectedMessage, PathExpectation expectedPath) {
		assertPathEquals( constraintViolationCreationContext.getPath(), expectedPath );
		assertEquals( constraintViolationCreationContext.getMessage(), expectedMessage, "Wrong message" );
	}
}
