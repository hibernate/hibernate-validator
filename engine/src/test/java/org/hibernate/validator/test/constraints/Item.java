package org.hibernate.validator.test.constraints;

import jakarta.validation.Valid;

/**
 * @author Hardy Ferentschik
 */
class Item {
	@Valid
	Interval interval;
}
