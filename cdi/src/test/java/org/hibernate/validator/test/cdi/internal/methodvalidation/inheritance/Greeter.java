/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation.inheritance;

import jakarta.validation.constraints.Size;

/**
 * @author Hardy Ferentschik
 */
public interface Greeter {
	String greet(@Size(max = 5) String greeting);
}
