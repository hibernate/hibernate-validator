/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

import java.lang.annotation.ElementType;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ElementKind;
import javax.validation.metadata.GroupConversionDescriptor;

import org.hibernate.validator.internal.engine.valuehandling.UnwrapMode;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.PropertyDescriptorImpl;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedElementKind;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredField;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethod;
import org.hibernate.validator.internal.util.privilegedactions.SetAccessibility;

/**
 * Represents the constraint related meta data for a JavaBeans property.
 * Abstracts from the concrete physical type of the underlying Java element(s)
 * (fields or getter methods).
 * <p>
 * In order to provide a unified access to all JavaBeans constraints also
 * class-level constraints are represented by this meta data type.
 * </p>
 * <p>
 * Identity is solely based on the property name, hence sets and similar
 * collections of this type may only be created in the scope of one Java type.
 * </p>
 *
 * @author Gunnar Morling
 */
public class PropertyMetaData extends AbstractConstraintMetaData implements Cascadable {
	private static final Log log = LoggerFactory.make();

	/**
	 * The member marked as cascaded (either field or getter). Used to retrieve
	 * this property's value during cascaded validation.
	 */
	private final Member cascadingMember;

	private final Type cascadableType;

	private final ElementType elementType;

	private final GroupConversionHelper groupConversionHelper;

	/**
	 * Type arguments constraints for this property
	 */
	private final Set<MetaConstraint<?>> typeArgumentsConstraints;

	private PropertyMetaData(String propertyName,
							 Type type,
							 Set<MetaConstraint<?>> constraints,
							 Set<MetaConstraint<?>> typeArgumentsConstraints,
							 Map<Class<?>, Class<?>> groupConversions,
							 Member cascadingMember,
							 UnwrapMode unwrapMode) {
		super(
				propertyName,
				type,
				constraints,
				ElementKind.PROPERTY,
				cascadingMember != null,
				cascadingMember != null || !constraints.isEmpty() || !typeArgumentsConstraints.isEmpty(),
				unwrapMode
		);

		if ( cascadingMember != null ) {
			this.cascadingMember = getAccessible( cascadingMember );
			this.cascadableType = ReflectionHelper.typeOf( cascadingMember );
			this.elementType = cascadingMember instanceof Field ? ElementType.FIELD : ElementType.METHOD;
		}
		else {
			this.cascadingMember = null;
			this.cascadableType =  null;
			this.elementType = ElementType.TYPE;
		}

		this.typeArgumentsConstraints = Collections.unmodifiableSet( typeArgumentsConstraints );
		this.groupConversionHelper = new GroupConversionHelper( groupConversions );
		this.groupConversionHelper.validateGroupConversions( isCascading(), this.toString() );
	}

	/**
	 * Returns an accessible version of the given member. Will be the given member itself in case it is accessible,
	 * otherwise a copy which is set accessible.
	 */
	private static Member getAccessible(Member original) {
		if ( ( (AccessibleObject) original ).isAccessible() ) {
			return original;
		}

		Class<?> clazz = original.getDeclaringClass();
		Member member;

		if ( original instanceof Field ) {
			member = run( GetDeclaredField.action( clazz, original.getName() ) );
		}
		else {
			member = run( GetDeclaredMethod.action( clazz, original.getName() ) );
		}

		run( SetAccessibility.action( member ) );

		return member;
	}

	@Override
	public ElementType getElementType() {
		return elementType;
	}

	@Override
	public Class<?> convertGroup(Class<?> from) {
		return groupConversionHelper.convertGroup( from );
	}

	@Override
	public Set<GroupConversionDescriptor> getGroupConversionDescriptors() {
		return groupConversionHelper.asDescriptors();
	}

	@Override
	public Set<MetaConstraint<?>> getTypeArgumentsConstraints() {
		return this.typeArgumentsConstraints;
	}

	@Override
	public PropertyDescriptorImpl asDescriptor(boolean defaultGroupSequenceRedefined, List<Class<?>> defaultGroupSequence) {
		return new PropertyDescriptorImpl(
				getType(),
				getName(),
				asDescriptors( getConstraints() ),
				isCascading(),
				defaultGroupSequenceRedefined,
				defaultGroupSequence,
				getGroupConversionDescriptors()
		);
	}

	@Override
	public Object getValue(Object parent) {
		if ( elementType == ElementType.METHOD ) {
			return ReflectionHelper.getValue( (Method) cascadingMember, parent );
		}
		else {
			return ReflectionHelper.getValue( (Field) cascadingMember, parent );
		}
	}

	@Override
	public Type getCascadableType() {
		return cascadableType;
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private static <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}

