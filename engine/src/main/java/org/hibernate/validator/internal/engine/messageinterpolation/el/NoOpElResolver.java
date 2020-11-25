/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.messageinterpolation.el;

import java.beans.FeatureDescriptor;
import java.util.Collections;
import java.util.Iterator;

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
	public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
		return Collections.emptyIterator();
	}

	@Override
	public Class<?> getCommonPropertyType(ELContext context, Object base) {
		return null;
	}
}
