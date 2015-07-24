/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.descriptor;

import org.hibernate.validator.internal.util.Contracts;

import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstructorDescriptor;
import javax.validation.metadata.MethodDescriptor;
import javax.validation.metadata.MethodType;
import javax.validation.metadata.PropertyDescriptor;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

/**
 * Describes a validated bean.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class BeanDescriptorImpl extends ElementDescriptorImpl implements BeanDescriptor {
	private final Map<String, PropertyDescriptor> constrainedProperties;
	private final Map<String, ExecutableDescriptorImpl> constrainedMethods;
	private final Map<String, ConstructorDescriptor> constrainedConstructors;

	public BeanDescriptorImpl(Type beanClass,
							  Set<ConstraintDescriptorImpl<?>> classLevelConstraints,
							  Map<String, PropertyDescriptor> constrainedProperties,
							  Map<String, ExecutableDescriptorImpl> constrainedMethods,
							  Map<String, ConstructorDescriptor> constrainedConstructors,
							  boolean defaultGroupSequenceRedefined,
							  List<Class<?>> defaultGroupSequence) {
		super( beanClass, classLevelConstraints, defaultGroupSequenceRedefined, defaultGroupSequence );

		this.constrainedProperties = Collections.unmodifiableMap( constrainedProperties );
		this.constrainedMethods = Collections.unmodifiableMap( constrainedMethods );
		this.constrainedConstructors = Collections.unmodifiableMap( constrainedConstructors );
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
		return constrainedConstructors.get( getElementClass().getSimpleName() + Arrays.toString( parameterTypes ) );
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

		return constrainedMethods.get( methodName + Arrays.toString( parameterTypes ) );
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
