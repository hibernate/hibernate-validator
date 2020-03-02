/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.groups.redefiningdefaultgroup;

import jakarta.validation.GroupSequence;
import jakarta.validation.groups.Default;

/**
 * @author Hardy Ferentschik
 */
@GroupSequence({ Default.class, CarChecks.class, DriverChecks.class })
public interface OrderedChecks {
}
