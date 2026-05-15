/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.*;

import java.util.HashMap;
import java.util.Map;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.ValidationException;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test for {@link ConstraintViolationImpl}.
 *
 * @author Shamkhal Maharramov
 */
public class ConstraintViolationImplTest {

	private ConstraintViolation<Object> violation;
	private ConstraintDescriptor<?> descriptor;
	private PathImpl path;

	@BeforeMethod
	public void setUp() {
		descriptor = createMock( ConstraintDescriptor.class );
		path = PathImpl.createPathFromString( "property" );
		violation = ConstraintViolationImpl.forBeanValidation(
				"Invalid value",
				null,
				null,
				"Invalid value",
				Object.class,
				null,
				null,
				null,
				path,
				descriptor,
				Object.class
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-2052")
	public void testInitializationCreatesNonNullViolation() {
		replay( descriptor );
		assertNotNull( violation );
		verify( descriptor );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2052")
	public void testNullValuesCreateNonNullViolation() {
		ConstraintViolation<Object> nullViolation = ConstraintViolationImpl
				.forBeanValidation(
						null, null, null, null, null, null, null, null, null, null, null );
		assertNotNull( nullViolation );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2052")
	public void testEqualityAndHashCodeForIdenticalViolations() {
		ConstraintViolation<Object> anotherViolation = ConstraintViolationImpl
				.forBeanValidation( "Invalid value", null, null, "Invalid value", Object.class, null, null, null, path,
						descriptor, null );
		replay( descriptor );
		assertTrue( violation.equals( anotherViolation ) );
		assertEquals( violation.hashCode(), anotherViolation.hashCode() );
		verify( descriptor );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2052")
	public void testStringRepresentationMatchesExpectedFormat() {
		String expectedString = "ConstraintViolationImpl{interpolatedMessage='Invalid value', propertyPath=property, rootBeanClass=class java.lang.Object, messageTemplate='Invalid value'}";
		replay( descriptor );
		assertEquals( violation.toString(), expectedString );
		verify( descriptor );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2052")
	public void testDifferentMessageTemplatesAreHandledCorrectly() {
		ConstraintViolation<Object> violationWithDifferentMessage = ConstraintViolationImpl.forBeanValidation(
				"Another invalid value", null, null, "Another invalid value", Object.class, null, null, null, path,
				descriptor, null );
		replay( descriptor );
		assertEquals( violationWithDifferentMessage.getMessageTemplate(), "Another invalid value" );
		verify( descriptor );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2052")
	public void testGettersReturnCorrectNonNullValues() {
		Object rootBean = new Object();
		Object leafBeanInstance = new Object();
		Object value = new Object();
		ConstraintViolation<Object> nonNullViolation = ConstraintViolationImpl.forBeanValidation(
				"Invalid value", null, null, "Invalid value", Object.class, rootBean, leafBeanInstance, value, path,
				descriptor, null );
		replay( descriptor );
		assertNotNull( nonNullViolation.getRootBean() );
		assertNotNull( nonNullViolation.getLeafBean() );
		assertNotNull( nonNullViolation.getInvalidValue() );
		verify( descriptor );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2052")
	public void testDynamicPayloadIsRetrievedCorrectly() {
		Object dynamicPayload = new Object();
		ConstraintViolation<Object> violationWithPayload = ConstraintViolationImpl.forBeanValidation(
				"Invalid value", null, null, "Invalid value", Object.class, null, null, null, path, descriptor,
				dynamicPayload );
		replay( descriptor );
		assertEquals( ( (ConstraintViolationImpl<Object>) violationWithPayload ).getDynamicPayload( Object.class ),
				dynamicPayload );
		verify( descriptor );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2052")
	public void testExpressionVariablesAreRetrievedCorrectly() {
		Map<String, Object> expressionVariables = new HashMap<>();
		expressionVariables.put( "key", "value" );
		ConstraintViolation<Object> violationWithExpressionVariables = ConstraintViolationImpl.forBeanValidation(
				"Invalid value", null, expressionVariables, "Invalid value", Object.class, null, null, null, path,
				descriptor, null );
		replay( descriptor );
		assertEquals( ( (ConstraintViolationImpl<Object>) violationWithExpressionVariables ).getExpressionVariables(),
				expressionVariables );
		verify( descriptor );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2052")
	public void testMessageParametersAreRetrievedCorrectly() {
		Map<String, Object> messageParameters = new HashMap<>();
		messageParameters.put( "param", "value" );
		ConstraintViolation<Object> violationWithMessageParameters = ConstraintViolationImpl.forBeanValidation(
				"Invalid value", messageParameters, null, "Invalid value", Object.class, null, null, null, path,
				descriptor, null );
		replay( descriptor );
		assertEquals( ( (ConstraintViolationImpl<Object>) violationWithMessageParameters ).getMessageParameters(),
				messageParameters );
		verify( descriptor );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2052")
	public void testEqualsMethodForSelfComparison() {
		ConstraintViolation<Object> violation = ConstraintViolationImpl.forBeanValidation(
				"Invalid value", null, null, "Invalid value", Object.class, null, null, null, path, descriptor, null );

		assertEquals( violation, violation );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2052")
	public void testEqualsMethodForNullComparison() {
		ConstraintViolation<Object> violation = ConstraintViolationImpl.forBeanValidation(
				"Invalid value", null, null, "Invalid value", Object.class, null, null, null, path, descriptor, null );

		assertNotEquals( violation, null );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2052")
	public void testEqualsMethodForDifferentClassComparison() {
		ConstraintViolation<Object> violation = ConstraintViolationImpl.forBeanValidation(
				"Invalid value", null, null, "Invalid value", Object.class, null, null, null, path, descriptor, null );
		Object differentClassObject = new Object();

		assertNotEquals( differentClassObject, violation );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2052")
	public void testEqualsMethodForInterpolatedMessage() {
		ConstraintViolation<Object> violation1 = ConstraintViolationImpl.forBeanValidation(
				null, null, null, "InterpolatedMessage1", Object.class, null, null, null, path, descriptor, null );
		ConstraintViolation<Object> violation2 = ConstraintViolationImpl.forBeanValidation(
				null, null, null, "InterpolatedMessage2", Object.class, null, null, null, path, descriptor, null );

		assertNotEquals( violation2, violation1 );
	}


	@Test
	@TestForIssue(jiraKey = "HV-2052")
	public void testEqualsMethodForMessageTemplate() {
		ConstraintViolation<Object> violation1 = ConstraintViolationImpl.forBeanValidation(
				"Template1", null, null, "Invalid value", Object.class, null, null, null, path, descriptor, null );
		ConstraintViolation<Object> violation2 = ConstraintViolationImpl.forBeanValidation(
				"Template2", null, null, "Invalid value", Object.class, null, null, null, path, descriptor, null );

		assertNotEquals( violation2, violation1 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2052")
	public void testEqualsMethodForPropertyPath() {
		Path path1 = PathImpl.createPathFromString( "path1" );
		Path path2 = PathImpl.createPathFromString( "path2" );

		ConstraintViolation<Object> violation1 = ConstraintViolationImpl.forBeanValidation(
				"Invalid value", null, null, "Invalid value", Object.class, null, null, null, path1, descriptor, null );
		ConstraintViolation<Object> violation2 = ConstraintViolationImpl.forBeanValidation(
				"Invalid value", null, null, "Invalid value", Object.class, null, null, null, path2, descriptor, null );

		assertNotEquals( violation2, violation1 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2052")
	public void testEqualsMethodForRootBean() {
		Object rootBean1 = new Object();
		Object rootBean2 = new Object();

		ConstraintViolation<Object> violation1 = ConstraintViolationImpl.forBeanValidation(
				"Invalid value", null, null, "Invalid value", Object.class, rootBean1, null, null, path, descriptor, null );
		ConstraintViolation<Object> violation2 = ConstraintViolationImpl.forBeanValidation(
				"Invalid value", null, null, "Invalid value", Object.class, rootBean2, null, null, path, descriptor, null );

		assertNotEquals( violation2, violation1 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2052")
	public void testEqualsMethodForLeafBeanInstance() {
		Object leafBean1 = new Object();
		Object leafBean2 = new Object();

		ConstraintViolation<Object> violation1 = ConstraintViolationImpl.forBeanValidation(
				"Invalid value", null, null, "Invalid value", Object.class, null, leafBean1, null, path, descriptor, null );
		ConstraintViolation<Object> violation2 = ConstraintViolationImpl.forBeanValidation(
				"Invalid value", null, null, "Invalid value", Object.class, null, leafBean2, null, path, descriptor, null );

		assertNotEquals( violation2, violation1 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2052")
	public void testEqualsMethodForValue() {

		ConstraintViolation<Object> violation1 = ConstraintViolationImpl.forBeanValidation(
				null, null, null, null, Object.class, null, null, "value1", path, descriptor, null );
		ConstraintViolation<Object> violation2 = ConstraintViolationImpl.forBeanValidation(
				null, null, null, null, Object.class, null, null, "value2", path, descriptor, null );

		assertNotEquals( violation2, violation1 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2052")
	public void testEqualsMethodForConstraintDescriptor() {
		ConstraintDescriptor<?> descriptor1 = createMock( ConstraintDescriptor.class );
		ConstraintDescriptor<?> descriptor2 = createMock( ConstraintDescriptor.class );

		ConstraintViolation<Object> violation1 = ConstraintViolationImpl.forBeanValidation(
				null, null, null, null, Object.class, null, null, null, path, descriptor1, null );
		ConstraintViolation<Object> violation2 = ConstraintViolationImpl.forBeanValidation(
				null, null, null, null, Object.class, null, null, null, path, descriptor2, null );

		assertNotEquals( violation2, violation1 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2052")
	public void testUnwrapToConstraintViolation() {
		ConstraintViolationImpl<Object> violation = (ConstraintViolationImpl<Object>) ConstraintViolationImpl.forBeanValidation(
				"Invalid value", null, null, "Invalid value", Object.class, null, null, null, path, descriptor, null );
		ConstraintViolation<?> unwrapped = violation.unwrap( ConstraintViolation.class );
		assertNotNull( unwrapped );
		assertSame( violation, unwrapped );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2052")
	public void testUnwrapToUnsupportedType() {
		ConstraintViolationImpl<Object> violation = (ConstraintViolationImpl<Object>) ConstraintViolationImpl.forBeanValidation(
				"Invalid value", null, null, "Invalid value", Object.class, null, null, null, path, descriptor, null );
		try {
			violation.unwrap( String.class );
			fail( "Expected an exception when unwrapping to unsupported type" );
		}
		catch (Exception e) {
			assertTrue( e instanceof ValidationException );
		}
	}
}
