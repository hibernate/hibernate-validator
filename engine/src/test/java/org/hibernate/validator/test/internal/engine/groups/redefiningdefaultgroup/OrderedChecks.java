/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
