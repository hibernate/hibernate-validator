/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.messageinterpolation.el;

import java.lang.reflect.RecordComponent;
import java.util.Objects;

import jakarta.el.ELContext;
import jakarta.el.RecordELResolver;

/**
 * Restricts record property resolution to component accessors. The standard resolver also accepts
 * arbitrary public methods without arguments, which must not be exposed at the bean properties feature level.
 * Non-component properties are left unresolved so that subsequent resolvers can handle JavaBean getters.
 */
public class RecordPropertiesELResolver extends RecordELResolver {

	@Override
	public Object getValue(ELContext context, Object base, Object property) {
		String recordProperty = recordProperty( context, base, property );
		return recordProperty == null ? null : super.getValue( context, base, recordProperty );
	}

	@Override
	public Class<?> getType(ELContext context, Object base, Object property) {
		String recordProperty = recordProperty( context, base, property );
		return recordProperty == null ? null : super.getType( context, base, recordProperty );
	}

	@Override
	public boolean isReadOnly(ELContext context, Object base, Object property) {
		String recordProperty = recordProperty( context, base, property );
		return recordProperty != null && super.isReadOnly( context, base, recordProperty );
	}

	@Override
	public void setValue(ELContext context, Object base, Object property, Object value) {
		String recordProperty = recordProperty( context, base, property );
		if ( recordProperty != null ) {
			super.setValue( context, base, recordProperty, value );
		}
	}

	private String recordProperty(ELContext context, Object base, Object property) {
		Objects.requireNonNull( context );
		if ( base instanceof Record && property != null ) {
			String propertyName = property.toString();
			for ( RecordComponent component : base.getClass().getRecordComponents() ) {
				if ( component.getName().equals( propertyName ) ) {
					return propertyName;
				}
			}
		}
		return null;
	}
}
