/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import javax.validation.constraints.Future;
import javax.validation.constraints.Past;

/**
 * @author Khalid Alqinyah
 */
public class ModelWithJava8DateTime {
	@Past
	@Future
	public ZonedDateTime zonedDateTime;

	@Past
	@Future
	public Instant instant;

	@Past
	@Future
	public OffsetDateTime offsetDateTime;
}
