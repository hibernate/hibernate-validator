/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.cascaded;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;

import java.lang.reflect.TypeVariable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintDeclarationException;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Size;
import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.ValueExtractor;
import javax.validation.valueextraction.ValueExtractorDeclarationException;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.internal.engine.cascading.AnnotatedObject;
import org.hibernate.validator.internal.engine.cascading.ValueExtractorDescriptor;
import org.hibernate.validator.internal.util.TypeHelper;
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

		assertCorrectPropertyPaths( violations, "addressByType<V>[work].email", "addressByType<V>[work].email" );
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

				assertCorrectPropertyPaths( violations, "addressByType<V>[work].email", "addressByType<V>[work].email" );
		} );
	}

	@Test
	public void canUseCustomValueExtractorPerValidatorForMultimaps() throws Exception {
		CustomerWithStringStringMultimap bob = new CustomerWithStringStringMultimap();

		Validator validator = Validation.buildDefaultValidatorFactory()
				.usingContext()
				.addValueExtractor( new MultimapValueExtractor() )
				.getValidator();

		Set<ConstraintViolation<CustomerWithStringStringMultimap>> violations = validator.validate( bob );

		assertCorrectPropertyPaths( violations, "addressByType<V>[work].multimap_value", "addressByType<V>[work].multimap_value" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1261")
	public void canUseValueExtractorGivenViaServiceLoader() {
		CustomerWithOptionalAddress bob = new CustomerWithOptionalAddress();

		Validator validator = Validation.buildDefaultValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<CustomerWithOptionalAddress>> violations = validator.validate( bob );

		assertCorrectPropertyPaths( violations, "address" );
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

				Validator validator = Validation.buildDefaultValidatorFactory()
						.getValidator();

				Set<ConstraintViolation<CustomerWithOptionalAddress>> violations = validator.validate( bob );

				// validation.xml overrides service loader
				assertCorrectPropertyPaths( violations, "address.1" );

				ValidatorFactory validatorFactory = Validation.byDefaultProvider()
						.configure()
						.addValueExtractor( new GuavaOptionalValueExtractor2() )
						.buildValidatorFactory();

				validator = validatorFactory.getValidator();

				violations = validator.validate( bob );

				// VF overrides validation.xml
				assertCorrectPropertyPaths( violations, "address.2" );

				validator = validatorFactory.usingContext()
						.addValueExtractor( new GuavaOptionalValueExtractor3() )
						.getValidator();

				violations = validator.validate( bob );

				// V overrides VF
				assertCorrectPropertyPaths( violations, "address.3" );
			}
		);
	}

	@Test
	public void canObtainDefaultExtractors() {
		HibernateValidatorConfiguration config = Validation.byProvider( HibernateValidator.class )
			.configure();

		Set<ValueExtractor<?>> defaultExtractors = config.getDefaultValueExtractors();
		assertThat( defaultExtractors ).isNotEmpty();

		Set<TypeVariable<?>> mapExtractors = defaultExtractors.stream()
			.filter( e -> {
				return TypeHelper.getErasedReferenceType( new ValueExtractorDescriptor( e ).getExtractedType() ) == Map.class;
			} )
			.map( e ->  new ValueExtractorDescriptor( e ).getExtractedTypeParameter() )
			.collect( Collectors.toSet() );

		assertThat( mapExtractors ).containsOnly(
				Map.class.getTypeParameters()[0], Map.class.getTypeParameters()[1], AnnotatedObject.INSTANCE  );

		Set<TypeVariable<?>> optionalExtractors = defaultExtractors.stream()
			.filter( e -> {
				return TypeHelper.getErasedReferenceType( new ValueExtractorDescriptor( e ).getExtractedType() ) == java.util.Optional.class;
			} )
			.map( e ->  new ValueExtractorDescriptor( e ).getExtractedTypeParameter() )
			.collect( Collectors.toSet() );

		assertThat( optionalExtractors ).describedAs( "Expecting extractor for <T>, but not the legacy extractor for java.util.Optional" )
			. containsOnly( java.util.Optional.class.getTypeParameters()
		);

		Set<TypeVariable<?>> guavaOptionalExtractors = defaultExtractors.stream()
			.filter( e -> {
				return TypeHelper.getErasedReferenceType( new ValueExtractorDescriptor( e ).getExtractedType() ) == Optional.class;
			} )
			.map( e ->  new ValueExtractorDescriptor( e ).getExtractedTypeParameter() )
			.collect( Collectors.toSet() );

		assertThat( guavaOptionalExtractors ).describedAs( "Extractor for Guava's Optional shouldn't be part of default extractors" )
			.isEmpty();
	}

	@Test(expectedExceptions = ValueExtractorDeclarationException.class, expectedExceptionsMessageRegExp = "HV000208.*")
	public void configuringMultipleExtractorsForSameTypeAndTypeUseCausesException() throws Exception {
		Validation.byDefaultProvider()
				.configure()
				.addValueExtractor( new GuavaOptionalValueExtractor1() )
				.addValueExtractor( new GuavaOptionalValueExtractor1() );
	}

	@Test(expectedExceptions = ValueExtractorDeclarationException.class, expectedExceptionsMessageRegExp = "HV000208.*")
	public void configuringValidatorWithMultipleExtractorsForSameTypeAndTypeUseCausesException() throws Exception {
		Validation.buildDefaultValidatorFactory()
				.usingContext()
				.addValueExtractor( new GuavaOptionalValueExtractor1() )
				.addValueExtractor( new GuavaOptionalValueExtractor1() );
	}

	@Test(expectedExceptions = ValueExtractorDeclarationException.class, expectedExceptionsMessageRegExp = "HV000208.*")
	public void configuringMultipleExtractorsForSameTypeAndTypeUseInValidationXmlCausesException() throws Exception {
		validationXmlTestHelper.runWithCustomValidationXml(
			"multiple-value-extractors-for-same-type-and-type-use-validation.xml",
			() -> Validation.buildDefaultValidatorFactory() );
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

	public static class GuavaOptionalValueExtractor3 implements ValueExtractor<Optional<@ExtractedValue ?>> {

		@Override
		public void extractValues(Optional<@ExtractedValue ?> originalValue, ValueExtractor.ValueReceiver receiver) {
			receiver.value( "3", originalValue.isPresent() ? originalValue.get() : null );
		}
	}
}
