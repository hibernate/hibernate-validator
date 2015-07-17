/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator;

import javax.validation.ValidatorFactory;

/**
 * Provides Hibernate Validator extensions to {@link ValidatorFactory}.
 *
 * @author Emmanuel Bernard
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public interface HibernateValidatorFactory extends ValidatorFactory {

	/**
	 * Returns a context for validator configuration via options from the
	 * Bean Validation API as well as specific ones from Hibernate Validator.
	 *
	 * @return A context for validator configuration.
	 */
	HibernateValidatorContext usingContext();
}
