/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.xml;

import java.util.List;
import java.util.Set;
import javax.validation.Configuration;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.MethodDescriptor;
import javax.validation.metadata.MethodType;
import javax.validation.metadata.ParameterDescriptor;

import org.testng.annotations.Test;

import org.hibernate.validator.testutil.ValidatorUtil;

import static org.testng.AssertJUnit.assertTrue;

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
		assertTrue( "There should be two constrained methods", methodDescriptors.size() == 2 );
		for ( MethodDescriptor methodDescriptor : methodDescriptors ) {
			assertTrue( "Parameter should be constrained", methodDescriptor.hasConstrainedParameters() );
			List<ParameterDescriptor> parameterDescriptorList = methodDescriptor.getParameterDescriptors();
			for ( ParameterDescriptor parameterDescriptor : parameterDescriptorList ) {
				assertTrue( "Parameter should be constrained", parameterDescriptor.isCascaded() );
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



