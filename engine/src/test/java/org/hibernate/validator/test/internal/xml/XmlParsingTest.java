/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.xml;

import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.testng.Assert.assertEquals;

import java.util.Set;

import jakarta.validation.Validator;
import jakarta.validation.metadata.BeanDescriptor;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.assertj.core.api.Assertions;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.Test;

/**
 * Various tests for parsing of XML mapping files.
 *
 * @author Guillaume Smet
 */
public class XmlParsingTest {

	@Test
	@TestForIssue(jiraKey = "HV-1101")
	public void xmlConstraintMappingSupportsTabsWithoutNewLines() {
		Validator validator = getConfiguration()
				.addMapping(
						XmlParsingTest.class.getResourceAsStream( "hv-1101-tabs-mapping.xml" ) )
				.buildValidatorFactory()
				.getValidator();

		ConstraintDescriptorImpl<?> descriptor = getSingleConstraintDescriptorForClass( validator, Double.class );
		MyConstraint constraint = (MyConstraint) descriptor.getAnnotation();
		Assertions.assertThat( constraint.additionalConstraints() ).hasSize( 1 );
		Assertions.assertThat( constraint.additionalConstraints()[0].constraint() ).isEqualTo( "AA" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1101")
	public void xmlConstraintMappingSupportsEmptyElementForStrings() {
		Validator validator = getConfiguration()
				.addMapping(
						XmlParsingTest.class.getResourceAsStream( "hv-1101-empty-element-mapping.xml" ) )
				.buildValidatorFactory()
				.getValidator();

		ConstraintDescriptorImpl<?> descriptor = getSingleConstraintDescriptorForClass( validator, Double.class );
		MyConstraint constraint = (MyConstraint) descriptor.getAnnotation();
		Assertions.assertThat( constraint.additionalConstraints() ).hasSize( 1 );
		Assertions.assertThat( constraint.additionalConstraints()[0].constraint() ).isEqualTo( "" );
	}

	private ConstraintDescriptorImpl<?> getSingleConstraintDescriptorForClass(Validator validator, Class<?> clazz) {
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( clazz );
		Set<ConstraintDescriptor<?>> constraintDescriptorSet = beanDescriptor.getConstraintDescriptors();
		assertEquals(
				constraintDescriptorSet.size(),
				1,
				"There should be only one constraint descriptor"
		);
		return (ConstraintDescriptorImpl<?>) constraintDescriptorSet.iterator().next();
	}

}
