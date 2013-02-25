/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
import org.hibernate.validator.testutil.ValidatorUtil;

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
