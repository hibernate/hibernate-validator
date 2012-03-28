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

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.testng.annotations.Test;

import org.hibernate.validator.testutil.ValidatorUtil;


/**
 * A <b>sscce</b> (Short, Self Contained, Correct Example) showing that the
 * simple custom Email validation constraint taken from <a href="http://blog.jteam.nl/2009/08/04/bean-validation-integrating-jsr-303-with-spring/"
 * >this blog</a> gives a validation result that is not Serializable with
 * Hibernate Validator 4.0.2.GA with underlying cause that
 * <p/>
 * <code>org.hibernate.validator.internal.util.annotationfactory.AnnotationProxy</code>
 * <p/>
 * <p/>
 * Note that Hibernate Validator does not guarantee at all that a
 * {@link ConstraintViolation} is Serializable because an entity need not be
 * Serializable, but otherwise there should not be much of a problem (right?).
 *
 * @author Henno Vermeulen
 * @author Hardy Ferentschik
 */
public class CustomConstraintSerializableTest {

	@Test
	public void testSerializeHibernateEmail() throws Exception {
		Validator validator = ValidatorUtil.getValidator();

		HibernateEmail invalidHibernateEmail = new HibernateEmail();
		invalidHibernateEmail.email = "test@";

		Set<ConstraintViolation<HibernateEmail>> constraintViolations = validator.validate( invalidHibernateEmail );
		doSerialize( constraintViolations.iterator().next() );
	}

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
		private static final long serialVersionUID = -9095271389455131159L;

		@Email
		String email;

	}

	static class HibernateEmail implements Serializable {
		private static final long serialVersionUID = 7206154160792549270L;

		@org.hibernate.validator.constraints.Email
		String email;
	}
}

