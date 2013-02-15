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
package org.hibernate.validator.test.internal.xml.mixedconfiguration;

import java.lang.annotation.Annotation;
import java.util.Set;
import javax.validation.Configuration;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;

import org.testng.annotations.Test;

import org.hibernate.validator.test.internal.xml.mixedconfiguration.annotation.Competition;
import org.hibernate.validator.test.internal.xml.mixedconfiguration.annotation.Fixture;
import org.hibernate.validator.test.internal.xml.mixedconfiguration.annotation.PersonCompetition;
import org.hibernate.validator.test.internal.xml.mixedconfiguration.annotation.TeamCompetition;
import org.hibernate.validator.testutil.DummyTraversableResolver;
import org.hibernate.validator.testutil.ValidatorUtil;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.FileAssert.fail;


/**
 * See HV-265
 *
 * @author Hardy Ferentschik
 */
public class InheritanceMappingsTest {

	@Test
	public void defaultConfigurationNoExplicitAnnotationDefinition1() {
		validateAnnotatedFixture(
				new PersonCompetition(),
				ValidatorUtil.getValidator()
		);
	}

	@Test
	public void defaultConfigurationNoExplicitAnnotationDefinition2() {
		validateAnnotatedFixture(
				new TeamCompetition(),
				ValidatorUtil.getValidator()
		);
	}

	@Test
	public void customConfigurationNoExplicitAnnotationDefinition1() {
		validateAnnotatedFixture(
				new PersonCompetition(),
				configure( "annotation-mappings.xml" )
		);
	}

	@Test
	public void customConfigurationNoExplicitAnnotationDefinition2() {
		validateAnnotatedFixture(
				new TeamCompetition(),
				configure( "annotation-mappings.xml" )
		);
	}

	@Test
	public void customConfigurationExplicitXmlDefinition() {
		validateXmlDefinedFixture(
				new org.hibernate.validator.test.internal.xml.mixedconfiguration.xml.PersonCompetition(),
				configure( "xml-mappings.xml" )
		);
	}

	@Test
	public void customConfigurationNoExplicitXmlDefinition() {
		validateXmlDefinedFixture(
				new org.hibernate.validator.test.internal.xml.mixedconfiguration.xml.TeamCompetition(),
				configure( "xml-mappings.xml" )
		);
	}

	private Validator configure(String mappingsUrl) {
		Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.traversableResolver( new DummyTraversableResolver() );
		configuration.addMapping( InheritanceMappingsTest.class.getResourceAsStream( mappingsUrl ) );

		ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		return validatorFactory.getValidator();
	}

	private void validateFixture(IFixture fixture, Validator validator) {
		Set<ConstraintViolation<IFixture>> violations = validator.validate( fixture );

		for ( ConstraintViolation<IFixture> violation : violations ) {
			if ( violation.getLeafBean() instanceof ICompetition
					&& "detail.competition.name".equals( violation.getPropertyPath().toString() ) ) {
				assertEquals( violation.getLeafBean(), fixture.getCompetition() );
				Annotation annotation = violation.getConstraintDescriptor().getAnnotation();
				assertEquals( annotation.annotationType(), NotNull.class );
				return;
			}
		}
		fail( "@NotNull constraint violation for 'detail.competition.name' not detected" );
	}

	private void validateAnnotatedFixture(Competition competition,
										  Validator validator) {
		Fixture fixture = new Fixture();
		fixture.setCompetition( competition );
		validateFixture( fixture, validator );
	}

	private void validateXmlDefinedFixture(org.hibernate.validator.test.internal.xml.mixedconfiguration.xml.Competition competition,
										   Validator validator) {
		org.hibernate.validator.test.internal.xml.mixedconfiguration.xml.Fixture fixture = new org.hibernate.validator.test.internal.xml.mixedconfiguration.xml.Fixture();
		fixture.setCompetition( competition );
		validateFixture( fixture, validator );
	}
}
