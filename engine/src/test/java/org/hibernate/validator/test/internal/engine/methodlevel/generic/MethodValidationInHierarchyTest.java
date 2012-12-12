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
package org.hibernate.validator.test.internal.engine.methodlevel.generic;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.testng.annotations.Test;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;

import static org.hibernate.validator.testutil.ValidatorUtil.getValidatingProxy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * @author Hardy Ferentschik
 */
public class MethodValidationInHierarchyTest {
	@Test
	@TestForIssue(jiraKey = "HV-618")
	public void testParameterConstraintAtGenericMethodFromBaseClassAreEvaluated() {
		Class<?>[] interfaces = new Class<?>[] { SimpleService.class };
		SimpleService<?> service = getValidatingProxy(
				new SimpleServiceImpl(), interfaces, ValidatorUtil.getValidator()
		);

		try {
			service.configure( null );
			fail( "Expected ConstraintViolationException wasn't thrown." );
		}
		catch ( ConstraintViolationException e ) {
			assertEquals( e.getConstraintViolations().size(), 1 );

			ConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertEquals( constraintViolation.getMessage(), "may not be null" );
		}
	}
}
