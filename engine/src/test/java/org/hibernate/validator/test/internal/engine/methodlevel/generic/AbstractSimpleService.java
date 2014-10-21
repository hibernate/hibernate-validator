/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodlevel.generic;

/**
 * @author Hardy Ferentschik
 */
public abstract class AbstractSimpleService<C> implements SimpleService<C> {
	@Override
	public void configure(C config) {
		if ( config == null ) {
			throw new IllegalStateException( "Config cannot be null!" );
		}
	}
}
