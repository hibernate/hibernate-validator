/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.xml;

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ValidationException;
import javax.validation.constraints.DecimalMin;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.engine.DefaultParameterNameProvider;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.xml.MappingXmlParser;
import org.hibernate.validator.testutil.TestForIssue;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * @author Hardy Ferentschik
 */
public class MappingXmlParserTest {

	private MappingXmlParser xmlMappingParser;
	private ConstraintHelper constraintHelper;

	@BeforeMethod
	public void setupParserHelper() {
		constraintHelper = new ConstraintHelper();
		xmlMappingParser = new MappingXmlParser( constraintHelper, new DefaultParameterNameProvider(), null );
	}

	@Test
	@TestForIssue(jiraKey = "HV-782")
	public void testAdditionalConstraintValidatorsGetAddedAndAreLastInList() {
		List<Class<? extends ConstraintValidator<DecimalMin, ?>>> validators = constraintHelper.getAllValidatorClasses(
				DecimalMin.class
		);

		assertFalse( validators.isEmpty(), "Wrong number of default validators" );
		assertFalse( validators.contains( DecimalMinValidatorForFoo.class ) , "The custom validator must be absent" );

		Set<InputStream> mappingStreams = newHashSet();
		mappingStreams.add( MappingXmlParserTest.class.getResourceAsStream( "decimal-min-mapping-1.xml" ) );

		xmlMappingParser.parse( mappingStreams );

		validators = constraintHelper.getAllValidatorClasses( DecimalMin.class );
		assertFalse( validators.isEmpty(), "Wrong number of default validators" );
		assertTrue( validators.contains( DecimalMinValidatorForFoo.class ), "Missing xml configured validator" );
		assertTrue( validators.indexOf( DecimalMinValidatorForFoo.class ) == validators.size() - 1,
				"The custom validator must be last" );
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
		public void initialize(DecimalMin constraintAnnotation) {
		}

		@Override
		public boolean isValid(Foo value, ConstraintValidatorContext context) {
			return false;
		}
	}

	public static class DecimalMinValidatorForBar implements ConstraintValidator<DecimalMin, Bar> {

		@Override
		public void initialize(DecimalMin constraintAnnotation) {
		}

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
