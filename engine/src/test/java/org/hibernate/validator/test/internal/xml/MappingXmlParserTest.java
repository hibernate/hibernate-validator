/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.xml;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.testutils.ConstraintValidatorInitializationHelper.getDummyConstraintCreationContext;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.DecimalMin;

import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.engine.DefaultPropertyNodeNameProvider;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorDescriptor;
import org.hibernate.validator.internal.properties.DefaultGetterPropertySelectionStrategy;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
import org.hibernate.validator.internal.xml.mapping.MappingXmlParser;
import org.hibernate.validator.testutil.TestForIssue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Hardy Ferentschik
 */
public class MappingXmlParserTest {

	private MappingXmlParser xmlMappingParser;
	private ConstraintCreationContext constraintCreationContext;

	@BeforeEach
	public void setupParserHelper() {
		constraintCreationContext = getDummyConstraintCreationContext();
		xmlMappingParser = new MappingXmlParser(
				constraintCreationContext,
				new JavaBeanHelper( new DefaultGetterPropertySelectionStrategy(), new DefaultPropertyNodeNameProvider() ), null
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-782")
	public void testAdditionalConstraintValidatorsGetAddedAndAreLastInList() {
		List<ConstraintValidatorDescriptor<DecimalMin>> validatorDescriptors = constraintCreationContext.getConstraintHelper().getAllValidatorDescriptors(
				DecimalMin.class
		);

		assertFalse( validatorDescriptors.isEmpty(), "Wrong number of default validators" );
		assertEquals( -1, getIndex( validatorDescriptors, DecimalMinValidatorForFoo.class ), "The custom validator must be absent" );

		Set<InputStream> mappingStreams = newHashSet();
		mappingStreams.add( MappingXmlParserTest.class.getResourceAsStream( "decimal-min-mapping-1.xml" ) );

		xmlMappingParser.parse( mappingStreams );

		validatorDescriptors = constraintCreationContext.getConstraintHelper().getAllValidatorDescriptors( DecimalMin.class );
		assertFalse( validatorDescriptors.isEmpty(), "Wrong number of default validators" );
		assertEquals( validatorDescriptors.size() - 1, getIndex( validatorDescriptors, DecimalMinValidatorForFoo.class ),
				"The custom validator must be last" );
	}

	private int getIndex(Iterable<? extends ConstraintValidatorDescriptor<?>> descriptors, Class<?> validatorType) {
		int i = 0;

		for ( ConstraintValidatorDescriptor<?> constraintValidatorDescriptor : descriptors ) {
			if ( constraintValidatorDescriptor.getValidatorClass() == validatorType ) {
				return i;
			}

			i++;
		}

		return -1;
	}

	@Test
	@TestForIssue(jiraKey = "HV-782")
	public void testOverridingOfConstraintValidatorsFromMultipleMappingFilesThrowsException() {
		Set<InputStream> mappingStreams = newHashSet();
		mappingStreams.add( MappingXmlParserTest.class.getResourceAsStream( "decimal-min-mapping-1.xml" ) );
		mappingStreams.add( MappingXmlParserTest.class.getResourceAsStream( "decimal-min-mapping-2.xml" ) );

		assertThatThrownBy( () -> xmlMappingParser.parse( mappingStreams ) )
				.isInstanceOf( ValidationException.class )
				.hasMessageStartingWith( "HV000167" );
	}

	public static class DecimalMinValidatorForFoo implements ConstraintValidator<DecimalMin, Foo> {

		@Override
		public boolean isValid(Foo value, ConstraintValidatorContext context) {
			return false;
		}
	}

	public static class DecimalMinValidatorForBar implements ConstraintValidator<DecimalMin, Bar> {

		@Override
		public boolean isValid(Bar value, ConstraintValidatorContext context) {
			return false;
		}
	}

	public static class Foo {
	}

	public static class Bar {
	}
}
