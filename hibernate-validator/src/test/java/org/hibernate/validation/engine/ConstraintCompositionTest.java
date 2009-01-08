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
package org.hibernate.validation.engine;

import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.Validation;
import javax.validation.groups.Default;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

import org.hibernate.validation.eg.Actor;
import org.hibernate.validation.eg.Address;
import org.hibernate.validation.eg.Animal;
import org.hibernate.validation.eg.Author;
import org.hibernate.validation.eg.Book;
import org.hibernate.validation.eg.Boy;
import org.hibernate.validation.eg.Customer;
import org.hibernate.validation.eg.Dictonary;
import org.hibernate.validation.eg.Engine;
import org.hibernate.validation.eg.EnglishDictonary;
import org.hibernate.validation.eg.Order;
import org.hibernate.validation.eg.Unconstraint;
import org.hibernate.validation.eg.First;
import org.hibernate.validation.eg.Second;
import org.hibernate.validation.eg.Last;
import org.hibernate.validation.eg.DefaultAlias;
import org.hibernate.validation.eg.FrenchAddress;
import org.hibernate.validation.HibernateValidatorFactoryBuilder;
import org.hibernate.validation.constraints.NotNullConstraint;
import org.hibernate.validation.constraints.SizeContraint;
import org.hibernate.tck.annotations.SpecAssertion;

/**
 * Tests for the implementation of <code>Validator</code>.
 *
 * @author Hardy Ferentschik
 */
public class ConstraintCompositionTest {

	private Validator hibernateValidator;

	//TODO should not be Hibernate specific in most case
	private Validator getHibernateValidator() {
		if ( hibernateValidator == null ) {
			HibernateValidatorFactoryBuilder builder = Validation
					.builderType( HibernateValidatorFactoryBuilder.class )
					.getBuilder();
			hibernateValidator = builder.build().getValidator();
		}
		return hibernateValidator;
	}

	@Test
	public void testComposition() {
		Validator validator = getHibernateValidator();

		FrenchAddress address = new FrenchAddress();
		address.setAddressline1( "10 rue des Treuils" );
		address.setAddressline2( "BP 12 " );
		address.setCity( "Bordeaux" );
		Set<ConstraintViolation<FrenchAddress>> constraintViolations = validator.validate( address );
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		ConstraintViolation<FrenchAddress> violation = constraintViolations.iterator().next();
		assertEquals(
				"Wrong error type",
				NotNullConstraint.class,
				violation.getConstraintDescriptor().getConstraintClass()
		);
		assertEquals( "Wrong message", "may not be null", violation.getInterpolatedMessage() );

		address.setZipCode( "123" );
		constraintViolations = validator.validate( address );
		assertEquals( "Wrong number of constraints", 1, constraintViolations.size() );
		violation = constraintViolations.iterator().next();
		assertEquals(
				"Wrong error type",
				SizeContraint.class,
				violation.getConstraintDescriptor().getConstraintClass()
		);

		address.setZipCode( "33023" );
		constraintViolations = validator.validate( address );
		assertEquals( "Wrong number of constraints", 0, constraintViolations.size() );
		assertEquals( "Wrong message", "size must be between 5 and 5", violation.getInterpolatedMessage() );
	}
}