/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints;

/**
 * @author Hardy Ferentschik
 */
/**
 * @author Hardy Ferentschik
 */
public class Coordinate {

	long longitude;
	long latitude;

	public Coordinate(long longitude, long latitude) {
		this.longitude = longitude;
		this.latitude = latitude;
	}
}
