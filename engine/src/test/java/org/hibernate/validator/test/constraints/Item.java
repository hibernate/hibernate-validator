/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints;

import jakarta.validation.Valid;

/**
 * @author Hardy Ferentschik
 */
class Item {
	@Valid
	Interval interval;
}
