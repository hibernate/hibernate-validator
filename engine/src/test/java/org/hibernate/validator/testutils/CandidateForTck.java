/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.testutils;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Target;

/**
 * Marks tests that should be promoted to the TCK.
 *
 * @author Guillaume Smet
 */
@Target({ TYPE, METHOD })
public @interface CandidateForTck {
}
