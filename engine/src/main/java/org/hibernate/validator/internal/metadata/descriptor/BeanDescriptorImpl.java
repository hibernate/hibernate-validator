/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.descriptor;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.validation.metadata.BeanDescriptor;
import jakarta.validation.metadata.ConstructorDescriptor;
import jakarta.validation.metadata.MethodDescriptor;
import jakarta.validation.metadata.MethodType;
import jakarta.validation.metadata.PropertyDescriptor;

import org.hibernate.validator.internal.properties.Signature;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * Describes a validated bean.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class BeanDescriptorImpl extends ElementDescriptorImpl implements BeanDescriptor {
	@Immutable
	private final Map<String, PropertyDescriptor> constrainedProperties;
	@Immutable
	private final Map<Signature, ExecutableDescriptorImpl> constrainedMethods;
	@Immutable
	private final Map<Signature, ConstructorDescriptor> constrainedConstructors;

	public BeanDescriptorImpl(Type beanClass,
							  Set<ConstraintDescriptorImpl<?>> classLevelConstraints,
							  Map<String, PropertyDescriptor> constrainedProperties,
							  Map<Signature, ExecutableDescriptorImpl> constrainedMethods,
							  Map<Signature, ConstructorDescriptor> constrainedConstructors,
							  boolean defaultGroupSequenceRedefined,
							  List<Class<?>> defaultGroupSequence) {
		super( beanClass, classLevelConstraints, defaultGroupSequenceRedefined, defaultGroupSequence );

		this.constrainedProperties = CollectionHelper.toImmutableMap( constrainedProperties );
		this.constrainedMethods = CollectionHelper.toImmutableMap( constrainedMethods );
		this.constrainedConstructors = CollectionHelper.toImmutableMap( constrainedConstructors );
	}

	@Override
	public final boolean isBeanConstrained() {
		return hasConstraints() || !constrainedProperties.isEmpty();
	}

	@Override
	public final PropertyDescriptor getConstraintsForProperty(String propertyName) {
		assertNotNull( propertyName, "The property name cannot be null" );
		return constrainedProperties.get( propertyName );
	}

	@Override
	public final Set<PropertyDescriptor> getConstrainedProperties() {
		return newHashSet( constrainedProperties.values() );
	}

	@Override
	public ConstructorDescriptor getConstraintsForConstructor(Class<?>... parameterTypes) {
		return constrainedConstructors.get( ExecutableHelper.getSignature( getElementClass().getSimpleName(), parameterTypes ) );
	}

	@Override
	public Set<ConstructorDescriptor> getConstrainedConstructors() {
		return newHashSet( constrainedConstructors.values() );
	}

	@Override
	public Set<MethodDescriptor> getConstrainedMethods(MethodType methodType, MethodType... methodTypes) {
		boolean includeGetters = MethodType.GETTER.equals( methodType );
		boolean includeNonGetters = MethodType.NON_GETTER.equals( methodType );
		if ( methodTypes != null ) {
			for ( MethodType type : methodTypes ) {
				if ( MethodType.GETTER.equals( type ) ) {
					includeGetters = true;
				}
				if ( MethodType.NON_GETTER.equals( type ) ) {
					includeNonGetters = true;
				}
			}
		}

		Set<MethodDescriptor> matchingMethodDescriptors = newHashSet();
		for ( ExecutableDescriptorImpl constrainedMethod : constrainedMethods.values() ) {
			boolean addToSet = false;
			if ( ( constrainedMethod.isGetter() && includeGetters ) || ( !constrainedMethod.isGetter() && includeNonGetters ) ) {
				addToSet = true;
			}

			if ( addToSet ) {
				matchingMethodDescriptors.add( constrainedMethod );
			}
		}

		return matchingMethodDescriptors;
	}

	@Override
	public MethodDescriptor getConstraintsForMethod(String methodName, Class<?>... parameterTypes) {
		Contracts.assertNotNull( methodName, MESSAGES.methodNameMustNotBeNull() );
		return constrainedMethods.get( ExecutableHelper.getSignature( methodName, parameterTypes ) );
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "BeanDescriptorImpl" );
		sb.append( "{class='" );
		sb.append( getElementClass().getSimpleName() );
		sb.append( "'}" );
		return sb.toString();
	}
}
