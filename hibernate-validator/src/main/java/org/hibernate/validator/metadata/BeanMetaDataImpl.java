/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.metadata;

import java.lang.annotation.ElementType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.GroupDefinitionException;
import javax.validation.groups.Default;
import javax.validation.metadata.BeanDescriptor;

import org.slf4j.Logger;

import org.hibernate.validator.group.DefaultGroupSequenceProvider;
import org.hibernate.validator.metadata.ConstraintMetaData.ConstraintMetaDataKind;
import org.hibernate.validator.method.metadata.TypeDescriptor;
import org.hibernate.validator.util.LoggerFactory;
import org.hibernate.validator.util.ReflectionHelper;

import static org.hibernate.validator.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.util.ReflectionHelper.computeAllImplementedInterfaces;
import static org.hibernate.validator.util.ReflectionHelper.getMethods;
import static org.hibernate.validator.util.ReflectionHelper.newInstance;

/**
 * This class encapsulates all meta data needed for validation. Implementations of {@code Validator} interface can
 * instantiate an instance of this class and delegate the metadata extraction to it.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public final class BeanMetaDataImpl<T> implements BeanMetaData<T> {

	private static final Logger log = LoggerFactory.make();

	/**
	 * The root bean class for this meta data.
	 */
	private final Class<T> beanClass;

	/**
	 * Map of all direct constraints which belong to the entity {@code beanClass}. The constraints are mapped to the class
	 * (eg super class or interface) in which they are defined.
	 */
	private final Map<Class<?>, List<MetaConstraint<?>>> metaConstraints = newHashMap();

	/**
	 * Set of all constraints for this bean type (defined on any implemented interfaces or super types)
	 */
	private final Set<MetaConstraint<?>> allMetaConstraints;

	/**
	 * Set of all constraints which are directly defined on the bean or any of the directly implemented interfaces
	 */
	private final Set<MetaConstraint<?>> directMetaConstraints;

	/**
	 * The main element descriptor for {@link #beanClass}.
	 */
	private BeanDescriptorImpl<T> beanDescriptor;

	/**
	 * Contains constrained related meta data for all methods of the type
	 * represented by this bean meta data. Keyed by method, values are an
	 * aggregated view on each method together with all the methods from the
	 * inheritance hierarchy with the same signature.
	 */
	private Map<String, MethodMetaData> methodMetaData;

	private final Map<String, PropertyMetaData> propertyMetaData;

	/**
	 * List of cascaded members.
	 */
	private Set<Member> cascadedMembers = newHashSet();

	/**
	 * The default groups sequence for this bean class.
	 */
	private List<Class<?>> defaultGroupSequence = newArrayList();

	/**
	 * The default group sequence provider.
	 *
	 * @see org.hibernate.validator.group.GroupSequenceProvider
	 * @see DefaultGroupSequenceProvider
	 */
	private DefaultGroupSequenceProvider<T> defaultGroupSequenceProvider;

	/**
	 * The class hierarchy for this class starting with the class itself going up the inheritance chain. Interfaces
	 * are not included.
	 */
	private List<Class<?>> classHierarchyWithoutInterfaces;

	/**
	 * A list of all property names in the class (constrained and un-constrained).
	 */
	// Used to avoid ReflectionHelper#containsMember which is slow
	private final Set<String> propertyNames = newHashSet( 30 );

	/**
	 * Constructor used when creating a bean meta data instance via the xml or programmatic API. In this case
	 * additional metadata (the already configured constraints, cascaded members, etc) are passed as well.
	 *
	 * @param beanClass The bean type for which to create the meta data
	 * @param defaultGroupSequence programmatic/xml configured default group sequence (overrides annotations)
	 * @param defaultGroupSequenceProvider programmatic configured default group sequence provider class (overrides annotations)
	 * @param constraints programmatic/xml configured constraints
	 * @param methodMetaDatas programmatic configured method constraints
	 * @param cascadedMembers programmatic/xml configured cascaded members
	 */
	public BeanMetaDataImpl(Class<T> beanClass,
							List<Class<?>> defaultGroupSequence,
							Class<? extends DefaultGroupSequenceProvider<?>> defaultGroupSequenceProvider,
							Set<ConstraintMetaData> constrainableElements) {
		this.beanClass = beanClass;
		beanDescriptor = new BeanDescriptorImpl<T>( this );

		this.propertyMetaData = newHashMap();

		Set<PropertyMetaData> propertyMetaDatas = newHashSet();
		Set<MethodMetaData> methodMetaDatas = newHashSet();

		for ( ConstraintMetaData oneElement : constrainableElements ) {

			if ( oneElement.getConstrainedMetaDataKind() == ConstraintMetaDataKind.PROPERTY ) {
				propertyMetaDatas.add( (PropertyMetaData) oneElement );
			}
			else {
				methodMetaDatas.add( (MethodMetaData) oneElement );
			}
		}

		for ( PropertyMetaData oneProperty : propertyMetaDatas ) {
			propertyMetaData.put( oneProperty.getPropertyName(), oneProperty );
		}

		Set<Member> cascadedMembers = newHashSet();

		for ( PropertyMetaData oneProperty : propertyMetaDatas ) {
			if ( oneProperty.isCascading() ) {
				cascadedMembers.addAll( oneProperty.getCascadingMembers() );
			}
			propertyNames.add( oneProperty.getPropertyName() );
		}
		this.cascadedMembers = Collections.unmodifiableSet( cascadedMembers );

		classHierarchyWithoutInterfaces = ReflectionHelper.computeClassHierarchy( beanClass, false );

		setDefaultGroupSequenceOrProvider( defaultGroupSequence, defaultGroupSequenceProvider );

		// add the explicitly configured constraints
		for ( PropertyMetaData onePropertyMetaData : propertyMetaDatas ) {
			for ( MetaConstraint<?> constraint : onePropertyMetaData ) {
				addMetaConstraint( constraint.getLocation().getBeanClass(), constraint );
			}
		}

		allMetaConstraints = buildAllConstraintSets();
		directMetaConstraints = buildDirectConstraintSets();

		this.methodMetaData = Collections.unmodifiableMap( buildMethodMetaData( methodMetaDatas ) );
	}

	private void setDefaultGroupSequenceOrProvider(List<Class<?>> defaultGroupSequence, Class<? extends DefaultGroupSequenceProvider<?>> defaultGroupSequenceProvider) {

		if ( defaultGroupSequence != null && defaultGroupSequenceProvider != null ) {
			throw new GroupDefinitionException(
					"Default group sequence and default group sequence provider cannot be defined at the same time."
			);
		}

		if ( defaultGroupSequenceProvider != null ) {
			this.defaultGroupSequenceProvider = newGroupSequenceProviderInstance( defaultGroupSequenceProvider );
		}
		else if ( defaultGroupSequence != null && !defaultGroupSequence.isEmpty() ) {
			setDefaultGroupSequence( defaultGroupSequence );
		}
		else {
			setDefaultGroupSequence( Arrays.<Class<?>>asList( beanClass ) );
		}
	}

	public Class<T> getBeanClass() {
		return beanClass;
	}

	public BeanDescriptor getBeanDescriptor() {
		return beanDescriptor;
	}

	public TypeDescriptor getTypeDescriptor() {
		return beanDescriptor;
	}

	public Set<Member> getCascadedMembers() {
		return cascadedMembers;
	}

	public Map<Class<?>, List<MetaConstraint<?>>> getMetaConstraintsAsMap() {
		return Collections.unmodifiableMap( metaConstraints );
	}

	public Set<MetaConstraint<?>> getMetaConstraints() {
		return allMetaConstraints;
	}

	public Set<MetaConstraint<?>> getDirectMetaConstraints() {
		return directMetaConstraints;
	}

	public MethodMetaData getMetaDataFor(Method method) {
		return methodMetaData.get( method.getName() + Arrays.toString( method.getParameterTypes() ) );
	}

	public Set<MethodMetaData> getAllMethodMetaData() {
		return new HashSet<MethodMetaData>( methodMetaData.values() );
	}

	public PropertyMetaData getMetaDataFor(String propertyName) {
		return propertyMetaData.get( propertyName );
	}

	public boolean isPropertyPresent(String name) {
		return propertyNames.contains( name );
	}

	public List<Class<?>> getDefaultGroupSequence(T beanState) {
		if ( hasDefaultGroupSequenceProvider() ) {
			List<Class<?>> providerDefaultGroupSequence = defaultGroupSequenceProvider.getValidationGroups( beanState );
			return getValidDefaultGroupSequence( providerDefaultGroupSequence );
		}

		return Collections.unmodifiableList( defaultGroupSequence );
	}

	public boolean defaultGroupSequenceIsRedefined() {
		return defaultGroupSequence.size() > 1 || hasDefaultGroupSequenceProvider();
	}

	private boolean hasDefaultGroupSequenceProvider() {
		return defaultGroupSequenceProvider != null;
	}

	public Set<PropertyMetaData> getAllPropertyMetaData() {
		return Collections.unmodifiableSet( new HashSet<PropertyMetaData>( propertyMetaData.values() ) );
	}

	private Set<MetaConstraint<?>> buildAllConstraintSets() {
		Set<MetaConstraint<?>> constraints = newHashSet();
		for ( List<MetaConstraint<?>> list : metaConstraints.values() ) {
			constraints.addAll( list );
		}
		return Collections.unmodifiableSet( constraints );
	}

	private Set<MetaConstraint<?>> buildDirectConstraintSets() {
		Set<MetaConstraint<?>> constraints = newHashSet();
		// collect all constraints directly defined in this bean
		if ( metaConstraints.get( beanClass ) != null ) {
			constraints.addAll( metaConstraints.get( beanClass ) );
		}
		Set<Class<?>> classAndInterfaces = computeAllImplementedInterfaces( beanClass );
		for ( Class<?> clazz : classAndInterfaces ) {
			if ( metaConstraints.get( clazz ) != null ) {
				constraints.addAll( metaConstraints.get( clazz ) );
			}
		}
		return Collections.unmodifiableSet( constraints );
	}

	/**
	 * Builds up the method meta data for this type by invoking each builder in
	 * {@link #methodMetaDataBuilders}.
	 */
	private Map<String, MethodMetaData> buildMethodMetaData(Set<MethodMetaData> allMethodMetaData) {

		Map<String, MethodMetaData> theValue = newHashMap();

		for ( MethodMetaData oneAggregatedMethodMetaData : allMethodMetaData ) {
			theValue.put(
					oneAggregatedMethodMetaData.getName() + Arrays.toString( oneAggregatedMethodMetaData.getParameterTypes() ),
					oneAggregatedMethodMetaData
			);
		}

		return theValue;
	}

	private void setDefaultGroupSequence(List<Class<?>> groupSequence) {
		defaultGroupSequence = getValidDefaultGroupSequence( groupSequence );
	}

	private List<Class<?>> getValidDefaultGroupSequence(List<Class<?>> groupSequence) {
		List<Class<?>> validDefaultGroupSequence = new ArrayList<Class<?>>();

		boolean groupSequenceContainsDefault = false;
		if ( groupSequence != null ) {
			for ( Class<?> group : groupSequence ) {
				if ( group.getName().equals( beanClass.getName() ) ) {
					validDefaultGroupSequence.add( Default.class );
					groupSequenceContainsDefault = true;
				}
				else if ( group.getName().equals( Default.class.getName() ) ) {
					throw new GroupDefinitionException( "'Default.class' cannot appear in default group sequence list." );
				}
				else {
					validDefaultGroupSequence.add( group );
				}
			}
		}
		if ( !groupSequenceContainsDefault ) {
			throw new GroupDefinitionException( beanClass.getName() + " must be part of the redefined default group sequence." );
		}
		if ( log.isTraceEnabled() ) {
			log.trace(
					"Members of the default group sequence for bean {} are: {}",
					beanClass.getName(),
					validDefaultGroupSequence
			);
		}

		return validDefaultGroupSequence;
	}

	private void addMetaConstraint(Class<?> clazz, MetaConstraint<?> metaConstraint) {

		// first we add the meta constraint to our meta constraint map
		List<MetaConstraint<?>> constraintList;
		if ( !metaConstraints.containsKey( clazz ) ) {
			constraintList = new ArrayList<MetaConstraint<?>>();
			metaConstraints.put( clazz, constraintList );
		}
		else {
			constraintList = metaConstraints.get( clazz );
		}
		constraintList.add( metaConstraint );

		// but we also have to update the descriptors exposing the BV metadata API
		if ( metaConstraint.getElementType() == ElementType.TYPE ) {
			beanDescriptor.addConstraintDescriptor( metaConstraint.getDescriptor() );
		}
	}

	public List<Class<?>> getClassHierarchy() {
		return classHierarchyWithoutInterfaces;
	}

	@SuppressWarnings("unchecked")
	private <U extends DefaultGroupSequenceProvider<?>> DefaultGroupSequenceProvider<T> newGroupSequenceProviderInstance(Class<U> providerClass) {
		Method[] providerMethods = getMethods( providerClass );
		for ( Method method : providerMethods ) {
			Class<?>[] paramTypes = method.getParameterTypes();
			if ( "getValidationGroups".equals( method.getName() ) && !method.isBridge()
					&& paramTypes.length == 1 && paramTypes[0].isAssignableFrom( beanClass ) ) {

				return (DefaultGroupSequenceProvider<T>) newInstance(
						providerClass, "the default group sequence provider"
				);
			}
		}

		throw new GroupDefinitionException(
				"The default group sequence provider defined for " + beanClass.getName() + " has the wrong type"
		);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "BeanMetaDataImpl" );
		sb.append( "{beanClass=" ).append( beanClass.getSimpleName() );
		sb.append( ", constraintCount=" ).append( getMetaConstraints().size() );
		sb.append( ", cascadedMemberCount=" ).append( cascadedMembers.size() );
		sb.append( ", defaultGroupSequence=" ).append( getDefaultGroupSequence( null ) );
		sb.append( '}' );
		return sb.toString();
	}
}
