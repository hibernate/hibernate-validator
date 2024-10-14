/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.messageinterpolation.el;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;

/**
 * @author Guillaume Smet
 */
public class NoOpElResolver extends ELResolver {

	@Override
	public Object invoke(ELContext context, Object base, Object method, Class<?>[] paramTypes, Object[] params) {
		throw new DisabledFeatureELException( "Method execution is not supported when only enabling Expression Language variables resolution." );
	}

	@Override
	public Object getValue(ELContext context, Object base, Object property) {
		throw new DisabledFeatureELException( "Accessing properties is not supported when only enabling Expression Language variables resolution" );
	}

	@Override
	public Class<?> getType(ELContext context, Object base, Object property) {
		return null;
	}

	@Override
	public void setValue(ELContext context, Object base, Object property, Object value) {
		throw new DisabledFeatureELException( "Accessing properties is not supported when only enabling Expression Language variables resolution" );
	}

	@Override
	public boolean isReadOnly(ELContext context, Object base, Object property) {
		return true;
	}

	@Override
	public Class<?> getCommonPropertyType(ELContext context, Object base) {
		return null;
	}
}
