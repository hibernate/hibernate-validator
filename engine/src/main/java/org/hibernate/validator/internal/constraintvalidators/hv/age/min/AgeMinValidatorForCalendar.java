package org.hibernate.validator.internal.constraintvalidators.hv.age.min;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;

/**
 *  Checks that the number of Years, Days, Months, etc. according to an unit {@code java.time.temporal.ChronoUnit}
 *  from a given {@code java.util.Calendar} to current day is greater than or equal to the specified value if inclusive is true
 *  or is greater when inclusive is false.
 *
 * @author Hillmer Chona
 * @since 6.0.8
 */
public class AgeMinValidatorForCalendar extends AbstractAgeMinInstantBasedValidator<Calendar> {
	@Override
	protected long getCurrentAge(Calendar value) {
		ZonedDateTime zdt1 = getInstant( value ).atZone( ZoneId.systemDefault() );
		LocalDate date1 = zdt1.toLocalDate();

		ZonedDateTime zdt2 = super.referenceClock.instant().atZone( ZoneId.systemDefault() );
		LocalDate date2 = zdt2.toLocalDate();

		return super.unit.between( date1, date2 );
	}

	private Instant getInstant(Calendar value) {
		return value.toInstant();
	}
}
