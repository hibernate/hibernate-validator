/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.internal.constraintvalidators.AbstractEmailValidator;

/**
 * Checks that a given character sequence (e.g. string) is a well-formed email address.
 *
 * @author Guillaume Smet
 */
@SuppressWarnings("deprecation")
public class EmailValidator extends AbstractEmailValidator<Email> {

}
