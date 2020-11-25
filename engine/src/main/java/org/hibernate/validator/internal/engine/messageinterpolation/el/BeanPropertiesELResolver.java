/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.messageinterpolation.el;

import jakarta.el.BeanELResolver;
import jakarta.el.ELContext;

/**
 * @author Guillaume Smet
 */
public class BeanPropertiesELResolver extends BeanELResolver {

	BeanPropertiesELResolver() {
		super( false );
	}

	@Override
	public Object invoke(ELContext context, Object base, Object methodName, Class<?>[] paramTypes, Object[] params) {
		throw new DisabledFeatureELException( "Method execution is not supported when only enabling Expression Language bean property resolution." );
	}
}
