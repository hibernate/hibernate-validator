/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
