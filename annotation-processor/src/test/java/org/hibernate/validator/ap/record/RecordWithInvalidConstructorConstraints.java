/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.record;

import java.util.Date;

import jakarta.validation.constraints.FutureOrPresent;

/**
 * @author Jan Schatteman
 */
public record RecordWithInvalidConstructorConstraints(String string, Date date) {
	public RecordWithInvalidConstructorConstraints(@FutureOrPresent String string, @FutureOrPresent Date date) {
		this.string = string;
		this.date = date;
	}
}
