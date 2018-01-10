/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.hibernate.validator.internal.util.logging.formatter.DurationFormatter;

import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class DurationFormatterTest {

	@Test
	public void testDurationFormatting() throws Exception {
		assertThat( new DurationFormatter( Duration.ZERO ).toString() ).isEqualTo( "0" );
		assertThat( new DurationFormatter( Duration.ofSeconds( 62, 100 ) ).toString() ).isEqualTo( "1 minute 2 seconds 100 nanoseconds" );
		assertThat( new DurationFormatter( Duration.ofHours( 49 ).plusSeconds( 121 ) ).toString() ).isEqualTo( "2 days 1 hour 2 minutes 1 second" );
		assertThat( new DurationFormatter( Duration.ofDays( 1 ).plusHours( 10 ).plusMinutes( 15 ).plusSeconds( 20 ).plusMillis( 25 ) ).toString() )
				.isEqualTo( "1 day 10 hours 15 minutes 20 seconds 25 milliseconds" );
	}
}
