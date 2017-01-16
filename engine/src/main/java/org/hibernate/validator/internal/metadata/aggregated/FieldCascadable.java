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
import java.lang.reflect.TypeVariable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.validation.metadata.GroupConversionDescriptor;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
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
	private final List<TypeVariable<?>> cascadingTypeParameters;
	private final GroupConversionHelper groupConversionHelper;

	FieldCascadable(Field field, List<TypeVariable<?>> cascadingTypeParameters, Map<Class<?>, Class<?>> groupConversions) {
		this.field = field;
		this.propertyName = field.getName();
		this.cascadableType = ReflectionHelper.typeOf( field );
		this.cascadingTypeParameters = Collections.unmodifiableList( cascadingTypeParameters );
		this.groupConversionHelper = new GroupConversionHelper( groupConversions );
		this.groupConversionHelper.validateGroupConversions( !cascadingTypeParameters.isEmpty(), field.toString() );
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
	public List<TypeVariable<?>> getCascadingTypeParameters() {
		return cascadingTypeParameters;
	}

	public static class Builder implements Cascadable.Builder {

		private static final Log LOG = LoggerFactory.make();

		private final Field field;
		private final List<TypeVariable<?>> cascadingTypeParameters = new ArrayList<>();
		private final Map<Class<?>, Class<?>> groupConversions = new HashMap<>();

		public Builder(Field field) {
			this.field = field;
		}

		@Override
		public void addCascadingTypeParameters(List<TypeVariable<?>> cascadingTypeParameters) {
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
		public FieldCascadable build() {
			return new FieldCascadable( getAccessible( field ), cascadingTypeParameters, groupConversions );
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
