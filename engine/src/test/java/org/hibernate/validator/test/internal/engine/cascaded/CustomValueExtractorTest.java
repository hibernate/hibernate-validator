/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.cascaded;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;

import java.util.Map.Entry;
import java.util.Set;

import javax.validation.ConstraintDeclarationException;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Size;
import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidationXmlTestHelper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;

/**
 * @author Gunnar Morling
 */
public class CustomValueExtractorTest {

	private ValidationXmlTestHelper validationXmlTestHelper;

	@BeforeMethod
	public void setupValidationXmlTestHelper() {
		validationXmlTestHelper = new ValidationXmlTestHelper( getClass() );
	}

	@Test
	public void canUseCustomExtractor() throws Exception {
		Cinema cinema = new Cinema();
		cinema.visitor = new SomeReference<>( new Visitor() );

		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addValueExtractor( new ReferenceValueExtractor() )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<Cinema>> violations = validator.validate( cinema );

		assertCorrectPropertyPaths( violations, "visitor.name" );
	}

	@Test
	public void canUseCustomValueExtractorForMultimaps() throws Exception {
		CustomerWithMultimap bob = new CustomerWithMultimap();

		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addValueExtractor( new MultimapValueExtractor() )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<CustomerWithMultimap>> violations = validator.validate( bob );

		assertCorrectPropertyPaths( violations, "addressByType[work].email", "addressByType[work].email" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1260")
	public void canUseValueExtractorGivenInValidationXml() {
		validationXmlTestHelper.runWithCustomValidationXml(
				"value-extractor-validation.xml", () -> {

				CustomerWithMultimap bob = new CustomerWithMultimap();

				Validator validator = Validation.buildDefaultValidatorFactory()
						.getValidator();

				Set<ConstraintViolation<CustomerWithMultimap>> violations = validator.validate( bob );

				assertCorrectPropertyPaths( violations, "addressByType[work].email", "addressByType[work].email" );
		} );
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000197.*")
	public void missingCustomExtractorThrowsException() throws Exception {
		Cinema cinema = new Cinema();
		cinema.visitor = new SomeReference<>( new Visitor() );

		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory()
				.getValidator();

		validator.validate( cinema );
	}

	@Test
	public void valueExtractorPrecedenceIsAppliedCorrectly() {
		validationXmlTestHelper.runWithCustomValidationXml(
			"value-extractor-validation.xml",
			() -> {
				CustomerWithOptionalAddress bob = new CustomerWithOptionalAddress();

				ValidatorFactory validatorFactory = Validation.byDefaultProvider()
						.configure()
						.addValueExtractor( new GuavaOptionalValueExtractor2() )
						.buildValidatorFactory();

				Validator validator = validatorFactory.getValidator();

				Set<ConstraintViolation<CustomerWithOptionalAddress>> violations = validator.validate( bob );

				// VF overrides validation.xml
				assertCorrectPropertyPaths( violations, "address.2" );
			}
		);
	}

	private static class CustomerWithMultimap {

		ListMultimap<String, @Valid EmailAddress> addressByType;

		public CustomerWithMultimap() {
			addressByType = ArrayListMultimap.create();
			addressByType.put( "work", new EmailAddress( "work@bob.de" ) );
			addressByType.put( "work", new EmailAddress( "invalid" ) );
			addressByType.put( "work", new EmailAddress( "alsoinvalid" ) );
			addressByType.put( "home", new EmailAddress( "home@bob.de" ) );
		}
	}

	private static class CustomerWithStringStringMultimap {

		ListMultimap<String, @Email String> addressByType;

		public CustomerWithStringStringMultimap() {
			addressByType = ArrayListMultimap.create();
			addressByType.put( "work", "work@bob.de" );
			addressByType.put( "work", "invalid" );
			addressByType.put( "work", "alsoinvalid" );
			addressByType.put( "home", "home@bob.de" );
		}
	}

	private static class CustomerWithOptionalAddress {

		Optional<@Size(min = 5) String> address = Optional.of( "aaa" );
	}

	public static class CustomMultimapValueExtractor implements ValueExtractor<Multimap<?, @ExtractedValue ?>> {

		@Override
		public void extractValues(Multimap<?, ?> originalValue, ValueExtractor.ValueReceiver receiver) {
			for ( Entry<?, ?> entry : originalValue.entries() ) {
				receiver.keyedValue( "multimap_value_1", entry.getKey(), entry.getValue() );
			}
		}
	}

	public static class GuavaOptionalValueExtractor1 implements ValueExtractor<Optional<@ExtractedValue ?>> {

		@Override
		public void extractValues(Optional<@ExtractedValue ?> originalValue, ValueExtractor.ValueReceiver receiver) {
			receiver.value( "1", originalValue.isPresent() ? originalValue.get() : null );
		}
	}

	public static class GuavaOptionalValueExtractor2 implements ValueExtractor<Optional<@ExtractedValue ?>> {

		@Override
		public void extractValues(Optional<@ExtractedValue ?> originalValue, ValueExtractor.ValueReceiver receiver) {
			receiver.value( "2", originalValue.isPresent() ? originalValue.get() : null );
		}
	}
}