	@Override
	public String toString() {
		return "PropertyMetaData [type=" + getType() + ", propertyName="
				+ getName() + ", cascadingMember=[" + cascadingMember + "]]";
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !super.equals( obj ) ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		return true;
	}

	public static class Builder extends MetaDataBuilder {

		private static final EnumSet<ConstrainedElementKind> SUPPORTED_ELEMENT_KINDS = EnumSet.of(
				ConstrainedElementKind.TYPE,
				ConstrainedElementKind.FIELD,
				ConstrainedElementKind.METHOD
		);

		private final String propertyName;
		private final Type propertyType;
		private Member cascadingMember;
		private final Set<MetaConstraint<?>> typeArgumentsConstraints = newHashSet();
		private UnwrapMode unwrapMode = UnwrapMode.AUTOMATIC;
		private boolean unwrapModeExplicitlyConfigured = false;

		public Builder(Class<?> beanClass, ConstrainedField constrainedField, ConstraintHelper constraintHelper) {
			super( beanClass, constraintHelper );

			this.propertyName = constrainedField.getLocation().getPropertyName();
			this.propertyType = ReflectionHelper.typeOf( constrainedField.getLocation().getMember() );
			add( constrainedField );
		}

		public Builder(Class<?> beanClass, ConstrainedType constrainedType, ConstraintHelper constraintHelper) {
			super( beanClass, constraintHelper );

			this.propertyName = null;
			this.propertyType = null;
			add( constrainedType );
		}

		public Builder(Class<?> beanClass, ConstrainedExecutable constrainedMethod, ConstraintHelper constraintHelper) {
			super( beanClass, constraintHelper );

			this.propertyName = constrainedMethod.getLocation().getPropertyName();
			this.propertyType = ReflectionHelper.typeOf( constrainedMethod.getLocation().getMember() );
			add( constrainedMethod );
		}

		@Override
		public boolean accepts(ConstrainedElement constrainedElement) {
			if ( !SUPPORTED_ELEMENT_KINDS.contains( constrainedElement.getKind() ) ) {
				return false;
			}

			if ( constrainedElement.getKind() == ConstrainedElementKind.METHOD &&
					!( (ConstrainedExecutable) constrainedElement ).isGetterMethod() ) {
				return false;
			}

			return equals(
					constrainedElement.getLocation().getPropertyName(),
					propertyName
			);
		}

		@Override
		public final void add(ConstrainedElement constrainedElement) {
			super.add( constrainedElement );

			// HV-925
			// Trying to detect inconsistent value unwrapping configuration between a property field and its getter.
			// If a field or getter explicitly uses @UnwrapValidatedValue, the corresponding getter / field needs to either
			// not use @UnwrapValidatedValue or use the same value for the annotation.
			UnwrapMode newUnwrapMode = constrainedElement.unwrapMode();
			if ( unwrapModeExplicitlyConfigured ) {
				if ( !UnwrapMode.AUTOMATIC.equals( newUnwrapMode ) && !newUnwrapMode.equals( unwrapMode ) ) {
					throw log.getInconsistentValueUnwrappingConfigurationBetweenFieldAndItsGetterException(
							propertyName,
							getBeanClass()
					);
				}
			}
			else {
				if ( !UnwrapMode.AUTOMATIC.equals( newUnwrapMode ) ) {
					unwrapMode = constrainedElement.unwrapMode();
					unwrapModeExplicitlyConfigured = true;
				}
			}

			if ( constrainedElement.getKind() == ConstrainedElementKind.FIELD ) {
				typeArgumentsConstraints.addAll( ( (ConstrainedField) constrainedElement ).getTypeArgumentsConstraints() );
			}
			else if ( constrainedElement.getKind() == ConstrainedElementKind.METHOD ) {
				typeArgumentsConstraints.addAll( ( (ConstrainedExecutable) constrainedElement ).getTypeArgumentsConstraints() );
			}

			if ( constrainedElement.isCascading() && cascadingMember == null ) {
				cascadingMember = constrainedElement.getLocation().getMember();
			}
		}

		@Override
		public UnwrapMode unwrapMode() {
			return unwrapMode;
		}

		@Override
		public PropertyMetaData build() {
			return new PropertyMetaData(
					propertyName,
					propertyType,
					adaptOriginsAndImplicitGroups( getConstraints() ),
					typeArgumentsConstraints,
					getGroupConversions(),
					cascadingMember,
					unwrapMode()
			);
		}

		private boolean equals(String s1, String s2) {
			return ( s1 != null && s1.equals( s2 ) ) || ( s1 == null && s2 == null );
		}
	}
}
