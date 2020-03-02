/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.xml;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.testutils.ConstraintValidatorInitializationHelper.getDummyConstraintCreationContext;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class MappingXmlParserTest {

	private MappingXmlParser xmlMappingParser;
	private ConstraintCreationContext constraintCreationContext;

	@BeforeMethod
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
		assertEquals( getIndex( validatorDescriptors, DecimalMinValidatorForFoo.class ), -1, "The custom validator must be absent" );

		Set<InputStream> mappingStreams = newHashSet();
		mappingStreams.add( MappingXmlParserTest.class.getResourceAsStream( "decimal-min-mapping-1.xml" ) );

		xmlMappingParser.parse( mappingStreams );

		validatorDescriptors = constraintCreationContext.getConstraintHelper().getAllValidatorDescriptors( DecimalMin.class );
		assertFalse( validatorDescriptors.isEmpty(), "Wrong number of default validators" );
		assertEquals( getIndex( validatorDescriptors, DecimalMinValidatorForFoo.class ), validatorDescriptors.size() - 1,
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

		try {
			xmlMappingParser.parse( mappingStreams );
			fail( "Constraint definitions for a given constraint can only be overridden once" );
		}
		catch (ValidationException e) {
			assertTrue( e.getMessage().startsWith( "HV000167" ) );
		}
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
