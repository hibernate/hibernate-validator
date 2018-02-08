package org.hibernate.validator.internal.constraintvalidators.hv.age.min;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Checks that the number of Years, Days, Months, etc. according to an unit {@code java.time.temporal.ChronoUnit}
 * from a given {@code java.time.LocalDate} to current day is greater than or equal to the specified value if inclusive is true
 * or is greater when inclusive is false.
 *
 * @author Hillmer Chona
 * @since 6.0.8
 */
public class AgeMinValidatorForLocalDate extends AbstractAgeMinTimeBasedValidator<LocalDate> {

	@Override
	protected long getCurrentAge(LocalDate value) {
		return super.unit.between( value, getReferenceValue( super.referenceClock ) );
	}

	private LocalDate getReferenceValue(Clock reference) {
		return LocalDate.now( reference );
	}

}
