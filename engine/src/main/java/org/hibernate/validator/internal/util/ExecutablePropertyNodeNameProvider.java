/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util;

import java.io.Serializable;

import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider;

public class ExecutablePropertyNodeNameProvider implements Serializable {
	private final PropertyNodeNameProvider delegate;

	public ExecutablePropertyNodeNameProvider(PropertyNodeNameProvider delegate) {
		this.delegate = delegate;
	}


	public String getName(String propertyName, Object object) {
		return delegate.getName( propertyName, object );
	}



	// HACK because I was too lazy to do it properly :)
	public ExecutablePropertyNodeNameProvider create(Object currentBean) {
		return new GnabberPropertyNodeNameProvider(currentBean);
	}
	private class GnabberPropertyNodeNameProvider extends ExecutablePropertyNodeNameProvider {

		private final Object currentBean;

		public GnabberPropertyNodeNameProvider(Object currentBean) {
			super(delegate);
			this.currentBean = currentBean;
		}

		@Override
		public String getName(String propertyName, Object object) {
			return delegate.getName( propertyName, currentBean.getClass() );
		}
	}

}
