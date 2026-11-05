package org.hibernate.validator.ap.record;

import java.util.Date;

import jakarta.validation.constraints.FutureOrPresent;

/**
 * @author Jan Schatteman
 */
public record RecordWithInvalidConstraints(/* Not allowed */ @FutureOrPresent String string, @FutureOrPresent Date date) {
}
