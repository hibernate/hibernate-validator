/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.bean;

import java.lang.invoke.MethodHandles;

import org.hibernate.validator.bean.BeanHolder;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.spi.bean.BeanProvider;

public final class NoConfiguredBeanManagerBeanProvider implements BeanProvider {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	@Override
	public void close() {
	}

	@Override
	public <T> BeanHolder<T> forType(Class<T> typeReference) {
		throw LOG.getNoBeanManagerConfiguredException();
	}

	@Override
	public <T> BeanHolder<T> forTypeAndName(Class<T> typeReference, String nameReference) {
		throw LOG.getNoBeanManagerConfiguredException();
	}
}
