/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.performance.statistical;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * @author Hardy Ferentschik
 */
public class TestEntity {
	public static final int MAX_DEPTH = 10;
	private static final Calendar cal = GregorianCalendar.getInstance( TimeZone.getTimeZone( ZoneId.of( "GMT" ) ), Locale.ROOT );

	public TestEntity(int depth) {
		if ( depth <= MAX_DEPTH ) {
			depth++;
			testEntity = new TestEntity( depth );
		}
	}

	// it is not really necessary to initialize the values
	@Null
	private String value1 = null;

	@NotNull
	private String value2 = "";

	@Size
	private String value3 = "";

	@Past
	private Date value4 = cal.getTime();

	@Future
	private Date value5 = cal.getTime();

	@Pattern(regexp = ".*")
	private String value6;

	@Min(0)
	private Integer value7 = 0;

	@Max(100)
	private Integer value8 = 0;

	@DecimalMin("1.0")
	private BigDecimal value9 = new BigDecimal( "1.0" );

	@DecimalMin("1.0")
	private BigDecimal value10 = new BigDecimal( "1.0" );

	@AssertFalse
	private boolean value11;

	@AssertTrue
	private boolean value12;

	@Valid
	private TestEntity testEntity;
}


