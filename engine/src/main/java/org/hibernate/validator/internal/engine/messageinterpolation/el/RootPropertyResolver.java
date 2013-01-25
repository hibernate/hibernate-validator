/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.validator.internal.engine.messageinterpolation.el;

import java.beans.FeatureDescriptor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.validation.ValidationException;

/**
 * @author Hardy Ferentschik
 */
public class RootPropertyResolver extends ELResolver {
	private final Map<String, Object> map = Collections.synchronizedMap( new HashMap<String, Object>() );

	@Override
	public Class<?> getCommonPropertyType(ELContext context, Object base) {
		return base == null ? String.class : null;
	}

	@Override
	public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
		return null;
	}

	@Override
	public Class<?> getType(ELContext context, Object base, Object property) {
		return resolve( context, base, property ) ? Object.class : null;
	}

	@Override
	public Object getValue(ELContext context, Object base, Object property) {
		if ( resolve( context, base, property ) ) {
			if ( !isProperty( (String) property ) ) {
				throw new PropertyNotFoundException( "Cannot find property " + property );
			}
			return getProperty( (String) property );
		}
		return null;
	}

	@Override
	public boolean isReadOnly(ELContext context, Object base, Object property) {
		return false;
	}

	@Override
	public void setValue(ELContext context, Object base, Object property, Object value) {
		if ( resolve( context, base, property ) ) {
			setProperty( (String) property, value );
		}
	}

	@Override
	public Object invoke(ELContext context, Object base, Object method, Class<?>[] paramTypes, Object[] params) {
		if ( resolve( context, base, method ) ) {
			throw new ValidationException( "Invalid property" );
		}
		return null;
	}

	private Object getProperty(String property) {
		return map.get( property );
	}

	private void setProperty(String property, Object value) {
		map.put( property, value );
	}

	private boolean isProperty(String property) {
		return map.containsKey( property );
	}

	private boolean resolve(ELContext context, Object base, Object property) {
		context.setPropertyResolved( base == null && property instanceof String );
		return context.isPropertyResolved();
	}
}



