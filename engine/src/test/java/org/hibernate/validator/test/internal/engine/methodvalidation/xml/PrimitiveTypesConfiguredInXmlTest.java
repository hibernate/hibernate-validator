/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.xml;

import jakarta.validation.Configuration;
import jakarta.validation.Validator;
import jakarta.validation.metadata.BeanDescriptor;
import jakarta.validation.metadata.MethodDescriptor;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import static org.testng.Assert.assertNotNull;

/**
 * @author Hardy Ferentschik
 */
@TestForIssue(jiraKey = "HV-791")
public class PrimitiveTypesConfiguredInXmlTest {

	private Validator validator;

	@BeforeMethod
	protected void setUp() {
		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping(
				PrimitiveTypesConfiguredInXmlTest.class.getResourceAsStream(
						"primitive-types-mapping.xml"
				)
		);
		validator = configuration.buildValidatorFactory().getValidator();
	}

	@Test
	public void testPrimitiveParametersConstrained() {
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( PrimitiveWrapper.class );

		Class<?>[] primitives = {
				boolean.class,
				char.class,
				double.class,
				float.class,
				long.class,
				int.class,
				short.class,
				byte.class,
		};

		for ( Class<?> primitive : primitives ) {
			char[] stringArray = primitive.getName().toCharArray();
			stringArray[0] = Character.toUpperCase( stringArray[0] );
			String tmp = new String( stringArray );

			MethodDescriptor methodDescriptor = beanDescriptor.getConstraintsForMethod(
					"set" + tmp,
					primitive
			);

			assertNotNull(
					methodDescriptor,
					"MethodDescriptor for set" + tmp + "() is expected to be not null since it is constrained via XML."
			);
		}
	}

	public class PrimitiveWrapper {
		public void setBoolean(boolean b) {
		}

		public void setChar(char c) {
		}

		public void setDouble(double d) {
		}

		public void setFloat(float f) {
		}

		public void setLong(long l) {
		}

		public void setInt(int i) {
		}

		public void setShort(short s) {
		}

		public void setByte(byte b) {
		}
	}
}


