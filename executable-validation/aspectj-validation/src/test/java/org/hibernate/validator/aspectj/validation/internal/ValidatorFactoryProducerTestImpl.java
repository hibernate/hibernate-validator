/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.aspectj.validation.internal;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import javax.validation.ClockProvider;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.aspectj.validation.spi.ValidatorFactoryProducer;

/**
 * @author Marko Bekhta
 */
public class ValidatorFactoryProducerTestImpl implements ValidatorFactoryProducer {

	@Override
	public ValidatorFactory getConfiguredValidatorFactory() {
		return Validation.byDefaultProvider().configure()
				.clockProvider( new FixedClockProvider() )
				.buildValidatorFactory();
	}

	public static class FixedClockProvider implements ClockProvider {

		@Override
		public Clock getClock() {
			ZonedDateTime dateTime = ZonedDateTime.of( LocalDateTime.of( 2018, 3, 13, 0, 0 ), ZoneOffset.ofHours( 0 ) );
			return Clock.fixed( dateTime.toInstant(), dateTime.getZone() );
		}
	}
}
