/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cfg;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import jakarta.validation.GroupDefinitionException;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.metadata.BeanDescriptor;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.GenericConstraintDef;
import org.hibernate.validator.cfg.defs.AssertFalseDef;
import org.hibernate.validator.cfg.defs.NotNullDef;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;
import org.hibernate.validator.testutil.TestForIssue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link org.hibernate.validator.cfg.ConstraintMapping} et al.
 *
 * @author Hardy Ferentschik
 */
public class MultipleConstraintMappingsTest {
	HibernateValidatorConfiguration config;

	@BeforeEach
	public void setUp() {
		config = getConfiguration( HibernateValidator.class );
	}

	@Test
	@TestForIssue(jiraKey = "HV-500")
	public void testMultipleConstraintMappings() {
		ConstraintMapping marathonMapping = config.createConstraintMapping();
		marathonMapping.type( Marathon.class )
				.getter( "name" )
				.constraint( new NotNullDef() );

		ConstraintMapping runnerMapping = config.createConstraintMapping();
		runnerMapping.type( Runner.class )
				.getter( "name" )
				.constraint( new NotNullDef() );

		config.addMapping( marathonMapping );
		config.addMapping( runnerMapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( Marathon.class );
		assertTrue( beanDescriptor.isBeanConstrained(), "There should be constraints defined on the Marathon class" );
		assertEquals(
				"name",
				beanDescriptor.getConstrainedProperties().iterator().next().getPropertyName(),
				"The property name should be constrained"
		);

		beanDescriptor = validator.getConstraintsForClass( Runner.class );
		assertTrue( beanDescriptor.isBeanConstrained(), "There should be constraints defined on the Runner class" );
		assertEquals(
				"name",
				beanDescriptor.getConstrainedProperties().iterator().next().getPropertyName(),
				"The property name should be constrained"
		);
	}

	@TestForIssue(jiraKey = "HV-500")
	@Test
	public void testSameTypeConfiguredSeveralTimesInSameConstraintMappingCausesException() {
		assertThatThrownBy( () -> {
			ConstraintMapping marathonMapping = config.createConstraintMapping();
			marathonMapping
					.type( Marathon.class )
					.defaultGroupSequence( Foo.class, Marathon.class )
					.type( Marathon.class );
		} ).isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000171.*" );
	}

	@TestForIssue(jiraKey = "HV-500")
	@Test
	public void testSameTypeConfiguredSeveralTimesInDifferentConstraintMappingsCausesException() {
		assertThatThrownBy( () -> {
			ConstraintMapping marathonMapping1 = config.createConstraintMapping();
			marathonMapping1.type( Marathon.class )
					.defaultGroupSequenceProviderClass( MarathonDefaultGroupSequenceProvider.class );

			ConstraintMapping marathonMapping2 = config.createConstraintMapping();
			marathonMapping2.type( Marathon.class )
					.defaultGroupSequenceProviderClass( MarathonDefaultGroupSequenceProvider.class );

			config.addMapping( marathonMapping1 );
			config.addMapping( marathonMapping2 );

			config.buildValidatorFactory().getValidator();
		} ).isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000171.*" );
	}

	@TestForIssue(jiraKey = "HV-500")
	@Test
	public void testConfigurationOfSequenceProviderAndGroupSequenceCausesException() {
		assertThatThrownBy( () -> {
			ConstraintMapping marathonMapping = config.createConstraintMapping();
			marathonMapping.type( Marathon.class )
					.defaultGroupSequence( Foo.class, Marathon.class )
					.defaultGroupSequenceProviderClass( MarathonDefaultGroupSequenceProvider.class );

			config.addMapping( marathonMapping );
			Validator validator = config.buildValidatorFactory().getValidator();
			validator.validate( new Marathon() );
		} ).isInstanceOf( GroupDefinitionException.class )
				.hasMessageMatching( "HV000052.*" );
	}

	@Test
	public void testSamePropertyConfiguredSeveralTimesCausesException() {
		assertThatThrownBy( () -> {
			ConstraintMapping marathonMapping = config.createConstraintMapping();
			marathonMapping.type( Marathon.class )
					.getter( "name" )
					.constraint( new NotNullDef() )
					.getter( "name" );
		} ).isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000172.*" );
	}

	@Test
	public void testSameMethodConfiguredSeveralTimesCausesException() {
		assertThatThrownBy( () -> {
			ConstraintMapping marathonMapping = config.createConstraintMapping();
			marathonMapping.type( Marathon.class )
					.method( "setTournamentDate", Date.class )
					.parameter( 0 )
					.constraint( new NotNullDef() )
					.method( "setTournamentDate", Date.class );
		} ).isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000173.*" );
	}

	@Test
	public void testSameParameterConfiguredSeveralTimesCausesException() {
		assertThatThrownBy( () -> {
			ConstraintMapping marathonMapping = config.createConstraintMapping();
			marathonMapping.type( Marathon.class )
					.method( "setTournamentDate", Date.class )
					.parameter( 0 )
					.constraint( new NotNullDef() )
					.parameter( 0 );
		} ).isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000174.*" );
	}

	@Test
	public void testReturnValueConfiguredSeveralTimesCausesException() {
		assertThatThrownBy( () -> {
			ConstraintMapping marathonMapping = config.createConstraintMapping();
			marathonMapping.type( Marathon.class )
					.method( "addRunner", Runner.class )
					.returnValue()
					.constraint( new AssertFalseDef() )
					.parameter( 0 )
					.returnValue();
		} ).isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000175.*" );
	}

	@Test
	public void testCrossParameterConfiguredSeveralTimesCausesException() {
		assertThatThrownBy( () -> {
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
		} ).isInstanceOf( ValidationException.class )
				.hasMessageMatching( "HV000177.*" );
	}

	private interface Foo {
	}

	public static class MarathonDefaultGroupSequenceProvider implements DefaultGroupSequenceProvider<Marathon> {
		@Override
		public List<Class<?>> getValidationGroups(Class<?> klass, Marathon object) {
			return Arrays.<Class<?>>asList( Foo.class, Marathon.class );
		}
	}
}
