/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.xml;

import java.util.List;
import java.util.Set;
import jakarta.validation.Configuration;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.metadata.BeanDescriptor;
import jakarta.validation.metadata.MethodDescriptor;
import jakarta.validation.metadata.MethodType;
import jakarta.validation.metadata.ParameterDescriptor;

import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/**
 * Test that arrays and varargs can be specified in XML with and without default package
 *
 * @author Hardy Ferentschik
 */
public class DefaultPackageInVarargParameterTest {
	Validator validator;

	@Test
	public void testArrayParametersAreConfigurableWithAndWithoutDefaultPackage() {
		Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping(
				DefaultPackageInVarargParameterTest.class.getResourceAsStream(
						"array-and-vararg-parameter-mapping.xml"
				)
		);
		ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		validator = validatorFactory.getValidator();

		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( Foo.class );
		Set<MethodDescriptor> methodDescriptors = beanDescriptor.getConstrainedMethods( MethodType.NON_GETTER );
		assertTrue( methodDescriptors.size() == 2, "There should be two constrained methods" );
		for ( MethodDescriptor methodDescriptor : methodDescriptors ) {
			assertTrue( methodDescriptor.hasConstrainedParameters(), "Parameter should be constrained" );
			List<ParameterDescriptor> parameterDescriptorList = methodDescriptor.getParameterDescriptors();
			for ( ParameterDescriptor parameterDescriptor : parameterDescriptorList ) {
				assertTrue( parameterDescriptor.isCascaded(), "Parameter should be constrained" );
			}
		}
	}

	public static class Foo {
		public void fubar(Bar[] barArray) {

		}

		public void snafu(Bar... barVarArg) {

		}
	}

	public static class Bar {
	}
}



