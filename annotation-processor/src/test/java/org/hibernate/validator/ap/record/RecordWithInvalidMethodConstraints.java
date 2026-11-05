package org.hibernate.validator.ap.record;

import java.util.Date;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;

/**
 * @author Jan Schatteman
 */
public record RecordWithInvalidMethodConstraints(@NotBlank String string, @FutureOrPresent Date date) {

	public void doNothing(@FutureOrPresent String string) {
		//
	}
}
