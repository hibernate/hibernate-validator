/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import java.lang.annotation.ElementType;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.metadata.cascading.CascadingTypeParameter;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredField;
import org.hibernate.validator.internal.util.privilegedactions.SetAccessibility;

/**
 * A {@link Cascadable} backed by a field of a Java bean.
 *
 * @author Gunnar Morling
 */
public class FieldCascadable implements Cascadable {

	private final Field field;
	private final String propertyName;
	private final Type cascadableType;
	private final CascadingMetaData cascadingMetaData;

	FieldCascadable(Field field, CascadingMetaData cascadingMetaData) {
		this.field = field;
		this.propertyName = field.getName();
		this.cascadableType = ReflectionHelper.typeOf( field );
		this.cascadingMetaData = cascadingMetaData;
		this.cascadingMetaData.validateGroupConversions( field.toString() );
	}

	@Override
	public ElementType getElementType() {
		return ElementType.FIELD;
	}

	@Override
	public Type getCascadableType() {
		return cascadableType;
	}

	@Override
	public Object getValue(Object parent) {
		return ReflectionHelper.getValue( field, parent );
	}

	@Override
	public void appendTo(PathImpl path) {
		path.addPropertyNode( propertyName );
	}

	@Override
	public CascadingMetaData getCascadingMetaData() {
		return cascadingMetaData;
	}

	public static class Builder implements Cascadable.Builder {

		private final Field field;
		private CascadingTypeParameter cascadingMetaData;

		public Builder(Field field, CascadingTypeParameter cascadingMetaData) {
			this.field = field;
			this.cascadingMetaData = cascadingMetaData;
		}

		@Override
		public void mergeCascadingMetaData(CascadingTypeParameter cascadingMetaData) {
			this.cascadingMetaData = this.cascadingMetaData.merge( cascadingMetaData );
		}

		@Override
		public FieldCascadable build() {
			return new FieldCascadable( getAccessible( field ), new CascadingMetaData( cascadingMetaData ) );
		}

		/**
		 * Returns an accessible version of the given member. Will be the given member itself in case it is accessible,
		 * otherwise a copy which is set accessible.
		 */
		private Field getAccessible(Field original) {
			if ( ( (AccessibleObject) original ).isAccessible() ) {
				return original;
			}

			Class<?> clazz = original.getDeclaringClass();
			Field member = run( GetDeclaredField.action( clazz, original.getName() ) );

			run( SetAccessibility.action( member ) );

			return member;
		}

		/**
		 * Runs the given privileged action, using a privileged block if required.
		 * <p>
		 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
		 * privileged actions within HV's protection domain.
		 */
		private <T> T run(PrivilegedAction<T> action) {
			return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
		}
	}
}
