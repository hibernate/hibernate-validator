/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.serialization;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Size;
import jakarta.validation.executable.ExecutableValidator;
import jakarta.validation.groups.Default;
import jakarta.validation.metadata.ConstraintDescriptor;
import jakarta.validation.metadata.ValidateUnwrappedValue;

import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForCharSequence;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.test.internal.engine.serialization.SerializableClass.TestPayload;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.junit.jupiter.api.Test;

/**
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 */
public class ConstraintViolationSerializationTest {

	@Test
	@TestForIssue(jiraKey = { "HV-245", "HV-1485" })
	public void testSuccessfulSerialization() throws Exception {
		Validator validator = ValidatorUtil.getValidator();
		SerializableClass testInstance = new SerializableClass( "s" );
		Set<ConstraintViolation<SerializableClass>> constraintViolations = validator.validate( testInstance );

		byte[] bytes = serialize( constraintViolations );
		Set<ConstraintViolation<?>> deserializedViolations = deserialize( bytes );
		assertThat( deserializedViolations ).containsOnlyViolations(
				violationOf( Size.class )
						.withProperty( "foo" )
		);

		checkSerializedViolation( deserializedViolations.iterator().next(), testInstance, null, null );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1485")
	public void testSuccessfulSerializationWithMethodParameters() throws Exception {
		ExecutableValidator validator = ValidatorUtil.getValidator().forExecutables();
		SerializableClass testInstance = new SerializableClass( "s" );
		Set<ConstraintViolation<SerializableClass>> constraintViolations = validator.validateParameters( testInstance,
				SerializableClass.class.getMethod( "fooParameter", String.class ), new Object[] { "s" } );

		byte[] bytes = serialize( constraintViolations );
		Set<ConstraintViolation<?>> deserializedViolations = deserialize( bytes );
		assertThat( deserializedViolations ).containsOnlyViolations(
				violationOf( Size.class ).withPropertyPath( pathWith()
						.method( "fooParameter" )
						.parameter( "foo", 0 ) ) );

		checkSerializedViolation( deserializedViolations.iterator().next(), testInstance, new Object[] { "s" }, null );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1485")
	public void testSuccessfulSerializationWithMethodReturnValue() throws Exception {
		ExecutableValidator validator = ValidatorUtil.getValidator().forExecutables();
		SerializableClass testInstance = new SerializableClass( "s" );
		Set<ConstraintViolation<SerializableClass>> constraintViolations = validator.validateReturnValue( testInstance,
				SerializableClass.class.getMethod( "fooReturnValue" ), "s" );

		byte[] bytes = serialize( constraintViolations );
		Set<ConstraintViolation<?>> deserializedViolations = deserialize( bytes );
		assertThat( deserializedViolations ).containsOnlyViolations(
				violationOf( Size.class ).withPropertyPath( pathWith()
						.method( "fooReturnValue" )
						.returnValue() ) );

		checkSerializedViolation( deserializedViolations.iterator().next(), testInstance, null, "s" );
	}

	@TestForIssue(jiraKey = "HV-245")
	@Test
	public void testUnSuccessfulSerialization() throws Exception {
		Validator validator = ValidatorUtil.getValidator();
		UnSerializableClass testInstance = new UnSerializableClass();
		Set<ConstraintViolation<UnSerializableClass>> constraintViolations = validator.validate( testInstance );

		assertThatThrownBy( () -> serialize( constraintViolations ) )
				.isInstanceOf( NotSerializableException.class );
	}

	private byte[] serialize(Object o) throws Exception {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ObjectOutput out = new ObjectOutputStream( stream );
		out.writeObject( o );
		out.close();
		byte[] serialized = stream.toByteArray();
		stream.close();
		return serialized;

	}

	private Set<ConstraintViolation<?>> deserialize(byte[] byteData) throws Exception {
		ByteArrayInputStream byteIn = new ByteArrayInputStream( byteData );
		ObjectInputStream in = new ObjectInputStream( byteIn );
		@SuppressWarnings("unchecked")
		Set<ConstraintViolation<?>> deserializedViolations = (Set<ConstraintViolation<?>>) in.readObject();
		in.close();
		byteIn.close();
		return deserializedViolations;
	}

	private void checkSerializedViolation(ConstraintViolation<?> constraintViolation, SerializableClass testInstance, Object[] executableParameters,
			Object executableReturnValue) {
		assertEquals( testInstance, constraintViolation.getLeafBean() );
		assertEquals( testInstance, constraintViolation.getRootBean() );
		assertEquals( SerializableClass.class, constraintViolation.getRootBeanClass() );
		assertEquals( "size must be between 5 and 2147483647", constraintViolation.getMessage() );
		assertEquals( "{jakarta.validation.constraints.Size.message}", constraintViolation.getMessageTemplate() );
		assertEquals( "s", constraintViolation.getInvalidValue() );
		assertArrayEquals( executableParameters, constraintViolation.getExecutableParameters() );
		assertEquals( executableReturnValue, constraintViolation.getExecutableReturnValue() );

		ConstraintDescriptor<?> constraintDescriptor = constraintViolation.getConstraintDescriptor();
		assertEquals( Size.class, constraintDescriptor.getAnnotation().annotationType() );
		assertEquals( CollectionHelper.asSet( Default.class ), constraintDescriptor.getGroups() );
		assertEquals( "{jakarta.validation.constraints.Size.message}", constraintDescriptor.getMessageTemplate() );
		assertEquals( CollectionHelper.asSet( TestPayload.class ), constraintDescriptor.getPayload() );
		assertNull( constraintDescriptor.getValidationAppliesTo() );
		assertEquals( ValidateUnwrappedValue.DEFAULT, constraintDescriptor.getValueUnwrapping() );
		assertTrue( constraintDescriptor.getConstraintValidatorClasses().contains( SizeValidatorForCharSequence.class ) );
	}
}
