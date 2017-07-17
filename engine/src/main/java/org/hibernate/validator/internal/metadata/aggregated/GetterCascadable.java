/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.hibernate.validator.HibernateValidatorPermission;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.cascading.CascadingTypeParameter;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethod;
import org.hibernate.validator.internal.util.privilegedactions.SetAccessibility;

/**
 * A {@link Cascadable} backed by a property getter of a Java bean.
 *
 * @author Gunnar Morling
 */
public class GetterCascadable implements Cascadable {

	private final Method method;
	private final String propertyName;
	private final Type cascadableType;
	private final CascadingMetaData cascadingMetaData;

	GetterCascadable(Method method, CascadingMetaData cascadingMetaData) {
		this.method = method;
		this.propertyName = ReflectionHelper.getPropertyName( method );
		this.cascadableType = ReflectionHelper.typeOf( method );
		this.cascadingMetaData = cascadingMetaData;
		this.cascadingMetaData.validateGroupConversions( method.toString() );
	}

	@Override
	public ElementType getElementType() {
		return ElementType.METHOD;
	}

	@Override
	public Type getCascadableType() {
		return cascadableType;
	}

	@Override
	public Object getValue(Object parent) {
		return ReflectionHelper.getValue( method, parent );
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

		private final ValueExtractorManager valueExtractorManager;
		private final Method method;
		private CascadingTypeParameter cascadingMetaData;

		public Builder(ValueExtractorManager valueExtractorManager, Method method, CascadingTypeParameter cascadingMetaData) {
			this.valueExtractorManager = valueExtractorManager;
			this.method = method;
			this.cascadingMetaData = cascadingMetaData;
		}

		@Override
		public void mergeCascadingMetaData(CascadingTypeParameter cascadingMetaData) {
			this.cascadingMetaData = this.cascadingMetaData.merge( cascadingMetaData );
		}

		@Override
		public GetterCascadable build() {
			return new GetterCascadable( getAccessible( method ), new CascadingMetaData( valueExtractorManager, cascadingMetaData ) );
		}

		/**
		 * Returns an accessible version of the given member. Will be the given member itself in case it is accessible,
		 * otherwise a copy which is set accessible.
		 */
		private Method getAccessible(Method original) {
			if ( original.isAccessible() ) {
				return original;
			}

			SecurityManager sm = System.getSecurityManager();
			if ( sm != null ) {
				sm.checkPermission( HibernateValidatorPermission.ACCESS_PRIVATE_MEMBERS );
			}

			Class<?> clazz = original.getDeclaringClass();
			Method member = run( GetDeclaredMethod.action( clazz, original.getName() ) );

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
