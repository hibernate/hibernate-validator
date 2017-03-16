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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.validation.metadata.GroupConversionDescriptor;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.metadata.cascading.CascadingTypeParameter;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethod;
import org.hibernate.validator.internal.util.privilegedactions.SetAccessibility;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * A {@link Cascadable} backed by a property getter of a Java bean.
 *
 * @author Gunnar Morling
 */
public class GetterCascadable implements Cascadable {

	private final Method method;
	private final String propertyName;
	private final Type cascadableType;
	@Immutable
	private final List<CascadingTypeParameter> cascadingTypeParameters;
	private final GroupConversionHelper groupConversionHelper;

	GetterCascadable(Method method, List<CascadingTypeParameter> cascadingTypeParameters, Map<Class<?>, Class<?>> groupConversions) {
		this.method = method;
		this.propertyName = ReflectionHelper.getPropertyName( method );
		this.cascadableType = ReflectionHelper.typeOf( method );
		this.cascadingTypeParameters = CollectionHelper.toImmutableList( cascadingTypeParameters );
		this.groupConversionHelper = new GroupConversionHelper( groupConversions );
		this.groupConversionHelper.validateGroupConversions( !cascadingTypeParameters.isEmpty(), method.toString() );
	}

	@Override
	public Class<?> convertGroup(Class<?> originalGroup) {
		return groupConversionHelper.convertGroup( originalGroup );
	}

	@Override
	public Set<GroupConversionDescriptor> getGroupConversionDescriptors() {
		return groupConversionHelper.asDescriptors();
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
	public List<CascadingTypeParameter> getCascadingTypeParameters() {
		return cascadingTypeParameters;
	}

	public static class Builder implements Cascadable.Builder {

		private static final Log LOG = LoggerFactory.make();

		private final Method method;
		private final List<CascadingTypeParameter> cascadingTypeParameters = new ArrayList<>();
		private final Map<Class<?>, Class<?>> groupConversions = new HashMap<>();

		public Builder(Method method) {
			this.method = method;
		}

		@Override
		public void addCascadingTypeParameters(List<CascadingTypeParameter> cascadingTypeParameters) {
			this.cascadingTypeParameters.addAll( cascadingTypeParameters );
		}

		@Override
		public void addGroupConversions(Map<Class<?>, Class<?>> groupConversions) {
			for ( Entry<Class<?>, Class<?>> oneConversion : groupConversions.entrySet() ) {
				if ( this.groupConversions.containsKey( oneConversion.getKey() ) ) {
					throw LOG.getMultipleGroupConversionsForSameSourceException(
							oneConversion.getKey(),
							CollectionHelper.<Class<?>>asSet(
									groupConversions.get( oneConversion.getKey() ),
									oneConversion.getValue()
							)
					);
				}
				else {
					this.groupConversions.put( oneConversion.getKey(), oneConversion.getValue() );
				}
			}
		}

		@Override
		public GetterCascadable build() {
			return new GetterCascadable( getAccessible( method ), cascadingTypeParameters, groupConversions );
		}

		/**
		 * Returns an accessible version of the given member. Will be the given member itself in case it is accessible,
		 * otherwise a copy which is set accessible.
		 */
		private Method getAccessible(Method original) {
			if ( original.isAccessible() ) {
				return original;
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
