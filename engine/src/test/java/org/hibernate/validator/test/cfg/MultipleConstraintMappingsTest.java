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
import java.util.Date;
import java.util.List;
import javax.validation.GroupDefinitionException;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.metadata.BeanDescriptor;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.GenericConstraintDef;
import org.hibernate.validator.cfg.defs.AssertFalseDef;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;
import org.hibernate.validator.testutil.TestForIssue;

import static java.lang.annotation.ElementType.METHOD;
import static org.hibernate.validator.testutil.ValidatorUtil.getConfiguration;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Unit test for {@link org.hibernate.validator.cfg.ConstraintMapping} et al.
 *
 * @author Hardy Ferentschik
 */
public class MultipleConstraintMappingsTest {
	HibernateValidatorConfiguration config;

	@BeforeMethod
	public void setUp() {
		config = getConfiguration( HibernateValidator.class );
	}

	@Test
	@TestForIssue(jiraKey = "HV-500")
	public void testMultipleConstraintMappings() {
		ConstraintMapping marathonMapping = config.createConstraintMapping();
		marathonMapping.type( Marathon.class )
				.property( "name", METHOD )
				.constraint( new NotNullDef() );

		ConstraintMapping runnerMapping = config.createConstraintMapping();
		runnerMapping.type( Runner.class )
				.property( "name", METHOD )
				.constraint( new NotNullDef() );

		config.addMapping( marathonMapping );
		config.addMapping( runnerMapping );
		Validator validator = config.buildValidatorFactory().getValidator();

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

	@TestForIssue(jiraKey = "HV-500")
	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000171.*")
	public void testSameTypeConfiguredSeveralTimesInSameConstraintMappingCausesException() {
		ConstraintMapping marathonMapping = config.createConstraintMapping();
		marathonMapping
				.type( Marathon.class )
					.defaultGroupSequence( Foo.class, Marathon.class )
				.type( Marathon.class );
	}

	@TestForIssue(jiraKey = "HV-500")
	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000171.*")
	public void testSameTypeConfiguredSeveralTimesInDifferentConstraintMappingsCausesException() {
		ConstraintMapping marathonMapping1 = config.createConstraintMapping();
		marathonMapping1.type( Marathon.class )
				.defaultGroupSequenceProviderClass( MarathonDefaultGroupSequenceProvider.class );

		ConstraintMapping marathonMapping2 = config.createConstraintMapping();
		marathonMapping2.type( Marathon.class )
				.defaultGroupSequenceProviderClass( MarathonDefaultGroupSequenceProvider.class );

		config.addMapping( marathonMapping1 );
		config.addMapping( marathonMapping2 );

		config.buildValidatorFactory().getValidator();
	}

	@TestForIssue(jiraKey = "HV-500")
	@Test(expectedExceptions = GroupDefinitionException.class, expectedExceptionsMessageRegExp = "HV000052.*")
	public void testConfigurationOfSequenceProviderAndGroupSequenceCausesException() {
		ConstraintMapping marathonMapping = config.createConstraintMapping();
		marathonMapping.type( Marathon.class )
				.defaultGroupSequence( Foo.class, Marathon.class )
				.defaultGroupSequenceProviderClass( MarathonDefaultGroupSequenceProvider.class );

		config.addMapping( marathonMapping );
		Validator validator = config.buildValidatorFactory().getValidator();
		validator.validate( new Marathon() );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000172.*")
	public void testSamePropertyConfiguredSeveralTimesCausesException() {
		ConstraintMapping marathonMapping = config.createConstraintMapping();
		marathonMapping.type( Marathon.class )
				.property( "name", METHOD )
					.constraint( new NotNullDef() )
				.property( "name", METHOD );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000173.*")
	public void testSameMethodConfiguredSeveralTimesCausesException() {
		ConstraintMapping marathonMapping = config.createConstraintMapping();
		marathonMapping.type( Marathon.class )
				.method( "setTournamentDate", Date.class )
					.parameter( 0 )
						.constraint( new NotNullDef() )
				.method( "setTournamentDate", Date.class );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000174.*")
	public void testSameParameterConfiguredSeveralTimesCausesException() {
		ConstraintMapping marathonMapping = config.createConstraintMapping();
		marathonMapping.type( Marathon.class )
				.method( "setTournamentDate", Date.class )
					.parameter( 0 )
						.constraint( new NotNullDef() )
				.parameter( 0 );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000175.*")
	public void testReturnValueConfiguredSeveralTimesCausesException() {
		ConstraintMapping marathonMapping = config.createConstraintMapping();
		marathonMapping.type( Marathon.class )
				.method( "addRunner", Runner.class )
					.returnValue()
						.constraint( new AssertFalseDef() )
					.parameter( 0 )
					.returnValue();
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000177.*")
	public void testCrossParameterConfiguredSeveralTimesCausesException() {
		ConstraintMapping marathonMapping = config.createConstraintMapping();
		marathonMapping.type( Marathon.class )
				.method( "addRunner", Runner.class )
					.crossParameter()
						.constraint(
							new GenericConstraintDef<GenericAndCrossParameterConstraint>(
								GenericAndCrossParameterConstraint.class
							)
						)
					.parameter( 0 )
					.crossParameter();
	}

	private interface Foo {
	}

	public static class MarathonDefaultGroupSequenceProvider implements DefaultGroupSequenceProvider<Marathon> {
		@Override
		public List<Class<?>> getValidationGroups(Marathon object) {
			return Arrays.<Class<?>>asList( Foo.class, Marathon.class );
		}
	}
}
