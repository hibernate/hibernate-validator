/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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



