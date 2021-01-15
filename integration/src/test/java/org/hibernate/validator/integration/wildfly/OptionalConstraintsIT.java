/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.wildfly;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import javax.inject.Inject;
import javax.money.MonetaryAmount;

import org.hibernate.validator.constraints.Currency;
import org.hibernate.validator.integration.AbstractArquillianIT;
import org.javamoney.moneta.Money;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.joda.time.DateTime;
import org.testng.annotations.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Future;

/**
 * Asserts that the constraints based on the JodaTime and Javax Money server modules can be used.
 *
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
public class OptionalConstraintsIT extends AbstractArquillianIT {

	private static final String WAR_FILE_NAME = OptionalConstraintsIT.class
			.getSimpleName() + ".war";

	@Deployment
	public static Archive<?> createTestArchive() {
		return buildTestArchive( WAR_FILE_NAME )
				.addAsWebInfResource( "jboss-deployment-structure-optional-constraints.xml", "jboss-deployment-structure.xml" )
				.addAsWebInfResource( EmptyAsset.INSTANCE, "beans.xml" );
	}

	@Inject
	private Validator validator;

	@Test
	public void canUseJodaTimeConstraintValidator() {
		Set<ConstraintViolation<Shipment>> violations = validator.validate( new Shipment() );


		assertThat( violations.size() ).isEqualTo( 1 );
		assertThat( violations.iterator().next().getConstraintDescriptor().getAnnotation().annotationType() ).isEqualTo( Future.class );
	}

	@Test(enabled = false)
	public void canUseJavaMoneyBasedConstraint() {
		Set<ConstraintViolation<Order>> violations = validator.validate( new Order( Money.of( 1200.0, "EUR" ) ) );

		assertThat( violations.size() ).isEqualTo( 1 );
		assertThat( violations.iterator().next().getConstraintDescriptor().getAnnotation().annotationType() ).isEqualTo( DecimalMax.class );

		violations = validator.validate( new Order( Money.of( 900.0, "USD" ) ) );

		assertThat( violations.size() ).isEqualTo( 1 );
		assertThat( violations.iterator().next().getConstraintDescriptor().getAnnotation().annotationType() ).isEqualTo( Currency.class );
	}

	private static class Shipment {

		@Future
		public DateTime deliveryDate = new DateTime( 2014, 10, 21, 0, 0, 0, 0 );
	}

	private static class Order {

		@DecimalMax("1000")
		@Currency("EUR")
		private MonetaryAmount amount;

		private Order(MonetaryAmount amount) {
			this.amount = amount;
		}
	}

}
