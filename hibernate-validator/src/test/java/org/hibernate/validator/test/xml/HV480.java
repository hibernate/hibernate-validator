/*
* JBoss, Home of Professional Open Source
* Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.xml;

import static org.hibernate.validator.test.util.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;

import java.lang.annotation.ElementType;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.groups.Default;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.SizeDef;
import org.hibernate.validator.test.util.ValidatorUtil;
import org.testng.annotations.Test;

/**
 * @author Gunnar Morling
 */
public class HV480 {

	/**
	 * HV-480. Class Customer is configured via XML and programmatic API, but only
	 * the constraint configured via XML is evaluated.
	 */
	@Test
	public void testConstraintsFromXmlAndProgrammaticApiAddUp() {

		ConstraintMapping mapping = new ConstraintMapping();
		mapping.type(Customer.class)
			.property("firstName", ElementType.FIELD)
			.constraint(ConstraintDef.create( SizeDef.class).min(2).max(10));
		
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration(HibernateValidator.class);
		configuration.addMapping(mapping);
		configuration.addMapping( HV480.class.getResourceAsStream( "HV-480.xml" ) );

		final ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		final Validator validator = validatorFactory.getValidator();

		Customer customer = new Customer();
		customer.setFirstName("");
		final Set<ConstraintViolation<Customer>> violations = validator.validate( customer, Default.class );

		//should pass but fails. Only the XML constraint is violated.
		assertCorrectConstraintViolationMessages(violations, "size must be between 1 and 10", "size must be between 2 and 10");
	}

}
