/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.cascaded;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.testng.annotations.Test;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * @author Gunnar Morling
 */
public class CustomValueExtractorTest {

	@Test
	public void canUseCustomExtractor() throws Exception {
		Cinema cinema = new Cinema();
		cinema.visitor = new SomeReference<>( new Visitor() );

		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addCascadedValueExtractor( new ReferenceValueExtractor() )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<Cinema>> violations = validator.validate( cinema );

		assertCorrectPropertyPaths( violations, "visitor.name" );
	}

	@Test
	public void canUseCustomKeyExtractorForMaps() throws Exception {
		CustomerWithCascadingKeys bob = new CustomerWithCascadingKeys();

		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addCascadedValueExtractor( new MapKeyExtractor() )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<CustomerWithCascadingKeys>> violations = validator.validate( bob );

		assertCorrectPropertyPaths( violations, "addressByType[key(too short)].value", "addressByType[key(too small)].value" );
	}

	@Test
	public void canUseCustomValueExtractorForMultimaps() throws Exception {
		CustomerWithMultimap bob = new CustomerWithMultimap();

		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addCascadedValueExtractor( new MultimapValueExtractor() )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<CustomerWithMultimap>> violations = validator.validate( bob );

		assertCorrectPropertyPaths( violations, "addressByType[work].value", "addressByType[work].value" );
	}

	@Test
	public void canUseKeyAndValueExtractorForMap() throws Exception {
		CustomerWithCascadingKeyAndValueMap bob = new CustomerWithCascadingKeyAndValueMap();

		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addCascadedValueExtractor( new MapKeyExtractor() )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<CustomerWithCascadingKeyAndValueMap>> violations = validator.validate( bob );

		assertCorrectPropertyPaths( violations, "addressByType[too small].value", "addressByType[long enough].value", "addressByType[key(too small)].value", "addressByType[key(too short)].value" );
	}

	private static class CustomerWithCascadingKeys {

		Map<@Valid AddressType, String> addressByType;

		public CustomerWithCascadingKeys() {
			addressByType = new HashMap<>();
			addressByType.put( new AddressType( "too short" ), "work@bob.de" );
			addressByType.put( new AddressType( "too small" ), "work@bob.de" );
			addressByType.put( new AddressType( "long enough" ), "work@bob.de" );
		}
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

	private static class CustomerWithCascadingKeyAndValueMap {

		Map<@Valid AddressType, @Valid EmailAddress> addressByType;

		public CustomerWithCascadingKeyAndValueMap() {
			addressByType = new HashMap<>();
			addressByType.put( new AddressType( "too short" ), new EmailAddress( "work@bob.de" ) );
			addressByType.put( new AddressType( "too small" ), new EmailAddress( "invalid" ) );
			addressByType.put( new AddressType( "long enough" ), new EmailAddress( "alsoinvalid" ) );
		}
	}
}
