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

	private static class CustomerWithCascadingKeys {

		Map<@Valid AddressType, String> addressByType;

		public CustomerWithCascadingKeys() {
			addressByType = new HashMap<>();
			addressByType.put( new AddressType( "too short" ), "work@bob.de" );
			addressByType.put( new AddressType( "too small" ), "work@bob.de" );
			addressByType.put( new AddressType( "long enough" ), "work@bob.de" );
		}
	}
}
