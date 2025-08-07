/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.serialization;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

/**
 * A <b>sscce</b> (Short, Self Contained, Correct Example) showing that the
 * simple custom Email validation constraint taken from <a href="http://blog.jteam.nl/2009/08/04/bean-validation-integrating-jsr-303-with-spring/"
 * >this blog</a> gives a validation result that is not Serializable with
 * Hibernate Validator 4.0.2.GA with underlying cause that
 * <p>
 * {@code org.hibernate.validator.internal.util.annotationfactory.AnnotationProxy}
 * <p>
 * Note that Hibernate Validator does not guarantee at all that a
 * {@link ConstraintViolation} is Serializable because an entity need not be
 * Serializable, but otherwise there should not be much of a problem (right?).
 *
 * @author Henno Vermeulen
 * @author Hardy Ferentschik
 */
public class CustomConstraintSerializableTest {

	/**
	 * HV-291
	 *
	 * @throws Exception in case the test fails
	 */
	@Test
	public void testSerializeCustomEmail() throws Exception {
		Validator validator = ValidatorUtil.getValidator();

		CustomEmail invalidCustomEmail = new CustomEmail();
		invalidCustomEmail.email = "test@";
		Set<ConstraintViolation<CustomEmail>> constraintViolations = validator.validate( invalidCustomEmail );
		doSerialize( constraintViolations.iterator().next() );
	}

	public static byte[] doSerialize(Object obj) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream( 512 );
		ObjectOutputStream out = new ObjectOutputStream( baos );
		out.writeObject( obj );
		out.close();
		return baos.toByteArray();
	}

	static class CustomEmail implements Serializable {
		@Serial
		private static final long serialVersionUID = -9095271389455131159L;

		@Email
		String email;

	}
}
