/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.cfg;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.GroupDefinitionException;
import javax.validation.Validator;
import javax.validation.metadata.BeanDescriptor;

import org.testng.annotations.Test;

import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.group.DefaultGroupSequenceProvider;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;

import static java.lang.annotation.ElementType.METHOD;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Unit test for {@link org.hibernate.validator.cfg.ConstraintMapping} et al.
 *
 * @author Hardy Ferentschik
 */
public class MultipleConstraintMappingsTest {
	@Test
	@TestForIssue(jiraKey = "HV-500")
	public void testMultipleConstraintMappings() {
		ConstraintMapping marathonMapping = new ConstraintMapping();
		marathonMapping.type( Marathon.class )
				.property( "name", METHOD )
				.constraint( new NotNullDef() );

		ConstraintMapping runnerMapping = new ConstraintMapping();
		runnerMapping.type( Runner.class )
				.property( "name", METHOD )
				.constraint( new NotNullDef() );

		Validator validator = ValidatorUtil.getValidatorForProgrammaticMapping( marathonMapping, runnerMapping );

		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( Marathon.class );
		assertTrue( beanDescriptor.isBeanConstrained(), "There should be constraints defined on the Marathon class" );
		assertEquals(
				beanDescriptor.getConstrainedProperties().iterator().next().getPropertyName(),
				"name",
				"The property name should be constrained"
		);

		beanDescriptor = validator.getConstraintsForClass( Runner.class );
		assertTrue( beanDescriptor.isBeanConstrained(), "There should be constraints defined on the Runner class" );
		assertEquals(
				beanDescriptor.getConstrainedProperties().iterator().next().getPropertyName(),
				"name",
				"The property name should be constrained"
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-500")
	public void testMultipleConstraintMappingsWithSameConfig() {
		ConstraintMapping marathonMapping1 = new ConstraintMapping();
		marathonMapping1.type( Marathon.class )
				.property( "name", METHOD )
				.constraint( new NotNullDef().message( "foo" ) );

		ConstraintMapping marathonMapping2 = new ConstraintMapping();
		marathonMapping2.type( Marathon.class )
				.property( "name", METHOD )
				.constraint( new NotNullDef().message( "bar" ) );


		Validator validator = ValidatorUtil.getValidatorForProgrammaticMapping( marathonMapping1, marathonMapping2 );

		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( Marathon.class );
		assertTrue( beanDescriptor.isBeanConstrained(), "There should be constraints defined on the Marathon class" );
		assertEquals(
				beanDescriptor.getConstrainedProperties().size(),
				1,
				"There should be constraints defined on the Marathon class"
		);

		Marathon marathon = new Marathon();
		Set<ConstraintViolation<Marathon>> violations = validator.validate( marathon );
		assertCorrectConstraintViolationMessages( violations, "foo", "bar" );
	}

	@TestForIssue(jiraKey = "HV-500")
	@Test(expectedExceptions = GroupDefinitionException.class,
			expectedExceptionsMessageRegExp = "HV[0-9]*: Multiple definitions of default group sequence.")
	public void testMultipleConstraintMappingsWithGroupSequenceForSameClass() {
		ConstraintMapping marathonMapping1 = new ConstraintMapping();
		marathonMapping1.type( Marathon.class )
				.defaultGroupSequence( Foo.class, Marathon.class );

		ConstraintMapping marathonMapping2 = new ConstraintMapping();
		marathonMapping2.type( Marathon.class )
				.defaultGroupSequence( Bar.class, Marathon.class );


		ValidatorUtil.getValidatorForProgrammaticMapping( marathonMapping1, marathonMapping2 );
	}

	@TestForIssue(jiraKey = "HV-500")
	@Test(expectedExceptions = GroupDefinitionException.class,
			expectedExceptionsMessageRegExp = "HV[0-9]*: Multiple definitions of default group sequence provider.")
	public void testMultipleConstraintMappingsWithGroupSequenceProviderForSameClass() {
		ConstraintMapping marathonMapping1 = new ConstraintMapping();
		marathonMapping1.type( Marathon.class )
				.defaultGroupSequenceProvider( MarathonDefaultGroupSequenceProvider.class );

		ConstraintMapping marathonMapping2 = new ConstraintMapping();
		marathonMapping2.type( Marathon.class )
				.defaultGroupSequenceProvider( MarathonDefaultGroupSequenceProvider.class );

		ValidatorUtil.getValidatorForProgrammaticMapping( marathonMapping1, marathonMapping2 );
	}

	@Test(expectedExceptions = GroupDefinitionException.class)
	@TestForIssue(jiraKey = "HV-500")
	public void testMultipleConstraintMappingsWithGroupSequenceProviderAndGroupSequence() {
		ConstraintMapping marathonMapping1 = new ConstraintMapping();
		marathonMapping1.type( Marathon.class )
				.defaultGroupSequence( Foo.class, Marathon.class );

		ConstraintMapping marathonMapping2 = new ConstraintMapping();
		marathonMapping2.type( Marathon.class )
				.defaultGroupSequenceProvider( MarathonDefaultGroupSequenceProvider.class );

		Validator validator = ValidatorUtil.getValidatorForProgrammaticMapping( marathonMapping1, marathonMapping2 );
		validator.validate( new Marathon() );
	}

	private interface Foo {
	}

	private interface Bar {
	}

	public static class MarathonDefaultGroupSequenceProvider implements DefaultGroupSequenceProvider<Marathon> {
		@SuppressWarnings("unchecked")
		public List<Class<?>> getValidationGroups(Marathon object) {
			return Arrays.asList( Foo.class, Marathon.class );
		}
	}
}
