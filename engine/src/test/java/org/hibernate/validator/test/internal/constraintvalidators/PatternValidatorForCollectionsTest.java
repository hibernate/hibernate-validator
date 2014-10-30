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
package org.hibernate.validator.test.internal.constraintvalidators;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.internal.constraintvalidators.PatternValidatorForCollection;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.testng.annotations.Test;

/**
 * @author Radoslaw Smogura
 */
public class PatternValidatorForCollectionsTest {

	/** Creates test validator with. */
	private PatternValidatorForCollection createTestValidator(String regex, String message) {
		AnnotationDescriptor<Pattern> descriptor = new AnnotationDescriptor<Pattern>( Pattern.class );
		descriptor.setValue( "regexp", regex );
		if ( message != null ) {
			descriptor.setValue( "message", message );
		}
		Pattern p = AnnotationFactory.create( descriptor );
		PatternValidatorForCollection validator = new PatternValidatorForCollection();
		validator.initialize( p );

		return validator;
	}

	@Test
	public void testNullAndEmptyLists() {
		PatternValidatorForCollection validator = createTestValidator( ".*", null );
		assertTrue( validator.isValid( null, null ) );
		assertTrue( validator.isValid( Collections.<String> emptyList(), null ) );
	}

	@Test
	public void testHappyPath() {
		PatternValidatorForCollection validator = createTestValidator( ".*", null );
		assertTrue( validator.isValid( Arrays.asList( new String[] { "", "any string", null } ), null ) );
	}

	@Test
	public void testValidationFails() {
		PatternValidatorForCollection validator = createTestValidator( "^a", null );
		assertFalse( validator.isValid( Collections.singleton( "b" ), null ) );
		assertFalse( validator.isValid( Collections.singleton( "ba" ), null ) );

		// Same but for lists
		assertFalse( validator.isValid( Arrays.asList( new String[] { "", "any string", null, "b" } ), null ) );
		assertFalse( validator.isValid( Arrays.asList( new String[] { "b", "any string", null } ), null ) );
	}

	@Test
	public void testSelectingCollectionValidator() {
		final Validator validator = Validation.byProvider( HibernateValidator.class ).configure().buildValidatorFactory().getValidator();

		assertTrue( validator.validate( this.new ValidatorSelection() ).isEmpty() );
	}

	private class ValidatorSelection {

		@Pattern(regexp = "^a")
		private List<? extends CharSequence> testList = Collections.singletonList( "a" );
	}
}
