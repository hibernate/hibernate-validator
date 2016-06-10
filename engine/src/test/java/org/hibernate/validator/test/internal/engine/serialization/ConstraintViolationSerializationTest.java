/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.testng.annotations.Test;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;

/**
 * @author Hardy Ferentschik
 */
public class ConstraintViolationSerializationTest {

	@Test
	@TestForIssue(jiraKey = "HV-245")
	public void testSuccessfulSerialization() throws Exception {
		Validator validator = ValidatorUtil.getValidator();
		SerializableClass testInstance = new SerializableClass();
		Set<ConstraintViolation<SerializableClass>> constraintViolations = validator.validate( testInstance );

		byte[] bytes = serialize( constraintViolations );
		Set<ConstraintViolation<?>> deserializedViolations = deserialize( bytes );
		assertNumberOfViolations( deserializedViolations, 1 );
	}

	@TestForIssue(jiraKey = "HV-245")
	@Test(expectedExceptions = NotSerializableException.class)
	public void testUnSuccessfulSerialization() throws Exception {
		Validator validator = ValidatorUtil.getValidator();
		UnSerializableClass testInstance = new UnSerializableClass();
		Set<ConstraintViolation<UnSerializableClass>> constraintViolations = validator.validate( testInstance );

		serialize( constraintViolations );
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
}
