/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints;

/**
 * @author Hardy Ferentschik
 */
@StartLessThanEnd
class Interval {

	int start;
	int end;
}
