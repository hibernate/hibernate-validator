// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validator.xml;

import java.util.Set;
import javax.validation.Configuration;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.groups.Default;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class XmlMappingTest {

	@Test
	/**
	 * HV-214
	 */
	public void testConstraintInheritanceWithXmlConfiguration() {

		final Configuration<?> configuration = Validation.byDefaultProvider().configure();
		configuration.addMapping( XmlMappingTest.class.getResourceAsStream( "mapping.xml" ) );

		final ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		final Validator validator = validatorFactory.getValidator();

		final Set<ConstraintViolation<Customer>> violations = validator.validate( new Customer(), Default.class );

		assertEquals( violations.size(), 1 );
	}
}
