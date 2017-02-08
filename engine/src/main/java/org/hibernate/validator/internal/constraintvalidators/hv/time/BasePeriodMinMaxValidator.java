/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv.time;

import java.lang.annotation.Annotation;
import java.time.Period;
import javax.validation.ConstraintValidator;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Base class for PeriodMinMax validators containing comparing method for {@link Period}.
 *
 * @author Marko Bekhta
 */
public abstract class BasePeriodMinMaxValidator<T extends Annotation> implements ConstraintValidator<T, Period> {

	private static final Log log = LoggerFactory.make();

	private static int MONTHS_IN_YEAR = 12;

	private long totalMonths;
	private int remainingDays;
	private int daysInMonth;

	protected final void initialize(int years, int months, int days, int daysInMonth) {
		if ( daysInMonth < 1 ) {
			log.getDaysInMonthCannotBeNegativeException( daysInMonth );
		}
		this.daysInMonth = daysInMonth;
		this.remainingDays = days % daysInMonth;
		this.totalMonths = years * MONTHS_IN_YEAR + months + days / daysInMonth;
	}

	protected int compareTo(Period period) {
		if ( period.toTotalMonths() + period.getDays() / daysInMonth >= this.totalMonths ) {
			return period.getDays() % daysInMonth - this.remainingDays;
		}
		else {
			return -1;
		}
	}
}
